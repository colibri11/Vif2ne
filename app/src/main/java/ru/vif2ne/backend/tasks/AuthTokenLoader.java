package ru.vif2ne.backend.tasks;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;

import ru.vif2ne.Session;
import ru.vif2ne.backend.RemoteService;
import ru.vif2ne.throwable.ApplicationException;

/**
 * Created by serg on 08.09.15.
 */
public class AuthTokenLoader extends AsyncTaskLoader<String> {

    private static final String LOG_TAG = AuthTokenLoader.class.getSimpleName();
    private String user;
    private String passwd;
    private String token;
    private Session session;

    public AuthTokenLoader(Context context, String user, String passwd, Session session) {
        super(context);
        Log.d(LOG_TAG, "init");
        this.user = user;
        this.passwd = passwd;
        this.session = session;
    }

    @Override
    protected void onStartLoading() {
        if (TextUtils.isEmpty(token)) {
            forceLoad();
        } else {
            deliverResult(token);
        }
    }

    @Override
    public void deliverResult(String data) {
        token = data;
        super.deliverResult(data);
    }

    @Override
    public String loadInBackground() {
        Log.d(AuthTokenLoader.class.getSimpleName(), "loadInBackground");
        RemoteService remoteService = session.getRemoteService();
        try {
            return remoteService.login(user, passwd);
        } catch (IOException | ApplicationException e) {
            e.printStackTrace();
        }
        return null;
    }
}
