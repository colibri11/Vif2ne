package ru.mazelab.vif2ne.ui;

import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import ru.mazelab.vif2ne.R;
import ru.mazelab.vif2ne.backend.LocalUtils;
import ru.mazelab.vif2ne.backend.domains.EventEntry;

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
    protected EventEntry eventEntry;

    protected TextView entryUserNameView, entryDateView, entryTitleView;

    protected EditText entryNewTitle, entryEditArticle;
    protected CheckBox entryToRoot;


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
    }

    @Override
    protected void bind() {
        if (eventEntry != null) {
            entryDateView.setText(LocalUtils.formatDateTime(this, eventEntry.getDate()));
            entryUserNameView.setText(eventEntry.getAuthor());
            entryTitleView.setText(eventEntry.getTitleArticle());
            String s = eventEntry.getTitleArticle();
            if (s.length() > 10)
                s = s.substring(0, 9);
            entryNewTitle.setText(String.format("Re: %s...", s));
            s = eventEntry.getArticle();
            entryEditArticle.setText(s);
        }

    }
}
