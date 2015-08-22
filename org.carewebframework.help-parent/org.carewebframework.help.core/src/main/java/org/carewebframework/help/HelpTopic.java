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

import java.net.URL;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;

/**
 * Represents a single help topic
 */
public class HelpTopic implements Comparable<HelpTopic> {
    
    private final URL url;
    
    private final String label;
    
    private final String source;
    
    /**
     * Creates an unlinked help topic with a label only.
     * 
     * @param label The topic label.
     */
    public HelpTopic(String label) {
        this(null, label, null);
    }
    
    /**
     * Creates a help topic from a URL, a label, and a source.
     * 
     * @param url Topic URL.
     * @param label Topic label.
     * @param source Topic source.
     */
    public HelpTopic(URL url, String label, String source) {
        this.label = label;
        this.source = source;
        this.url = url;
    }
    
    /**
     * Copy constructor
     * 
     * @param topic Help topic to copy.
     */
    public HelpTopic(HelpTopic topic) {
        this(topic.url, topic.label, topic.source);
    }
    
    /**
     * Returns the source of the topic.
     * 
     * @return A displayable source.
     */
    public String getSource() {
        return source;
    }
    
    /**
     * Returns the label associated with the topic.
     * 
     * @return A displayable label.
     */
    public String getLabel() {
        return label;
    }
    
    /**
     * Returns the URL associated with the topic.
     * 
     * @return A URL (may be null).
     */
    public URL getURL() {
        return url;
    }
    
    /**
     * Returns true if the topic is considered a duplicate.
     * 
     * @param topic Topic to compare.
     * @return True if a duplicate.
     */
    public boolean isDuplicate(HelpTopic topic) {
        return ObjectUtils.equals(url, topic.url) && compareTo(topic) == 0;
    }
    
    @Override
    public int compareTo(HelpTopic helpTopic) {
        return label.compareToIgnoreCase(helpTopic.label);
    }
    
    @Override
    public boolean equals(Object object) {
        if (!(object instanceof HelpTopic)) {
            return false;
        }
        
        HelpTopic ht = (HelpTopic) object;
        return ht == this || (StringUtils.equals(label, ht.label) && ObjectUtils.equals(url, ht.url));
    }
}
