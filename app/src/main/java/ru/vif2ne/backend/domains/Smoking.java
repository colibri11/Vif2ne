package ru.vif2ne.backend.domains;

import java.util.ArrayList;

/**
 * Created by serg on 05.06.15.
 */
public class Smoking {
    private ArrayList<SmokingMessage> smokingMessages;
    private long lastId;
    private ArrayList<String> activeUsers;
    private String titleWho;
    private String title;
    private String traffic;

    public Smoking() {
        smokingMessages = new ArrayList<>();
        activeUsers = new ArrayList<>();
        lastId = 0;
    }

    public int getMessagePositionByAnchor(String anchor) {
        int i = 0;
        for (SmokingMessage message : smokingMessages) {
            if (anchor.equals(message.getAnchor()))
                return i;
            i++;
        }
        return 0;
    }

    public int sizeMessages() {
        return smokingMessages.size();
    }

    public void addMessage(SmokingMessage smokingMessage) {
        smokingMessages.add(smokingMessage);
    }

    public void mergeMessages(ArrayList<SmokingMessage> messages) {
        smokingMessages.addAll(0, messages);
    }

    public int sizeActive() {
        return activeUsers.size();
    }

    public void addActive(String user) {
        activeUsers.add(user);
    }

    public String getTraffic() {
        return traffic;
    }

    public void setTraffic(String traffic) {
        this.traffic = traffic;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getLastId() {
        return lastId;
    }

    public void setLastId(long lastId) {
        this.lastId = lastId;
    }

    @Override
    public String toString() {
        return "Smoking{" +
                "smokingMessages=" + smokingMessages +
                ", lastId=" + lastId +
                ", activeUsers=" + activeUsers +
                ", title='" + title + '\'' +
                ", traffic='" + traffic + '\'' +
                '}';
    }

    public String getTitleWho() {
        return titleWho;
    }

    public void setTitleWho(String titleWho) {
        this.titleWho = titleWho;
    }

    public SmokingMessage getMessage(int position) {
        return smokingMessages.get(position);
    }
}
