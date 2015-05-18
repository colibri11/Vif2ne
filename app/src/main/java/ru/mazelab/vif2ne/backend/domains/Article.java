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
        //       put("id",Long.toString(id));
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

    public String getQuery() throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, String> entry : entrySet()) {
            if (first)
                first = false;
            else
                result.append("&");
            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
        }
        return result.toString();
    }

}
