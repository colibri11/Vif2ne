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

package ru.vif2ne.ui;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

import ru.vif2ne.R;
import ru.vif2ne.backend.RemoteService;
import ru.vif2ne.backend.account.Vif2neAccount;
import ru.vif2ne.backend.tasks.LoginTask;

public class LoginDialog extends BaseActivity {

    private static final String LOG_TAG = "LoginDialog";
    private EditText usernameEditView, passView;
    private Button loginAction, cancelAction, logoutAction, cancelLogoutAction;
    private ImageView loginStatusImage;
    private LinearLayout loginLayout, logoutLayout;
    private TextView usernameView;
    private String goOnExit;
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        usernameEditView = (EditText) findViewById(R.id.username_edit_view);
        passView = (EditText) findViewById(R.id.passwd_view);
        loginAction = (Button) findViewById(R.id.action_login);
        cancelAction = (Button) findViewById(R.id.action_cancel);
        logoutAction = (Button) findViewById(R.id.logout_action);
        cancelLogoutAction = (Button) findViewById(R.id.action_cancel_logout);
        loginLayout = (LinearLayout) findViewById(R.id.login_layout);
        logoutLayout = (LinearLayout) findViewById(R.id.logout_layout);
        usernameView = (TextView) findViewById(R.id.username_view);

        loginStatusImage = (ImageView) findViewById(R.id.login_status_image);
        loadText();
        cancelAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        cancelLogoutAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        logoutAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                remoteService.logout();
                session.getEventEntries().clearEntriesDB();
                Log.d(LOG_TAG, "size bl:" + session.getEventEntries().size());
                session.loadTree(-1);
                Log.d(LOG_TAG, "size al:" + session.getEventEntries().size());
                finish();
            }
        });
        loginAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
      /*          AccountManager am = AccountManager.get(getApplicationContext());
                Account[] accounts = am.getAccountsByType(Vif2neAccount.TYPE);
                if (accounts.length == 0) {
                    addNewAccount(am);
                }*/
                new LoginTask(session, usernameEditView.getText().toString(), passView.getText().toString()) {
                    @Override
                    public void goSuccess(Object result) {
                        session.loadTree(-1);
                        Toast.makeText(getApplicationContext(), "Login ok", Toast.LENGTH_LONG).show();
                        if (!TextUtils.isEmpty(goOnExit)) {
                            intent = new Intent(session.getCurrentActivity(), NewArticleActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            session.getCurrentActivity().startActivity(intent);
                            finish();
                        } else
                            finish();
                    }

                }.execute((Void) null);
            }
        });


        intent = getIntent();
        goOnExit = intent.getStringExtra("goto");
    }

    private void addNewAccount(AccountManager am) {
        am.addAccount(Vif2neAccount.TYPE, Vif2neAccount.TOKEN_FULL_ACCESS, null, null, this,
                new AccountManagerCallback<Bundle>() {
                    @Override
                    public void run(AccountManagerFuture<Bundle> future) {
                        try {
                            Log.d(LOG_TAG,future.getResult().toString());
                        } catch (OperationCanceledException | IOException | AuthenticatorException e) {
                            e.printStackTrace();
                            Log.d(LOG_TAG, " addNewAccount finish");
                            LoginDialog.this.finish();
                        }
                    }
                }, null
        );
    }

    @Override
    protected void bind() {
        if (remoteService.isAuthenticated()) {
            usernameView.setText(remoteService.getUserName());
            loginLayout.setVisibility(View.GONE);
            logoutLayout.setVisibility(View.VISIBLE);
            loginStatusImage.setImageDrawable(getResources().getDrawable(R.drawable.ic_person));
        } else {
            loginLayout.setVisibility(View.VISIBLE);
            logoutLayout.setVisibility(View.GONE);
            loginStatusImage.setImageDrawable(getResources().getDrawable(R.drawable.ic_perm_identity));
        }
    }

    void loadText() {
        if (!TextUtils.isEmpty(remoteService.getUserName()) &&
                !remoteService.getUserName().equals(RemoteService.EMPTY_USER)) {
            usernameEditView.setText(remoteService.getUserName());
        } else {
            usernameEditView.setText("");
        }
    }

}
