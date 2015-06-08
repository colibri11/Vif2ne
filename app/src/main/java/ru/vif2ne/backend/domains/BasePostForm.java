package ru.vif2ne.backend.domains;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import ru.vif2ne.backend.LocalUtils;

/**
 * Created by serg on 08.06.15.
 */
public class BasePostForm extends HashMap<String, String> {



    public String getQuery() throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, String> entry : entrySet()) {
            if (first)
                first = false;
            else
                result.append("&");
            result.append(LocalUtils.encode(entry.getKey()));
            result.append("=");
            result.append(LocalUtils.encode(entry.getValue()));
        }
        return result.toString();
    }

}
