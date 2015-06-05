package ru.vif2ne.backend;

import android.util.Log;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.ArrayList;

import ru.vif2ne.backend.domains.Smoking;
import ru.vif2ne.backend.domains.SmokingMessage;

import static ru.vif2ne.backend.NetUtils.readTag;
import static ru.vif2ne.backend.NetUtils.skipTag;

/**
 * Created by serg on 05.06.15.
 */
public class Vif2NeXmlSmokingParser {
    // namespace
    private static final String ns = null;
    private static final String LOG_TAG = "Vid2NeXmlSmoking";

    private ArrayList<SmokingMessage> smokingMessages;

    public Vif2NeXmlSmokingParser() {
        smokingMessages = new ArrayList<>();
    }

    public boolean parse(Smoking smoking, InputStream in) throws XmlPullParserException, IOException, ParseException {
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();
            readXml(smoking, parser);
            smoking.mergeMessages(smokingMessages);
            return true;
        } finally {
            in.close();
        }
    }

    private void readXml(Smoking smoking, XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "root");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("ID")) {
                smoking.setLastId(Long.parseLong(readTag(parser, ns, "ID")));
            } else if (name.equals("titleText")) {
                smoking.setTitle(readTag(parser, ns, "titleText"));
            } else if (name.equals("titleWho")) {
                smoking.setTitleWho(readTag(parser, ns, "titleWho"));
            } else if (name.equals("traffic")) {
                smoking.setTitle(readTag(parser, ns, "traffic"));
            } else if (name.equals("person")) {
                smoking.addActive(readTag(parser, ns, "person"));
            } else if (name.equals("message")) {
                SmokingMessage smokingMessage = new SmokingMessage(readTag(parser, ns, "message"));
                smokingMessages.add(smokingMessage);
            } else {
                Log.d(LOG_TAG, name);
                skipTag(parser);
            }
        }
    }



}
