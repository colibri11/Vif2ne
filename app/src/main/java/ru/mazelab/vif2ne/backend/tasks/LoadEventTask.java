package ru.mazelab.vif2ne.backend.tasks;

import android.util.Log;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.text.ParseException;

import ru.mazelab.vif2ne.R;
import ru.mazelab.vif2ne.Session;
import ru.mazelab.vif2ne.throwable.ApplicationException;

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
 * LoadEventTask.java
 *
 *
 */

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
            if (eventId == -1) {
                eventId = session.getEventEntries().load();
                Log.d(LOG_TAG, "intent start");
                session.intentNeedRefresh("end: session.getEventEntries().load()");
            }
            return remoteService.loadEventEntries(session.getEventEntries(), eventId);
        } catch (IOException e) {
            e.printStackTrace();
            throw new ApplicationException(session.getApplication().getResources().getString(R.string.exception_network));
        } catch (XmlPullParserException e) {
            e.printStackTrace();
            throw new ApplicationException(session.getApplication().getResources().getString(R.string.xml_error));
        } catch (ParseException e) {
            e.printStackTrace();
            throw new ApplicationException(session.getApplication().getResources().getString(R.string.date_format_error));
        }

    }
}
