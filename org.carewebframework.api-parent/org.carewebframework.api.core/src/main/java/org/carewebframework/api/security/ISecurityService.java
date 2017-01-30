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

import org.carewebframework.api.domain.IUser;

/**
 * Interface implemented by the security service.
 */
public interface ISecurityService {
    
    static final String ALIAS_TYPE_AUTHORITY = "AUTHORITY";
    
    /**
     * Logout out the current desktop instance.
     * 
     * @param force If true, force logout without user interaction.
     * @param target Optional target url for next login.
     * @param message Optional message to indicate reason for logout.
     */
    void logout(boolean force, String target, String message);
    
    /**
     * Validates the current user's password.
     * 
     * @param password Password to validate.
     * @return True if the password is valid.
     */
    boolean validatePassword(String password);
    
    /**
     * Changes the user's password.
     * 
     * @param oldPassword Current password.
     * @param newPassword New password.
     * @return Null or empty if succeeded. Otherwise, displayable reason why change failed.
     */
    String changePassword(String oldPassword, String newPassword);
    
    /**
     * Invokes change password dialog.
     */
    void changePassword();
    
    /**
     * Returns true if the user can change password.
     * 
     * @return True if the user can change password.
     */
    boolean canChangePassword();
    
    /**
     * Generates a random password.
     * 
     * @return The randomly generated password.
     */
    String generateRandomPassword();
    
    /**
     * Register an alias for an authority.
     * 
     * @param authority String representation of an authority.
     * @param alias String representation of an authority alias. If null, removes an existing alias.
     */
    void setAuthorityAlias(String authority, String alias);
    
    /**
     * Returns whether the current context has authenticated
     * 
     * @return boolean true if Authentication token is found and is not an Anonymous User
     */
    boolean isAuthenticated();
    
    /**
     * Returns the authenticated user object from the current security context.
     * 
     * @return The authenticated user object, or null if none present.
     */
    IUser getAuthenticatedUser();
    
    /**
     * Returns true if the Authentication object is granted debug privilege.
     * 
     * @return True if authenticated principal is granted a debug privilege.
     */
    boolean hasDebugRole();
    
    /**
     * Returns true if the Authentication object has the specified <code>authority</code>
     * <p>
     * <i>Note:</i>Privileges are prefixed with "PRIV_" and roles are prefixed with "ROLE_"
     * </p>
     * 
     * @param authority String representation of an authority
     * @return boolean true if found
     */
    public boolean isGranted(String authority);
    
    /**
     * Checks the current SecurityContext for the specified authorities.
     * 
     * @param authorities Comma-delimited string of granted authorities
     * @param requiresAll If true, all authorities must be granted. If false, only one of the listed
     *            authorities must be granted.
     * @return True if Authentication is granted authorities
     */
    public boolean isGranted(String authorities, boolean requiresAll);
    
    /**
     * Returns a non-null value if logins are disabled.
     * 
     * @return Returns the login disabled message if logins are disabled; null otherwise.
     */
    public String loginDisabled();
    
}
