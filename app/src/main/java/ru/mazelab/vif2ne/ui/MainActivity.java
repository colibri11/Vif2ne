package ru.mazelab.vif2ne.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.ArrayList;

import ru.mazelab.vif2ne.R;
import ru.mazelab.vif2ne.Session;
import ru.mazelab.vif2ne.backend.domains.EventEntry;
import ru.mazelab.vif2ne.backend.tasks.LoadArticleTreeTask;
import ru.mazelab.vif2ne.ui.adapter.EntryRecyclerAdapter;

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
 * MainActivity.java
 *
 *
 */

public class MainActivity extends BaseActivity {


    private static final String LOG_TAG = "MainActivity";

    protected EntryRecyclerAdapter adapter;
    protected RecyclerView recyclerView;

    protected ArrayList<EventEntry> navigatorEventEntries;
    protected MenuItem searchMenuItem;
    protected SearchView mSearchView;
    protected MenuItem menuItemDownload;
    private EventEntry parentEventEntry;
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

        recyclerView = (RecyclerView) findViewById(R.id.main_recycler);
        RecyclerView.ItemAnimator itemAnimator = new DefaultItemAnimator();
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(itemAnimator);

        recyclerView.setAdapter(adapter);


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
        ProgressBar progressBar = (ProgressBar) findViewById(R.id.progress);
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
/*
            if (menuItem.getTitle().equals(getResources().getString(R.string.menu_sign_in))) {
                if (remoteService.isAuthenticated()) {
                    menuItem.setIcon(R.drawable.ic_action_person);
                } else {
                    menuItem.setIcon(R.drawable.ic_action_person_outline);
                }
            }
*/

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
                if (getMode() != Session.FAVORITES_MODE) {
                    menuItem.setIcon(R.drawable.ic_action_grade);
                } else {
                    menuItem.setIcon(R.drawable.ic_action_subject);
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
/*
                    case R.id.bottom_menu_auth:
                        session.navigate(null);
                        intent = new Intent(session.getCurrentActivity(), LoginDialog.class);
                        session.getCurrentActivity().startActivity(intent);
                        break;
*/
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
                            intent = new Intent(session.getCurrentActivity(), LoginDialog.class);
                            intent.putExtra("goto", "add");
                            session.getCurrentActivity().startActivity(intent);
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
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.top_menu_auth:
                session.navigate(null);
                Intent intent = new Intent(session.getCurrentActivity(), LoginDialog.class);
                session.getCurrentActivity().startActivity(intent);
                break;
            default:
                break;

        }
        return super.onOptionsItemSelected(item);
    }


    private void showFavorites() {
        if (getMode() != Session.FAVORITES_MODE) {
            setMode(Session.FAVORITES_MODE);
            session.getEventEntries().loadFavoritesEventEntries(navigatorEventEntries);
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
        new LoadArticleTreeTask(session, navigatorEventEntries) {
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
