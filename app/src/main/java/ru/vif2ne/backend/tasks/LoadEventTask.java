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

package ru.vif2ne.backend.tasks;

import android.text.TextUtils;
import android.util.Log;

import ru.vif2ne.Session;
import ru.vif2ne.throwable.ApplicationException;

public abstract class LoadEventTask extends AbstractTask {

    private static final String LOG_TAG = "LoadEventTask";
    private Long eventId;

    protected LoadEventTask(Session session, long eventId) {
        super(session);
        this.eventId = eventId;
    }

    @Override
    protected String getClassName() {
        return LoadEventTask.class.getName();
    }

    @Override
    protected Object remoteCall() throws ApplicationException {
        try {
            if (!remoteService.isAuthenticated() &&
                    !TextUtils.isEmpty(remoteService.getUserName()) &&
                    !TextUtils.isEmpty(remoteService.getPasswd())) {
                try {
                    Log.d(LOG_TAG, "login");

                    remoteService.login(session.getRemoteService().getUserName(),
                            session.getRemoteService().getPasswd());
                } catch (ApplicationException ae) {
                    session.getRemoteService().setUserName(null);
                }
            }

            if (eventId == -1) {
                session.getEventEntries().clearEntries();
                eventId = session.getEventEntries().load(session.getRemoteService().getUserName());
                Log.d(LOG_TAG, "last event:" + Long.toString(session.getEventEntries().getLastEvent()));
                session.intentNeedRefresh("end: session.getEventEntries().load()");
            }
            try {
                Boolean rs = remoteService.loadEventEntries(session.getEventEntries(), eventId);
                session.intentNeedRefresh("end: remoteService.loadEventEntries()");
                return rs;
            } catch (ApplicationException e) {
                if (e.getCode() == 201) {
                    session.getEventEntries().clearEntriesDB();
                    return remoteService.loadEventEntries(session.getEventEntries(), -1);
                } else
                    throw new ApplicationException(e);
            }
        } catch (Exception e) {
            throw new ApplicationException(e);
        }

    }
}
