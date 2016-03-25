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
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.carewebframework.api.AppFramework;
import org.carewebframework.api.IRegisterEvent;
import org.carewebframework.api.context.CCOWContextManager.CCOWState;
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
        none, disabled, changing, joined, broken
    };
    
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
     * 
     * @return True if the operation was successful.
     */
    public boolean ccowJoin() {
        if (ccowIsActive()) {
            return true;
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
            
            if (!init()) {
                ccowContextManager.suspend();
            }
        }
        
        updateCCOWStatus();
        return ccowIsActive();
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
     * @return {@link #init(IManagedContext)}
     */
    public boolean init() {
        return init(null);
    }
    
    /**
     * Initializes one or all managed contexts to their default state.
     * 
     * @param item Managed context to initialize or, if null, initializes all managed contexts.
     * @return True if the operation was successful.
     */
    public boolean init(IManagedContext<?> item) {
        contextItems.clear();
        boolean result = true;
        
        if (ccowIsActive()) {
            contextItems.addItems(ccowContextManager.getCCOWContext());
        }
        
        if (item != null) {
            result = initItem(item);
        } else {
            for (IManagedContext<?> managedContext : managedContexts) {
                result &= initItem(managedContext);
            }
        }
        return result;
    }
    
    /**
     * Initializes the managed context.
     * 
     * @param item Managed context to initialize.
     * @return True if change was accepted.
     */
    private boolean initItem(IManagedContext<?> item) {
        try {
            localChangeBegin(item);
            
            if (hasSubject(item.getContextName())) {
                item.setContextItems(contextItems);
            } else {
                item.init();
            }
            
            return StringUtils.isEmpty(localChangeEnd(item));
        } catch (ContextException e) {
            log.error("Error initializing context.", e);
            return false;
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
     * @return Reason if context change rejected.
     */
    public String setMarshaledContext(ContextItems marshaledContext) {
        return setMarshaledContext(marshaledContext, true);
    }
            
            /**
             * Updates managed contexts based on the marshaledContext.
             * 
             * @param marshaledContext The marshaled context to process.
             * @param commit If true the pending contexts are committed.
             * @return Reason if context change rejected.
             */
            /*package*/String setMarshaledContext(ContextItems marshaledContext, boolean commit) {
        StringBuilder reason = new StringBuilder();
        
        for (IManagedContext<?> managedContext : managedContexts) {
            try {
                if (managedContext.setContextItems(marshaledContext)) {
                    localChangeBegin(managedContext);
                    appendResponse(reason, localChangeEnd(managedContext, true, true));
                }
            } catch (Exception e) {
                log.error("Error processing marshaled context change.", e);
                appendResponse(reason, e.toString());
            }
        }
        
        if (commit) {
            commitContexts(reason.length() == 0, false);
        }
        
        return reason.toString();
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
            return ccowEnabled ? CCOWStatus.none : CCOWStatus.disabled;
        } else if (ccowTransaction) {
            return CCOWStatus.changing;
        } else {
            switch (ccowContextManager.getState()) {
                case csParticipating:
                    return CCOWStatus.joined;
                case csSuspended:
                    return CCOWStatus.broken;
                default:
                    return CCOWStatus.none;
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
    public String localChangeEnd(IManagedContext<?> managedContext) throws ContextException {
        return localChangeEnd(managedContext, false, false);
    }
    
    /**
     * Commits a pending context change.
     * 
     * @param managedContext The managed context of interest.
     * @param silent If true, this is a silent context change.
     * @param deferCommit If true, don't commit the context change, just survey subscribers.
     * @return The response(s) returned by subscribers.
     * @throws ContextException during illegal context change nesting
     */
    public String localChangeEnd(IManagedContext<?> managedContext, boolean silent,
                                 boolean deferCommit) throws ContextException {
        
        if (pendingStack.isEmpty() || pendingStack.peek() != managedContext) {
            throw new ContextException("Illegal context change nesting.");
        }
        
        if (!managedContext.isPending()) {
            pendingStack.pop();
            return null;
        }
        
        commitStack.push(managedContext);
        String survey = managedContext.surveySubscribers(silent);
        boolean accept = StringUtils.isEmpty(survey);
        
        if (!accept && log.isDebugEnabled()) {
            log.debug("Survey of managed context " + managedContext.getContextName() + " returned '" + survey + "'.");
        }
        
        pendingStack.remove(managedContext);
        
        if (!deferCommit && (!accept || pendingStack.isEmpty())) {
            commitContexts(accept, accept);
        }
        
        return survey;
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
    public boolean reset(boolean silent) {
        pendingStack.clear();
        commitStack.clear();
        boolean result = true;
        
        for (IManagedContext<?> managedContext : managedContexts) {
            result &= resetItem(managedContext, silent);
            
            if (!silent && !result) {
                break;
            }
        }
        
        boolean commit = silent || result;
        commitContexts(commit, commit);
        return result;
    }
    
    /**
     * Resets the managed context.
     * 
     * @param item Managed context to reset.
     * @param silent Silent flag.
     * @return True if change was accepted.
     */
    private boolean resetItem(IManagedContext<?> item, boolean silent) {
        try {
            localChangeBegin(item);
            item.reset();
            return StringUtils.isEmpty(localChangeEnd(item, silent, true));
        } catch (ContextException e) {
            return true;
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
        try {
            ccowTransaction = true;
            updateCCOWStatus();
            String reason = setMarshaledContext(contextItems, false);
            
            if (!reason.isEmpty()) {
                sender.setSurveyResponse(reason.toString());
            }
        } finally {
            ccowTransaction = false;
            updateCCOWStatus();
        }
    }
    
}
