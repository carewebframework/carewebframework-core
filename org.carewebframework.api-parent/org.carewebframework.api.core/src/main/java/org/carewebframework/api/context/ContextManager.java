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
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.carewebframework.api.AppFramework;
import org.carewebframework.api.IRegisterEvent;
import org.carewebframework.api.context.CCOWContextManager.CCOWState;
import org.carewebframework.api.context.ISurveyResponse.IResponseCallback;
import org.carewebframework.api.event.IEventManager;
import org.carewebframework.api.security.IDigitalSignature;
import org.carewebframework.api.spring.SpringUtil;

/**
 * Manages context objects and mediates context change requests on their behalf. Context objects are
 * special services that maintain information about different shared context states (user, patient,
 * etc.) Each context object must implement the IManagedContext interface which is used by the
 * context manager to interact with the context object. Furthermore, each context object must
 * declare an extension of the IContextEvent interface which will be implemented by subscribers to
 * process context change events.
 */
public class ContextManager implements IContextManager, CCOWContextManager.ICCOWContextEvent, IRegisterEvent {
    
    private static final Log log = LogFactory.getLog(ContextManager.class);
    
    private final Set<IManagedContext<?>> managedContexts = new TreeSet<>();
    
    private final Stack<IManagedContext<?>> pendingStack = new Stack<>();
    
    private Stack<IManagedContext<?>> commitStack = new Stack<>();
    
    private CCOWContextManager ccowContextManager;
    
    private final ContextItems contextItems = new ContextItems();
    
    private boolean ccowEnabled;
    
    private boolean ccowTransaction;
    
    private IEventManager eventManager;
    
    private AppFramework appFramework;
    
    /**
     * Represents the different CCOW statuses that can be represented by the official CCOW status
     * icons.
     */
    private enum CCOWStatus {
        NONE, DISABLED, CHANGING, JOINED, BROKEN
    }
    
    /**
     * Accumulate response values in string buffer.
     * 
     * @param buffer StringBuilder to which to append response
     * @param response Appended to buffer
     */
    public static void appendResponse(StringBuilder buffer, String response) {
        if (response != null && !response.isEmpty()) {
            if (buffer.length() > 0) {
                buffer.append("\r\n");
            }
            
            buffer.append(response);
        }
    }
    
    /**
     * Returns the context manager for this application context.
     * 
     * @return IContextManager
     */
    public static IContextManager getInstance() {
        return SpringUtil.getBean("contextManager", IContextManager.class);
    }
    
    /**
     * Set the event manager instance.
     * 
     * @param eventManager IEventManager
     */
    public void setEventManager(IEventManager eventManager) {
        this.eventManager = eventManager;
    }
    
    /**
     * Set the application framework instance.
     * 
     * @param appFramework AppFramework
     */
    public void setAppFramework(AppFramework appFramework) {
        this.appFramework = appFramework;
    }
    
    /**
     * Checks if a CCOW context manager is active.
     * 
     * @return True if CCOW context management is active.
     */
    private boolean ccowIsActive() {
        return ccowContextManager != null && ccowContextManager.isActive();
    }
    
    /**
     * Joins the CCOW common context, if available.
     */
    public void ccowJoin() {
        if (ccowIsActive()) {
            return;
        }
        
        if (ccowContextManager == null && ccowEnabled) {
            ccowContextManager = new CCOWContextManager();
            ccowContextManager.subscribe(this);
            ccowContextManager.run("CareWebFramework#", "", true, "*");
        }
        
        if (ccowContextManager != null) {
            if (!ccowContextManager.isActive()) {
                ccowContextManager.resume();
            }
            
            init(response -> {
                if (response.rejected()) {
                    ccowContextManager.suspend();
                }
                
                updateCCOWStatus();
            });
        }
    }
    
    /**
     * Leave the CCOW common context.
     */
    public void ccowLeave() {
        if (ccowContextManager != null && ccowContextManager.getState() != CCOWState.csSuspended) {
            ccowContextManager.suspend();
        }
    }
    
    /**
     * Initialize context objects to their default state. The default state should match the CCOW
     * context if one exists, or an initial state as determined by the context object.
     * 
     * @param callback Callback to report subscriber responses.
     * @see {@link #init(IManagedContext)}
     */
    public void init(IResponseCallback callback) {
        init(null, callback);
    }
    
