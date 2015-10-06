package ru.vif2ne.ui;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.app.LoaderManager;
import android.content.Loader;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import ru.vif2ne.MainApplication;
import ru.vif2ne.R;
import ru.vif2ne.Session;
import ru.vif2ne.backend.account.Vif2neAccount;
import ru.vif2ne.backend.tasks.AuthTokenLoader;

/**
 * Created by serg on 08.09.15.
 */
public class LoginActivity extends AccountAuthenticatorActivity implements LoaderManager.LoaderCallbacks<String>, View.OnClickListener {


    private static final String LOG_TAG = LoginActivity.class.getSimpleName();
    public static String EXTRA_TOKEN_TYPE = Vif2neAccount.TYPE+".EXTRA_TOKEN_TYPE";

    private Button loginAction, cancelAction;
    private EditText userNameEditText, passwdEditText;
    private AccountManager accountManager;
    private Session session;


    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.activity_auth);
        loginAction = (Button) findViewById(R.id.action_login);
        cancelAction = (Button) findViewById(R.id.action_cancel);
        userNameEditText = (EditText) findViewById(R.id.username_edit_view);
        passwdEditText = (EditText) findViewById(R.id.passwd_view);
        loginAction.setOnClickListener(this);
        MainApplication application = (MainApplication) getApplication();
        session = application.getSession();
        cancelAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        Log.d(LOG_TAG, "username:"+session.getRemoteService().getUserName());
        accountManager = AccountManager.get(this);
    }

    public void onTokenReceived(Account account, String password, String token) {
        Log.d(LOG_TAG, "onTokenReceived");
        final Bundle result = new Bundle();
        if (accountManager.addAccountExplicitly(account, password, new Bundle())) {
            result.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
            result.putString(AccountManager.KEY_ACCOUNT_TYPE, account.type);
            result.putString(AccountManager.KEY_AUTHTOKEN, token);
            accountManager.setAuthToken(account, account.type, token);
        } else {
            result.putString(AccountManager.KEY_ERROR_MESSAGE, getString(R.string.account_already_exists));
        }
        setAccountAuthenticatorResult(result);
        setResult(RESULT_OK);
        finish();
    }

    @Override
    public Loader<String> onCreateLoader(int id, Bundle args) {
        Log.d(LOG_TAG, "onCreateLoader");
        if (id == R.id.auth_token_loader) {
            Log.d(LOG_TAG, "AuthTokenLoader");
            return new AuthTokenLoader(
                    this,
                    userNameEditText.getText().toString(),
                    passwdEditText.getText().toString(), session
            );
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<String> loader, String token) {
        Log.d(LOG_TAG, "onLoadFinished");
        if (loader.getId() == R.id.auth_token_loader && !TextUtils.isEmpty(token)) {
            onTokenReceived(
                    new Vif2neAccount(userNameEditText.getText().toString()),
                    passwdEditText.getText().toString(), token
            );
        }
    }

    @Override
    public void onLoaderReset(Loader<String> loader) {

    }

    @Override
    public void onClick(View v) {
        getLoaderManager().restartLoader(R.id.auth_token_loader, null, this);
    }
}
