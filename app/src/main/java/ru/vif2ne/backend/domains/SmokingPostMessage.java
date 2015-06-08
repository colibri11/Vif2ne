package ru.vif2ne.backend.domains;

/**
 * Created by serg on 08.06.15.
 */
public class SmokingPostMessage extends BasePostForm {

    public SmokingPostMessage(String message, boolean _private) {
        put("InBuf", message);
        put("private", _private ? "on" : "off");
        put("rightSend", "Сказать");
    }

}
