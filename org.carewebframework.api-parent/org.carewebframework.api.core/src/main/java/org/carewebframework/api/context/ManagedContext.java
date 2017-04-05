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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.carewebframework.api.AppFramework;
import org.carewebframework.api.IRegisterEvent;
import org.carewebframework.api.context.ISurveyResponse.ISurveyCallback;
import org.carewebframework.api.context.SurveyResponse.ResponseState;
import org.carewebframework.api.event.IEventManager;
import org.carewebframework.api.event.IGenericEvent;
import org.carewebframework.common.StopWatchFactory;
import org.carewebframework.common.StopWatchFactory.IStopWatch;

/**
 * Base class for creating context objects. Descendant classes wrap domain objects such as patient
 * or user that represent a shared context for other objects to reference. By implementing the
 * IManagedContext interface, the management of this shared context is delegated to the
 * ContextManager class which orchestrates polling and notifying subscribers when changes in the
 * shared context occur.
 *
 * @param <DomainClass> Class of underlying domain object.
 */
public class ManagedContext<DomainClass> implements Comparable<IManagedContext<DomainClass>>, IRegisterEvent, IManagedContext<DomainClass> {
    
    private static final Log log = LogFactory.getLog(ManagedContext.class);
    
    private static final int CONTEXT_CURRENT = 0;
    
    private static final int CONTEXT_PENDING = 1;
    
    private final Object[] domainObject = new Object[2];
    
    private Class<? extends IContextEvent> eventInterface;
    
    private String contextName;
    
    private boolean isPending;
    
    private final List<IContextEvent> subscribers = new ArrayList<>();
    
    private final List<IContextEvent> surveyed = new ArrayList<>();
    
    protected IContextManager contextManager;
    
    protected IEventManager eventManager;
    
    protected AppFramework appFramework;
    
    protected ContextItems contextItems = new ContextItems();
    
    /**
     * Every managed context must specify a unique context name and the context change event
     * interface it supports.
     *
     * @param contextName Unique name for this context.
     * @param eventInterface The context change interface supported by this managed context.
     */
    protected ManagedContext(String contextName, Class<? extends IContextEvent> eventInterface) {
        this(contextName, eventInterface, null);
    }
    
    /**
     * Every managed context must specify a unique context name and the context change event
     * interface it supports.
     *
     * @param contextName Unique name for this context.
     * @param eventInterface The context change interface supported by this managed context.
     * @param initialContext The initial context state. May be null.
     */
    protected ManagedContext(String contextName, Class<? extends IContextEvent> eventInterface, DomainClass initialContext) {
        this.contextName = contextName;
        this.eventInterface = eventInterface;
        setPending(initialContext);
        commit(true);
    }
    
    /**
     * Extracts and returns the CCOW context from the specified domain object. Each subclass should
     * override this and supply their own implementation.
     *
     * @param domainObject The domain object.
     * @return Context items extracted from the domain object.
     */
    protected ContextItems toCCOWContext(DomainClass domainObject) {
        return contextItems;
    }
    
    /**
     * Creates a local context based on the specified CCOW context. Each subclass should override
     * this and supply their own implementation.
     *
     * @param contextItems Map containing CCOW-compliant context settings.
     * @return Instance of the domain object that matches the CCOW context. Return null if this is
     *         not supported or the CCOW context is not valid for this context object.
     */
    protected DomainClass fromCCOWContext(ContextItems contextItems) {
        return null;
    }
    
    /**
     * Sets the pending state to the specified domain object.
     *
     * @param domainObject The domain object.
     */
    protected void setPending(DomainClass domainObject) {
        this.domainObject[CONTEXT_PENDING] = domainObject;
        isPending = true;
    }
    
    /**
     * Compares whether two domain objects are the same. This is used to determine whether a context
     * change request really represents a different context. It may be overridden if the default
     * implementation is inadequate.
     *
     * @param domainObject1 First domain object for comparison.
     * @param domainObject2 Second domain object for comparison.
     * @return True if the two objects represent the same context.
     */
    protected boolean isSameContext(DomainClass domainObject1, DomainClass domainObject2) {
        return ObjectUtils.equals(domainObject1, domainObject2);
    }
    
    /**
     * Sets the context manager instance.
     *
     * @param contextManager The context manager.
     */
    public void setContextManager(IContextManager contextManager) {
        this.contextManager = contextManager;
    }
    
    /**
     * Sets the event manager instance.
     *
     * @param eventManager The event manager.
     */
    public void setEventManager(IEventManager eventManager) {
        this.eventManager = eventManager;
    }
    
