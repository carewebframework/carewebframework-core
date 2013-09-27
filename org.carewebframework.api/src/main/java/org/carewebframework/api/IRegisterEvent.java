/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.api;

/**
 * Classes that implement this will be notified when an object is registered to or unregistered from
 * the framework.
 */
public interface IRegisterEvent {
    
    /**
     * Called when an object is registered to the framework.
     * 
     * @param object Object being registered.
     */
    void registerObject(Object object);
    
    /**
     * Called when an object is unregistered from the framework.
     * 
     * @param object Object being unregistered.
     */
    void unregisterObject(Object object);
}
