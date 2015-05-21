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

package ru.mazelab.vif2ne.throwable;

import java.security.GeneralSecurityException;

public class ApplicationException extends GeneralSecurityException {


    public static final Integer SC_EMPTY_RESULT = 1;
    public static final Integer SC_UNKNOWN = 2;
    private Integer code;

    public ApplicationException(String msg, int code) {
        super(msg);
        this.code = code;
    }

    public ApplicationException(String msg) {
        super(msg);
        this.code = SC_UNKNOWN;
    }

    public ApplicationException() {
    }

    public ApplicationException(String message, Throwable cause) {
        super(message, cause);
    }

    public ApplicationException(Throwable cause) {
        super(cause);
    }

    public Integer getCode() {
        return code;
    }

}
