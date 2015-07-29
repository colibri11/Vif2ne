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
 * Created by serg 31.05.15 16:09
 */

package ru.vif2ne.ui;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import ru.vif2ne.MainApplication;
import ru.vif2ne.R;
import ru.vif2ne.Session;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        MainApplication application = (MainApplication) getApplication();
        Session session = application.getSession();


        TextView versionTextView = (TextView) findViewById(R.id.version_view);
        PackageInfo pInfo = null;

        try {
            pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            String version = pInfo.versionName + " (" + pInfo.versionCode + ")"
                    + " db (" + loadCountEvent(session)
                    + ")-" + loadCountEvents(session);
            versionTextView.setText("version:" + version);
        } catch (PackageManager.NameNotFoundException e) {
            versionTextView.setVisibility(View.GONE);
            e.printStackTrace();
        }


    }

    public int loadCountEvent(Session session) {
        SQLiteDatabase readableDatabase = session.getDbHelper().getReadableDatabase();
        int totalCount = 0;
        Cursor c = readableDatabase.rawQuery("select count(*) from event", null);
        if (c.moveToFirst()) {
            totalCount += c.getInt(0);
        }
        c.close();
        return totalCount;
    }

    public int loadCountEvents(Session session) {
        SQLiteDatabase readableDatabase = session.getDbHelper().getReadableDatabase();
        int totalCount = 0;
        Cursor c = readableDatabase.rawQuery("select count(*) from events", null);
        if (c.moveToFirst()) {
            totalCount += c.getInt(0);
        }
        c.close();
        return totalCount;
    }
}
