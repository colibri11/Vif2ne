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

package ru.mazelab.vif2ne.backend.domains;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;

import ru.mazelab.vif2ne.Session;


public class EventEntries {

    public static final String TABLE_NAME = "events";
    private static final String LOG_TAG = "EventEntries";
    private static final long MAX_DB_RECORDS = 5000;

    protected ArrayList<Long> lastLoadedIds;

    protected ArrayList<EventEntry> eventEntries;
    protected long lastEvent;
    protected Session session;
    protected boolean filterConcurrentModification;
    protected Date refreshDate;

    protected EventEntry rootEntry;

    public EventEntries(Session session) {
        lastEvent = -1;
        this.session = session;
        eventEntries = new ArrayList<>();
        rootEntry = new EventEntry();
        eventEntries.add(rootEntry);
        lastLoadedIds = new ArrayList<>();
        filterConcurrentModification = false;
        refreshDate = new Date();
    }

    public long packDb() {
        SQLiteDatabase db = session.getDbHelper().getWritableDatabase();
        String lastEventDB = Long.toString(lastEvent - MAX_DB_RECORDS);
        return db.delete("event", "(id < ? and favorite = 0) or (deleted = 1) or (title=\"root\" and author = \"vif2\")", new String[]{lastEventDB});
    }

    public long load() {
        Log.d(LOG_TAG, "sqlite start load:" + new Date().toString());
        int i = 0;
        try {
            SQLiteDatabase readableDatabase = session.getDbHelper().getReadableDatabase();
            Cursor c = readableDatabase.query("events", null, "id = ?", new String[]{"1"}, null, null, null);
            lastEvent = -1;
            refreshDate = new Date();
            if (c != null) {
                if (c.moveToFirst()) {
                    lastEvent = c.getLong(c.getColumnIndex("last"));
                    setRefreshDate(c.getLong(c.getColumnIndex("time")));
                }
                c.close();
            }
            if (lastEvent == -1) {
                reSetEventEntries();
                return -1;
            }
            c = readableDatabase.query("event", null, "deleted = 0", null, null, null, null);
            if (c.moveToFirst()) {
                do {
                    EventEntry entry = new EventEntry();
                    entry.unpack(c);
                    eventEntries.add(entry);
                    i++;
                } while (c.moveToNext());
            }
            makeTree(null);
            return lastEvent;
        } finally {
            Log.d(LOG_TAG, "sqlite end load:" + new Date().toString() + " cnt:" + i + " size:" + eventEntries.size());
        }
    }

    public void reSetEventEntries() {
        eventEntries.clear();
        eventEntries.add(rootEntry);
        SQLiteDatabase db = session.getDbHelper().getWritableDatabase();
        db.delete(EventEntry.TABLE_NAME, null, null);
        save();
    }

