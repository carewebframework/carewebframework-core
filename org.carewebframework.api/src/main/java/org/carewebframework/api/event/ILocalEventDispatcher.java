/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.api.event;

/**
 * Interface implemented by the local event dispatcher.
 */
public interface ILocalEventDispatcher {
    
    /**
     * Registers the global event dispatcher.
     * 
     * @param globalEventDispatcher The global event dispatcher to register. This may be null if
     *            global event propagation is not supported.
     */
    public void setGlobalEventDispatcher(IGlobalEventDispatcher globalEventDispatcher);
    
    /**
     * Returns the global event dispatcher.
     * 
     * @return The global event dispatcher. This may be null if global event propagation is not
     *         supported.
     */
    public IGlobalEventDispatcher getGlobalEventDispatcher();
    
    /**
     * Fires the specified event locally.
     * 
     * @param eventName Name of the event to fire.
     * @param eventData Associated data object.
     */
    void fireLocalEvent(String eventName, Object eventData);
}
