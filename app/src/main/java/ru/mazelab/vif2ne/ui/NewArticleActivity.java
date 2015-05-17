package ru.mazelab.vif2ne.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;

import ru.mazelab.vif2ne.R;
import ru.mazelab.vif2ne.backend.LocalUtils;
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

    protected Button postAction;


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
        postAction = (Button) findViewById(R.id.post_action);
        postAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Log.d(LOG_TAG, "on click");
                    Article article = new Article(eventEntry.getArtNo(),
                            entryNewTitle.getText().toString(),
                            entryEditArticle.getText().toString(),
                            entryToRoot.isChecked()
                    );
                    new PostArticleTask(session, article) {
                        @Override
                        public void goSuccess(Object result) {
                            Toast.makeText(getApplicationContext(), "need refresh", Toast.LENGTH_SHORT).show();
                        }
                    }.execute((Void) null);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    protected void bind() {
        if (eventEntry != null) {
            entryDateView.setText(LocalUtils.formatDateTime(this, eventEntry.getDate()));
            entryUserNameView.setText(eventEntry.getAuthor());
            entryTitleView.setText(eventEntry.getTitleArticle());
            String s = eventEntry.getTitleArticle();
            if (s.length() > 20)
                s = s.substring(0, 19);
            entryNewTitle.setText(String.format("Re: %s...", s));
            //s = eventEntry.getArticle();
            //entryEditArticle.setText(s);
        }

    }
}
