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
 * Created by serg 06.06.15 23:25
 */

package ru.vif2ne.backend.domains;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 * Created by serg on 06.06.15.
 */
public class UserSettings {
    private static final String LOG_TAG = "UserSettings";

    private String headerMessage;
    private String footerMessage;
    private boolean quote;

    public UserSettings(String source) {
        Document doc = Jsoup.parse(source);
        headerMessage = doc.getElementsByAttributeValue("name", "tegLine").first().val();
        footerMessage = doc.getElementsByAttributeValue("name", "tearLine").first().val();
        quote = doc.getElementsByAttributeValue("name", "quotting").first().hasAttr("checked");
    }

    public String getHeaderMessage() {
        return headerMessage + "\n";
    }

    public String getFooterMessage() {
        return footerMessage + "\n";
    }

    public boolean isQuote() {
        return quote;
    }
}
