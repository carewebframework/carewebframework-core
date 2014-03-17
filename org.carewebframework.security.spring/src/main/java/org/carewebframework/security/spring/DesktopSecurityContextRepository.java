/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.security.spring;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.carewebframework.ui.LifecycleEventDispatcher;
import org.carewebframework.ui.LifecycleEventListener.ILifecycleCallback;

import org.springframework.security.authentication.AuthenticationTrustResolver;
import org.springframework.security.authentication.AuthenticationTrustResolverImpl;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.web.context.HttpRequestResponseHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SaveContextOnUpdateOrErrorResponseWrapper;
import org.springframework.security.web.context.SecurityContextRepository;

import org.zkoss.zk.ui.Desktop;

/**
 * This is based on the HttpSessionSecurityContextRepository, but is desktop-, not session-, based.
 */
public class DesktopSecurityContextRepository implements SecurityContextRepository, ILifecycleCallback<Desktop> {
    
    private static final String CONTEXT_KEY = HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY;
    
    protected final Log log = LogFactory.getLog(this.getClass());
    
    /** SecurityContext instance used to check for equality with default (unauthenticated) content */
    private final Object contextObject = SecurityContextHolder.createEmptyContext();
    
    private boolean allowSessionCreation = true;
    
    private boolean disableUrlRewriting;
    
    private final AuthenticationTrustResolver authenticationTrustResolver = new AuthenticationTrustResolverImpl();
    
    /**
     * Given a servlet request, returns Spring security context object.
     * 
     * @param request HttpServletRequest
     * @return SecurityContext The Spring security context. First looks for the desktop-based
     *         security context. If not found, then looks for a session-based security context. This
     *         call will convert a session-based security context to desktop-based if a desktop
     *         identifier is found in the request object, a desktop-based security context does not
     *         exist, and a session-based security context does exist.
     * @throws IllegalStateException if session is invalidated
     */
    public static SecurityContext getSecurityContext(HttpServletRequest request) {
        final HttpSession session = request.getSession(false);
        boolean ignore = "rmDesktop".equals(request.getParameter("cmd_0"));
        return ignore || session == null ? SecurityContextHolder.createEmptyContext() : getSecurityContext(session,
            request.getParameter("dtid"));
    }
    
    /**
     * Given a desktop, returns Spring security context object.
     * 
     * @param desktop The desktop whose security context is sought.
     * @return SecurityContext The Spring security context.
     */
    public static SecurityContext getSecurityContext(Desktop desktop) {
        final String key = getDesktopContextKey(desktop.getId());
        HttpSession session = (HttpSession) desktop.getSession().getNativeSession();
        return (SecurityContext) session.getAttribute(key);
    }
    
    /**
     * Given a session and desktop id, returns Spring security context object.
     * 
     * @param session Session where security context is stored.
     * @param dtid Id of desktop whose security context is sought.
     * @return SecurityContext The Spring security context. First looks for the desktop-based
     *         security context. If not found, then looks for a session-based security context. This
     *         call will convert a session-based security context to desktop-based if a desktop
     *         identifier is found in the request object, a desktop-based security context does not
     *         exist, and a session-based security context does exist.
     * @throws IllegalStateException if session is invalidated
     */
    private static SecurityContext getSecurityContext(HttpSession session, String dtid) {
        final String key = getDesktopContextKey(dtid);
        
        if (key == null) {
            return getSecurityContext(session, false);
        }
        
        // Check for desktop-associated security context
        SecurityContext securityContext = (SecurityContext) session.getAttribute(key);
        
        // If no desktop security context, check session.
        if (securityContext == null) {
            securityContext = getSecurityContext(session, true);
            
            // If session security context found and this is a managed desktop, move into desktop.
            if (securityContext != null) {
                session.setAttribute(key, securityContext);
            }
        }
        return securityContext;
    }
    
    /**
     * Returns the security context from the session, if it exists.
     * 
     * @param session Session where security context is stored.
     * @param remove If true and a security context is found in the session, it is removed.
     * @return The security context, or null if none found.
     */
    private static SecurityContext getSecurityContext(HttpSession session, boolean remove) {
        SecurityContext securityContext = (SecurityContext) session.getAttribute(CONTEXT_KEY);
        
        if (securityContext != null && remove) {
            session.removeAttribute(CONTEXT_KEY);
        }
        
        return securityContext;
    }
    
    /**
     * Returns the desktop-specific (if desktop id available) or session-specific context key from
     * the request.
     * 
     * @param request The request object.
     * @return The context key (never null).
     */
    private static String getDesktopContextKey(HttpServletRequest request) {
        String key = getDesktopContextKey(request.getParameter("dtid"));
        return key == null ? CONTEXT_KEY : key;
    }
    
    /**
     * Returns the desktop-specific session key from the desktop id.
     * 
     * @param dtid The desktop id.
     * @return
     */
    private static String getDesktopContextKey(String dtid) {
        return StringUtils.isEmpty(dtid) ? null : CONTEXT_KEY + "-" + dtid;
    }
    
