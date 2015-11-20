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

import java.util.Collection;

import org.carewebframework.help.viewer.IHelpView;

/**
 * Interface to be implemented by a help set provider.
 */
public interface IHelpSet {
    
    /**
     * Return the unique id of the help set.
     * 
     * @return The help set unique id.
     */
    String getId();
    
    /**
     * Returns the name (title) of the help set.
     * 
     * @return The help set name.
     */
    String getName();
    
    /**
     * Returns the id of the home topic, if any.
     * 
     * @return The home topic id (may be null).
     */
    String getHomeID();
    
    /**
     * Returns the help topic associated with the topic id.
     * 
     * @param topicId A topic id.
     * @return The associated help topic (may be null).
     */
    HelpTopic getTopic(String topicId);
    
    /**
     * Returns a collection of all help views contained within the help set.
     * 
     * @return Help views in the help set.
     */
    Collection<IHelpView> getAllViews();
    
}
