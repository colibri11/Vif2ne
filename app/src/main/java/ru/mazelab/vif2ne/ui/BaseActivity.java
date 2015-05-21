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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import ru.mazelab.vif2ne.MainApplication;
import ru.mazelab.vif2ne.Session;
import ru.mazelab.vif2ne.backend.RemoteService;
import ru.mazelab.vif2ne.backend.tasks.AbstractTask;

public abstract class BaseActivity extends AppCompatActivity {


    private static final String LOG_TAG = "BaseActivity";
    protected Session session;
    protected MainApplication mainApplication;
    protected RemoteService remoteService;

    private BroadcastReceiver refreshBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context ctxt, Intent i) {
            Log.d(LOG_TAG, "intent refresh: " + i.getStringExtra("log"));
            bind();
        }
    };

    private BroadcastReceiver taskBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context ctxt, Intent i) {
            if (i != null && i.getStringExtra("class") != null) {
                onTaskAction(i, i.getStringExtra("class"), i.getIntExtra("mode", AbstractTask.TASK_START));
            } else
                Log.d(LOG_TAG, "intent null");
        }
    };

    protected void onTaskAction(Intent intent, String taskClassName, Integer mode) {

    }

    ;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

/*

        LocalBroadcastManager.getInstance(this).registerReceiver(taskBroadcastReceiver,
                new IntentFilter(Session.INTENT_TASK));
        LocalBroadcastManager.getInstance(this).registerReceiver(refreshBroadcastReceiver,
                new IntentFilter(Session.INTENT_NEED_REFRESH));
*/
//        taskBroadcastReceiver.onReceive(this, null);

        mainApplication = (MainApplication) getApplication();
        session = mainApplication.getSession();
        remoteService = session.getRemoteService();
    }

    protected abstract void bind();

    @Override
    protected void onResume() {
        super.onResume();

        LocalBroadcastManager.getInstance(this).registerReceiver(taskBroadcastReceiver,
                new IntentFilter(Session.INTENT_TASK));
        LocalBroadcastManager.getInstance(this).registerReceiver(refreshBroadcastReceiver,
                new IntentFilter(Session.INTENT_NEED_REFRESH));

        bind();
    }

    @Override
    protected void onPause() {
        Log.d(LOG_TAG, "onPause");
        LocalBroadcastManager.getInstance(this).unregisterReceiver(taskBroadcastReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(refreshBroadcastReceiver);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        Log.d(LOG_TAG, "onDestroy");
        super.onDestroy();
    }
}
