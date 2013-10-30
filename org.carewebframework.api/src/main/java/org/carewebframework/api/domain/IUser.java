/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.api.domain;

/**
 * Standard interface for accessing a user domain object. This is used by the framework to access
 * the authenticated user in an implementation-independent fashion.
 */
public interface IUser extends IPerson {
    
    void setUsername(String username);
    
    String getUsername();
    
}
