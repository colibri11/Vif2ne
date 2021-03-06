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

package ru.vif2ne.backend.domains;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import ru.vif2ne.Session;
import ru.vif2ne.backend.LocalUtils;
import ru.vif2ne.backend.RemoteService;


public class EventEntries {

    public static final String TABLE_NAME = "events";
    private static final String LOG_TAG = "EventEntries";
    private static final long MAX_DB_RECORDS = 3000;

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

    public static long loadHeaderDb(Session session) {
        SQLiteDatabase readableDatabase = session.getDbHelper().getReadableDatabase();
        Cursor c = readableDatabase.query("events", null, "id = ?",
                new String[]{"1"}, null, null, null);
        long lastEvent = -1;

        if (c != null) {
            if (c.moveToFirst()) {
                lastEvent = c.getLong(c.getColumnIndex("last"));
                session.getRemoteService().setUserName(c.getString(c.getColumnIndex("username")));
                session.getRemoteService().setPasswd(
                        LocalUtils.dec(c.getString(c.getColumnIndex("code"))));
            }
            c.close();
        }
        return lastEvent;
    }

    public boolean isNew(EventEntry entry) {
        if (lastLoadedIds == null)
            return false;
        for (Long id : lastLoadedIds) {
            if (entry.getArtNo() == id) return true;
        }
        return false;
    }

    public HashMap<String, Integer> loadChildCountEventEntriesWithTree(EventEntry eventEntry) {
        int cnt = 0;
        int newEntry = 0;
        HashMap<String, Integer> res = new HashMap<>();
        for (EventEntry entry : eventEntry.getChildEventEntries()) {
            try {
                cnt++;
                if (isNew(entry))
                    newEntry++;
                HashMap<String, Integer> localRes = loadChildCountEventEntriesWithTree(entry);
                cnt = cnt + localRes.get("cnt");
                newEntry = newEntry + localRes.get("new");
            } catch (Exception e) {
                break;
            }
        }
        res.put("cnt", cnt);
        res.put("new", newEntry);
        return res;
    }

    public void clearEntries() {
        if (rootEntry == null)
            rootEntry = new EventEntry();
        rootEntry.getChildEventEntries().clear();
        eventEntries.clear();
        eventEntries.add(rootEntry);
        if (lastLoadedIds == null)
            lastLoadedIds = new ArrayList<>();
        else
            lastLoadedIds.clear();
    }


    public void clearEntriesDB() {
        lastEvent = -1;
        refreshDate = new Date();
        clearEntries();
        clearDb();
    }

    public long minEventsId(Session session) {
        SQLiteDatabase readableDatabase = session.getDbHelper().getReadableDatabase();
        long i = 1;
        Cursor c = readableDatabase.rawQuery("select min(id) from events", null);
        if (c.moveToFirst()) {
            i += c.getLong(0);
        }
        c.close();
        return i;
    }

    public long packDb() {
        Long minId = minEventsId(session);
        SQLiteDatabase db = session.getDbHelper().getWritableDatabase();
        String lastEventDB = Long.toString(lastEvent - MAX_DB_RECORDS);
        Log.d(LOG_TAG,"minId:"+minId);
        int del = db.delete(TABLE_NAME, "id > ?", new String[]{Long.toString(minId)});
        Log.d(LOG_TAG, "del entries:" + del);
        del = db.delete(EventEntry.TABLE_NAME, "(id < ? and favorite = 0) or (deleted = 1) or (title=\"root\" and author = \"vif2\")", new String[]{lastEventDB});
        Log.d(LOG_TAG, "del entry:" + del);
        return del;
    }

    private void clearDb() {
        SQLiteDatabase db = session.getDbHelper().getWritableDatabase();
        db.delete(TABLE_NAME, null, null);
        db.delete(EventEntry.TABLE_NAME, null, null);
    }

    public long load(String username) {
        Log.d(LOG_TAG, "sqlite start load:" + new Date().toString());
        int i = 0;
        try {
            SQLiteDatabase readableDatabase = session.getDbHelper().getReadableDatabase();
            if (TextUtils.isEmpty(username)) username = RemoteService.EMPTY_USER;
            Cursor c = readableDatabase.query(TABLE_NAME, null, "id = ? and username = ?", new String[]{"1", username}, null, null, null);
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
                clearEntriesDB();
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


    public void save() {
        Log.d(LOG_TAG, "sqlite start save:" + new Date().toString());
        RemoteService rs = session.getRemoteService();
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
            cv.put("username", rs.getUserName());
            cv.put("code", LocalUtils.enc(rs.getPasswd()));
            db.insertWithOnConflict(EventEntries.TABLE_NAME, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
            cv.clear();
            cv.put("last", lastEvent);
            cv.put("cnt", getLastLoadedIds().size());
            cv.put("time", refreshDate.getTime());
            cv.put("username", rs.getUserName());
            cv.put("code", LocalUtils.enc(rs.getPasswd()));
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
        Log.d(LOG_TAG, "makeTree eventEntries size:" + eventEntries.size());
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
        Log.d(LOG_TAG, "makeTree End");
        if (eventEntries.size() > 0)
            sortTree(eventEntries.get(0));
        setLastLoadedIds(idParse);
        if (idParse != null)
            save();
        Log.d(LOG_TAG, "makeTree Full End");
    }

    public void sortTree(EventEntry eventEntry) {

        if (eventEntry.isRoot()) {
            Collections.sort(eventEntry.getChildEventEntries());
            Log.d(LOG_TAG, "fake root:" + eventEntry.getChildEventEntries().size());
        } else {
            Collections.sort(eventEntry.getChildEventEntries(), new Comparator<EventEntry>() {
                @Override
                public int compare(EventEntry lhs, EventEntry rhs) {
                    return EventEntry.cmp(lhs.date, rhs.date);
                }
            });
        }

        for (EventEntry entry : eventEntry.getChildEventEntries()) {
            sortTree(entry);
        }
    }

    public void sortTreeOld(ArrayList<EventEntry> entries) {
        for (EventEntry entry : entries) {
            Collections.sort(entry.getChildEventEntries());
            sortTreeOld(entry.getChildEventEntries());
        }
    }

    public EventEntry get(int position) {
        return eventEntries.get(position);
    }

    /*
    * Loaders  begin
    * */

    public EventEntry getByArtNo(long id) {
        for (EventEntry entry : eventEntries) {
            if (entry.artNo == id) {
                return entry;
            }
        }
        return null;
    }

    public void loadChildEventEntries(ArrayList<EventEntry> events, EventEntry eventEntry) {
        for (EventEntry entry : eventEntry.getChildEventEntries()) {
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

    public void loadMatchEventEntries(String matchString, ArrayList<EventEntry> events) {
        events.clear();
        for (Iterator<EventEntry> it = eventEntries.iterator(); it.hasNext(); ) {
            EventEntry entry = it.next();
            if (entry.titleArticle != null &&
                    entry.titleArticle.toUpperCase().contains(matchString.toUpperCase()))
                events.add(entry);
        }
        Collections.sort(events);
    }

    public void loadMatchUserEventEntries(String matchString, ArrayList<EventEntry> events) {
        events.clear();
        for (Iterator<EventEntry> it = eventEntries.iterator(); it.hasNext(); ) {
            EventEntry entry = it.next();
            if (entry.getAuthor() != null &&
                    entry.getAuthor().toUpperCase().contains(matchString.toUpperCase()))
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

    public void setLastEvent(long lastEvent) {
        this.lastEvent = lastEvent;
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
