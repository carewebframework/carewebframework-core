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
    public boolean validatePassword(String password) {
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
    public String changePassword(String oldPassword, String newPassword) {
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
