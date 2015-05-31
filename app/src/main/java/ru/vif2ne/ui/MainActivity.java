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

package ru.vif2ne.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.ArrayList;

import ru.vif2ne.R;
import ru.vif2ne.Session;
import ru.vif2ne.backend.LocalUtils;
import ru.vif2ne.backend.domains.EventEntries;
import ru.vif2ne.backend.domains.EventEntry;
import ru.vif2ne.backend.tasks.LoadArticleTreeTask;
import ru.vif2ne.ui.adapter.EntryRecyclerAdapter;

public class MainActivity extends BaseActivity {


    private static final String LOG_TAG = "MainActivity";
    public SwipeRefreshLayout swipeRefresh;
    protected EntryRecyclerAdapter adapter;
    protected RecyclerView recyclerView;
    protected ArrayList<EventEntry> navigatorEventEntries;
    protected MenuItem searchMenuItem;
    protected SearchView mSearchView;
    protected MenuItem menuItemDownload;
    protected MainActivity activity;
    private EventEntry parentEventEntry;
    private ProgressBar progressBar;
    private Toolbar toolbarBottom;
    private int mode;

    @Override
    protected void onTaskAction(Intent intent, String taskClassName, Integer mode) {
/*

        Log.d(LOG_TAG, "task = " + " mode:" + mode + " class:" + taskClassName);
        if (LoadEventTask.class.getName().equals(taskClassName) && mode == AbstractTask.TASK_END) {
            bind();
        }
*/
        showNonBlockingProgress();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.navigatorEventEntries = new ArrayList<>();
        session.setCurrentActivity(this);
        setParentEventEntry(session.getEventEntry());
        if (getParentEventEntry().isRoot())
            setMode(Session.FLAT_MODE);
        else
            setMode(Session.TREE_MODE);

        Log.d(LOG_TAG, Integer.toString(getMode()));

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        adapter = new EntryRecyclerAdapter(session, this.navigatorEventEntries, layoutManager);

        swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefresh.setEnabled(false);
                if (session.getTasksContainer().size() == 0) {
                    session.loadTree(session.getEventEntries().getLastEvent());
                    showNonBlockingProgress();
                }
            }
        });

        recyclerView = (RecyclerView) findViewById(R.id.main_recycler);
        RecyclerView.ItemAnimator itemAnimator = new DefaultItemAnimator();
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(itemAnimator);

        recyclerView.setAdapter(adapter);


        session.loadPrefs();
        EventEntries.loadHeaderDb(session);

        if (session.getEventEntries().getLastEvent() == -1) {
            session.loadTree(-1);
        }

        Toolbar toolbarTop = (Toolbar) findViewById(R.id.toolbar_top);
        setSupportActionBar(toolbarTop);
/*
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);
*/
        initBottomToolbar();
        progressBar = (ProgressBar) findViewById(R.id.progress);
        try {
            String enc = LocalUtils.enc("");
            Log.d(LOG_TAG, "*****:" + LocalUtils.dec(enc) + " " + enc);
            enc = LocalUtils.enc("34567asdfg");
            Log.d(LOG_TAG, "*****:" + LocalUtils.dec(enc) + " " + enc);
        } catch (Exception e) {
            e.printStackTrace();
        }

        this.activity = this;


    }


    @Override
    protected void bind() {
        session.setCurrentActivity(this);
        session.setEventEntry(getParentEventEntry());
        navigatorEventEntries.clear();
        if (getParentEventEntry().isRoot()) {
            session.getEventEntries().loadChildEventEntries(navigatorEventEntries, getParentEventEntry());
        } else {
            session.getEventEntries().loadChildEventEntriesWithTree(navigatorEventEntries, getParentEventEntry(), 0);
            navigatorEventEntries.add(0, getParentEventEntry());
        }

        adapter.notifyDataSetChanged();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            if (getParentEventEntry().isRoot()) {
                actionBar.setDisplayShowHomeEnabled(true);
                actionBar.setDisplayHomeAsUpEnabled(false);
            } else {
                actionBar.setDisplayShowHomeEnabled(false);
                actionBar.setDisplayHomeAsUpEnabled(true);
            }
        }