    public void save() {
        Log.d(LOG_TAG, "sqlite start save:" + new Date().toString());
        if (getLastLoadedIds() == null)
            return;
        int i = 0;
        try {
            SQLiteDatabase db = session.getDbHelper().getWritableDatabase();
            ContentValues cv = new ContentValues();
            cv.put("id", 1);
            cv.put("last", lastEvent);
            cv.put("cnt", getLastLoadedIds().size());
            cv.put("time", refreshDate.getTime());
            db.insertWithOnConflict(EventEntries.TABLE_NAME, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
            cv.clear();
            cv.put("last", lastEvent);
            cv.put("cnt", getLastLoadedIds().size());
            cv.put("time", refreshDate.getTime());
            db.insert(EventEntries.TABLE_NAME, null, cv);
            db.beginTransaction();
            try {
                for (Iterator<EventEntry> it = eventEntries.iterator(); it.hasNext(); ) {
                    EventEntry entry = it.next();
                    if (lastEvent == -1 || lastLoadedIds.contains(entry.getArtNo())) {
                        if (!entry.isRoot()) {
                            cv.clear();
                            entry.pack(cv);
                            db.insertWithOnConflict(EventEntry.TABLE_NAME, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
                            i++;
                        }
                    }
                }
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
            long cntDelete = packDb();
            Log.d(LOG_TAG, "delete records:" + cntDelete);
        } finally {
            Log.d(LOG_TAG, "sqlite end save:" + new Date().toString() + " size:" + i);
        }
    }

    public void addFromRemote(EventEntry eventEntry) {
        if (eventEntry.getArtNo() == 0) return;
        if (!TextUtils.isEmpty(eventEntry.eaType) && eventEntry.eaType.equals("fix")) {
            EventEntry entry = getByArtNo(eventEntry.getArtNo());
            if (entry != null) {
                entry.setFixed(Integer.parseInt(eventEntry.getEaMode()));
                return;
            }
        }
        if (!TextUtils.isEmpty(eventEntry.eaType) && eventEntry.eaType.equals("parent")) {
            EventEntry entry = getByArtNo(eventEntry.getArtNo());
            if (entry != null) {
                EventEntry parent = getByArtNo(entry.getArtParent());
                if (parent != null && parent.getChildEventEntries() != null)
                    parent.getChildEventEntries().remove(entry);
                entry.setArtParent(eventEntry.getArtParent());
                return;
            }
        }
        if (!TextUtils.isEmpty(eventEntry.eaType) && eventEntry.eaType.equals("del")) {
            EventEntry entry = getByArtNo(eventEntry.getArtNo());
            if (entry != null) {
                entry.setDeleted(true);
                Log.d(LOG_TAG, "del:" + entry);
                return;
            }
        }
        if (eventEntry.getTitleArticle().equals("root")) {
            Log.d(LOG_TAG, "article not found:" + eventEntry);
        } else if (eventEntry.getEaType() != null && eventEntry.getEaType().equals("add"))
            eventEntries.add(eventEntry);
    }

    public void makeTree(ArrayList<Long> idParse) {
        Log.d(LOG_TAG, "eventEntries size:" + eventEntries.size());
        for (Iterator<EventEntry> it = eventEntries.iterator(); it.hasNext(); ) {
            EventEntry entry = it.next();
            if (entry.isDeleted()) {
                Log.d(LOG_TAG, "deleted: " + entry.toString());
            }
            for (Iterator<EventEntry> itSub = eventEntries.iterator(); itSub.hasNext(); ) {
                EventEntry subEntry = itSub.next();
                if (entry.getArtParent() == subEntry.getArtNo()) {
                    if (!subEntry.childEventEntries.contains(entry))
                        subEntry.addChild(entry);
                    entry.setParentEventEntry(subEntry);
                    break;
                }
            }
        }
        sortTree(eventEntries);
        setLastLoadedIds(idParse);
        save();
    }

    public void sortTree(ArrayList<EventEntry> entries) {
        for (Iterator<EventEntry> it = entries.iterator(); it.hasNext(); ) {
            EventEntry entry = it.next();
            Collections.sort(entry.getChildEventEntries());
            sortTree(entry.getChildEventEntries());
        }
    }


    public EventEntry get(int position) {
        return eventEntries.get(position);
    }

    public EventEntry getByArtNo(long id) {
        for (Iterator<EventEntry> it = eventEntries.iterator(); it.hasNext(); ) {
            EventEntry entry = it.next();
            if (entry.artNo == id) {
                return entry;
            }
        }
        return null;
    }

    /*
    * Loaders  begin
    * */

    public void loadChildEventEntries(ArrayList<EventEntry> events, EventEntry eventEntry) {
        for (Iterator<EventEntry> it = eventEntry.getChildEventEntries().iterator(); it.hasNext(); ) {
            EventEntry entry = it.next();
            events.add(entry);
        }
//        Collections.sort(events);
    }

    public void loadChildEventEntriesWithTree(ArrayList<EventEntry> events, EventEntry eventEntry, int level) {
        if (level > 100) return;
        level++;
        for (Iterator<EventEntry> it = eventEntry.getChildEventEntries().iterator(); it.hasNext(); ) {
            try {
                EventEntry entry = it.next();
                entry.setLevel(level);
                if (!(entry.titleArticle.indexOf("root") == 0))
                    events.add(entry);
                loadChildEventEntriesWithTree(events, entry, level);
            } catch (Exception e) {
                Log.d(LOG_TAG, "level error: " + level + " " + eventEntry.getChildEventEntries().toString());
                break;

            }
        }
    }

    public static int loadChildCountEventEntriesWithTree(EventEntry eventEntry, int cnt) {
        if (cnt > 1000) return cnt;
        for (EventEntry entry : eventEntry.getChildEventEntries()) {
            try {
                cnt++;
                cnt = cnt + loadChildCountEventEntriesWithTree(entry, 0);
            } catch (Exception e) {
                break;
            }
        }
        return cnt;
    }
    public void loadMatchEventEntries(String matchString, ArrayList<EventEntry> events) {
        events.clear();
        for (Iterator<EventEntry> it = eventEntries.iterator(); it.hasNext(); ) {
            EventEntry entry = it.next();
            if (entry.titleArticle != null && entry.titleArticle.toUpperCase().contains(matchString.toUpperCase()))
                events.add(entry);
        }
        Collections.sort(events);
    }


    public void loadFavoritesEventEntries(ArrayList<EventEntry> events) {
        events.clear();
        for (Iterator<EventEntry> it = eventEntries.iterator(); it.hasNext(); ) {
            EventEntry entry = it.next();
            if (entry.isFavorites())
                events.add(entry);
        }
        Collections.sort(events);

    }

    /*
    * Loaders end
    * */

    public long getLastEvent() {
        return lastEvent;
    }


    public void setLastEvent(String lastEvent) {
        this.lastEvent = Long.parseLong(lastEvent);
    }


    @Override
    public String toString() {
        return "EventEntries{" +
                "eventEntries=" + eventEntries.size() +
                ", lastEvent=" + lastEvent +
                '}';
    }

    public int size() {
        return eventEntries.size();
    }

    public Date getRefreshDate() {
        return refreshDate;
    }

    public void setRefreshDate(long time) {
        if (this.refreshDate == null)
            this.refreshDate = new Date();
        this.refreshDate.setTime(time);
    }

    public ArrayList<Long> getLastLoadedIds() {
        return lastLoadedIds;
    }

    public void setLastLoadedIds(ArrayList<Long> lastLoadedIds) {
        this.lastLoadedIds = lastLoadedIds;
    }
}
