/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.api.context;

/**
 * Base interface for context change event. All context objects should define a descendant of this
 * interface.
 */
public interface IContextEvent {
    
    /**
     * Survey of context subscriber
     * 
     * @param silent If true, user interaction is not permitted.
     * @return Null or empty string if context change should proceed. Any other value constitutes a
     *         no vote for the context change request.
     */
    String pending(boolean silent);
    
    /**
     * Committed context event
     */
    void committed();
    
    /**
     * Cancellation of context event
     */
    void canceled();
}