//        getSupportActionBar().setDisplayHomeAsUpEnabled(!session.getNavigator().getEventEntry().isRoot());
        showNonBlockingProgress();
        refreshBottomMenu();
    }

    public void showNonBlockingProgress() {

        if (progressBar != null) {
            if (session.getTasksContainer().size() > 0) {
                Log.d(LOG_TAG, "show progress");
                progressBar.setVisibility(View.VISIBLE);
            } else {
                progressBar.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (mode == Session.SEARCH_MODE)
            searchMenuItem.collapseActionView();
        if (mode == Session.FAVORITES_MODE) {
            showFavorites();
        } else
            super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void refreshBottomMenu() {
        for (int i = 0; i < toolbarBottom.getMenu().size(); i++) {
            MenuItem menuItem = toolbarBottom.getMenu().getItem(i);

            if (menuItem.getTitle().equals(getResources().getString(R.string.menu_sign_in))) {
                if (remoteService.isAuthenticated()) {
                    menuItem.setIcon(R.drawable.ic_action_person);
                } else {
                    menuItem.setIcon(R.drawable.ic_action_person_outline);
                }
            }

            if (menuItem.getTitle().equals(getResources().getString(R.string.menu_up))) {
                if (!getParentEventEntry().isRoot() && getMode() == Session.TREE_MODE) {
                    menuItem.setEnabled(true);
                    menuItem.setIcon(R.drawable.ic_action_expand_less);
                } else {
                    menuItem.setEnabled(false);
                    menuItem.setIcon(R.drawable.ic_action_expand_less_disabled);
                }
            }
            if (menuItem.getTitle().equals(getResources().getString(R.string.menu_home))) {
                if (!getParentEventEntry().isRoot() && getMode() == Session.TREE_MODE) {
                    menuItem.setEnabled(true);
                    menuItem.setIcon(R.drawable.ic_action_home);
                } else {
                    menuItem.setEnabled(false);
                    menuItem.setIcon(R.drawable.ic_action_home_disabled);
                }
            }
            if (menuItem.getTitle().equals(getResources().getString(R.string.menu_add))) {
                if (getParentEventEntry().isRoot()) {
                    menuItem.setEnabled(false);
                    menuItem.setIcon(R.drawable.ic_action_add_disabled);
                } else {
                    menuItem.setEnabled(true);
                    menuItem.setIcon(R.drawable.ic_action_add);
                }
            }

            if (menuItem.getTitle().equals(getResources().getString(R.string.menu_download))) {
                menuItemDownload = menuItem;
                if (!getParentEventEntry().isRoot() && getMode() == Session.TREE_MODE) {
                    menuItem.setEnabled(true);
                    menuItem.setIcon(R.drawable.ic_action_file_download);
                } else {
                    menuItem.setEnabled(false);
                    menuItem.setIcon(R.drawable.ic_action_file_download_disabled);
                }
            }
            if (menuItem.getTitle().equals(getResources().getString(R.string.menu_favorites))) {
                if (getMode() == Session.FAVORITES_MODE) {
                    menuItem.setIcon(R.drawable.ic_action_toggle_star_outline);
                } else {
                    menuItem.setIcon(R.drawable.ic_action_toggle_star);
                }
            }


        }
    }

    public void setDownloadMenuItemEnabled(boolean enabled) {
        if (enabled && !getParentEventEntry().isRoot() && getMode() == Session.TREE_MODE) {
            menuItemDownload.setEnabled(true);
            menuItemDownload.setIcon(R.drawable.ic_action_file_download);
        } else {
            menuItemDownload.setEnabled(false);
            menuItemDownload.setIcon(R.drawable.ic_action_file_download_disabled);
        }

    }

    private void initBottomToolbar() {
        toolbarBottom = (Toolbar) findViewById(R.id.toolbar_bottom);
        toolbarBottom.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Intent intent;
                switch (item.getItemId()) {
                    case R.id.bottom_menu_home:
                        session.navigate(null);
                        break;
                    case R.id.bottom_menu_refresh:
                        if (session.getTasksContainer().size() == 0) {
                            session.loadTree(session.getEventEntries().getLastEvent());
                            showNonBlockingProgress();
                        }
                        break;
                    case R.id.bottom_menu_auth:
                        session.navigate(null);
                        intent = new Intent(session.getCurrentActivity(), LoginDialog.class);
                        session.getCurrentActivity().startActivity(intent);
                        break;
                    case R.id.bottom_menu_favorites:
                        showFavorites();
                        break;
                    case R.id.bottom_menu_up:
                        if (!getParentEventEntry().isRoot()) {
                            session.navigate(getParentEventEntry().getParentEventEntry());
                        }
                        break;
                    case R.id.bottom_menu_add:
                        if (!remoteService.isAuthenticated()) {
                        /*    intent = new Intent(session.getCurrentActivity(), LoginDialog.class);
                            intent.putExtra("goto", "add");
                            session.getCurrentActivity().startActivity(intent);
                            */
                            Toast.makeText(activity, getResources().getString(R.string.not_login), Toast.LENGTH_SHORT).show();
                        } else {
                            intent = new Intent(session.getCurrentActivity(), NewArticleActivity.class);
                            session.getCurrentActivity().startActivity(intent);
                        }
                        break;
                    case R.id.bottom_menu_download_tree:
                        setDownloadMenuItemEnabled(false);
                        loadTreeArticles();
                        break;
                }
                return true;
            }
        });
        toolbarBottom.inflateMenu(R.menu.toolbar_bottom);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_top, menu);

        searchMenuItem = menu.findItem(R.id.top_menu_search);
        mSearchView = (SearchView) searchMenuItem.getActionView();
        setupSearchView();

        showNonBlockingProgress();

        return true;
    }

    public void setupSearchView() {
        mSearchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                Log.d(LOG_TAG, "onClose");
                resetMode();
                return false;
            }
        });
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.length() > 2) {
                    session.getEventEntries().loadMatchEventEntries(newText, navigatorEventEntries);
                    adapter.notifyDataSetChanged();
                } else if (getMode() != Session.SEARCH_MODE) {
                    navigatorEventEntries.clear();
                    setMode(Session.SEARCH_MODE);
                    adapter.notifyDataSetChanged();
                }
                return false;
            }
        });


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
/*            case R.id.top_menu_auth:
                session.navigate(null);
                intent = new Intent(session.getCurrentActivity(), LoginDialog.class);
                session.getCurrentActivity().startActivity(intent);
                break;
 */
            case R.id.top_menu_preferences:
                intent = new Intent(session.getCurrentActivity(), MainPreferences.class);
                session.getCurrentActivity().startActivity(intent);
            default:
                break;

        }
        return super.onOptionsItemSelected(item);
    }


    private void showFavorites() {
        if (getMode() != Session.FAVORITES_MODE) {
            setMode(Session.FAVORITES_MODE);
            session.getEventEntries().loadFavoritesEventEntries(navigatorEventEntries);
            refreshBottomMenu();
            adapter.notifyDataSetChanged();
        } else {
            resetMode();
        }
    }

    private void resetMode() {
        setMode(getParentEventEntry().isRoot() ? Session.FLAT_MODE : Session.TREE_MODE);
        bind();
    }

    private void loadTreeArticles() {
        int i = 0;
        for (EventEntry eventEntry : navigatorEventEntries) {
            if (TextUtils.isEmpty(eventEntry.getArticle()) && eventEntry.getSize() > 0) {
                i++;
            }
        }
        progressBar.setMax(i);
        progressBar.setProgress(0);
        progressBar.setIndeterminate(false);
        new LoadArticleTreeTask(session, navigatorEventEntries) {
            @Override
            protected void onProgressUpdate(Object... values) {
                progressBar.setProgress((Integer) values[0]);
                super.onProgressUpdate(values);
            }

            @Override
            public void goSuccess(Object result) {
                Toast.makeText(getApplicationContext(), String.format(
                        getResources().getString(R.string.task_load_articles), (Integer) result
                ), Toast.LENGTH_SHORT).show();
                session.intentNeedRefresh("end: MainActivity loadTreeArticles success");
            }

            @Override
            protected void onPostExecute(Object o) {
                super.onPostExecute(o);
                setDownloadMenuItemEnabled(true);
                progressBar.setIndeterminate(true);
            }
        }.execute((Void) null);
    }

    public EventEntry getParentEventEntry() {
        return parentEventEntry;
    }

    public void setParentEventEntry(EventEntry parentEventEntry) {
        this.parentEventEntry = parentEventEntry;
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

}
