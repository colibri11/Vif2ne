package ru.vif2ne.backend.account;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import ru.vif2ne.MainApplication;

/**
 * Created by serg on 08.09.15.
 */
public class Vif2neAuthenticatorService extends Service {
    private static final String LOG_TAG = Vif2neAuthenticatorService.class.getSimpleName();
    private Vif2neAuthenticator vif2neAuthenticator;

    @Override
    public void onCreate() {
        Log.d(LOG_TAG, "onCreate");
        super.onCreate();
        MainApplication application = (MainApplication) getApplication();
        vif2neAuthenticator = new Vif2neAuthenticator(getApplicationContext(), application.getSession());
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(LOG_TAG,"onBind");
        return vif2neAuthenticator.getIBinder();
    }
}
