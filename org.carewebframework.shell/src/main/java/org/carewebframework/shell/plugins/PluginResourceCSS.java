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
