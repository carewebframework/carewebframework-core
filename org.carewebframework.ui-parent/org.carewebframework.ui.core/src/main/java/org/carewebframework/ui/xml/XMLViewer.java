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
package org.carewebframework.ui.xml;

import org.carewebframework.common.MiscUtil;
import org.carewebframework.web.component.BaseComponent;
import org.carewebframework.web.event.Event;
import org.carewebframework.web.event.EventUtil;
import org.carewebframework.web.page.PageUtil;
import org.w3c.dom.Document;

/**
 * Static utility class for XML viewing functions.
 */
public class XMLViewer {
    
    /**
     * Show the dialog, loading the specified document.
     * 
     * @param document The XML document.
     * @throws Exception
     */
    public static void showXML(Document document) {
        try {
            BaseComponent dlg = PageUtil.createPage(XMLConstants.VIEW_DIALOG, null).get(0);
            EventUtil.send(new Event("modal", dlg, document));
        } catch (Exception e) {
            throw MiscUtil.toUnchecked(e);
        }
    }
    
    /**
     * Display the ZUML for the component tree rooted at root.
     * 
     * @param root Root component of tree.
     */
    public static void showZUML(BaseComponent root) {
        showZUML(root, XMLConstants.EXCLUDED_PROPERTIES);
        
    }
    
    /**
     * Display the ZUML for the component tree rooted at root.
     * 
     * @param root Root component of tree.
     * @param excludedProperties Excluded properties.
     */
    public static void showZUML(BaseComponent root, String... excludedProperties) {
        showXML(CWF2XML.toDocument(root, excludedProperties));
        
    }
    
    /**
     * Enforce static class.
     */
    private XMLViewer() {
    }
}
