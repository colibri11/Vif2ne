package ru.mazelab.vif2ne.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import ru.mazelab.vif2ne.R;
import ru.mazelab.vif2ne.backend.tasks.LoginTask;

/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Created by Colibri  15.05.15 22:19
 * LoginDialog.java
 *
 *
 */

public class LoginDialog extends BaseActivity {

    private static final String LOGIN_NAME = "login";
    EditText loginView, passView;
    Button okAction, cancelAction;
    ImageView loginStatusImage;
    private String goOnExit;
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        loginView = (EditText) findViewById(R.id.login_view);
        passView = (EditText) findViewById(R.id.passwd_view);
        okAction = (Button) findViewById(R.id.action_ok);
        cancelAction = (Button) findViewById(R.id.action_cancel);
        loginStatusImage = (ImageView) findViewById(R.id.login_status_image);
        loadText();
        cancelAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                remoteService.logout();
                finish();
            }
        });
        okAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new LoginTask(session, loginView.getText().toString(), passView.getText().toString()) {
                    @Override
                    public void goSuccess(Object result) {
                        Toast.makeText(getApplicationContext(), "Login ok", Toast.LENGTH_LONG).show();
                        saveText();
                        if (!TextUtils.isEmpty(goOnExit)) {
                            intent = new Intent(session.getCurrentActivity(), NewArticleActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            session.getCurrentActivity().startActivity(intent);
                        } else
                            finish();
                    }

                }.execute((Void) null);
            }
        });


        intent = getIntent();
        goOnExit = intent.getStringExtra("goto");
    }

    @Override
    protected void bind() {
        if (remoteService.isAuthenticated()) {
            loginStatusImage.setImageDrawable(getResources().getDrawable(R.drawable.ic_person));
        } else {
            loginStatusImage.setImageDrawable(getResources().getDrawable(R.drawable.ic_perm_identity));
        }
    }

    void saveText() {
        if (loginView == null) return;
        SharedPreferences sPref = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor ed = sPref.edit();
        ed.putString(LOGIN_NAME, loginView.getText().toString());
        ed.apply();
    }

    void loadText() {
        SharedPreferences sPref = getPreferences(MODE_PRIVATE);
        String savedText = sPref.getString(LOGIN_NAME, "");
        loginView.setText(savedText);
    }

}
