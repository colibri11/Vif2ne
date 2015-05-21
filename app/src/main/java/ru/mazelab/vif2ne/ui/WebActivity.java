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

package ru.mazelab.vif2ne.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.HttpAuthHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

import ru.mazelab.vif2ne.R;
import ru.mazelab.vif2ne.backend.RemoteService;
import ru.mazelab.vif2ne.backend.domains.Article;

public class WebActivity extends BaseActivity {
    private static final String LOG_TAG = "WebActivity";
    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);
        webView = (WebView) findViewById(R.id.web_view);

    }

    @Override
    protected void bind() {
/*        String webContent = "";
        webView.loadData(session.getWebContent(),"text-html","windows-1251");
        webView.setHttpAuthUsernamePassword(null,null,remoteService.getUserName(),remoteService.getPasswd());
        */
        webView.setHttpAuthUsernamePassword("http://", null, remoteService.getUserName(), remoteService.getPasswd());

        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setBlockNetworkLoads(false);
        webView.getSettings().setAllowContentAccess(true);
        webView.getSettings().setBlockNetworkImage(false);
        webView.getSettings().setLoadsImagesAutomatically(true);

        webView.setVisibility(View.VISIBLE);


        Article article = session.getArticle();


        String html = "<html>" +
                "\n<body onLoad=\"document.getElementById('form').submit()\">" +
                "\n<form id=\"form\" target=\"_self\" accept-charset=\"windows-1251\" enctype=\"application/x-www-form-urlencoded\" method=\"POST\" action=\"" +
                String.format(RemoteService.URL_POST_PREVIEW, article.getId())
                + "\">";
        for (Map.Entry<String, String> entry : article.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            Log.d(LOG_TAG, value);
            try {
                value = URLEncoder.encode(
                        new String(value
                                .replace("\"","&quot;")
                                        //     .replace(">","&gt;")
//                                .replace("<","&lt;")
                                .getBytes(), "UTF-8"), "windows-1251").replace("+", "%20");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            html = html + "\n<input type=\"hidden\" name=\"" + key + "\" value=\"" + value + "\" />";
        }
        html = html + "\n</form>\n</body>\n</html>";


        Log.d(LOG_TAG, html);

        webView.loadData(html, "text/html; charset=windows-1251", null);
        webView.setBackgroundColor(getResources().getColor(R.color.vif_dark));

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onReceivedHttpAuthRequest(WebView view, HttpAuthHandler handler, String host, String realm) {
                handler.proceed(remoteService.getUserName(), remoteService.getPasswd());
            }
        });


    }

}
