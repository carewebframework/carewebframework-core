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
package org.carewebframework.shell.layout;

import org.zkoss.zk.ui.HtmlBasedComponent;
import org.zkoss.zul.Div;
import org.zkoss.zul.Iframe;
import org.zkoss.zul.Include;

/**
 * UI element that encapsulates an iframe or an include (as determined by URL).
 */
public class UIElementFrame extends UIElementZKBase {
    
    static {
        registerAllowedParentClass(UIElementFrame.class, UIElementBase.class);
    }
    
    private final Div root = new Div();
    
    private HtmlBasedComponent child;
    
    private String url;
    
    public UIElementFrame() {
        super();
        root.setZclass("cwf-plugin-container");
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
            child.detach();
            child = null;
        }
        
        if (url.startsWith("http")) {
            child = new Iframe();
            ((Iframe) child).setSrc(url);
        } else {
            child = new Include();
            ((Include) child).setSrc(url);
        }
        
        fullSize(child);
        root.appendChild(child);
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
