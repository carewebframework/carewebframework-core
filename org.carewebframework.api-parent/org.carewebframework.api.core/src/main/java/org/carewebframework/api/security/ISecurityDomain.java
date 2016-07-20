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
package org.carewebframework.api.security;

import java.io.Serializable;
import java.util.List;

import org.carewebframework.api.domain.IUser;

/**
 * Interface for a security domain. A security domain is responsible for providing user
 * authentication and authorization.
 */
public interface ISecurityDomain extends Serializable {
    
    /**
     * Returns the domain's name.
     * 
     * @return Domain's display name.
     */
    String getName();
    
    /**
     * Returns the logical identifier for the security domain.
     * 
     * @return Security domain logical identifier.
     */
    String getLogicalId();
    
    /**
     * Returns a named attribute for the security domain.
     * 
     * @param name The attribute name.
     * @return The attribute value, or null if not found.
     */
    String getAttribute(String name);
    
    /**
     * Authenticates a user.
     * 
     * @param username The username.
     * @param password The password.
     * @return The authenticated user, or null if authentication failed.
     */
    IUser authenticate(String username, String password);
    
    /**
     * Returns a list of granted authorities for a user.
     * 
     * @param user User whose granted authorities are sought.
     * @return A list of granted authorities (never null).
     */
    List<String> getGrantedAuthorities(IUser user);
    
    /**
     * Returns the native security domain object if this is a proxy.
     * 
     * @return The native security domain object.
     */
    Object getNativeSecurityDomain();
}
