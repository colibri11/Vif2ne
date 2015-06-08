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

import java.util.ArrayList;

import ru.vif2ne.Session;
import ru.vif2ne.backend.domains.EventEntry;
import ru.vif2ne.throwable.ApplicationException;

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
                    publishProgress(i);
                    result = remoteService.loadArticle(eventEntry.getArtNo());
                    eventEntry.setArticle(result);
                    eventEntry.save(session.getDbHelper().getWritableDatabase());
                    Log.d(LOG_TAG, "no:" + eventEntry.getArtNo());
                    i++;
                }
            }
            return i;
        } catch (Exception e) {
            throw new ApplicationException(e);
        } finally {
            tasksContainer.setLock(false);
        }
    }

}
