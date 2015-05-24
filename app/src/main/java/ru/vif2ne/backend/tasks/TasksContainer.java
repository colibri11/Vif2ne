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

import android.support.annotation.NonNull;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import ru.vif2ne.Session;

public class TasksContainer {
    protected Session session;
    protected boolean lock;
    protected HashMap<String, AbstractTask> backgroundTasks;

    public TasksContainer(Session session) {
        this.session = session;
        this.backgroundTasks = new HashMap<>();
        this.lock = false;
    }

    public AbstractTask get(@NonNull String taskName) {
        return backgroundTasks.get(taskName);
    }

    public boolean isLock() {
        return lock;
    }

    public void setLock(boolean lock) {
        this.lock = lock;
    }

    public void add(@NonNull String taskName, @NonNull AbstractTask task) {
        AbstractTask asyncTask = backgroundTasks.get(taskName);
        if (asyncTask != null)
            backgroundTasks.remove(taskName);
        backgroundTasks.put(taskName, task);
    }

    public void cancel(@NonNull String taskName) {
        AbstractTask asyncTask = backgroundTasks.get(taskName);
        if (asyncTask != null)
            asyncTask.cancel(true);
    }

    public void remove(@NonNull String taskName) {
        cancel(taskName);
        backgroundTasks.remove(taskName);
    }

    public HashMap<String, AbstractTask> getBackgroundTasks() {
        return backgroundTasks;
    }

    public void clear() {
        Iterator it = backgroundTasks.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, AbstractTask> item = (Map.Entry) it.next();
            item.getValue().cancel(true);
//          Задача удаляется сама, в методе cancel
//            it.remove();
        }
    }

    public int size() {
        return backgroundTasks.size();
    }
}
