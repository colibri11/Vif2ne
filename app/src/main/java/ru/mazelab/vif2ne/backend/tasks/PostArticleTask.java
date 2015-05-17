package ru.mazelab.vif2ne.backend.tasks;

import java.io.IOException;

import ru.mazelab.vif2ne.Session;
import ru.mazelab.vif2ne.backend.domains.Article;
import ru.mazelab.vif2ne.throwable.ApplicationException;

/**
 * Created by serg on 17.05.15.
 */
public abstract class PostArticleTask extends AbstractTask {

    private Article article;

    protected PostArticleTask(Session session, Article article) {
        super(session);
        this.article = article;
    }


    @Override
    protected String getClassName() {
        return PostArticleTask.class.getName();
    }

    @Override
    protected Object remoteCall() throws ApplicationException {
        try {
            return remoteService.postArticle(article);
        } catch (IOException e) {
            e.printStackTrace();
            throw new ApplicationException(e.getMessage());
        }
    }
}