    public DesktopSecurityContextRepository() {
        super();
        LifecycleEventDispatcher.addDesktopCallback(this);
    }
    
    /**
     * Gets the security context for the current request (if available) and returns it.
     * <p>
     * If the session is null, the context object is null or the context object stored in the
     * session is not an instance of <tt>SecurityContext</tt>, a new context object will be
     * generated and returned.
     * <p>
     * If <tt>cloneFromHttpSession</tt> is set to true, it will attempt to clone the context object
     * first and return the cloned instance.
     */
    @Override
    public SecurityContext loadContext(HttpRequestResponseHolder requestResponseHolder) {
        HttpServletRequest request = requestResponseHolder.getRequest();
        HttpServletResponse response = requestResponseHolder.getResponse();
        HttpSession httpSession = request.getSession(false);
        SecurityContext context = readSecurityContextFromRequest(request);
        
        if (context == null) {
            if (log.isDebugEnabled()) {
                log.debug("No SecurityContext was available from the HttpSession: " + httpSession + ". "
                        + "A new one will be created.");
            }
            context = generateNewContext();
            
        }
        
        requestResponseHolder.setResponse(new SaveToSessionResponseWrapper(response, request, httpSession != null, context));
        
        return context;
    }
    
    @Override
    public void saveContext(SecurityContext context, HttpServletRequest request, HttpServletResponse response) {
        SaveToSessionResponseWrapper responseWrapper = (SaveToSessionResponseWrapper) response;
        // saveContext() might already be called by the response wrapper
        // if something in the chain called sendError() or sendRedirect(). This ensures we only call it
        // once per request.
        if (!responseWrapper.isContextSaved()) {
            responseWrapper.saveContext(context);
        }
    }
    
    @Override
    public boolean containsContext(HttpServletRequest request) {
        return getSecurityContext(request) != null;
    }
    
    /**
     * Reads the security context from the request object.
     * 
     * @param request
     * @return
     */
    private SecurityContext readSecurityContextFromRequest(HttpServletRequest request) {
        // Session exists, so try to obtain a context from it.
        
        SecurityContext securityContext = getSecurityContext(request);
        
        if (securityContext == null) {
            if (log.isDebugEnabled()) {
                log.debug("HttpSession returned null object for SPRING_SECURITY_CONTEXT");
            }
            
            return null;
        }
        
        if (log.isDebugEnabled()) {
            log.debug("Obtained a valid SecurityContext from SPRING_SECURITY_CONTEXT: '" + securityContext + "'");
        }
        
        // Everything OK. The only non-null return from this method.
        
        return securityContext;
    }
    
    /**
     * By default, calls {@link SecurityContextHolder#createEmptyContext()} to obtain a new context
     * (there should be no context present in the holder when this method is called). Using this
     * approach the context creation strategy is decided by the
     * {@link SecurityContextHolderStrategy} in use. The default implementations will return a new
     * <tt>SecurityContextImpl</tt>.
     * <p>
     * An alternative way of customizing the <tt>SecurityContext</tt> implementation is by setting
     * the <tt>securityContextClass</tt> property. In this case, the method will attempt to invoke
     * the no-args constructor on the supplied class instead and return the created instance.
     * 
     * @return a new SecurityContext instance. Never null.
     */
    private SecurityContext generateNewContext() {
        return SecurityContextHolder.createEmptyContext();
    }
    
    /**
     * If set to true (the default), a session will be created (if required) to store the security
     * context if it is determined that its contents are different from the default empty context
     * value.
     * <p>
     * Note that setting this flag to false does not prevent this class from storing the security
     * context. If your application (or another filter) creates a session, then the security context
     * will still be stored for an authenticated user.
     * 
     * @param allowSessionCreation
     */
    public void setAllowSessionCreation(boolean allowSessionCreation) {
        this.allowSessionCreation = allowSessionCreation;
    }
    
    /**
     * Allows the use of session identifiers in URLs to be disabled. Off by default.
     * 
     * @param disableUrlRewriting set to <tt>true</tt> to disable URL encoding methods in the
     *            response wrapper and prevent the use of <tt>jsessionid</tt> parameters.
     */
    public void setDisableUrlRewriting(boolean disableUrlRewriting) {
        this.disableUrlRewriting = disableUrlRewriting;
    }
    
    //~ Inner Classes ==================================================================================================
    
    /**
     * Wrapper that is applied to every request/response to update the <code>HttpSession<code> with
     * the <code>SecurityContext</code> when a <code>sendError()</code> or <code>sendRedirect</code>
     * happens. See SEC-398.
     * <p>
     * Stores the necessary state from the start of the request in order to make a decision about
     * whether the security context has changed before saving it.
     */
    final class SaveToSessionResponseWrapper extends SaveContextOnUpdateOrErrorResponseWrapper {
        
        private final HttpServletRequest request;
        
