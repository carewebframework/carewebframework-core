/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.ui.util;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.carewebframework.api.security.SecurityUtil;
import org.carewebframework.ui.FrameworkWebSupport;

import org.springframework.util.Assert;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Utilities for dealing with <code>ServletRequest</code>s and <code>Execution</code>s.
 * <p>
 * Note that due to static nature of these getters and such scope'd objects as Request/Session, we
 * are throwing IllegalStateException for most methods, with the exception of {@link #getRequest()}
 * & {@link #getSession()}.
 */
public class RequestUtil {
    
    private static Log log = LogFactory.getLog(RequestUtil.class);
    
    /**
     * Return current HttpServletRequest. Note that this will return null when invoked outside the
     * scope of an execution/request.
     * 
     * @see RequestContextHolder#currentRequestAttributes()
     * @return HttpServletRequest, null when invoked outside the scope of an
     *         Execution/ServletRequest
     */
    public static HttpServletRequest getRequest() {
        final ServletRequestAttributes requestAttrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (requestAttrs == null) {
            return null;
        }
        return requestAttrs.getRequest();
    }
    
    /**
     * Return current HttpSession
     * 
     * @return HttpSession, null when invoked outside the scope of an Execution/ServletRequest
     */
    public static HttpSession getSession() {
        return getSession(getRequest());
    }
    
    /**
     * Return current HttpSession given request.
     * 
     * @param request Http servlet request object.
     * @return HttpSession, null when invoked outside the scope of an Execution/ServletRequest
     */
    public static HttpSession getSession(final HttpServletRequest request) {
        return request == null ? null : request.getSession(false);
    }
    
    /**
     * Logs at trace level the request headers
     * 
     * @throws IllegalStateException if called outside scope of an HttpServletRequest
     */
    public static void logHeaderNames() throws IllegalStateException {
        final HttpServletRequest request = assertRequest();
        final Enumeration<?> enumeration = request.getHeaderNames();
        while (enumeration.hasMoreElements()) {
            final String headerName = (String) enumeration.nextElement();
            log.trace(String.format("HeaderName: %s", headerName));
        }
    }
    
    /**
     * Return server name.
     * 
     * @see HttpServletRequest#getServerName()
     * @return server name
     */
    public static String getServerName() {
        final HttpServletRequest request = getRequest();
        if (request == null) {
            return null;
        }
        return request.getServerName();
    }
    
    /**
     * Return local host IP. Note: HttpServletRequest#getLocalAddr() doesn't seem to be consistent.
     * This method uses java.net.InetAddress.
     * 
     * @see InetAddress#getHostAddress()
     * @return server IP
     */
    public static String getLocalHostAddress() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (final Exception e) {
            log.debug("Exception occurred obtaining localhost IP address", e);
            return null;
        }
    }
    
    /**
     * Return client's ip address.
     * <p>
     * Must be called in the scope of an Execution/ServletRequest. This considers header
     * X-FORWARDED-FOR (i.e. useful if behind a proxy)
     * 
     * @return the client's IP
     * @throws IllegalStateException if called outside scope of an Execution/ServletRequest
     */
    public static String getRemoteAddress() {
        //final Execution execution = assertExecution();
        final HttpServletRequest request = assertRequest();
        String ipAddress = request.getHeader("x-forwarded-for");
        boolean ipFromHeader = true;
        if (isEmpty(ipAddress)) {
            ipAddress = request.getHeader("X_FORWARDED_FOR");
            if (isEmpty(ipAddress)) {
                ipFromHeader = false;
                ipAddress = request.getRemoteAddr();
            }
            logHeaderNames();
        }
        //log headers in case we find a case where above logic doesn't return correct ip
        if (log.isTraceEnabled()) {
            logHeaderNames();
            log.trace(String.format("Remote address: %s , obtained from X-FORWARDED_FOR header?", ipAddress, ipFromHeader));
        }
        return ipAddress;
    }
    
    /**
     * Get current request's session id
     * 
     * @throws IllegalStateException if called outside scope of an Execution/ServletRequest
     * @return String representing session id
     */
    public static String getSessionId() {
        return assertRequest().getSession().getId();
    }
    
    private static HttpServletRequest assertRequest() {
        final HttpServletRequest request = getRequest();
        Assert.state(request != null, "Method must be invoked within the scope of an Execution/ServletRequest");
        return request;
    }
    
    private static boolean isEmpty(final String s) {
        return StringUtils.isEmpty(s) || "unknown".equalsIgnoreCase(s);
    }
    
    /**
     * <p>
     * As convenience, constructs the following diagnostic context, as an ordered List.
     * <ul>
     * <li>Session ID</li>
     * <li>Authentication Principal Username</li>
     * <li>ZK Desktop ID</li>
     * <li>Client Remote Address</li>
     * <li>Server Name</li>
     * </ul>
     * </p>
     * 
     * @return order List of Strings representing the diagnostic context
     */
    public static List<String> getStandardDiagnosticContext() {
        final List<String> dc = new ArrayList<String>();
        dc.add(getSessionId());
        dc.add(SecurityUtil.getAuthenticatedUsername());
        dc.add(FrameworkWebSupport.getDesktopId());
        dc.add(getRemoteAddress());
        dc.add(getLocalHostAddress());
        return dc;
    }
    
    /**
     * Enforce static class.
     */
    private RequestUtil() {
    }
}
