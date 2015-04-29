/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.ui.xml;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Events;

import org.w3c.dom.Document;

/**
 * Static utility class for XML viewing functions.
 */
public class XMLViewer {
    
    /**
     * Show the dialog, loading the specified document.
     * 
     * @param document The XML document.
     */
    public static void showXML(Document document) {
        Component dlg = Executions.createComponents(XMLConstants.VIEW_DIALOG, null, null);
        Events.sendEvent(Events.ON_MODAL, dlg, document);
    }
    
    /**
     * Display the ZUML for the component tree rooted at root.
     * 
     * @param root Root component of tree.
     */
    public static void showZUML(Component root) {
        showZUML(root, XMLConstants.EXCLUDED_PROPERTIES);
        
    }
    
    /**
     * Display the ZUML for the component tree rooted at root.
     * 
     * @param root Root component of tree.
     * @param excludedProperties Excluded properties.
     */
    public static void showZUML(Component root, String... excludedProperties) {
        showXML(ZK2XML.toDocument(root, excludedProperties));
        
    }
    
    /**
     * Enforce static class.
     */
    private XMLViewer() {
    }
}
