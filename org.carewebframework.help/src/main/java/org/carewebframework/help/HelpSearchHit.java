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

import org.carewebframework.common.NumUtil;

/**
 * Represents a single topic hit from a search operation.
 */
public class HelpSearchHit implements Comparable<HelpSearchHit> {
    
    private final HelpTopic topic;
    
    private final double confidence;
    
    public HelpSearchHit(HelpTopic topic, double confidence) {
        this.topic = topic;
        this.confidence = confidence;
    }
    
    /**
     * Returns the topic associated with this hit.
     * 
     * @return A help topic.
     */
    public HelpTopic getTopic() {
        return topic;
    }
    
    /**
     * Returns the confidence score associated with this hit.
     * 
     * @return A confidence score.
     */
    public double getConfidence() {
        return confidence;
    }
    
    /**
     * Used to sort hits by confidence level.
     */
    @Override
    public int compareTo(HelpSearchHit hit) {
        int result = -NumUtil.compare(confidence, hit.confidence);
        return result != 0 ? result : topic.compareTo(hit.topic);
    }
    
    /**
     * Returns true if confidence score and topic are equal.
     */
    @Override
    public boolean equals(Object object) {
        if (!(object instanceof HelpSearchHit)) {
            return false;
        }
        
        HelpSearchHit hit = (HelpSearchHit) object;
        return confidence == hit.confidence && topic.equals(hit.topic);
    }
}
