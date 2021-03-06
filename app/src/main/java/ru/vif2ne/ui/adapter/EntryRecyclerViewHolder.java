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

package ru.vif2ne.ui.adapter;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.HashMap;

import ru.vif2ne.R;
import ru.vif2ne.Session;
import ru.vif2ne.backend.ExtHtmlTagHandler;
import ru.vif2ne.backend.LocalUtils;
import ru.vif2ne.backend.domains.EventEntries;
import ru.vif2ne.backend.domains.EventEntry;
import ru.vif2ne.backend.tasks.LoadArticleTask;

public class EntryRecyclerViewHolder extends RecyclerView.ViewHolder {


    private static final String LOG_TAG = "EntryRecyclerViewHolder";
    protected TextView entryUserName, entryTitle, entryDate, entrySize, entryChild, entryFavorites,
            levelView, articleWOImages;
    protected View view, signView;
    protected WebView mainWebView;
    protected Session session;
    protected EventEntries eventEntries;
    protected LinearLayoutManager layoutManager;
    protected LinearLayout entryLayout, subRootLayout, entryBodyLayout;
    protected String webContent = "<head>\n" +
            "    <style type=\"text/css\">\n" +
            "        body {\n" +
            "        font-size: 11pt;\n" +
            "        }\n" +
            "    </style>\n" +
            "</head>\n" +
            "<body>\n" +
            "%s\n" +
            "</body>\n";


    public EntryRecyclerViewHolder(Session sessionInput,
                                   View itemView, LinearLayoutManager linearLayoutManager) {
        super(itemView);
        this.view = itemView;
        this.session = sessionInput;
        this.eventEntries = session.getEventEntries();
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

        entryBodyLayout = (LinearLayout) itemView.findViewById(R.id.entry_body_layout);
        signView = itemView.findViewById(R.id.replay_sign);


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

        View.OnClickListener onClickEntry = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventEntry entry = (EventEntry) v.getTag();
                if (!(entry.getPosition() == 0 && !session.getEventEntry().isRoot()) ||
                        session.getCurrentActivity().getMode() != Session.TREE_MODE)
                    session.navigate(entry);
            }
        };

        entryLayout.setOnClickListener(onClickEntry);
        entryBodyLayout.setOnClickListener(onClickEntry);
    }

    public void showArticle(EventEntry eventEntry) {
        if (TextUtils.isEmpty(eventEntry.getArticle())) return;
        if (eventEntry.getArticle().contains("<img ")) {
            articleWOImages.setVisibility(View.GONE);
            mainWebView.loadData(String.format(webContent, eventEntry.getArticle()), "text/html; charset=utf-8", null);
            mainWebView.setBackgroundColor(view.getResources().getColor(R.color.transparent));
        } else {
            articleWOImages.setMaxLines(1000);
            articleWOImages.setText(Html.fromHtml(eventEntry.getArticle()));
            articleWOImages.setVisibility(View.VISIBLE);
        }
    }

    public void bind(EventEntry eventEntry) {
        int position = getAdapterPosition();
        eventEntry.setPosition(position);
        mainWebView.setTag(eventEntry);
        entryFavorites.setTag(eventEntry);
        entryLayout.setTag(eventEntry);
        entryBodyLayout.setTag(eventEntry);

        String s = "";
        if (position != 0) {
            for (int i = 0; i < eventEntry.getLevel(); i++) {
                s = s + "◦";
            }
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
                        //      articleWOImages.setMaxLines(4);
                        //      articleWOImages.setLines(4);
                        //      articleWOImages.setEllipsize(TextUtils.TruncateAt.END);
                        articleWOImages.setText(Html.fromHtml(eventEntry.getArticle()));
                    } else {
                        if (eventEntry.getSize() > 0)
                            articleWOImages.setText("...");
                        else
                            articleWOImages.setText("");
                    }

            }
        }
        HashMap<String, Integer> chld = eventEntries.loadChildCountEventEntriesWithTree(eventEntry);
        Integer newEvent = chld.get("new");
        Integer cntEvent = chld.get("cnt");
        // entryChild.setText(Integer.toString(eventEntry.getChildEventEntries().size()));
        if (eventEntry.getParentEventEntry() != null && eventEntry.getParentEventEntry().isRoot()) {
            entryChild.setText(cntEvent + ((newEvent == 0) ? "" : " нов:" + newEvent));
            if (newEvent > 0) {
                entryChild.setTextColor(view.getResources().getColor(R.color.newEntry));
            } else {
                entryChild.setTextColor(view.getResources().getColor(R.color.primary_text));
            }
            entryChild.setVisibility(View.VISIBLE);
            signView.setVisibility(View.VISIBLE);
        } else {
            signView.setVisibility(View.GONE);
            entryChild.setVisibility(View.GONE);
        }
        if (eventEntries.isNew(eventEntry)) {
            entryTitle.setTextColor(view.getResources().getColor(R.color.newEntry));
        } else
            entryTitle.setTextColor(view.getResources().getColor(R.color.primary_text));
        entryDate.setText(LocalUtils.formatDateTime(view.getContext(), eventEntry.getDate()));
        entrySize.setText(String.format(view.getResources().getString(R.string.bytes), eventEntry.getSize()));
        String author = String.format(view.getResources().getString(R.string.author_template), eventEntry.getAuthor());
        String replay_template = view.getResources().getString(R.string.author_replay_template);
        if (eventEntry.getParentEventEntry().isRoot())
            entryUserName.setText(Html.fromHtml(author));
        else
            entryUserName.setText(Html.fromHtml(
                            author + " " +
                                    String.format(replay_template,
                                            eventEntry.getParentEventEntry().getAuthor())
                    )
            );

        if (eventEntry.getTitleArticle() != null) {
/*
            SpannableString str = new SpannableString(eventEntry.getTitleArticle());
            if (eventEntry.getFixed() > 0) {
                str.setSpan(new StyleSpan(Typeface.BOLD), 0, str.length(), 0);
            }
*/
            String str = eventEntry.getTitleArticle();
            if (eventEntry.getFixed() > 0)
                str = String.format("<b>%s</b>", str);
            if (eventEntry.isDeleted())
                str = String.format("<strike>%s</strike>", str);
            entryTitle.setText(Html.fromHtml(str, null, new ExtHtmlTagHandler()));
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
