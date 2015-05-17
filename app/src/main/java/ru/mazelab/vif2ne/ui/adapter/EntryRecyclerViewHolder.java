package ru.mazelab.vif2ne.ui.adapter;

import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.TextView;

import ru.mazelab.vif2ne.R;
import ru.mazelab.vif2ne.Session;
import ru.mazelab.vif2ne.backend.LocalUtils;
import ru.mazelab.vif2ne.backend.domains.EventEntry;
import ru.mazelab.vif2ne.backend.tasks.LoadArticleTask;

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
 * EntryRecyclerViewHolder.java
 *
 *
 */

public class EntryRecyclerViewHolder extends RecyclerView.ViewHolder {


    private static final String LOG_TAG = "EntryRecyclerViewHolder";
    protected TextView entryUserName, entryTitle, entryDate, entrySize, entryChild, entryFavorites,
            levelView, articleWOImages;
    protected View view;
    protected WebView mainWebView;
    protected Session session;
    protected LinearLayoutManager layoutManager;
    protected LinearLayout entryLayout, subRootLayout;
    protected String webContent = "<head>\n" +
            "    <style type=\"text/css\">\n" +
            "        body {\n" +
            "        font-size: 11pt;\n" +
            "        background-color: #d7ccc8\n" +
            "        }\n" +
            "    </style>\n" +
            "</head>\n" +
            "<body>\n" +
            "%s\n" +
            "</body>\n";


