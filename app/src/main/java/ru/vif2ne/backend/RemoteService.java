/*
 * Copyright (C) 2015 by Sergey Omarov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Created by serg 21.05.15 20:19
 */

package ru.vif2ne.backend;

import android.content.Context;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ru.vif2ne.R;
import ru.vif2ne.Session;
import ru.vif2ne.backend.domains.Article;
import ru.vif2ne.backend.domains.EventEntries;
import ru.vif2ne.throwable.ApplicationException;

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

public class RemoteService {

    public static final String URL_POST = "http://vif2ne.ru/nvk/forum/0/security/reply/%d";
    public static final String URL_POST_PREVIEW = "http://vif2ne.ru/nvk/forum/0/security/preview/%d";
    public static final String URL_POST_REFERER = "http://vif2ne.ru/nvk/forum/0/security/replymsg/%d";
    public static final String URL_EVENT_LOG = "http://vif2ne.ru/nvk/forum/0/co/tree?xml=%d";
    public static final String URL_ARTICLE = "http://vif2ne.ru/nvk/forum/0/co/%d.htm?plain";
    private static final String LOG_TAG = "RemoteService";
    private static final String URL_ACCESS = "http://vif2ne.ru/nvk/forum/security";

    private static final String COOKIE_SET = "Set-Cookie";
    private static final String LOGIN_NAME = "login";
    private static final String PASSWD = "dwp";
    private final Session session;
    private String basicAuth;

    private ArrayList<String> setCookies;
    private String userName;
    private String passwd;


    public RemoteService(Session session) {
        CookieManager cookieManager = new CookieManager();
        CookieHandler.setDefault(cookieManager);
        this.session = session;
        setCookies = new ArrayList<>();
        basicAuth = "";
    }

    public boolean isAuthenticated() {
        return !TextUtils.isEmpty(basicAuth);
    }

    public void logout() {
        basicAuth = "";
        setUserName("");
        setPasswd("");
        setCookies.clear();
    }

    public void auth(URLConnection conn) {
        if (!isAuthenticated()) return;
        conn.setRequestProperty("Authorization", basicAuth);
        Log.d(LOG_TAG, "Authorization:" + basicAuth);
        String c = "";
        for (String cookie : setCookies) {
            if (TextUtils.isEmpty(c))
                c = cookie;
            else
                c = c + ";" + cookie;
        }
        conn.setRequestProperty("Cookie", c);
        Log.d(LOG_TAG, "Cookie:" + c);
    }


    public void addCookie(String cookie) {
        String cookieName = cookie.substring(0, cookie.indexOf("="));
        int i = 0;
        int ii = -1;
        for (String s : setCookies) {
            String f = s.substring(0, s.indexOf("="));
            if (f.equals(cookieName)) {
                ii = i;
            }
            i++;
        }
        if (ii >= 0) {
            setCookies.remove(ii);
        }
        setCookies.add(cookie);
    }

    public void resetCookie(HttpURLConnection connection) {
        Map<String, List<String>> hf = connection.getHeaderFields();
        List<String> cookieStrings = hf.get(COOKIE_SET);
        if (cookieStrings == null) return;
        for (String cookie : cookieStrings) {
            String c = cookie.substring(0, cookie.indexOf(";"));
            Log.d(LOG_TAG, c);
            addCookie(c);
        }
    }

    public String postArticle(String urlPost, Article article) throws IOException, ApplicationException {
        if (article == null) return null;
        String qry = article.getQuery();
        URL url = new URL(String.format(urlPost, article.getId()));
        Log.d(LOG_TAG, url.toString());
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        auth(connection);
        connection.setDoOutput(true);
        connection.setFixedLengthStreamingMode(qry.getBytes().length);
        connection.setRequestProperty("Content-type", "application/x-www-form-urlencoded");
        connection.setRequestProperty("Referer", String.format(URL_POST_REFERER, article.getId()));
        Log.d(LOG_TAG, "post:" + article.getQuery());
        try {
            OutputStream out = new BufferedOutputStream(connection.getOutputStream());
            out.write(qry.getBytes());
            out.flush();
            out.close();
            String preview = NetUtils.readStreamToString(connection.getInputStream(), "windows-1251");
            int responseCode = connection.getResponseCode();
            Log.d(LOG_TAG, "rc:" + responseCode + " html:" + preview);
            if (responseCode == 200)
                return preview;
            else
                return null;

        } finally {
            resetCookie(connection);
            connection.disconnect();
        }

    }


    public void login(String user, String passwd) throws IOException, ApplicationException {
        URL url = new URL(URL_ACCESS);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        setUserName("");
        basicAuth = "Basic " + Base64.encodeToString((user + ":" + passwd).getBytes(), Base64.DEFAULT);
        connection.setRequestProperty("Authorization", basicAuth);
        connection.setUseCaches(false);
        connection.setDoOutput(true);
        try {
            connection.connect();
            resetCookie(connection);

            int responseCode = connection.getResponseCode();
            Log.d(LOG_TAG, "responseCode:" + responseCode);
            switch (responseCode) {
                case 200: {
                    setUserName(user);
                    setPasswd(passwd);
                    break;
                }
                case 401: {
                    basicAuth = "";
                    throw new ApplicationException("Неверный пароль", ApplicationException.SC_BAD_PASSWD);
                }
                default: {
                    basicAuth = "";
                    throw new ApplicationException("Проверьте наличие сети HTTP код ошибки:" + Integer.toString(responseCode), ApplicationException.SC_UNKNOWN);
                }
            }
        } finally {
            connection.disconnect();
        }
    }

    public Boolean loadEventEntries(EventEntries entries, long lastEventId) throws IOException, XmlPullParserException, ParseException, ApplicationException {
        URL url = new URL(String.format(URL_EVENT_LOG, lastEventId));
        Log.d(LOG_TAG, url.toString());
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        auth(urlConnection);
        Context ctx = session.getApplication().getApplicationContext();
        try {
            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            int responseCode = urlConnection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK)
                throw new ApplicationException(String.format(
                        ctx.getResources().getString(R.string.error_get_xml_tree), responseCode),
                        responseCode);
            return new Vif2NeXmlParser().parse(entries, lastEventId, in);
        } finally {
            resetCookie(urlConnection);
            urlConnection.disconnect();
        }

    }

    public String loadEventsXML(long lastEventId) throws IOException, ApplicationException {
        URL url = new URL(String.format(URL_EVENT_LOG, lastEventId));
        Log.d(LOG_TAG, url.toString());
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        auth(urlConnection);
        try {
            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            return NetUtils.readStreamToString(in, "windows-1251");
        } finally {
            resetCookie(urlConnection);
            urlConnection.disconnect();
        }
    }

    public String loadArticle(long articleNo) throws IOException, ApplicationException {
        URL url = new URL(String.format(URL_ARTICLE, articleNo));
        Log.d(LOG_TAG, url.toString());
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        auth(urlConnection);
        try {
            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            return NetUtils.readStreamToString(in, "windows-1251");
        } finally {
            resetCookie(urlConnection);
            urlConnection.disconnect();
        }
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPasswd() {
        return passwd;
    }

    public void setPasswd(String passwd) {
        this.passwd = passwd;
    }
}