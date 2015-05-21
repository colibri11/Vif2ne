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

package ru.mazelab.vif2ne.backend.tasks;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.widget.Toast;

import ru.mazelab.vif2ne.R;
import ru.mazelab.vif2ne.Session;
import ru.mazelab.vif2ne.backend.RemoteService;
import ru.mazelab.vif2ne.throwable.ApplicationException;


public abstract class AbstractTask extends AsyncTask<Void, Void, Object> {


    public static final Integer TASK_START = 0;
    public static final Integer TASK_END = 1;
    private static final String LOG_TAG = "AbstractTask";
    protected Context ctx;
    protected Session session;


    protected RemoteService remoteService;
    protected ApplicationException throwable;
    protected boolean showError;
    protected String customErrorMessage;
    protected TasksContainer tasksContainer;

    protected AbstractTask(Session session) {
        customErrorMessage = "";
        showError = true;
        this.session = session;
        this.tasksContainer = session.getTasksContainer();
        this.ctx = session.getApplication().getApplicationContext();
        this.remoteService = session.getRemoteService();
    }

    public abstract void goSuccess(Object result);

    public void goError() {
    }

    ;

    public void goCancelled() {
    }

    ;

    protected abstract String getClassName();

    protected abstract Object remoteCall() throws ApplicationException;


    protected void postExecuteBackground() {
    }


    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        intentAction(TASK_START);
    }

    protected void intentAction(int mode) {
        Intent serviceStartedIntent = new Intent(Session.INTENT_TASK);
        serviceStartedIntent.putExtra("class", getClassName());
        serviceStartedIntent.putExtra("mode", mode);
        LocalBroadcastManager.getInstance(ctx).sendBroadcast(serviceStartedIntent);
        if (mode == TASK_START) {
            session.getTasksContainer().add(getClassName(), this);
        } else
            session.getTasksContainer().remove(getClassName());
    }

    @Override
    protected void onPostExecute(Object o) {
        super.onPostExecute(o);
        if (o != null) {
            goSuccess(o);
            postExecuteBackground();
        } else {

            if (showError && !tasksContainer.isLock()) {
                String errorMsg;
                if (TextUtils.isEmpty(customErrorMessage))
                    errorMsg = session.getApplication().getResources().getString(
                            R.string.error_background_task) + getClassName() + ":" + getErrorCode();
                else
                    errorMsg = customErrorMessage;
                Toast.makeText(ctx, errorMsg, Toast.LENGTH_LONG).show();
            }
            goError();
        }
        intentAction(TASK_END);
    }

    @Override
    protected void onCancelled() {
        goCancelled();
        super.onCancelled();
        tasksContainer.remove(getClassName());
    }

    @Override
    protected Object doInBackground(Void... params) {
        if (tasksContainer.isLock())
            return null;
        try {
            Object o = remoteCall();
            throwable = null;
            return o;
        } catch (ApplicationException e) {
            throwable = e;
            e.printStackTrace();
            return null;
        }
    }

    public ApplicationException getThrowable() {
        return throwable;
    }

    public Integer getErrorCode() {
        if (throwable == null) return ApplicationException.SC_EMPTY_RESULT;
        return throwable.getCode();
    }
}