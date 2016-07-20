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
 * Every context object must implement this interface. The context manager uses this interface to
 * manage context changes.
 * 
 * @param <DomainClass> This represents the domain class that is wrapped by this managed context.
 */
public interface IManagedContext<DomainClass> extends ISharedContext<DomainClass> {
    
    /**
     * Commits or rejects the pending context change.
     * 
     * @param accept If true, the pending change is committed. If false, the pending change is
     *            canceled.
     */
    void commit(boolean accept);
    
    /**
     * Returns the CCOW context that corresponds to the current or pending context.
     * 
     * @param pending If true, use the pending context. If false, use the current context.
     * @return The requested context.
     */
    ContextItems getContextItems(boolean pending);
    
    /**
     * Returns the name associated with this context. If the context supports CCOW, this should
     * match the CCOW subject name that corresponds to this context.
     * 
     * @return The associated context name.
     */
    String getContextName();
    
    /**
     * Returns the priority that governs the sequence of context change commits when multiple
     * context changes are committed in the same transaction.
     * 
     * @return The context priority.
     */
    int getPriority();
    
    /**
     * Sets the context to an initial state.
     */
    void init();
    
    /**
     * Returns true if a context change is pending.
     * 
     * @return True if a context change is pending.
     */
    boolean isPending();
    
    /**
     * Sets the pending context to null.
     */
    void reset();
    
    /**
     * Set the pending context to match the specified CCOW context.
     * 
     * @param contextItems Map representing the CCOW context.
     * @return True if the pending context was successfully set.
     */
    boolean setContextItems(ContextItems contextItems);
    
    /**
     * Adds a context change subscriber to the subscription list for this context.
     * 
     * @param subscriber Object that is to subscribe to context changes.
     * @return True if the subscription request was successful.
     */
    boolean addSubscriber(Object subscriber);
    
    /**
     * Adds multiple context change subscribers.
     * 
     * @param subscribers List of subscribers to add.
     * @return True if at least one subscription request succeeded.
     */
    boolean addSubscribers(Iterable<Object> subscribers);
    
    /**
     * Removes a subscriber from the subscription list.
     * 
     * @param subscriber Object to be removed.
     */
    void removeSubscriber(Object subscriber);
    
    /**
     * Removes multiple context change subscribers.
     * 
     * @param subscribers List of subscribers to remove.
     */
    void removeSubscribers(Iterable<Object> subscribers);
    
    /**
     * Notify all subscribers of the context change outcome.
     * 
     * @param accept If true, context change was committed. If false, it was canceled.
     * @param all If false, notify only those subscribers who were polled. Otherwise, notify all
     *            subscribers.
     */
    void notifySubscribers(boolean accept, boolean all);
    
    /**
     * Survey all subscribers for context change response.
     * 
     * @param silent If true, subscribers should not request user interaction and all subscribers
     *            will be surveyed regardless of response. If false, a subscriber may request user
     *            interaction and the first nonempty response will terminate the polling.
     * @return Result of survey. If empty string, all subscribers acquiesced to the context change
     *         request.
     */
    String surveySubscribers(boolean silent);
}
