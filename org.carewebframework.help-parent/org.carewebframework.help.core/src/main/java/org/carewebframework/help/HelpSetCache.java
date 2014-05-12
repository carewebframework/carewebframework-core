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

import org.carewebframework.common.AbstractCache;
import org.carewebframework.help.HelpSetCache.HelpSetDescriptor;

/**
 * Maintains a cache of all known help sets. This is a singleton class.
 */
public class HelpSetCache extends AbstractCache<HelpSetDescriptor, IHelpSet> {
    
    private static final HelpSetCache instance = new HelpSetCache();
    
    private String baseURL = HelpUtil.getBaseUrl() + "/help/";
    
    public class HelpSetDescriptor {
        
        protected final String url;
        
        protected final String format;
        
        public HelpSetDescriptor(String url, String format) {
            this.url = url;
            this.format = format;
        }
        
        @Override
        public boolean equals(Object object) {
            if (!(object instanceof HelpSetDescriptor)) {
                return false;
            }
            
            HelpSetDescriptor hsd = (HelpSetDescriptor) object;
            return url.equals(hsd.url) && format.equals(hsd.format);
        }
    }
    
    public static HelpSetCache getInstance() {
        return instance;
    }
    
    /**
     * Enforce singleton instance.
     */
    private HelpSetCache() {
        super();
    }
    
    /**
     * Set the base url for non-compressed help sets.
     * 
     * @param baseURL
     */
    public void setBaseURL(String baseURL) {
        this.baseURL = baseURL;
    }
    
    /**
     * Get the base url for non-compressed help sets.
     * 
     * @return The base url.
     */
    public String getBaseURL() {
        return baseURL;
    }
    
    @Override
    protected IHelpSet fetch(HelpSetDescriptor descriptor) {
        return HelpSetFactory.create(descriptor.format, descriptor.url);
    }
    
    /**
     * Returns the help set corresponding to the specified url. If the help set has not been
     * previously loaded, it is loaded at this time. If the help set cannot be found, null is
     * returned.
     * 
     * @param url Url of the requested help set. This can be an absolute or relative path. Where the
     *            "~./" prefix is used, special processing is required. This is because ZK
     *            interferes with accessing files with an xml extension via this special path. So,
     *            we use an alternate HelpSet constructor that accesses the help set resources via
     *            the class loader. However, when we do this, Oracle Help sets the base url for the
     *            help set to the local file system path, which then prevents accessing the help
     *            content via the web UI. To fix this, the help viewer must translate this local
     *            file system path back to a web-based URL.
     * @param format The help set format specifier.
     * @return A help set.
     */
    public IHelpSet get(String url, String format) {
        return get(new HelpSetDescriptor(encodeURL(url), format));
    }
    
    /**
     * Encodes the specified url path, which may be absolute or relative.
     * <p>
     * For compressed help sets, use the ~./ prefix. When using this syntax, ZK requires that the
     * root folder in the jar file be named "web" with the specified path being relative to that
     * folder.
     * 
     * @param url
     * @return
     */
    private String encodeURL(String url) {
        if (url.startsWith("~./")) {
            return "/web" + url.substring(2);
        }
        
        if (!url.contains(":/")) {
            return baseURL + url;
        }
        
        return url;
    }
    
}