    /**
     * Initializes one or all managed contexts to their default state.
     * 
     * @param item Managed context to initialize or, if null, initializes all managed contexts.
     * @param callback Callback to report subscriber responses.
     * @see {@link #init(IManagedContext)}
     */
    public void init(IManagedContext<?> item, IResponseCallback callback) {
        contextItems.clear();
        
        if (ccowIsActive()) {
            contextItems.addItems(ccowContextManager.getCCOWContext());
        }
        
        if (item != null) {
            initItem(item, callback);
        } else {
            SurveyResponse response = new SurveyResponse();
            initItem(managedContexts.iterator(), response, callback);
        }
    }
    
    private void initItem(Iterator<IManagedContext<?>> iter, SurveyResponse response, IResponseCallback callback) {
        if (iter.hasNext()) {
            IManagedContext<?> managedContext = iter.next();
            
            initItem(managedContext, aresponse -> {
                response.merge(aresponse);
                initItem(iter, response, callback);
            });
        } else if (callback != null) {
            callback.response(response);
        }
    }
    
    /**
     * Initializes the managed context.
     * 
     * @param item Managed context to initialize.
     * @param callback Callback to report subscriber responses.
     */
    private void initItem(IManagedContext<?> item, IResponseCallback callback) {
        try {
            localChangeBegin(item);
            
            if (hasSubject(item.getContextName())) {
                item.setContextItems(contextItems);
            } else {
                item.init();
            }
            
            localChangeEnd(item, callback);
        } catch (ContextException e) {
            log.error("Error initializing context.", e);
            execCallback(callback, new SurveyResponse(e.toString()));
        }
    }
    
    /**
     * Returns true if the CCOW context contains context settings pertaining to the named subject.
     * 
     * @param subject Name of the CCOW subject
     * @return True if settings for the specified subject were found.
     */
    private boolean hasSubject(String subject) {
        boolean result = false;
        String s = subject + ".";
        int c = s.length();
        
        for (String propName : contextItems.getItemNames()) {
            result = s.equalsIgnoreCase(propName.substring(0, c));
            
            if (result) {
                break;
            }
        }
        
        return result;
    }
    
    /**
     * Commit or cancel all pending context changes.
     * 
     * @param accept If true, pending changes are committed. If false, they are canceled.
     * @param all If false, only polled subscribers are notified.
     */
    private void commitContexts(boolean accept, boolean all) {
        Stack<IManagedContext<?>> stack = commitStack;
        commitStack = new Stack<IManagedContext<?>>();
        // First, commit or cancel all pending context changes.
        for (IManagedContext<?> managedContext : stack) {
            if (managedContext.isPending()) {
                managedContext.commit(accept);
            }
        }
        // Then notify subscribers of the changes.
        while (!stack.isEmpty()) {
            stack.pop().notifySubscribers(accept, all);
        }
    }
    
    /**
     * Surveys all CCOW participants.
     * 
     * @param all If true, survey all participants.
     * @return True if context change may proceed.
     */
    @SuppressWarnings("unused")
    private boolean surveyCCOW(boolean all) {
        return true;
        // TODO: finish
    }
    
    /**
     * Sets the committed state of all context objects based on the marshaled context.
     * 
     * @param marshaledContext The marshaled context to process.
     * @param callback Callback to report subscriber responses.
     */
    public void setMarshaledContext(ContextItems marshaledContext, IResponseCallback callback) {
        setMarshaledContext(marshaledContext, true, callback);
    }
    
    /**
     * Updates managed contexts based on the marshaledContext.
     * 
     * @param marshaledContext The marshaled context to process.
     * @param commit If true the pending contexts are committed.
     * @param callback Callback to report subscriber responses.
     */
    /*package*/void setMarshaledContext(ContextItems marshaledContext, boolean commit, IResponseCallback callback) {
        ISurveyResponse response = new SurveyResponse();
        Iterator<IManagedContext<?>> iter = managedContexts.iterator();
        
        setMarshaledContext(marshaledContext, iter, response, __ -> {
            if (commit) {
                commitContexts(!response.rejected(), false);
            }
        });
    }
    
    private void setMarshaledContext(ContextItems marshaledContext, Iterator<IManagedContext<?>> iter,
                                     ISurveyResponse response, IResponseCallback callback) {
        if (iter.hasNext()) {
            IManagedContext<?> managedContext = iter.next();
            
            try {
                if (managedContext.setContextItems(marshaledContext)) {
                    localChangeBegin(managedContext);
                    localChangeEnd(managedContext, true, true, aresponse -> {
                        response.merge(aresponse);
                        setMarshaledContext(marshaledContext, iter, response, callback);
                    });
                }
            } catch (Exception e) {
                log.error("Error processing marshaled context change.", e);
                response.reject(e.toString());
                setMarshaledContext(marshaledContext, iter, response, callback);
            }
        } else {
            execCallback(callback, response);
        }
    }
    
