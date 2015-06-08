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
 * Created by serg 06.06.15 23:20
 */

package ru.vif2ne.backend.tasks;

import ru.vif2ne.Session;
import ru.vif2ne.throwable.ApplicationException;

/**
 * Created by serg on 06.06.15.
 */
public abstract class LoadSettingsTask extends AbstractTask {


    protected LoadSettingsTask(Session session) {
        super(session);
    }

    @Override
    protected String getClassName() {
        return LoadSettingsTask.class.getName();
    }

    @Override
    protected Object remoteCall() throws ApplicationException {
        try {
            return remoteService.loadSettings();
        } catch (Exception e) {
            throw new ApplicationException(e);
        }
    }
}

