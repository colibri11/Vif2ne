package ru.vif2ne.backend.tasks;

import android.text.TextUtils;
import android.util.Log;

import ru.vif2ne.Session;
import ru.vif2ne.throwable.ApplicationException;

/**
 * Created by serg on 05.06.15.
 */
public abstract class LoadSmokingTask extends AbstractTask {

    private static final String LOG_TAG = "LoadSmokingTask";

    public LoadSmokingTask(Session session) {
        super(session);
    }

    @Override
    protected String getClassName() {
        return LoadSmokingTask.class.getName();
    }

    @Override
    protected Object remoteCall() throws ApplicationException {
        try {
            if (!remoteService.isAuthenticated() &&
                    !TextUtils.isEmpty(remoteService.getUserName()) &&
                    !TextUtils.isEmpty(remoteService.getPasswd())) {
                Log.d(LOG_TAG, "login");
                remoteService.login(session.getRemoteService().getUserName(),
                        session.getRemoteService().getPasswd());

            }
            return remoteService.loadSmoking(session.getSmoking());
        } catch (Exception e) {
            throw new ApplicationException(e);
        }
    }
}
