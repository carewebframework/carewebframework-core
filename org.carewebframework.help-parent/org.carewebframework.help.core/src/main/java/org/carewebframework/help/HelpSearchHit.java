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
     * Returns the topic source.
     * 
     * @return The topic source.
     */
    public String getSource() {
        return topic.getSource();
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