        private final boolean httpSessionExistedAtStartOfRequest;
        
        private final SecurityContext contextBeforeExecution;
        
        private final Authentication authBeforeExecution;
        
        /**
         * Takes the parameters required to call <code>saveContext()</code> successfully in addition
         * to the request and the response object we are wrapping.
         * 
         * @param response The response object
         * @param request The request object.
         * @param httpSessionExistedAtStartOfRequest Indicates whether there was a session in place
         *            before the filter chain executed. If this is true, and the session is found to
         *            be null, this indicates that it was invalidated during the request and a new
         *            session will now be created.
         * @param context The security context
         */
        SaveToSessionResponseWrapper(HttpServletResponse response, HttpServletRequest request,
            boolean httpSessionExistedAtStartOfRequest, SecurityContext context) {
            super(response, disableUrlRewriting);
            this.request = request;
            this.httpSessionExistedAtStartOfRequest = httpSessionExistedAtStartOfRequest;
            this.contextBeforeExecution = context;
            this.authBeforeExecution = context.getAuthentication();
        }
        
        /**
         * Stores the supplied security context in the session (if available) and if it has changed
         * since it was set at the start of the request. If the AuthenticationTrustResolver
         * identifies the current user as anonymous, then the context will not be stored.
         * 
         * @param context the context object obtained from the SecurityContextHolder after the
         *            request has been processed by the filter chain.
         *            SecurityContextHolder.getContext() cannot be used to obtain the context as it
         *            has already been cleared by the time this method is called.
         */
        @Override
        protected void saveContext(SecurityContext context) {
            Authentication authentication = context.getAuthentication();
            HttpSession httpSession = request.getSession(false);
            
            // See SEC-776
            if (authentication == null || authenticationTrustResolver.isAnonymous(authentication)) {
                if (log.isDebugEnabled()) {
                    log.debug("SecurityContext contents are anonymous - context will not be stored in HttpSession. ");
                }
                
                if (httpSession != null && !contextObject.equals(contextBeforeExecution)) {
                    // SEC-1587 A non-anonymous context may still be in the session
                    // SEC-1735 remove if the contextBeforeExecution was not anonymous
                    httpSession.removeAttribute(getDesktopContextKey(request));
                }
                return;
            }
            
            if (httpSession == null) {
                httpSession = createNewSessionIfAllowed(context);
            }
            
            // If HttpSession exists, store current SecurityContextHolder contents but only if
            // the SecurityContext has actually changed (see JIRA SEC-37)
            if (httpSession != null && contextChanged(context)) {
                httpSession.setAttribute(getDesktopContextKey(request), context);
                
                if (log.isDebugEnabled()) {
                    log.debug("SecurityContext stored to HttpSession: '" + context + "'");
                }
            }
        }
        
        private boolean contextChanged(SecurityContext context) {
            return context != contextBeforeExecution || context.getAuthentication() != authBeforeExecution;
        }
        
        private HttpSession createNewSessionIfAllowed(SecurityContext context) {
            if (httpSessionExistedAtStartOfRequest) {
                if (log.isDebugEnabled()) {
                    log.debug("HttpSession is now null, but was not null at start of request; "
                            + "session was invalidated, so do not create a new session");
                }
                
                return null;
            }
            
            if (!allowSessionCreation) {
                if (log.isDebugEnabled()) {
                    log.debug("The HttpSession is currently null, and the "
                            + "HttpSessionContextIntegrationFilter is prohibited from creating an HttpSession "
                            + "(because the allowSessionCreation property is false) - SecurityContext thus not "
                            + "stored for next request");
                }
                
                return null;
            }
            // Generate a HttpSession only if we need to
            
            if (contextObject.equals(context)) {
                if (log.isDebugEnabled()) {
                    log.debug("HttpSession is null, but SecurityContext has not changed from default empty context: ' "
                            + context + "'; not creating HttpSession or storing SecurityContext");
                }
                
                return null;
            }
            
            if (log.isDebugEnabled()) {
                log.debug("HttpSession being created as SecurityContext is non-default");
            }
            
            try {
                return request.getSession(true);
            } catch (IllegalStateException e) {
                // Response must already be committed, therefore can't create a new session
                log.warn("Failed to create a session, as response has been committed. Unable to store" + " SecurityContext.");
            }
            
            return null;
        }
    }
    
    /**
     * Force transfer of session-based security context to desktop.
     */
    @Override
    public void onInit(Desktop desktop) {
        HttpSession session = (HttpSession) desktop.getSession().getNativeSession();
        getSecurityContext(session, desktop.getId());
    }
    
    /**
     * Remove desktop security context on desktop cleanup.
     */
    @Override
    public void onCleanup(Desktop desktop) {
        HttpSession session = (HttpSession) desktop.getSession().getNativeSession();
        session.removeAttribute(getDesktopContextKey(desktop.getId()));
    }
    
    @Override
    public int getPriority() {
        return Integer.MIN_VALUE;
    }
}
