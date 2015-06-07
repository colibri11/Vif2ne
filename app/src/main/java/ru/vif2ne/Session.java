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

package ru.vif2ne;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import ru.vif2ne.backend.RemoteService;
import ru.vif2ne.backend.domains.Article;
import ru.vif2ne.backend.domains.DBHelper;
import ru.vif2ne.backend.domains.EventEntries;
import ru.vif2ne.backend.domains.EventEntry;
import ru.vif2ne.backend.domains.Smoking;
import ru.vif2ne.backend.domains.SmokingSettings;
import ru.vif2ne.backend.domains.UserSettings;
import ru.vif2ne.backend.tasks.LoadEventTask;
import ru.vif2ne.backend.tasks.LoadSmokingTask;
import ru.vif2ne.backend.tasks.TasksContainer;
import ru.vif2ne.ui.MainActivity;

public class Session {

    public static final String INTENT_TASK = "ru.vif2ne.INTENT_TASK";
    public static final String INTENT_NEED_REFRESH = "ru.vif2ne.INTENT_NEED_REFRESH";
    public static final int TREE_MODE = 0;
    public static final int FLAT_MODE = 1;
    public static final int SEARCH_MODE = 2;
    public static final int FAVORITES_MODE = 3;
    private static final String LOG_TAG = "Session";
    protected MainApplication application;
    protected TasksContainer tasksContainer;

    protected EventEntries eventEntries;

    protected EventEntry eventEntry;
    protected UserSettings userSettings;


    protected RemoteService remoteService;

    protected MainActivity currentActivity;

    protected DBHelper dbHelper;
    private String webContent;
    private Article article;
    private boolean detailView;
    private Boolean credentialStatus;
    private boolean findUser;

    private Smoking smoking;
    private SmokingSettings smokingSettings;

    public Session(MainApplication application) {
        this.application = application;
        this.tasksContainer = new TasksContainer(this);
        this.remoteService = new RemoteService(this);
        this.eventEntries = new EventEntries(this);
        this.eventEntry = eventEntries.get(0);
        this.smoking = new Smoking();
        this.dbHelper = null;
        this.findUser = false;
        this.userSettings = null;
        this.smokingSettings = null;
        loadPrefs();
        EventEntries.loadHeaderDb(this);
    }

    public void loadSmoking() {
        Log.d(LOG_TAG, "loadSmoking:" + smoking.getLastId());
        new LoadSmokingTask(this) {
            @Override
            public void goSuccess(Object result) {
                Log.d(LOG_TAG, "active users:" + smoking.sizeActive());
                Log.d(LOG_TAG, "messages:" + smoking.sizeMessages());
                Log.d(LOG_TAG, "lastid:" + smoking.getLastId());
            }
        }.execute((Void) null);
    }

    public void loadTree(long eventId) {
        Log.d(LOG_TAG, "loadTree:" + eventId);
        Log.d(LOG_TAG, eventEntry.toString());
        new LoadEventTask(this, eventId) {
            @Override
            protected void postExecuteBackground() {
                super.postExecuteBackground();
                if (getCurrentActivity() != null) {
                    if (getCurrentActivity().swipeRefresh.isRefreshing()) {
                        getCurrentActivity().swipeRefresh.setRefreshing(false);
                        getCurrentActivity().swipeRefresh.setEnabled(true);
                    }
                }
            }

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
                Log.d(LOG_TAG, "entries size:" + eventEntries.size());
            }
        }.execute((Void) null);
    }

    public void loadPrefs() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(application);
        setDetailView(prefs.getBoolean("pref_detail", false));
        setFindUser(prefs.getBoolean("pref_find_users", false));
        Log.d(LOG_TAG, "detail:" + isDetailView());
        intentNeedRefresh("end: loadPrefs");
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

    public boolean isDetailView() {
        return detailView;
    }

    public void setDetailView(boolean detailView) {
        this.detailView = detailView;
    }

    public Boolean getCredentialStatus() {
        return credentialStatus;
    }

    public void setCredentialStatus(Boolean credentialStatus) {
        this.credentialStatus = credentialStatus;
    }

    public boolean isFindUser() {
        return findUser;
    }

    public void setFindUser(boolean findUser) {
        this.findUser = findUser;
    }


    public Smoking getSmoking() {
        return smoking;
    }

    public UserSettings getUserSettings() {
        return userSettings;
    }

    public void setUserSettings(UserSettings userSettings) {
        this.userSettings = userSettings;
    }

    public SmokingSettings getSmokingSettings() {
        return smokingSettings;
    }

    public void setSmokingSettings(SmokingSettings smokingSettings) {
        this.smokingSettings = smokingSettings;
    }
}
