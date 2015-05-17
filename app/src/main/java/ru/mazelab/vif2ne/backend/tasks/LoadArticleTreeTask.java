package ru.mazelab.vif2ne.backend.tasks;

import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;

import ru.mazelab.vif2ne.R;
import ru.mazelab.vif2ne.Session;
import ru.mazelab.vif2ne.backend.domains.EventEntry;
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
 * Created by Colibri  15.05.15 23:52
 * LoadArticleTreeTask.java
 *
 *
 */

public abstract class LoadArticleTreeTask extends AbstractTask {

    private static final String LOG_TAG = "LoadArticleTask";
    protected ArrayList<EventEntry> eventEntries;

    protected LoadArticleTreeTask(Session session, ArrayList<EventEntry> eventEntries) {
        super(session);
        this.eventEntries = eventEntries;
    }

    @Override
    protected String getClassName() {
        return LoadArticleTask.class.getName();
    }

    @Override
    protected Object remoteCall() throws ApplicationException {
        tasksContainer.setLock(true);
        try {
            Integer i = 0;
            for (EventEntry eventEntry : eventEntries) {
                if (TextUtils.isEmpty(eventEntry.getArticle()) && eventEntry.getSize() > 0) {
                    String result = "";
                    Thread.sleep(10);
                    result = remoteService.loadArticle(eventEntry.getArtNo());
                    eventEntry.setArticle(result);
                    eventEntry.save(session.getDbHelper().getWritableDatabase());
                    Log.d(LOG_TAG, "no:" + eventEntry.getArtNo());
                    i++;
                }
            }
            return i;
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            throw new ApplicationException(session.getApplication().getResources().getString(R.string.exception_load_article));
        } finally {
            tasksContainer.setLock(false);
        }
    }

}
