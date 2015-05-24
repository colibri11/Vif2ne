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

package ru.vif2ne.backend.domains;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by serg on 17.05.15.
 */
public class Article extends HashMap<String, String> {
    private long id;


    public Article(long id, String subject, String body, boolean toplevel) {
        this.id = id;
        put("subject", subject);
        if (toplevel)
            put("toplevel", "1");

        put("body", body);
        put("hello", "");
        put("bye", "");

    }

    public long getId() {
        return id;
    }


    public String encode(String value) {
        try {
            return URLEncoder.encode(
                    new String(value
                            .replace("\"", "&quot;")
                            .replace("\n", "\r\n")
//                            .replace(">", "&gt;")
//                            .replace("<", "&lt;")
                            .getBytes(), "UTF-8"), "windows-1251").replace("+", "%20");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return "";
        }
    }

    public String getQuery() throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, String> entry : entrySet()) {
            if (first)
                first = false;
            else
                result.append("&");
            result.append(encode(entry.getKey()));
            result.append("=");
            result.append(encode(entry.getValue()));
        }
        return result.toString();
    }

}
