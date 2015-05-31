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

package ru.vif2ne.backend;

import android.util.Log;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.ArrayList;

import ru.vif2ne.backend.domains.EventEntries;
import ru.vif2ne.backend.domains.EventEntry;

import static ru.vif2ne.backend.NetUtils.readTag;
import static ru.vif2ne.backend.NetUtils.skipTag;

public class Vif2NeXmlParser {
    // namespace
    private static final String ns = null;
    private static final String LOG_TAG = "Vif2NeXmlParser";

    protected ArrayList<Long> idParse;


    public boolean parse(EventEntries entries, long lastEventId, InputStream in) throws XmlPullParserException, IOException, ParseException {
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();
            readEvents(entries, lastEventId, parser);
            return true;
        } finally {
            in.close();
        }
    }


    private void readEvents(EventEntries entries,
                            long lastEventId,
                            XmlPullParser parser) throws XmlPullParserException, IOException, ParseException {

        if (lastEventId == -1) {
            entries.reSetEventEntries();
        }

        this.idParse = new ArrayList<>();

        parser.require(XmlPullParser.START_TAG, ns, "root");
        int i = 0;
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            // Starts by looking for the event tag
            if (name.equals("event")) {
                i++;
                EventEntry eventEntry = readEntry(parser);
                if (!idParse.contains(eventEntry.getArtNo())) {
                    idParse.add(eventEntry.getArtNo());
                }
                entries.addFromRemote(eventEntry);
            } else if (name.equals("lastEvent")) {
                entries.setLastEvent(readTag(parser, ns, "lastEvent"));
            } else {
                Log.d(LOG_TAG, name);
                skipTag(parser);
            }
        }
        entries.makeTree(idParse);
    }

    private EventEntry readEntry(XmlPullParser parser) throws IOException, XmlPullParserException, ParseException {
        parser.require(XmlPullParser.START_TAG, ns, "event");
        EventEntry eventEntry = new EventEntry();

        eventEntry.setEaNo(parser.getAttributeValue(null, "no"));
        eventEntry.setEaParent(parser.getAttributeValue(null, "parent"));
        eventEntry.setEaType(parser.getAttributeValue(null, "type"));
        if (eventEntry.getEaType().equals("fix")) {
            eventEntry.setEaMode(parser.getAttributeValue(null, "mode"));
        }

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            switch (name) {
                case "title":
                    eventEntry.setTitleArticle(readTag(parser, ns, "title"));
                    break;
                case "author":
                    eventEntry.setAuthor(readTag(parser, ns, "author"));
                    break;
                case "date":
                    eventEntry.setDate(readTag(parser, ns, "date"));
                    break;
                case "size":
                    eventEntry.setSize(readTag(parser, ns, "size"));
                    break;
                case "crc":
                    eventEntry.setCrc(readTag(parser, ns, "crc"));
                    break;
                default:
                    skipTag(parser);
                    break;
            }
        }
        return eventEntry;
    }


}
