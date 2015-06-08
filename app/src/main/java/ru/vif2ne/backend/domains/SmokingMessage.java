package ru.vif2ne.backend.domains;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.ArrayList;

/**
 * Created by serg on 05.06.15.
 */
public class SmokingMessage {

    private static final String LOG_TAG = "SmokingMessage";
    private String message;
    private String author;
    private String anchor;
    private ArrayList<String> recipient;
    private String time;


    /*
     0 - author string
     1 - author + time
     2 - author
     3 && # - recipient
     */
    public SmokingMessage(String message) {
        recipient = new ArrayList<>();
        Document doc = Jsoup.parse(message.replace("&nbsp;&gt;&nbsp;&nbsp;&nbsp;&nbsp;", ""));
        int i = 0;
        String href;
        for (Element element : doc.select("a")) {
            href = element.attr("href");
            switch (i) {
                case 0: {
                    anchor = element.attr("name");
                    time = anchor.split(", ", 2)[1];
                    element.remove();
                    break;
                }
                case 1: {
                    element.remove();
                    break;
                }
                case 2: {
                    author = element.text();
                    element.remove();
                    break;
                }
                default: {
                    if (href.startsWith("#")) {
                        recipient.add(href.substring(1));
                        element.attr("href", "vif2ne://" +href.substring(1));
                    }
                }
            }
        //    if (i < 3) element.remove();
            i++;
        }
        this.message = doc.toString();
    }
    public String getMessage() {
        return message;
    }


    public String getAuthor() {
        return author;
    }

    public ArrayList<String> getRecipient() {
        return recipient;
    }

    public String getTime() {
        return time;
    }

    public String getAnchor() {
        return anchor;
    }

    @Override
    public String toString() {
        return "SmokingMessage{" +
                "message='" + message + '\'' +
                ", author='" + author + '\'' +
                ", anchor='" + anchor + '\'' +
                ", recipient='" + recipient + '\'' +
                '}';
    }
}
