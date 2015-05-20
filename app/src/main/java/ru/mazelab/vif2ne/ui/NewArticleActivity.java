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

import ru.mazelab.vif2ne.R;
import ru.mazelab.vif2ne.backend.LocalUtils;
import ru.mazelab.vif2ne.backend.RemoteService;
import ru.mazelab.vif2ne.backend.domains.Article;
import ru.mazelab.vif2ne.backend.domains.EventEntry;
import ru.mazelab.vif2ne.backend.tasks.PostArticleTask;

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
 * Created by Colibri  15.05.15 23:52
 * NewArticleActivity.java
 *
 *
 */

public class NewArticleActivity extends BaseActivity {
    private static final String LOG_TAG = "NewArticleActivity";
    protected EventEntry eventEntry;

    protected TextView entryUserNameView, entryDateView, entryTitleView;

    protected EditText entryNewTitle, entryEditArticle;
    protected CheckBox entryToRoot;

    protected LinearLayout editArticleLayout;
    protected WebView webView;

    protected Button postAction, previewAction, clearAction;


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

        webView.setWebViewClient(new WebViewClient() {
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
            }
        });
        postAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new PostArticleTask(session, RemoteService.URL_POST, setArticle()) {
                    @Override
                    public void goSuccess(Object result) {
                        session.setWebContent((String) result);
                        session.loadTree(session.getEventEntries().getLastEvent());
                        Toast.makeText(getApplicationContext(), "refreshing", Toast.LENGTH_SHORT).show();
                        Log.d(LOG_TAG, (String) result);
                        editArticleLayout.setVisibility(View.GONE);
                        webView.setVisibility(View.VISIBLE);
                        webView.loadDataWithBaseURL(RemoteService.URL_POST.substring(0, RemoteService.URL_POST.length() - 3), (String) result, "text/html", "windows-1251", "about:blank");
//                        finish();
                    }
                }.execute((Void) null);
            }
        });
        previewAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new PostArticleTask(session, RemoteService.URL_POST_PREVIEW, setArticle()) {
                    @Override
                    public void goSuccess(Object result) {
                        session.setWebContent((String) result);
                        session.loadTree(session.getEventEntries().getLastEvent());
                        Toast.makeText(getApplicationContext(), "refreshing", Toast.LENGTH_SHORT).show();
                        editArticleLayout.setVisibility(View.GONE);
                        Log.d(LOG_TAG, (String) result);
                        webView.setVisibility(View.VISIBLE);
                        webView.loadDataWithBaseURL(RemoteService.URL_POST.substring(0, RemoteService.URL_POST_PREVIEW.length() - 3), (String) result, "text/html", "windows-1251", "about:blank");
                    }
                }.execute((Void) null);
            }
        });
        bindOne();
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