    public EntryRecyclerViewHolder(final Session session, View itemView, LinearLayoutManager linearLayoutManager) {
        super(itemView);
        this.view = itemView;
        this.session = session;
        this.layoutManager = linearLayoutManager;
        entryLayout = (LinearLayout) itemView.findViewById(R.id.entry_layout);
        entryDate = (TextView) itemView.findViewById(R.id.entry_date);
        entrySize = (TextView) itemView.findViewById(R.id.entry_size);
        entryTitle = (TextView) itemView.findViewById(R.id.entry_title);
        entryChild = (TextView) itemView.findViewById(R.id.entry_child);
        entryUserName = (TextView) itemView.findViewById(R.id.entry_user_name);
        entryFavorites = (TextView) itemView.findViewById(R.id.entry_favorites);
        levelView = (TextView) itemView.findViewById(R.id.level_view);
        subRootLayout = (LinearLayout) itemView.findViewById(R.id.sub_root_layout);
        articleWOImages = (TextView) itemView.findViewById(R.id.article_wo_images);
        articleWOImages.setMovementMethod(LinkMovementMethod.getInstance());


        entryFavorites.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(LOG_TAG, "click");
                EventEntry eventEntry = (EventEntry) view.getTag();
                eventEntry.setFavorites(!eventEntry.isFavorites());
                if (eventEntry.isFavorites()) {
                    entryFavorites.setText(view.getResources().getString(R.string.fa_star));
                } else {
                    entryFavorites.setText(view.getResources().getString(R.string.fa_star_o));
                }
                eventEntry.save(session.getDbHelper().getWritableDatabase());
            }
        });

        mainWebView = (WebView) itemView.findViewById(R.id.entry_web_view);

        mainWebView.getSettings().setBlockNetworkLoads(false);
        mainWebView.getSettings().setAllowContentAccess(true);
        mainWebView.getSettings().setBlockNetworkImage(false);
        mainWebView.getSettings().setLoadsImagesAutomatically(true);
        mainWebView.getSettings().setAllowFileAccess(true);

        mainWebView.setVisibility(View.GONE);

        mainWebView.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                mainWebView.setBackgroundColor(view.getResources().getColor(R.color.transparent));
                if (session.getCurrentActivity().getMode() == Session.TREE_MODE) {
                    mainWebView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                view.getContext().startActivity(intent);
                return true;
            }
        });

        entryLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventEntry entry = (EventEntry) v.getTag();
                if (!(entry.getPosition() == 0 && !session.getEventEntry().isRoot()) ||
                        session.getCurrentActivity().getMode() != Session.TREE_MODE)
                    session.navigate(entry);
            }
        });
    }

    public void showArticle(EventEntry eventEntry) {
        if (TextUtils.isEmpty(eventEntry.getArticle())) return;
        articleWOImages.setMaxLines(1000);
        articleWOImages.setText(Html.fromHtml(eventEntry.getArticle()));
        if (eventEntry.getArticle().contains("<img ")) {
            articleWOImages.setVisibility(View.GONE);
            mainWebView.loadData(String.format(webContent, eventEntry.getArticle()), "text/html; charset=utf-8", null);
            mainWebView.setBackgroundColor(view.getResources().getColor(R.color.transparent));
        } else {
            articleWOImages.setVisibility(View.VISIBLE);
        }
    }

    public void bind(EventEntry eventEntry) {
        int position = getAdapterPosition();
        eventEntry.setPosition(position);
        mainWebView.setTag(eventEntry);
        entryFavorites.setTag(eventEntry);
        entryLayout.setTag(eventEntry);

        String s = "";
        if (position != 0)
            for (int i = 0; i < eventEntry.getLevel(); i++) {
                s = s + "◦";
            }
        levelView.setText(s);
        subRootLayout.setVisibility(eventEntry.getParentEventEntry().isRoot() ? View.VISIBLE : View.GONE);

        if (position % 2 == 0) {
            view.setBackgroundResource(R.color.tab_even);
        } else {
            view.setBackgroundResource(R.color.tab_odd);
        }
        eventEntry.setPosition(position);
        view.setTag(eventEntry);
        mainWebView.setVisibility(View.GONE);
        articleWOImages.setVisibility(View.GONE);

        if (!eventEntry.isRoot()
                && session.getCurrentActivity().getMode() == Session.TREE_MODE
                && session.getEventEntry().getSize() > 0) {
            if (position == 0) {
                if (TextUtils.isEmpty(eventEntry.getArticle())) {
                    new LoadArticleTask(session, eventEntry) {
                        @Override
                        public void goSuccess(Object result) {
                            session.intentNeedRefresh("end load one article success");
                        }
                    }.execute((Void) null);
                } else {
                    showArticle(eventEntry);
                }
            } else {
                articleWOImages.setVisibility(View.VISIBLE);
                if (!TextUtils.isEmpty(eventEntry.getArticle())) {
                    articleWOImages.setText(Html.fromHtml(eventEntry.getArticle()));
                    articleWOImages.setMaxLines(4);
                    articleWOImages.setEllipsize(TextUtils.TruncateAt.END);
                } else {
                    if (eventEntry.getSize() > 0)
                        articleWOImages.setText("...");
                    else
                        articleWOImages.setText("");
                }
            }
        }

        entryChild.setText(Integer.toString(eventEntry.getChildEventEntries().size()));
        entryDate.setText(LocalUtils.formatDateTime(view.getContext(), eventEntry.getDate()));
        entrySize.setText("(" + eventEntry.getSize() + " b)");
        entryUserName.setText(eventEntry.getAuthor());
        if (eventEntry.getTitleArticle() != null) {
            SpannableString str = new SpannableString(eventEntry.getTitleArticle());
            if (eventEntry.getFixed() > 0) {
                str.setSpan(new StyleSpan(Typeface.BOLD), 0, str.length(), 0);
            }
            entryTitle.setText(str);
        } else {
            entryTitle.setText("***");
        }

        if (eventEntry.getFixed() > 0) {
            entryFavorites.setText(view.getResources().getString(R.string.fa_thumb_tack));
        } else {
            if (eventEntry.isFavorites()) {
                entryFavorites.setText(view.getResources().getString(R.string.fa_star));
            } else {
                entryFavorites.setText(view.getResources().getString(R.string.fa_star_o));
            }
        }
    }


}
