package ru.mazelab.vif2ne.backend;

import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ru.mazelab.vif2ne.backend.domains.EventEntries;
import ru.mazelab.vif2ne.throwable.ApplicationException;

/**
 * Сервер хранит массив последних событий в дереве, до 4К элементов, при переполнении старшая половина массива чистится.
 * Запрос выдается в форме /nvk/forum/0/tree?xml=lastEvent
 * где lastEvent - id события, с которого осуществляется выдача (не включительно). Если lastEvent==-1 то выдается массив целиком.
 * Если события с таким id не найдено, отдается 201 Protocol mismatch с пустым телом ответа.
 * В противном случае ответ включает в себя секцию [lastEvent] где прописан id последнего события и ряд событий.
 * Формат события
 * 1) Добавление статьи в ветку
 * [event no="%X" type="add" parent="%X"]
 * no - id статьи в шестнадцатеричном виде
 * parent - id родителя статьи в шестнадцатеричном виде
 * Далее элементы [title], [author], [date], [size] (в байтах) и [crc] (CRC статьи, adler32 от мета-данных статьи)
 * 2) Удаление статьи
 * [event no="%X" type="del" parent="%X"/]
 * 3) Смена родителя
 * [event no="%X" type="parent" parent="%X"/]
 * 4) Фиксация ветки в дереве
 * [event no="%X" type="fix" mode="%u" /]
 * Возможные значения mode
 * 0 - закрепление ветки убрано
 * 1 - закрепление ветки с прибитем кверху
 * 256 - закрепление ветки без прибития кверху
 * <p/>
 * Прочее.
 * <p/>
 * Запрос ответов на статью в xml форме, формат тот же, что и выше, будут только события типа "add", пример
 * http://vif2ne.ru/nvk/forum/0/co/2692768.htm?xml
 * <p/>
 * Выдача текста статьи без layout, пример
 * http://vif2ne.ru/nvk/forum/0/co/2692768.htm?plain
 * <p/>
 * Получить список пользователей, имя которых начинается с подстройки name (не менее 3 символов), нужна авторизация, пример
 * http://vif2ne.ru/nvk/forum/0/security/peoplelist?xml=1&name=Nov
 * <p/>
 * P.S. Ввиду того, что обработку html entities я в свое время реализовал криво, квадратные скобки в тексте выше нужно заменить на угловые.
 * <p/>
 * <p/>
 * <p/>
 * http://vif2ne.ru/nvk/forum/0/co/2692781.htm
 */

/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Created by Colibri  15.05.15 22:19
 * RemoteService.java
 *
 *
 */

/**
 * ответ
 * <p/>
 * http://vif2ne.ru/nvk/forum/0/security/replymsg/2698188
 */
public class RemoteService {

    private static final String LOG_TAG = "RemoteService";

    private static final String URL_NAME_EVENT_LOG = "http://vif2ne.ru/nvk/forum/0/co/tree?xml=%d";
    private static final String URL_NAME_ARTICLE = "http://vif2ne.ru/nvk/forum/0/co/%d.htm?plain";

    private static final String COOKIE_SET = "Set-Cookie";
    private static final String URL_ACCESS = "http://vif2ne.ru/nvk/forum/security";

    private String basicAuth;

    private ArrayList<String> setCookies;


    public RemoteService() {
        setCookies = new ArrayList<>();
        basicAuth = "";
    }

    public boolean isAuthenticated() {
        return !TextUtils.isEmpty(basicAuth);
    }

    public void auth(URLConnection conn) {
        if (!isAuthenticated()) return;
        conn.setRequestProperty("Authorization", basicAuth);
        Log.d(LOG_TAG, "Authorization:" + basicAuth);
        for (String cookie : setCookies) {
            Log.d(LOG_TAG, "Cookie:" + cookie);
            conn.setRequestProperty("Cookie", cookie);
        }
    }

    public void logout() {
        basicAuth = "";
        setCookies.clear();
    }

    public void login(String user, String passwd) throws IOException, ApplicationException {
        URL url = new URL(URL_ACCESS);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        basicAuth = "Basic " + Base64.encodeToString((user + ":" + passwd).getBytes(), Base64.DEFAULT);
        connection.setRequestProperty("Authorization", basicAuth);
        connection.setUseCaches(false);
        connection.setDoOutput(true);
        try {
            connection.connect();
            Map<String, List<String>> hf = connection.getHeaderFields();
            int responseCode = connection.getResponseCode();
            Log.d(LOG_TAG, "responseCode:" + responseCode);
            switch (responseCode) {
                case 200: {
                    break;
                }
                case 401: {
                    throw new ApplicationException("Неверный пароль");
                }
                default: {
                    throw new ApplicationException("Проверьте наличие сети HTTP код ошибки:" + Integer.toString(responseCode));
                }
            }
            List<String> cookieStrings = hf.get(COOKIE_SET);
            setCookies.clear();
            if (cookieStrings == null) throw new ApplicationException("Cookie empty");
            if (cookieStrings.size() == 0) throw new ApplicationException("Cookie size=0");
            for (String cookie : cookieStrings) {
                setCookies.add(cookie);
            }
        } finally {
            connection.disconnect();
        }
    }

    public Boolean loadEventEntries(EventEntries entries, long lastEventId) throws IOException, XmlPullParserException, ParseException {
        URL url = new URL(String.format(URL_NAME_EVENT_LOG, lastEventId));
        Log.d(LOG_TAG, url.toString());
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        auth(urlConnection);
        try {
            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            return new Vif2NeXmlParser().parse(entries, lastEventId, in);
//            return NetUtils.readStreamToString(in, "windows-1251");
        } finally {
            urlConnection.disconnect();
        }

    }

    public String loadEventsXML(long lastEventId) throws IOException {
        URL url = new URL(String.format(URL_NAME_EVENT_LOG, lastEventId));
        Log.d(LOG_TAG, url.toString());
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        auth(urlConnection);
        try {
            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            return NetUtils.readStreamToString(in, "windows-1251");
        } finally {
            urlConnection.disconnect();
        }
    }

    public String loadArticle(long articleNo) throws IOException {
        URL url = new URL(String.format(URL_NAME_ARTICLE, articleNo));
        Log.d(LOG_TAG, url.toString());
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        auth(urlConnection);
        try {
            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            return NetUtils.readStreamToString(in, "windows-1251");
        } finally {
            urlConnection.disconnect();
        }
    }
}
