/*
 * #%L
 * carewebframework
 * %%
 * Copyright (C) 2008 - 2016 Regenstrief Institute, Inc.
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * This Source Code Form is also subject to the terms of the Health-Related
 * Additional Disclaimer of Warranty and Limitation of Liability available at
 *
 *      http://www.carewebframework.org/licensing/disclaimer.
 *
 * #L%
 */
package org.carewebframework.shell.elements;

import org.carewebframework.web.component.BaseUIComponent;
import org.carewebframework.web.component.Div;
import org.carewebframework.web.component.Iframe;
import org.carewebframework.web.component.Import;

/**
 * UI element that encapsulates an iframe or an include (as determined by URL).
 */
public class ElementFrame extends ElementBase {

    static {
        registerAllowedParentClass(ElementFrame.class, ElementBase.class);
    }

    private final Div root = new Div();

    private BaseUIComponent child;

    private String url;

    public ElementFrame() {
        super();
        root.addClass("cwf-plugin-container");
        fullSize(root);
        setOuterComponent(root);
    }

    /**
     * Sets the URL of the content to be retrieved. If the URL starts with "http", it is fetched
     * into an iframe. Otherwise, an include component is created and used to fetch the content.
     *
     * @param url Content URL.
     */
    public void setUrl(String url) {
        this.url = url;

        if (child != null) {
            child.destroy();
            child = null;
        }

        if (url.startsWith("http") || !url.endsWith(".cwf")) {
            child = new Iframe();
            ((Iframe) child).setSrc(url);
        } else {
            child = new Import();
            ((Import) child).setSrc(url);
        }

        fullSize(child);
        root.addChild(child);
    }

    /**
     * Returns the URL of the content.
     *
     * @return A URL.
     */
    public String getUrl() {
        return url;
    }

}
