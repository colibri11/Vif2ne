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

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;

import ru.vif2ne.backend.LocalUtils;


public class EventEntry implements Comparable<EventEntry> {

    public static final String TYPE_ADD = "add";
    public static final String FORMAT_DATE = "dd.MM.yyyy HH:mm:ss";
    public static final String TABLE_NAME = "event";
    private static final String LOG_TAG = "EventEntry";
    protected String eaType, eaMode;
    protected String titleArticle, author;
    protected long crc;
    protected int size;
    protected Date date;
    protected boolean deleted;
    protected long artNo;
    protected long artParent;
    protected int position;
    protected Integer fixed;
    protected ArrayList<EventEntry> childEventEntries;
    protected EventEntry parentEventEntry;
    protected boolean favorites;
    protected String article;

    protected int level;


    public EventEntry() {
        level = 0;
        size = 0;
        crc = 0;
        fixed = 0;
        artNo = 0;
        artParent = -1;
        titleArticle = "root";
        author = "vif2";
        article = "";
        deleted = false;
        parentEventEntry = null;
        date = new Date();
        childEventEntries = new ArrayList<>();
        favorites = false;
    }

    public static int cmp(Comparable a, Comparable b) {
        if (a == null && b == null) return 0;
        if (a == null) return -1;
        if (b == null) return 1;
        return a.compareTo(b);
    }

    public void pack(ContentValues cv) {
        cv.clear();
        cv.put("id", getArtNo());
        cv.put("parent", getArtParent());
        cv.put("title", getTitleArticle());
        cv.put("author", getAuthor());
        cv.put("time", getDate().getTime());
        cv.put("size", getSize());
        cv.put("crc", getCrc());
        cv.put("fixed", getFixed());
        cv.put("deleted", (isDeleted() ? 1 : 0));
        cv.put("favorite", (isFavorites() ? 1 : 0));
        cv.put("article", getArticle());
    }

    public void unpack(Cursor c) {
        setArtNo(c.getLong(c.getColumnIndex("id")));
        setArtParent(c.getLong(c.getColumnIndex("parent")));
        setAuthor(c.getString(c.getColumnIndex("author")));
        setTitleArticle(c.getString(c.getColumnIndex("title")));
        setDate(c.getLong(c.getColumnIndex("time")));
        setSize(Integer.toString(c.getInt(c.getColumnIndex("size"))));
        setCrc(c.getLong(c.getColumnIndex("crc")));
        setFixed(c.getInt(c.getColumnIndex("fixed")));
        setDeleted(c.getInt(c.getColumnIndex("deleted")) > 0);
        setFavorites(c.getInt(c.getColumnIndex("favorite")) > 0);
        setArticle(c.getString(c.getColumnIndex("article")));
    }

    public void save(SQLiteDatabase db) {
        if (isRoot()) return;
        ContentValues cv = new ContentValues();
        pack(cv);
        db.insertWithOnConflict(TABLE_NAME, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
    }

    private void setCrc(long crc) {
        this.crc = crc;
    }

    private void setDate(long time) {
        if (date == null)
            date = new Date();
        date.setTime(time);
    }

    public void addChild(EventEntry eventEntry) {
        childEventEntries.add(eventEntry);
    }

    public Boolean isRoot() {
        return artParent == -1;
    }

    public Boolean isFakeRoot() {
        return artNo == -1;
    }


    /**
     * ******************
     */


    public ArrayList<EventEntry> getChildEventEntries() {
        return childEventEntries;
    }

    public EventEntry getParentEventEntry() {
        return parentEventEntry;
    }

    public void setParentEventEntry(EventEntry parentEventEntry) {
        this.parentEventEntry = parentEventEntry;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public long getCrc() {
        return crc;
    }

    public void setCrc(String crc) {
        this.crc = Long.parseLong(crc);
    }

    public Date getDate() {
        return date;
    }

    public void setDate(String date) throws ParseException {
        this.date = LocalUtils.stringToDate(date, FORMAT_DATE);
    }

    public String getEaMode() {
        return eaMode;
    }

    public void setEaMode(String eaMode) {
        this.eaMode = eaMode;
    }

    public void setEaNo(String eaNo) {
        this.artNo = Long.parseLong(eaNo, 16);
    }

    public void setEaParent(String eaParent) {
        if (!TextUtils.isEmpty(eaParent) && !eaParent.equals("null"))
            this.artParent = Long.parseLong(eaParent, 16);
        else
            this.artParent = 0;
    }

    public long getArtParent() {
        return artParent;
    }

    public void setArtParent(long artParent) {
        this.artParent = artParent;
    }

    public String getEaType() {
        return eaType;
    }

    public void setEaType(String eaType) {
        this.eaType = eaType;
    }

    public int getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = Integer.parseInt(size);
    }

    public String getTitleArticle() {
        return titleArticle;
    }

    public void setTitleArticle(String titleArticle) {
        this.titleArticle = titleArticle;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EventEntry entry = (EventEntry) o;

        if (artNo != entry.artNo) return false;
        if (artParent != entry.artParent) return false;
        if (eaType != null ? !eaType.equals(entry.eaType) : entry.eaType != null) return false;
        return !(eaMode != null ? !eaMode.equals(entry.eaMode) : entry.eaMode != null);

    }

    @Override
    public int hashCode() {
        int result = eaType != null ? eaType.hashCode() : 0;
        result = 31 * result + (eaMode != null ? eaMode.hashCode() : 0);
        result = 31 * result + (int) (artNo ^ (artNo >>> 32));
        result = 31 * result + (int) (artParent ^ (artParent >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "EventEntry{" +
                ", eaType='" + eaType + '\'' +
                ", eaMode='" + eaMode + '\'' +
                ", titleArticle='" + titleArticle + '\'' +
                ", author='" + author + '\'' +
                ", crc=" + crc +
                ", size=" + size +
                ", date=" + date +
                ", deleted=" + deleted +
                ", artNo=" + artNo +
                ", artParent=" + artParent +
                ", position=" + position +
                ", fixed=" + fixed +

                '}';
    }

    @Override
    public int compareTo(EventEntry another) {
        Integer a = (this.fixed == 1 ? 1000 : this.fixed);
        Integer b = (another.getFixed() == 1 ? 1000 : this.fixed);
        if (a != 0 || b != 0) {
            int i = b.compareTo(a);
            if (i != 0) return i;
        }
        return EventEntry.cmp(another.date, this.date);
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public Integer getFixed() {
        return fixed;
    }

    public void setFixed(int fixed) {
        this.fixed = fixed;
    }

    public long getArtNo() {
        return artNo;
    }

    public void setArtNo(long artNo) {
        this.artNo = artNo;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public boolean isFavorites() {
        return favorites;
    }

    public void setFavorites(boolean favorites) {
        this.favorites = favorites;
    }

    public String getArticle() {
        return article;
    }

    public void setArticle(String article) {
        this.article = article;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }
}