    /**
     * Enables or disables CCOW support.
     * 
     * @param ccowEnabled True enables CCOW support if it is available.
     */
    public void setCCOWEnabled(boolean ccowEnabled) {
        this.ccowEnabled = ccowEnabled;
        
        if (!ccowEnabled && ccowContextManager != null) {
            ccowContextManager.suspend();
            ccowContextManager = null;
        }
        
        updateCCOWStatus();
    }
    
    /**
     * Returns the marshaled context representing the state of all shared contexts.
     * 
     * @return A ContextItems object representing the current state of all shared contexts.
     */
    public ContextItems getMarshaledContext() {
        ContextItems marshaledContext = new ContextItems();
        
        for (IManagedContext<?> managedContext : managedContexts) {
            marshaledContext.addItems(managedContext.getContextItems(false));
        }
        
        return marshaledContext;
    }
    
    /**
     * Returns the current status of the CCOW common context. Return values correspond to possible
     * states of the standard CCOW status icon.
     * 
     * @return Status of the CCOW common context.
     */
    private CCOWStatus getCCOWStatus() {
        if (ccowContextManager == null) {
            return ccowEnabled ? CCOWStatus.NONE : CCOWStatus.DISABLED;
        } else if (ccowTransaction) {
            return CCOWStatus.CHANGING;
        } else {
            switch (ccowContextManager.getState()) {
                case csParticipating:
                    return CCOWStatus.JOINED;
                case csSuspended:
                    return CCOWStatus.BROKEN;
                default:
                    return CCOWStatus.NONE;
            }
        }
    }
    
    /**
     * Notifies subscribers of a change in the CCOW status via a generic event.
     */
    private void updateCCOWStatus() {
        if (ccowEnabled && eventManager != null) {
            eventManager.fireLocalEvent("CCOW", Integer.toString(getCCOWStatus().ordinal()));
        }
    }
    
    // ************************************************************************************************
    // * IRegisterEvent implementation
    // ***********************************************************************************************/
    
    /**
     * Register an object with the context manager if it implements the IManagedContext interface.
     * 
     * @param object Object to register.
     */
    @Override
    public void registerObject(Object object) {
        if (object instanceof IManagedContext) {
            managedContexts.add((IManagedContext<?>) object);
        }
    }
    
    /**
     * Unregister an object from the context manager.
     * 
     * @param object Object to unregister.
     */
    @Override
    public void unregisterObject(Object object) {
        if (object instanceof IContextEvent) {
            for (IManagedContext<?> managedContext : managedContexts) {
                managedContext.removeSubscriber(object);
            }
        }
        
        if (object instanceof IManagedContext) {
            managedContexts.remove(object);
        }
    }
    
    // ************************************************************************************************
    // * IContextManager implementation
    // ***********************************************************************************************/
    
    /**
     * @see org.carewebframework.api.context.IContextManager#localChangeBegin
     */
    @Override
    public void localChangeBegin(IManagedContext<?> managedContext) throws ContextException {
        if (pendingStack.contains(managedContext) || commitStack.contains(managedContext)) {
            throw new ContextException("Circular context change detected.");
        }
        
        pendingStack.push(managedContext);
    }
    
    /**
     * @see org.carewebframework.api.context.IContextManager#localChangeEnd
     */
    @Override
    public void localChangeEnd(IManagedContext<?> managedContext, IResponseCallback callback) throws ContextException {
        localChangeEnd(managedContext, false, false, callback);
    }
    
    /**
     * Commits a pending context change.
     * 
     * @param managedContext The managed context of interest.
     * @param silent If true, this is a silent context change.
     * @param deferCommit If true, don't commit the context change, just survey subscribers.
     * @param response Holds the response(s) returned by subscribers.
     * @throws ContextException during illegal context change nesting
     */
    private void localChangeEnd(IManagedContext<?> managedContext, boolean silent, boolean deferCommit,
                                IResponseCallback callback) throws ContextException {
        
        if (pendingStack.isEmpty() || pendingStack.peek() != managedContext) {
            throw new ContextException("Illegal context change nesting.");
        }
        
        if (!managedContext.isPending()) {
            pendingStack.pop();
            return;
        }
        
        commitStack.push(managedContext);
        managedContext.surveySubscribers(silent, response -> {
            boolean accept = !response.rejected();
            
            if (!accept && log.isDebugEnabled()) {
                log.debug("Survey of managed context " + managedContext.getContextName() + " returned '" + response + "'.");
            }
            
            pendingStack.remove(managedContext);
            
            if (!deferCommit && (!accept || pendingStack.isEmpty())) {
                commitContexts(accept, accept);
            }
            
            execCallback(callback, response);
        });
    }
    
