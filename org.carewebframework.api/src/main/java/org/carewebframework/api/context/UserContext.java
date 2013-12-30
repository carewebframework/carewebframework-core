/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.api.context;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.carewebframework.api.domain.IInstitution;
import org.carewebframework.api.domain.IUser;

/**
 * Wrapper for shared user context.
 */
public class UserContext extends ManagedContext<IUser> {
    
    private static final Log log = LogFactory.getLog(UserContext.class);
    
    private static final String SUBJECT_NAME = "User";
    
    private static final String CCOW_USERNM = SUBJECT_NAME + ".Id.Logon";
    
    private static final String CCOW_FULLNAME = SUBJECT_NAME + ".Co.Name";
    
    // This is the interface that every user context subscriber must implement.
    public interface IUserContextEvent extends IContextEvent {};
    
    /**
     * Request a user context change.
     * 
     * @param user New user
     */
    public static void changeUser(IUser user) {
        try {
            getUserContext().requestContextChange(user);
        } catch (Exception e) {
            log.error("Error during user context change.", e);
        }
    }
    
    /**
     * Returns the managed user context.
     * 
     * @return IDomainUser context
     */
    @SuppressWarnings("unchecked")
    public static ISharedContext<IUser> getUserContext() {
        return (ISharedContext<IUser>) ContextManager.getInstance().getSharedContext(UserContext.class.getName());
    }
    
    /**
     * Returns the user in the current context.
     * 
     * @return IDomainUser
     */
    public static IUser getActiveUser() {
        return getUserContext().getContextObject(false);
    }
    
    /**
     * Returns the institution of the active user, or null if no active user.
     * 
     * @return Institution
     */
    public static IInstitution getInstitution() {
        IUser user = getActiveUser();
        return user == null ? null : user.getInstitution();
    }
    
    /**
     * Create a shared user context with an initial null state.
     */
    public UserContext() {
        this(null);
    }
    
    /**
     * Create a shared user context with a specified initial state.
     * 
     * @param user User that will be the initial state.
     */
    public UserContext(IUser user) {
        super(SUBJECT_NAME, IUserContextEvent.class, user);
    }
    
    /**
     * Creates a CCOW context from the specified user object.
     */
    @Override
    public ContextItems toCCOWContext(IUser user) {
        contextItems.setItem(CCOW_USERNM, user.getUsername());
        contextItems.setItem(CCOW_FULLNAME, user.getFullName());
        return contextItems;
    }
    
    /**
     * Returns a user object based on the specified CCOW context.
     */
    @Override
    public IUser fromCCOWContext(ContextItems contextItems) {
        if (contextItems == null) {
            return null;
        }
        
        IUser user = null; //TODO: finish
        return user;
    }
    
    /**
     * Returns a priority value of -100. Among concurrent context change transactions, a user
     * context change should generally occur last, therefore it has a negative priority value.
     * 
     * @return Priority value for context manager.
     */
    @Override
    public int getPriority() {
        return -100;
    }
    
}
