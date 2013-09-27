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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.carewebframework.api.AppFramework;
import org.carewebframework.api.IRegisterEvent;
import org.carewebframework.api.event.IEventManager;
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
public class ManagedContext<DomainClass> implements Comparable<IManagedContext>, IRegisterEvent, IManagedContext, ISharedContext<DomainClass> {
    
    private static final Log log = LogFactory.getLog(ManagedContext.class);
    
    private static final int CONTEXT_CURRENT = 0;
    
    private static final int CONTEXT_PENDING = 1;
    
    private final Object[] domainObject = new Object[2];
    
    private Class<? extends IContextEvent> eventInterface;
    
    private String contextName;
    
    private boolean isPending;
    
    private final List<IContextEvent> subscribers = new ArrayList<IContextEvent>();
    
    private final List<IContextEvent> surveyed = new ArrayList<IContextEvent>();
    
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
     * @param domainObject
     * @return
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
     * @param domainObject
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
     * @param contextManager
     */
    public void setContextManager(IContextManager contextManager) {
        this.contextManager = contextManager;
    }
    
    /**
     * Sets the event manager instance.
     * 
     * @param eventManager
     */
    public void setEventManager(IEventManager eventManager) {
        this.eventManager = eventManager;
    }
    
    /**
     * Sets the application framework instance.
     * 
     * @param appFramework
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
    public boolean addSubscriber(Object subscriber) {
        if (!eventInterface.isInstance(subscriber)) {
            return false;
        }
        
        IContextEvent event = (IContextEvent) subscriber;
        
        if (subscribers.contains(event)) {
            return false;
        }
        
        subscribers.add(event);
        return true;
    }
    
    /**
     * @see org.carewebframework.api.context.IManagedContext#addSubscribers(java.lang.Iterable)
     */
    @Override
    public boolean addSubscribers(Iterable<Object> subscribers) {
        boolean result = false;
        
        for (Object subscriber : subscribers) {
            result |= addSubscriber(subscriber);
        }
        
        return result;
    }
    
    /**
     * @see org.carewebframework.api.context.IManagedContext#removeSubscriber(java.lang.Object)
     */
    @Override
    public void removeSubscriber(Object subscriber) {
        if (eventInterface.isInstance(subscriber)) {
            subscribers.remove(subscriber);
            surveyed.remove(subscriber);
        }
    }
    
    /**
     * @see org.carewebframework.api.context.IManagedContext#removeSubscribers(java.lang.Iterable)
     */
    @Override
    public void removeSubscribers(Iterable<Object> subscribers) {
        for (Object subscriber : subscribers) {
            removeSubscriber(subscriber);
        }
    }
    
    /**
     * @see org.carewebframework.api.context.IManagedContext#notifySubscribers(boolean, boolean)
     */
    @Override
    public void notifySubscribers(boolean accept, boolean all) {
        Map<String, Object> map = null;
        
        if (log.isDebugEnabled()) {
            map = new HashMap<String, Object>();
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
            eventManager.fireLocalEvent("CONTEXT.CHANGED." + getContextName(), getContextObject(false));
        }
    }
    
    /**
     * Returns a callback list that is safe for iteration.
     * 
     * @param all If true, all callbacks are returned. If false, only callbacks for surveyed
     *            subscribers are returned.
     * @return
     */
    private Iterable<IContextEvent> getIterable(boolean all) {
        return new ArrayList<IContextEvent>(all ? subscribers : surveyed);
    }
    
    /**
     * @see org.carewebframework.api.context.IManagedContext#surveySubscribers(boolean)
     */
    @Override
    public String surveySubscribers(boolean silent) {
        StringBuilder result = new StringBuilder();
        
        for (IContextEvent event : getIterable(true)) {
            try {
                ContextManager.appendResponse(result, event.pending(silent));
            } catch (Throwable e) {
                log.error("Error during surveysubscribers.", e);
                ContextManager.appendResponse(result, e.toString());
            }
            
            surveyed.add(event); // Add to list of surveyed subscribers.
            
            if (!silent && result.length() > 0) {
                break;
            }
        }
        
        return result.toString();
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
        contextManager.localChangeEnd(this);
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
    public int compareTo(IManagedContext o) {
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
     * @param object
     */
    @Override
    public void registerObject(Object object) {
        addSubscriber(object);
    }
    
    /**
     * Remove an object as a subscriber if it implements the callback interface.
     * 
     * @see org.carewebframework.api.IRegisterEvent#unregisterObject(Object)
     * @param object
     */
    @Override
    public void unregisterObject(Object object) {
        removeSubscriber(object);
    }
    
}
