package ru.vif2ne.backend.tasks;

import ru.vif2ne.Session;
import ru.vif2ne.throwable.ApplicationException;

/**
 * Created by serg on 08.06.15.
 */
public abstract class PostSmokingMessageTask extends AbstractTask {

    public PostSmokingMessageTask(Session session) {
        super(session);
    }

    @Override
    protected String getClassName() {
        return PostSmokingMessageTask.class.getName();
    }

    @Override
    protected Object remoteCall() throws ApplicationException {
        try {
            return remoteService.postSmokingMessage(false);
        } catch (Exception e) {
           throw new ApplicationException(e);
        }
    }
}
