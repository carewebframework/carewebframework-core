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

/**
 * Descriptor used to construct an instance of a help set.
 */
public class HelpSetDescriptor {
    
    protected String url;
    
    protected String format;
    
    protected String title;
    
    protected HelpSetDescriptor() {
    }
    
    public HelpSetDescriptor(String url, String format, String title) {
        this.url = url;
        this.format = format;
        this.title = title;
    }
    
    public String getUrl() {
        return encodeURL(url);
    }
    
    public String getFormat() {
        return format;
    }
    
    public String getTitle() {
        return title;
    }
    
    @Override
    public boolean equals(Object object) {
        if (!(object instanceof HelpSetDescriptor)) {
            return false;
        }
        
        HelpSetDescriptor hsd = (HelpSetDescriptor) object;
        return url.equals(hsd.url) && format.equals(hsd.format);
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
