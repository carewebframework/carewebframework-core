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
     * @param appId The application id.
     * @param passCode The pass code.
     * @param join Whether to join the common context.
     * @param contextFilter The context filter.
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
     * @param subscriber Context subscriber to add.
     */
    public void subscribe(ICCOWContextEvent subscriber) {
        
    }
    
    /**
     * Remove a CCOW context subscriber.
     * 
     * @param subscriber Context subscriber to remove.
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
     * @param reason Survey response.
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
