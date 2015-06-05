package ru.vif2ne.backend.domains;

/**
 * Created by serg on 05.06.15.
 */
public class SmokingMessage {

    private String message;

    public SmokingMessage(String message) {
        this.message = message;
    }

    public SmokingMessage() {
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "SmokingMessage{" +
                "message='" + message + '\'' +
                '}';
    }
}