    /**
     * Sets the application framework instance.
     *
     * @param appFramework The application framework.
     */
    public void setAppFramework(AppFramework appFramework) {
        this.appFramework = appFramework;
    }
    
    // ************************************************************************************************
    // * IManagedContext implementation
    // ***********************************************************************************************/
    
    /**
     * @see org.carewebframework.api.context.IManagedContext#commit(boolean)
     */
    @Override
    public void commit(boolean accept) {
        if (accept) {
            domainObject[CONTEXT_CURRENT] = domainObject[CONTEXT_PENDING];
        }
        
        domainObject[CONTEXT_PENDING] = null;
        isPending = false;
    }
    
    /**
     * @see org.carewebframework.api.context.IManagedContext#getContextItems(boolean)
     */
    @Override
    public ContextItems getContextItems(boolean pending) {
        contextItems.clear();
        DomainClass domainObject = getContextObject(pending);
        return domainObject == null ? contextItems : toCCOWContext(domainObject);
    }
    
    /**
     * @see org.carewebframework.api.context.IManagedContext#getContextName()
     */
    @Override
    public String getContextName() {
        return contextName;
    }
    
    /**
     * @see org.carewebframework.api.context.IManagedContext#isPending()
     */
    @Override
    public boolean isPending() {
        return isPending;
    }
    
    /**
     * @see org.carewebframework.api.context.IManagedContext#getPriority()
     */
    @Override
    public int getPriority() {
        return 0;
    }
    
    /**
     * @see org.carewebframework.api.context.IManagedContext#init()
     */
    @Override
    public void init() {
        reset();
    }
    
    /**
     * @see org.carewebframework.api.context.IManagedContext#reset()
     */
    @Override
    public void reset() {
        setPending(null);
    }
    
    /**
     * @see org.carewebframework.api.context.IManagedContext#setContextItems(org.carewebframework.api.context.ContextItems)
     */
    @Override
    public boolean setContextItems(ContextItems contextItems) {
        DomainClass domainObject = fromCCOWContext(contextItems);
        
        if (domainObject == null) {
            return false;
        }
        
        setPending(domainObject);
        return true;
    }
    
    /**
     * @see org.carewebframework.api.context.IManagedContext#addSubscriber(java.lang.Object)
     */
    @Override
    public boolean addSubscriber(IContextEvent subscriber) {
        if (!eventInterface.isInstance(subscriber)) {
            return false;
        }
        
        if (subscribers.contains(subscriber)) {
            return false;
        }
        
        subscribers.add(subscriber);
        return true;
    }
    
    /**
     * @see org.carewebframework.api.context.IManagedContext#addSubscribers(java.lang.Iterable)
     */
    @Override
    public boolean addSubscribers(Iterable<IContextEvent> subscribers) {
        boolean result = false;
        
        for (IContextEvent subscriber : subscribers) {
            result |= addSubscriber(subscriber);
        }
        
        return result;
    }
    
    /**
     * @see org.carewebframework.api.context.IManagedContext#removeSubscriber(java.lang.Object)
     */
    @Override
    public void removeSubscriber(IContextEvent subscriber) {
        if (eventInterface.isInstance(subscriber)) {
            subscribers.remove(subscriber);
            surveyed.remove(subscriber);
        }
    }
    
    /**
     * @see org.carewebframework.api.context.IManagedContext#removeSubscribers(java.lang.Iterable)
     */
    @Override
    public void removeSubscribers(Iterable<IContextEvent> subscribers) {
        for (IContextEvent subscriber : subscribers) {
            removeSubscriber(subscriber);
        }
    }
    
    /**
     * @see org.carewebframework.api.context.IManagedContext#notifySubscribers(boolean, boolean)
     */
    @Override
    public void notifySubscribers(boolean accept, boolean all) {
        Map<String, Object> map = null;
        
        if (log.isDebugEnabled() && StopWatchFactory.hasFactory()) {
            map = new HashMap<>();
            map.put("action", accept ? "committed" : "canceled");
            map.put("context", getContextName());
        }
        
        for (IContextEvent event : getIterable(all)) {
            IStopWatch sw = null;
            
            try {
                if (map != null) {
                    map.remove("exception");
                    map.put("subscriber", event.getClass().getName());
                    sw = StopWatchFactory.create("org.carewebframework.context.notifySubscribers", map);
                    
                    if (sw != null) {
                        sw.start();
                    }
                }
                
                if (accept) {
                    event.committed();
                } else {
                    event.canceled();
                }
                
            } catch (Throwable e) {
                log.error("Error during notifySubscribers.", e);
                
                if (map != null) {
                    map.put("exception", e.toString());
                }
            }
            
            if (sw != null) {
                sw.stop();
            }
        }
        
        surveyed.clear();
        
        if (accept) {
            eventManager.fireLocalEvent(getEventName(), getContextObject(false));
        }
    }
    
