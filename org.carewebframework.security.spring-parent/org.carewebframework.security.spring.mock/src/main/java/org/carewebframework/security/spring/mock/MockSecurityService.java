/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.security.spring.mock;

import org.carewebframework.api.spring.SpringUtil;
import org.carewebframework.security.spring.AbstractSecurityService;

/**
 * Mock Spring-based service implementation.
 */
public class MockSecurityService extends AbstractSecurityService {
    
    /**
     * Validates the current user's password.
     * 
     * @param password The password
     * @return True if the password is valid.
     */
    @Override
    public boolean validatePassword(final String password) {
        return password.equals(SpringUtil.getProperty("mock.password"));
    }
    
    /**
     * Changes the user's password.
     * 
     * @param oldPassword Current password.
     * @param newPassword New password.
     * @return Null or empty if succeeded. Otherwise, displayable reason why change failed.
     */
    @Override
    public String changePassword(final String oldPassword, final String newPassword) {
        return "Operation not supported";
    }
    
    /**
     * Return login disabled message.
     */
    @Override
    public String loginDisabled() {
        return null;
    }
    
}
