package ru.mazelab.vif2ne.backend.tasks;

import java.io.IOException;

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
 * LoginTask.java
 *
 *
 */

public abstract class LoginTask extends AbstractTask {

    private String user;
    private String passwd;

    protected LoginTask(Session session, String user, String passwd) {
        super(session);
        this.user = user;
        this.passwd = passwd;
    }

    @Override
    protected String getClassName() {
        return AbstractTask.class.getName();
    }

    @Override
    protected Object remoteCall() throws ApplicationException {
        Boolean res = false;
        try {
            remoteService.login(user, passwd);
            res = true;
        } catch (IOException e) {
            e.printStackTrace();
            return res;
        }
        return res;
    }
}
