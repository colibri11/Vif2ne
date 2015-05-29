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

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBHelper extends SQLiteOpenHelper {

    private static final int DB_VERSION = 5;
    private static final String DB_NAME = "vif2";
    private static final String LOG_TAG = "DBHelper";

    public DBHelper(Context context) {

        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE event (" +
                "id INTEGER PRIMARY KEY  NOT NULL UNIQUE , " +
                "parent INTEGER NOT NULL , " +
                "title VARCHAR NOT NULL , " +
                "author VARCHAR NOT NULL , " +
                "time INTEGER NOT NULL , " +
                "size INTEGER NOT NULL , " +
                "crc INTEGER NOT NULL , " +
                "fixed INTEGER, " +
                "deleted INTEGER, " +
                "favorite INTEGER, " +
                "article TEXT)");
        db.execSQL("CREATE TABLE events (id INTEGER PRIMARY KEY  AUTOINCREMENT  NOT NULL  UNIQUE , " +
                "last INTEGER NOT NULL , " +
                "cnt INTEGER NOT NULL , " +
                "time INTEGER NOT NULL," +
                "username VARCHAR )");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(LOG_TAG, " --- Upgrade database from " + oldVersion
                + " to " + newVersion + " version --- ");
        if (oldVersion == 4 && newVersion == 5)
            db.execSQL("ALTER TABLE events ADD COLUMN username VARCHAR");

/*
        if (oldVersion == 2 && newVersion == 3) {
            Log.d(LOG_TAG, "Upgrade from 2 to 3");
            db.execSQL("drop table favirites;");
            db.execSQL("create table favorites ( id long primary key);");
        }
        if (oldVersion == 3 && newVersion == 4) {
            db.execSQL("drop table article;");
            db.execSQL("drop table favorites;");
            onCreate(db);
        }
*/
    }
}
