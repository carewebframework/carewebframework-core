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

import org.carewebframework.api.domain.IDomainObject;

/**
 * Wrapper for shared user context.
 */
public class UserContext extends ManagedContext<IDomainObject> {
    
    private static final Log log = LogFactory.getLog(UserContext.class);
    
    protected static final String SUBJECT_NAME = "User";
    
    protected static final String CCOW_USERNM = SUBJECT_NAME + ".Id.Logon";
    
    protected static final String CCOW_FULLNAME = SUBJECT_NAME + ".Co.Name";
    
    // This is the interface that every user context subscriber must implement.
    public interface IUserContextEvent extends IContextEvent {};
    
    /**
     * Request a user context change.
     * 
     * @param user New user
     */
    public static void changeUser(IDomainObject user) {
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
    public static ISharedContext<IDomainObject> getUserContext() {
        return (ISharedContext<IDomainObject>) ContextManager.getInstance().getSharedContext(UserContext.class.getName());
    }
    
    /**
     * Returns the user in the current context.
     * 
     * @return User in current context.
     */
    public static IDomainObject getActiveUser() {
        return getUserContext().getContextObject(false);
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
    public UserContext(IDomainObject user) {
        super(SUBJECT_NAME, IUserContextEvent.class, user);
    }
    
    /**
     * Creates a CCOW context from the specified user object.
     */
    @Override
    public ContextItems toCCOWContext(IDomainObject user) {
        //contextItems.setItem(CCOW_USERNM, user.getUsername());
        //contextItems.setItem(CCOW_FULLNAME, user.getFullName());
        return contextItems;
    }
    
    /**
     * Returns a user object based on the specified CCOW context.
     */
    @Override
    public IDomainObject fromCCOWContext(ContextItems contextItems) {
        if (contextItems == null) {
            return null;
        }
        
        IDomainObject user = null; //TODO: finish
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
