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

package ru.vif2ne.throwable;

import org.xmlpull.v1.XmlPullParserException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.HttpURLConnection;
import java.text.ParseException;

public class ApplicationException extends Exception {


    public static final int SC_EMPTY_RESULT = 1;
    public static final int SC_UNKNOWN = 2;
    public static final int SC_BAD_XML = 3;
    public static final int SC_BAD_DATE_FORMAT = 4;
    public static final int SC_BAD_PASSWD = 5;
    public static final int APP_ERROR_PARSE = 1001;
    private Integer code;

    private Throwable throwable;

    public ApplicationException(String msg, int code) {
        super(msg);
        this.code = code;
    }

    public ApplicationException() {
    }

    public ApplicationException(String message, Throwable cause) {
        super(message, cause);
    }

    public ApplicationException(Throwable throwable) {
        super(throwable);
        this.throwable = throwable;
        this.code = extractCode(throwable);
    }

    private static int extractCode(Throwable throwable) {
        int res;
        if (throwable instanceof FileNotFoundException) {
            res = HttpURLConnection.HTTP_NOT_FOUND;
        } else if (throwable instanceof NullPointerException) {
            res = HttpURLConnection.HTTP_INTERNAL_ERROR;
        } else if (throwable instanceof IOException) {
            res = HttpURLConnection.HTTP_CLIENT_TIMEOUT;
        } else if (throwable instanceof XmlPullParserException) {
            res = APP_ERROR_PARSE;
        } else if (throwable instanceof ParseException) {
            res = APP_ERROR_PARSE;
        } else if (throwable instanceof UndeclaredThrowableException) {
            if (throwable.getCause() != null)
                res = extractCode(throwable.getCause());
            else
                res = HttpURLConnection.HTTP_INTERNAL_ERROR;
        } else {
            res = HttpURLConnection.HTTP_INTERNAL_ERROR;
        }
        return res;
    }

    public Integer getCode() {
        return code;
    }

}
