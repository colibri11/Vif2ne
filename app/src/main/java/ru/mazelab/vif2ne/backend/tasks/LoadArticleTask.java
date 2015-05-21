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

import android.util.Log;

import java.io.IOException;

import ru.mazelab.vif2ne.R;
import ru.mazelab.vif2ne.Session;
import ru.mazelab.vif2ne.backend.domains.EventEntry;
import ru.mazelab.vif2ne.throwable.ApplicationException;

public abstract class LoadArticleTask extends AbstractTask {

    private static final String LOG_TAG = "LoadArticleTask";
    protected EventEntry eventEntry;

    protected LoadArticleTask(Session session, EventEntry eventEntry) {
        super(session);
        this.eventEntry = eventEntry;
    }

    @Override
    protected String getClassName() {
        return LoadArticleTask.class.getName();
    }

    @Override
    protected Object remoteCall() throws ApplicationException {
        try {
            String result = "";
            result = remoteService.loadArticle(eventEntry.getArtNo());
            eventEntry.setArticle(result);
            Log.d(LOG_TAG, "no:" + eventEntry.getArtNo());
            eventEntry.save(session.getDbHelper().getWritableDatabase());
            return result;
        } catch (IOException e) {
            e.printStackTrace();
            throw new ApplicationException(session.getApplication().getResources().getString(R.string.exception_load_article));
        }
    }
}
