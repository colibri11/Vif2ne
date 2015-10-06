package ru.vif2ne.backend.account;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;

import ru.vif2ne.Session;
import ru.vif2ne.backend.RemoteService;
import ru.vif2ne.throwable.ApplicationException;
import ru.vif2ne.ui.LoginActivity;

/**
 * Created by serg on 08.09.15.
 */
public class Vif2neAuthenticator extends AbstractAccountAuthenticator {

    private static final String LOG_TAG = Vif2neAuthenticator.class.getSimpleName();

    private Context ctx;
    private Session session;

    public Vif2neAuthenticator(Context context, Session session) {
        super(context);
        this.ctx = context;
        this.session = session;
    }

    @Override
    public Bundle editProperties(AccountAuthenticatorResponse response, String accountType) {
        Log.d(LOG_TAG, "editProperties");
        return null;
    }

    @Override
    public Bundle addAccount(AccountAuthenticatorResponse response, String accountType, String authTokenType, String[] requiredFeatures, Bundle options) throws NetworkErrorException {
        Log.d(LOG_TAG,"addAccount");
        Intent intent = new Intent(ctx, LoginActivity.class);
        intent.putExtra(LoginActivity.EXTRA_TOKEN_TYPE, accountType);
        intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
        final Bundle bundle = new Bundle();
        if (options != null) {
            bundle.putAll(options);
        }
        bundle.putParcelable(AccountManager.KEY_INTENT, intent);
        return bundle;
    }

    @Override
    public Bundle confirmCredentials(AccountAuthenticatorResponse response, Account account, Bundle options) throws NetworkErrorException {
        Log.d(LOG_TAG,"confirmCredentials");
        return null;
    }

    @Override
    public Bundle getAuthToken(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle options) throws NetworkErrorException {
        Log.d(LOG_TAG,"getAuthToken");
        final Bundle result = new Bundle();

        final AccountManager am = AccountManager.get(ctx.getApplicationContext());
        String authToken = am.peekAuthToken(account, authTokenType);
        if (TextUtils.isEmpty(authToken)) {
            final String password = am.getPassword(account);
            if (!TextUtils.isEmpty(password)) {
                RemoteService remoteService = session.getRemoteService();
                try {
                    authToken = remoteService.login(account.name, password);
                } catch (IOException | ApplicationException e) {
                    e.printStackTrace();
                }
            }
        }
        if (!TextUtils.isEmpty(authToken)) {
            result.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
            result.putString(AccountManager.KEY_ACCOUNT_TYPE, account.type);
            result.putString(AccountManager.KEY_AUTHTOKEN, authToken);
        } else {
            final Intent intent = new Intent(ctx, LoginActivity.class);
            intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
            intent.putExtra(LoginActivity.EXTRA_TOKEN_TYPE, authTokenType);
            final Bundle bundle = new Bundle();
            bundle.putParcelable(AccountManager.KEY_INTENT, intent);
        }
        return result;
    }

    @Override
    public String getAuthTokenLabel(String authTokenType) {
        Log.d(LOG_TAG,"getAuthTokenLabel");
        return null;
    }

    @Override
    public Bundle updateCredentials(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle options) throws NetworkErrorException {
        Log.d(LOG_TAG,"updateCredentials");
        return null;
    }

    @Override
    public Bundle hasFeatures(AccountAuthenticatorResponse response, Account account, String[] features) throws NetworkErrorException {
        Log.d(LOG_TAG,"hasFeatures");
        return null;
    }
}
