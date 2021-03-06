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
import java.net.CookiePolicy;
import java.net.HttpCookie;
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
import ru.vif2ne.backend.domains.Smoking;
import ru.vif2ne.backend.domains.SmokingPostMessage;
import ru.vif2ne.backend.domains.SmokingSettings;
import ru.vif2ne.backend.domains.UserSettings;
import ru.vif2ne.throwable.ApplicationException;

public class RemoteService {
    public static final String URL_DOMAIN = "https://vif2ne.org";


    public static final String URL_POST = URL_DOMAIN+"/nvk/forum/0/security/reply/%d";
    public static final String URL_POST_PREVIEW = URL_DOMAIN+"/nvk/forum/0/security/preview/%d";
    public static final String URL_POST_REFERER = URL_DOMAIN+"/nvk/forum/0/security/replymsg/%d";
    public static final String URL_EVENT_LOG = URL_DOMAIN+"/nvk/forum/0/co/tree?xml=%d";
    public static final String URL_ARTICLE = URL_DOMAIN+"/nvk/forum/0/co/%d.htm?plain";
    public static final String URL_SETTINGS = URL_DOMAIN+"/nvk/forum/0/security/opt?msg";
    public static final String URL_SMOKING_SETTINGS = URL_DOMAIN+"/nvk/tlk/0/security/optframe";
    public static final String URL_SMOKING_SEND = URL_DOMAIN+"/nvk/tlk/0/security/message";

    public static final String URL_SMOKING = URL_DOMAIN+"/nvk/tlk/0/security/refresh?xml=1&msg=%d";

    public static final String EMPTY_USER = "anonymouse";
    private static final String LOG_TAG = RemoteService.class.getSimpleName();
    private static final String URL_ACCESS = URL_DOMAIN+"/nvk/forum/security";

    private static final String COOKIE_SET = "Set-Cookie";
    private static final String LOGIN_NAME = "login";
    private static final String PASSWD = "dwp";
    private final Session session;
    private final CookieManager cookieManager;
    private String basicAuth;


    private String userName;
    private String passwd;


    public RemoteService(Session session) {
        cookieManager = new CookieManager();
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
        CookieHandler.setDefault(cookieManager);
        setUserName(EMPTY_USER);
        setPasswd("");
        this.session = session;
        basicAuth = "";
    }

    public boolean isAuthenticated() {
        return !TextUtils.isEmpty(basicAuth);
    }

    public void logout() {
        basicAuth = "";
        setUserName(EMPTY_USER);
        setPasswd("");
        cookieManager.getCookieStore().removeAll();
    }

    public void auth(URLConnection conn) {
        if (!isAuthenticated()) return;
        conn.setRequestProperty("Authorization", basicAuth);
    }

    public String postSmokingMessage (boolean _private) throws IOException {
        URL url = new URL(URL_SMOKING_SEND);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        auth(connection);
        connection.setDoOutput(true);
        SmokingPostMessage smokingPostMessage = new SmokingPostMessage(session.getSmokingEditMessage().toString(), _private);
        String qry = smokingPostMessage.getQuery();
        connection.setFixedLengthStreamingMode(qry.getBytes().length);
        connection.setRequestProperty("Content-type", "content=\"application/x-www-form-urlencoded\"; charset=windows-1251");
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
            connection.disconnect();
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
        connection.setRequestProperty("Content-type", "content=\"application/x-www-form-urlencoded\"; charset=windows-1251");
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
            connection.disconnect();
        }

    }

    private void dumpCookie() {
        Log.d(LOG_TAG,"cookies");
        for (HttpCookie httpCookie : cookieManager.getCookieStore().getCookies()) {

            Log.d(LOG_TAG, "cookie:" + httpCookie.toString() +
                            " domain:" + httpCookie.getDomain() +
                            " path:" + httpCookie.getPath() +
                            " ports:" + httpCookie.getPortlist()
            );
        }
    }

    public String login(String user, String passwd) throws IOException, ApplicationException {
        URL url = new URL(URL_ACCESS);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        setUserName(EMPTY_USER);
        basicAuth = "Basic " + Base64.encodeToString((user + ":" + passwd).getBytes(), Base64.DEFAULT);
        connection.setRequestProperty("Authorization", basicAuth);
        connection.setUseCaches(false);
        connection.setDoOutput(true);
        try {
            connection.connect();
         //   resetCookie(connection);

            int responseCode = connection.getResponseCode();
            Log.d(LOG_TAG, "responseCode:" + responseCode);
            switch (responseCode) {
                case 200: {
                    setUserName(user);
                    setPasswd(passwd);
             //       dumpCookie();
                    return basicAuth;
                }
                case 401: {
                    basicAuth = "";
                    setPasswd("");
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


    public Boolean loadSmoking (Smoking smoking) throws ApplicationException, IOException, ParseException, XmlPullParserException {
        URL url = new URL(String.format(URL_SMOKING, smoking.getLastId()));
        Log.d(LOG_TAG, url.toString());
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        auth(urlConnection);
        Context ctx = session.getApplication().getApplicationContext();
        try {
            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            int responseCode = urlConnection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK)
                throw new ApplicationException(String.format(
                        ctx.getResources().getString(R.string.error_get_xml_smoking), responseCode),
                        responseCode);
            return new Vif2NeXmlSmokingParser().parse(smoking, in);
        } finally {
       //     resetCookie(urlConnection);
            urlConnection.disconnect();
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
       //     resetCookie(urlConnection);
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
       //     resetCookie(urlConnection);
            urlConnection.disconnect();
        }
    }

    public SmokingSettings loadSmokingSettings() throws IOException {
        URL url = new URL(URL_SMOKING_SETTINGS);
        Log.d(LOG_TAG, url.toString());
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        auth(urlConnection);
        try {
            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            return new SmokingSettings(NetUtils.readStreamToString(in, "windows-1251"));
        } finally {
       //     resetCookie(urlConnection);
            urlConnection.disconnect();
        }

    }

    public UserSettings loadSettings() throws IOException {
        URL url = new URL(URL_SETTINGS);
        Log.d(LOG_TAG, url.toString());
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        auth(urlConnection);
        try {
            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            return new UserSettings(NetUtils.readStreamToString(in, "windows-1251"));
        } finally {
       //     resetCookie(urlConnection);
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
      //      resetCookie(urlConnection);
            urlConnection.disconnect();
        }
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        if (TextUtils.isEmpty(userName)) {
            this.userName = EMPTY_USER;
        } else {
            this.userName = userName;
        }
    }

    public String getPasswd() {
        return passwd;
    }

    public void setPasswd(String passwd) {
        this.passwd = passwd;
    }
}
