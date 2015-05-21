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
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.HttpAuthHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.jsoup.Jsoup;

import java.util.Map;

import ru.mazelab.vif2ne.R;
import ru.mazelab.vif2ne.backend.LocalUtils;
import ru.mazelab.vif2ne.backend.RemoteService;
import ru.mazelab.vif2ne.backend.domains.Article;
import ru.mazelab.vif2ne.backend.domains.EventEntry;
import ru.mazelab.vif2ne.backend.tasks.PostArticleTask;

public class NewArticleActivity extends BaseActivity {
    private static final String LOG_TAG = "NewArticleActivity";
    protected EventEntry eventEntry;

    protected TextView entryUserNameView, entryDateView, entryTitleView;

    protected EditText entryNewTitle, entryEditArticle;
    protected CheckBox entryToRoot;

    protected LinearLayout editArticleLayout;
    protected WebView webView;

    protected Button postAction, previewAction, clearAction;
    protected Boolean post;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_article);
        eventEntry = session.getEventEntry();
        entryDateView = (TextView) findViewById(R.id.entry_date);
        entryEditArticle = (EditText) findViewById(R.id.entry_edit_article);
//        entryHeadlineView = (TextView) findViewById(R.id.entry_headline);
        entryUserNameView = (TextView) findViewById(R.id.entry_user_name);
        entryTitleView = (TextView) findViewById(R.id.entry_title);
        entryNewTitle = (EditText) findViewById(R.id.entry_new_title);
        entryToRoot = (CheckBox) findViewById(R.id.entry_to_root);

        previewAction = (Button) findViewById(R.id.preview_action);
        postAction = (Button) findViewById(R.id.post_action);
        clearAction = (Button) findViewById(R.id.clear_action);

        editArticleLayout = (LinearLayout) findViewById(R.id.edit_article_layout);
        webView = (WebView) findViewById(R.id.web_view);

        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setBlockNetworkLoads(false);
        webView.getSettings().setAllowContentAccess(true);
        webView.getSettings().setBlockNetworkImage(false);
        webView.getSettings().setLoadsImagesAutomatically(true);
        webView.setBackgroundColor(getResources().getColor(R.color.vif_dark));
        post = false;
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if (post) {
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.refreshing_tree), Toast.LENGTH_SHORT).show();
                    session.loadTree(session.getEventEntries().getLastEvent());
                    post = false;
                }
            }

            @Override
            public void onReceivedHttpAuthRequest(WebView view, HttpAuthHandler handler, String host, String realm) {
                handler.proceed(remoteService.getUserName(), remoteService.getPasswd());
            }
        });

        clearAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                entryNewTitle.setText("");
                entryEditArticle.setText("");
                entryToRoot.setChecked(false);
                entryNewTitle.requestFocus();
            }
        });
        postAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                webPost();

                new PostArticleTask(session, RemoteService.URL_POST, setArticle()) {
                    @Override
                    public void goSuccess(Object result) {
                        session.setWebContent((String) result);
                        session.loadTree(session.getEventEntries().getLastEvent());
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.refreshing_tree), Toast.LENGTH_SHORT).show();
//                        editArticleLayout.setVisibility(View.GONE);
//                        webView.setVisibility(View.VISIBLE);
//                        webView.loadDataWithBaseURL(RemoteService.URL_POST.substring(0, RemoteService.URL_POST.length() - 3), (String) result, "text/html", "windows-1251", "about:blank");
                        finish();
                    }
                }.execute((Void) null);
            }
        });
        previewAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                webPreview();
                new PostArticleTask(session, RemoteService.URL_POST_PREVIEW, setArticle()) {
                    @Override
                    public void goSuccess(Object result) {
                        session.setWebContent((String) result);
                        session.loadTree(session.getEventEntries().getLastEvent());
                        editArticleLayout.setVisibility(View.GONE);
                        webView.setVisibility(View.VISIBLE);
                        Log.d(LOG_TAG, "html" + (String) result);
                        webView.loadDataWithBaseURL(RemoteService.URL_POST_PREVIEW.substring(0, RemoteService.URL_POST_PREVIEW.length() - 3), (String) result, "text/html", "windows-1251", "about:blank");
                    }
                }.execute((Void) null);

            }
        });
        bindOne();
    }

    protected void webPost() {
        Article article = setArticle();
        String html = "<html>" +
                "\n<body onLoad=\"document.getElementById('form').submit()\">" +
                "\n<form id=\"form\" target=\"_self\" accept-charset=\"windows-1251\" enctype=\"application/x-www-form-urlencoded\" method=\"POST\" action=\"" +
                String.format(RemoteService.URL_POST, article.getId())
                + "\">";
        for (Map.Entry<String, String> entry : article.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            value = article.encode(value);
            html = html + "\n<input type=\"hidden\" name=\"" + key + "\" value=\"" + value + "\" />";
        }
        html = html + "\n</form>\n</body>\n</html>";
        Log.d(LOG_TAG, html);
        post = true;
        webView.loadData(html, "text/html; charset=windows-1251", null);
        webView.setVisibility(View.VISIBLE);
        editArticleLayout.setVisibility(View.GONE);
    }

    protected void webPreview() {
        Article article = setArticle();
        String html = "<html>" +
                "\n<body onLoad=\"document.getElementById('form').submit()\">" +
                "\n<form id=\"form\" target=\"_self\" accept-charset=\"windows-1251\" enctype=\"application/x-www-form-urlencoded\" method=\"POST\" action=\"" +
                String.format(RemoteService.URL_POST_PREVIEW, article.getId())
                + "\">";
        for (Map.Entry<String, String> entry : article.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            value = article.encode(value);
            html = html + "\n<input type=\"hidden\" name=\"" + key + "\" value=\"" + value + "\" />";
        }
        html = html + "\n</form>\n</body>\n</html>";
        Log.d(LOG_TAG, html);
        post = false;
        webView.loadData(html, "text/html; charset=windows-1251", null);
        webView.setVisibility(View.VISIBLE);
        editArticleLayout.setVisibility(View.GONE);
    }

    @Override
    protected void bind() {

    }

    public void bindOne() {
        if (eventEntry != null) {
            entryDateView.setText(LocalUtils.formatDateTime(this, eventEntry.getDate()));
            entryUserNameView.setText("К:" + eventEntry.getAuthor());
            entryTitleView.setText(eventEntry.getTitleArticle());
            String s = eventEntry.getTitleArticle();
            if (!TextUtils.isEmpty(s)) {
                if (s.indexOf("Re:") != 0) s = "Re:" + s;
                entryNewTitle.setText(s);
            }
            if (eventEntry.getArticle() != null) {
                entryEditArticle.setText(">" + Jsoup.parse(eventEntry.getArticle().replace("<BR>", "*%*")).text().replace("*%*", "\n>"));
            } else {
                entryEditArticle.setText("");
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (webView.getVisibility() == View.VISIBLE) {
            webView.setVisibility(View.GONE);
            editArticleLayout.setVisibility(View.VISIBLE);
        } else
            super.onBackPressed();
    }

    public Article setArticle() {
        return new Article(eventEntry.getArtNo(),
                entryNewTitle.getText().toString(),
                entryEditArticle.getText().toString(),
                entryToRoot.isChecked()
        );
    }
}
