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

/**
 * Class for future use to encapsulate CCOW context management capability.
 */
public class CCOWContextManager {
    
    private final ContextItems contextItems = new ContextItems();
    
    public interface ICCOWContextEvent {
        
        void ccowPending(CCOWContextManager sender, ContextItems contextItems);
        
        void ccowCommitted(CCOWContextManager sender);
        
        void ccowCanceled(CCOWContextManager sender);
    }
    
    public enum CCOWState {
        csUnknown, csParticipating, csSuspended
    };
    
    /**
     * Returns true if actively participating in the CCOW context.
     * 
     * @return True if actively participating.
     */
    public boolean isActive() {
        return getState() == CCOWState.csParticipating;
    }
    
    /**
     * Initializes the context manager.
     * 
     * @param appId
     * @param passCode
     * @param join
     * @param contextFilter
     */
    public void run(String appId, String passCode, boolean join, String contextFilter) {
        
    }
    
    /**
     * Temporarily suspends participation.
     */
    public void suspend() {
        
    }
    
    /**
     * Resumes a suspended participation.
     */
    public void resume() {
        
    }
    
    /**
     * Add a CCOW context subscriber.
     * 
     * @param subscriber
     */
    public void subscribe(ICCOWContextEvent subscriber) {
        
    }
    
    /**
     * Remove a CCOW context subscriber.
     * 
     * @param subscriber
     */
    public void unsubscribe(ICCOWContextEvent subscriber) {
        
    }
    
    /**
     * Return the current participation state.
     * 
     * @return The current participation state.
     */
    public CCOWState getState() {
        return CCOWState.csUnknown;
    }
    
    /**
     * Set the CCOW survey response.
     * 
     * @param reason
     */
    public void setSurveyResponse(String reason) {
        
    }
    
    /**
     * Return items in the CCOW context.
     * 
     * @return Context items.
     */
    public ContextItems getCCOWContext() {
        return contextItems;
    }
}
