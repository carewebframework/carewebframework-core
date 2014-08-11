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
 * Interface for a user.
 */
public interface IUser {
    
    /**
     * Returns the user's full name.
     * 
     * @return User's full name.
     */
    String getFullName();
    
    /**
     * Returns the name of the user's domain.
     * 
     * @return User's domain.
     */
    String getDomainName();
    
    /**
     * Returns the identifier for the user.
     * 
     * @return User identifier.
     */
    String getUserId();
    
    /**
     * Returns the native user object if this is a proxy.
     * 
     * @return The native user object.
     */
    Object getNativeUser();
}
