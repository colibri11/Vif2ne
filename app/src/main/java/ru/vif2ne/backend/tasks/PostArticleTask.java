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

package ru.vif2ne.backend.tasks;

import ru.vif2ne.Session;
import ru.vif2ne.backend.domains.Article;
import ru.vif2ne.throwable.ApplicationException;

/**
 * Created by serg on 17.05.15.
 */
public abstract class PostArticleTask extends AbstractTask {

    private Article article;
    private String urlPost;

    protected PostArticleTask(Session session, String urlPost, Article article) {
        super(session);
        this.article = article;
        this.urlPost = urlPost;
    }


    @Override
    protected String getClassName() {
        return PostArticleTask.class.getName();
    }

    @Override
    protected Object remoteCall() throws ApplicationException {
        try {
            return remoteService.postArticle(urlPost, article);
        } catch (Exception e) {
            throw new ApplicationException(e);
        }
    }
}
