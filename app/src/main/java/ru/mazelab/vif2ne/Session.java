package ru.mazelab.vif2ne;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import ru.mazelab.vif2ne.backend.RemoteService;
import ru.mazelab.vif2ne.backend.domains.Article;
import ru.mazelab.vif2ne.backend.domains.DBHelper;
import ru.mazelab.vif2ne.backend.domains.EventEntries;
import ru.mazelab.vif2ne.backend.domains.EventEntry;
import ru.mazelab.vif2ne.backend.tasks.LoadEventTask;
import ru.mazelab.vif2ne.backend.tasks.TasksContainer;
import ru.mazelab.vif2ne.ui.MainActivity;

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
 * Created by Colibri  15.05.15 22:19
 * Session.java
 *
 *
 */

public class Session {

    public static final String INTENT_TASK = "ru.mazelab.vif2ne.INTENT_TASK";
    public static final String INTENT_NEED_REFRESH = "ru.mazelab.vif2ne.INTENT_NEED_REFRESH";
    public static final int TREE_MODE = 0;
    public static final int FLAT_MODE = 1;
    public static final int SEARCH_MODE = 2;
    public static final int FAVORITES_MODE = 3;
    private static final String LOG_TAG = "Session";
    protected MainApplication application;
    protected TasksContainer tasksContainer;

    protected EventEntries eventEntries;

    protected EventEntry eventEntry;

    protected RemoteService remoteService;

    protected MainActivity currentActivity;

    protected DBHelper dbHelper;
    private String webContent;
    private Article article;

    public Session(MainApplication application) {
        this.application = application;
        this.tasksContainer = new TasksContainer(this);
        this.remoteService = new RemoteService();
        this.eventEntries = new EventEntries(this);
        eventEntry = eventEntries.get(0);
        this.dbHelper = null;
    }

    public void loadTree(long eventId) {
        Log.d(LOG_TAG, "loadTree:" + eventId);
        Log.d(LOG_TAG, eventEntry.toString());
        new LoadEventTask(this, eventId) {
            @Override
            public void goSuccess(Object result) {
                if (getCurrentActivity() != null && (Boolean) result) {
                    Toast.makeText(currentActivity,
                            String.format(
                                    currentActivity.getResources().getString(R.string.events_load),
                                    eventEntries.getLastLoadedIds().size())
                            , Toast.LENGTH_LONG).show();
                }
                intentNeedRefresh(String.format("end: session.loadTree (%d) success",
                        eventEntries.getLastLoadedIds().size()));
            }
        }.execute((Void) null);
    }


    public DBHelper getDbHelper() {
        if (dbHelper == null) dbHelper = new DBHelper(application.getApplicationContext());
        return dbHelper;
    }

    public MainApplication getApplication() {
        return application;
    }

    public TasksContainer getTasksContainer() {
        return tasksContainer;
    }

    public RemoteService getRemoteService() {
        return remoteService;
    }

    public MainActivity getCurrentActivity() {
        return currentActivity;
    }

    public void setCurrentActivity(MainActivity currentActivity) {
        this.currentActivity = currentActivity;
    }

    public EventEntries getEventEntries() {
        return eventEntries;
    }

    public void navigate(EventEntry entry) {
        Intent intent = new Intent(currentActivity, MainActivity.class);
        if (entry == null) {
            setEventEntry(eventEntries.get(0));
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        } else {
            setEventEntry(entry);

        }
        currentActivity.startActivity(intent);
    }


    public void intentNeedRefresh(String log) {
        Log.d(LOG_TAG, "intent: " + log);
        Intent serviceStartedIntent = new Intent(Session.INTENT_NEED_REFRESH);
        serviceStartedIntent.putExtra("log", log);
        LocalBroadcastManager.getInstance(application.getApplicationContext()).sendBroadcast(serviceStartedIntent);
    }

    public EventEntry getEventEntry() {
        return eventEntry;
    }

    public void setEventEntry(EventEntry eventEntry) {
        this.eventEntry = eventEntry;
    }

    public String getWebContent() {
        return webContent;
    }

    public void setWebContent(String webContent) {
        this.webContent = webContent;
    }

    public Article getArticle() {
        return article;
    }

    public void setArticle(Article article) {
        this.article = article;
    }
}
