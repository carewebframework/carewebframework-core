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

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.math.NumberUtils;

import org.carewebframework.api.security.ISecurityDomain;
import org.carewebframework.api.spring.SpringUtil;
import org.carewebframework.security.spring.AbstractSecurityService;
import org.carewebframework.security.spring.Constants;

import org.zkoss.util.resource.Labels;

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
     * Generates a new random password Length of password dictated by
     * {@link Constants#LBL_PASSWORD_RANDOM_CHARACTER_LENGTH}
     * 
     * @return String The generated password
     */
    @Override
    public String generateRandomPassword() {
        int len = NumberUtils.toInt(Labels.getLabel(Constants.LBL_PASSWORD_RANDOM_CHARACTER_LENGTH), 12);
        return RandomStringUtils.random(len);
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
    
    public void setSecurityDomain(ISecurityDomain securityDomain) {
        registerSecurityDomain(securityDomain);
    }
}
