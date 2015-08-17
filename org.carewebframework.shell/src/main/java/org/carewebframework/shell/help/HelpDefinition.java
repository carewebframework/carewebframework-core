/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.shell.help;

import org.carewebframework.help.HelpSetDescriptor;

/**
 * Each instance of this class defines a complete definition of a CareWeb help module.
 */
public class HelpDefinition extends HelpSetDescriptor {
    
    private HelpRegistry helpRegistry;
    
    private String id;
    
    private String description;
    
    private String creator;
    
    private String copyright;
    
    private String version;
    
    private String released;
    
    public static HelpDefinition getDefinition(String tag) {
        return HelpRegistry.getInstance().get(tag);
    }
    
    public HelpDefinition() {
    
    }
    
    /**
     * Remove the help definition from the registry when it is destroyed.
     */
    public void destroy() {
        if (helpRegistry != null) {
            helpRegistry.unregister(this);
        }
    }
    
    /**
     * Called when the definition is fully instantiated.
     */
    public void init() {
        if (title != null && !title.isEmpty()) {
            if (helpRegistry != null) {
                helpRegistry.register(this);
            }
        }
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public void setUrl(String url) {
        this.url = url;
    }
    
    public void setFormat(String format) {
        this.format = format;
    }
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getCreator() {
        return creator;
    }
    
    public void setCreator(String creator) {
        this.creator = creator;
    }
    
    public String getCopyright() {
        return copyright;
    }
    
    public void setCopyright(String copyright) {
        this.copyright = copyright;
    }
    
    public String getVersion() {
        return version;
    }
    
    public void setVersion(String version) {
        this.version = version;
    }
    
    public String getReleased() {
        return released;
    }
    
    public void setReleased(String released) {
        this.released = released;
    }
    
    public void setHelpRegistry(HelpRegistry helpRegistry) {
        this.helpRegistry = helpRegistry;
    }
    
    public HelpRegistry getHelpRegistry() {
        return helpRegistry;
    }
    
}
