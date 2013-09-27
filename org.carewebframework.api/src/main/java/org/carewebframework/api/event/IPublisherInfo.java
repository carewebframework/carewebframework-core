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
 * Returns information about a global event publisher.
 */
public interface IPublisherInfo {
    
    long getUserId();
    
    String getUserName();
    
    String getAppName();
    
    String getClientId();
    
    String formatPublisherInfo();
    
}
