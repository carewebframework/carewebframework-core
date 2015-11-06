/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.help;

import java.util.Locale;

/**
 * Each instance of this class represents a complete definition of a CareWeb help module.
 */
public class HelpModule {
    
    private String id;
    
    private String url;
    
    private Locale locale;
    
    private String format;
    
    private String title;
    
    private String description;
    
    private String creator;
    
    private String copyright;
    
    private String version;
    
    private String released;
    
    public static HelpModule getModule(String id) {
        return HelpModuleRegistry.getInstance().get(id);
    }
    
    public HelpModule() {
    }
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getUrl() {
        return encodeURL(url);
    }
    
    public void setUrl(String url) {
        this.url = url;
    }
    
    public Locale getLocale() {
        return locale;
    }
    
    public void setLocale(String locale) {
        this.locale = locale == null ? null : new Locale(locale);
    }
    
    public String getFormat() {
        return format;
    }
    
    public void setFormat(String format) {
        this.format = format;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
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
    
    /**
     * Encodes the specified URL path, which may be absolute or relative.
     * <p>
     * For compressed help sets, use the ~./ prefix. When using this syntax, ZK requires that the
     * root folder in the jar file be named "web" with the specified path being relative to that
     * folder.
     * 
     * @param url URL to encode.
     * @return The encoded URL.
     */
    private String encodeURL(String url) {
        return url.startsWith("/") || url.startsWith(".") ? url
                : url.startsWith("~./") ? "/web" + url.substring(2) : "/web/" + url;
    }
    
}
