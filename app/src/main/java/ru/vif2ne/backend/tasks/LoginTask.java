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

import ru.vif2ne.Session;
import ru.vif2ne.throwable.ApplicationException;

public abstract class LoginTask extends AbstractTask {

    private String user;
    private String passwd;

    protected LoginTask(Session session, String user, String passwd) {
        super(session);
        this.user = user;
        this.passwd = passwd;
    }


    public String getUser() {
        return user;
    }

    @Override
    protected String getClassName() {
        return AbstractTask.class.getName();
    }

    @Override
    protected Object remoteCall() throws ApplicationException {
        try {
            remoteService.login(user, passwd);
            return true;
        } catch (Exception e) {
            throw new ApplicationException(e);
        }

    }
}
