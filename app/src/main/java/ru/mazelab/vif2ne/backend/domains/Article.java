package ru.mazelab.vif2ne.backend.domains;

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