    /**
     * Returns a callback list that is safe for iteration.
     *
     * @param all If true, all callbacks are returned. If false, only callbacks for surveyed
     *            subscribers are returned.
     * @return Callback list.
     */
    private Iterable<IContextEvent> getIterable(boolean all) {
        return new ArrayList<>(all ? subscribers : surveyed);
    }
    
    /**
     * @see org.carewebframework.api.context.IManagedContext#surveySubscribers(boolean)
     */
    @Override
    public void surveySubscribers(boolean silent, ISurveyCallback callback) {
        SurveyResponse response = new SurveyResponse(silent);
        Iterator<IContextEvent> iter = getIterable(true).iterator();
        surveySubscribers(iter, response, callback);
    }
    
    private void surveySubscribers(Iterator<IContextEvent> iter, SurveyResponse response, ISurveyCallback callback) {
        if ((response.isSilent() || !response.rejected()) && iter.hasNext()) {
            IContextEvent subscriber = iter.next();
            
            response.reset(__ -> {
                surveySubscribers(iter, response, callback);
            });
            
            try {
                subscriber.pending(response);
            } catch (Throwable e) {
                log.error("Error during surveysubscribers.", e);
                response.reject(e.toString());
            }
            
            ResponseState state = response.getState();
            
            if (state != ResponseState.DEFERRED) {
                surveySubscribers(iter, response, callback);
            }
        } else if (callback != null) {
            callback.response(response);
        }
    }
    
    /**
     * Returns the name of the event fired after a successful context change.
     *
     * @return The name of the event fired after a successful context change.
     */
    private String getEventName() {
        return "CONTEXT.CHANGED." + getContextName();
    }

    @Override
    public void addListener(IGenericEvent<DomainClass> listener) {
        eventManager.subscribe(getEventName(), listener);
    }
    
    @Override
    public void removeListener(IGenericEvent<DomainClass> listener) {
        eventManager.unsubscribe(getEventName(), listener);
    }
    
    // ************************************************************************************************
    // * ISharedContext implementation
    // ***********************************************************************************************/
    
    /**
     * @see org.carewebframework.api.context.ISharedContext#requestContextChange(Object)
     */
    @Override
    public void requestContextChange(DomainClass newContextObject) throws ContextException {
        if (isSameContext(newContextObject, getContextObject(false))) {
            return;
        }
        
        if (isPending) {
            throw new ContextException("A context change is already pending.");
        }
        
        contextManager.localChangeBegin(this);
        domainObject[CONTEXT_PENDING] = newContextObject;
        isPending = true;
        contextManager.localChangeEnd(this, null);
    }
    
    /**
     * @see org.carewebframework.api.context.ISharedContext#getContextObject(boolean)
     */
    @Override
    @SuppressWarnings("unchecked")
    public DomainClass getContextObject(boolean pending) {
        return (DomainClass) domainObject[pending ? CONTEXT_PENDING : CONTEXT_CURRENT];
    }
    
    // ************************************************************************************************
    // * Comparable implementation
    // ***********************************************************************************************/
    
    /**
     * Compares by priority, with higher priorities collating first.
     */
    @Override
    public int compareTo(IManagedContext<DomainClass> o) {
        int pri1 = o.getPriority();
        int pri2 = getPriority();
        return this == o ? 0 : pri1 < pri2 ? -1 : 1;
    }
    
    // ************************************************************************************************
    // * IRegisterEvent implementation
    // ***********************************************************************************************/
    
    /**
     * Register an object as a subscriber if it implements the callback interface.
     *
     * @see org.carewebframework.api.IRegisterEvent#registerObject(Object)
     * @param object Object to register.
     */
    @Override
    public void registerObject(Object object) {
        if (object instanceof IContextEvent) {
            addSubscriber((IContextEvent) object);
        }
    }
    
    /**
     * Remove an object as a subscriber if it implements the callback interface.
     *
     * @see org.carewebframework.api.IRegisterEvent#unregisterObject(Object)
     * @param object Object to unregister.
     */
    @Override
    public void unregisterObject(Object object) {
        if (object instanceof IContextEvent) {
            removeSubscriber((IContextEvent) object);
        }
    }
    
}
