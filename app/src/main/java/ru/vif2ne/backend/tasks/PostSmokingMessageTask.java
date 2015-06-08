package ru.vif2ne.backend.tasks;

import ru.vif2ne.Session;
import ru.vif2ne.throwable.ApplicationException;

/**
 * Created by serg on 08.06.15.
 */
public abstract class PostSmokingMessageTask extends AbstractTask {

    private boolean smokingPrivate;
    public PostSmokingMessageTask(Session session, boolean smokingPrivate) {
        super(session);
        this.smokingPrivate = smokingPrivate;
    }

    @Override
    protected String getClassName() {
        return PostSmokingMessageTask.class.getName();
    }

    @Override
    protected Object remoteCall() throws ApplicationException {
        try {
            return remoteService.postSmokingMessage(smokingPrivate);
        } catch (Exception e) {
           throw new ApplicationException(e);
        }
    }
}
