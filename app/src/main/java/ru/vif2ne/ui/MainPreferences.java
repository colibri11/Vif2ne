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
 * Created by serg 24.05.15 19:43
 */

package ru.vif2ne.ui;

import android.os.Bundle;
import android.preference.PreferenceActivity;

import ru.vif2ne.MainApplication;
import ru.vif2ne.R;
import ru.vif2ne.Session;

/**
 * Created by serg on 24.05.15.
 */
public class MainPreferences extends PreferenceActivity {
    Session session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        session = ((MainApplication) getApplication()).getSession();
        addPreferencesFromResource(R.xml.main_preferences);
    }

    @Override
    protected void onDestroy() {
        session.loadPrefs();
        super.onDestroy();
    }
}
