/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.shell.plugins;

import org.carewebframework.shell.CareWebShell;
import org.carewebframework.shell.layout.UIElementBase;

/**
 * Resource for declaring style sheets associated with the plugin.
 */
public class PluginResourceCSS implements IPluginResource {
    
    
    // The url of the style sheet.
    private String url;
    
    /**
     * Returns the url of the associated style sheet.
     * 
     * @return A url.
     */
    public String getUrl() {
        return url;
    }
    
    /**
     * Sets the url of the associated style sheet.
     * 
     * @param url The url.
     */
    public void setUrl(String url) {
        this.url = url;
    }
    
    /**
     * Registers/unregisters a CSS resource.
     * 
     * @param shell The running shell.
     * @param owner Owner of the resource.
     * @param register If true, register the resource. If false, unregister it.
     */
    @Override
    public void register(CareWebShell shell, UIElementBase owner, boolean register) {
        if (register) {
            shell.registerStyleSheet(getUrl());
        }
    }
    
}
