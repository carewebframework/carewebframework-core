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
package org.carewebframework.api.domain;

import java.io.Serializable;

import org.carewebframework.api.security.ISecurityDomain;

/**
 * Interface for a user.
 */
public interface IUser extends Serializable {
    
    /**
     * Returns the logical identifier for the user.
     * 
     * @return User logical identifier.
     */
    String getLogicalId();
    
    /**
     * Returns the user's full name.
     * 
     * @return User's full name.
     */
    String getFullName();
    
    /**
     * Return the user's login name.
     * 
     * @return User's login name.
     */
    String getLoginName();
    
    /**
     * Return the user's password.
     * 
     * @return User's password.
     */
    String getPassword();
    
    /**
     * Returns the user's security domain.
     * 
     * @return User's security domain.
     */
    ISecurityDomain getSecurityDomain();
    
    /**
     * Returns the native user object if this is a proxy.
     * 
     * @return The native user object.
     */
    Object getNativeUser();
}