    private void execCallback(IResponseCallback callback, ISurveyResponse response) {
        if (callback != null) {
            callback.response(response);
        }
    }
    
    private void execCallback(IResponseCallback callback, Exception e) {
        execCallback(callback, new SurveyResponse(e.toString()));
    }
    
    /**
     * @see org.carewebframework.api.context.IContextManager#getSharedContexts
     */
    @Override
    public List<ISharedContext<?>> getSharedContexts() {
        return new ArrayList<ISharedContext<?>>(managedContexts);
    }
    
    /**
     * @see org.carewebframework.api.context.IContextManager#getSharedContext
     */
    @Override
    public ISharedContext<?> getSharedContext(String className) {
        try {
            Class<?> contextClass = Class.forName(className);
            
            for (ISharedContext<?> sharedContext : managedContexts) {
                if (contextClass.isInstance(sharedContext)) {
                    return sharedContext;
                }
            }
            
            IManagedContext<?> ctx = (IManagedContext<?>) contextClass.newInstance();
            registerObject(ctx);
            return ctx;
        } catch (Exception e) {
            throw new ContextException(e.getMessage());
        }
    }
    
    /**
     * @see org.carewebframework.api.context.IContextManager#getContextMarshaller
     */
    @Override
    public ContextMarshaller getContextMarshaller(String keyStoreName) throws ContextException {
        try {
            return new ContextMarshaller(
                    appFramework.getApplicationContext().getBean(keyStoreName, IDigitalSignature.class));
        } catch (Exception e) {
            throw new ContextException(
                    "An exception occurred while trying to access a context marshaller for the specified key store.", e);
        }
    }
    
    /**
     * @see org.carewebframework.api.context.IContextManager#reset
     */
    @Override
    public void reset(boolean silent, IResponseCallback callback) {
        pendingStack.clear();
        commitStack.clear();
        SurveyResponse response = new SurveyResponse();
        Iterator<IManagedContext<?>> iter = managedContexts.iterator();
        
        reset(silent, iter, response, __ -> {
            boolean commit = silent || !response.rejected();
            commitContexts(commit, commit);
            execCallback(callback, response);
        });
        
    }
    
    private void reset(boolean silent, Iterator<IManagedContext<?>> iter, SurveyResponse response,
                       IResponseCallback callback) {
        if (iter.hasNext()) {
            IManagedContext<?> managedContext = iter.next();
            resetItem(managedContext, silent, aresponse -> {
                response.merge(aresponse);
                
                if (silent || !response.rejected()) {
                    reset(silent, iter, response, callback);
                } else {
                    execCallback(callback, response);
                }
            });
        } else {
            execCallback(callback, response);
        }
    }
    
    /**
     * Resets the managed context.
     * 
     * @param item Managed context to reset.
     * @param silent Silent flag.
     */
    private void resetItem(IManagedContext<?> item, boolean silent, IResponseCallback callback) {
        try {
            localChangeBegin(item);
            item.reset();
            localChangeEnd(item, silent, true, callback);
        } catch (ContextException e) {
            execCallback(callback, e);
        }
    }
    
    // ************************************************************************************************
    // * ICCOWContextEvent implementation
    // ***********************************************************************************************/
    
    /**
     * Callback to handle canceled context change request from the CCOW context manager.
     */
    @Override
    public void ccowCanceled(CCOWContextManager sender) {
        commitContexts(false, false);
    }
    
    /**
     * Callback to handle committed context change request from the CCOW context manager.
     */
    @Override
    public void ccowCommitted(CCOWContextManager sender) {
        commitContexts(true, false);
    }
    
    /**
     * Callback to handle a polling request from the CCOW context manager.
     */
    @Override
    public void ccowPending(CCOWContextManager sender, ContextItems contextItems) {
        ccowTransaction = true;
        updateCCOWStatus();
        setMarshaledContext(contextItems, false, response -> {
            if (response.rejected()) {
                sender.setSurveyResponse(response.toString());
            }
            
            ccowTransaction = false;
            updateCCOWStatus();
        });
    }
    
}
