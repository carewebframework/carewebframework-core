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
import org.carewebframework.api.domain.IUser;
import org.carewebframework.api.security.SecurityUtil;
import org.carewebframework.web.component.Page;
import org.carewebframework.web.core.ExecutionContext;
import org.springframework.util.Assert;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Utilities for dealing with <code>ServletRequest</code>s and <code>Execution</code>s.
 * <p>
 * Note that due to static nature of these getters and such scope'd objects as Request/Session, we
 * are throwing IllegalStateException for most methods, with the exception of {@link #getRequest()}
 * and {@link #getSession()}.
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
        ServletRequestAttributes requestAttrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        
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
    public static HttpSession getSession(HttpServletRequest request) {
        return request == null ? null : request.getSession(false);
    }
    
    /**
     * Logs at trace level the request headers
     */
    public static void logHeaderNames() {
        HttpServletRequest request = getRequest();
        if (request == null) {
            log.debug("logHeaderNames() invoked outside the scope of an Execution/ServletRequest");
        } else {
            Enumeration<?> enumeration = request.getHeaderNames();
            while (enumeration.hasMoreElements()) {
                String headerName = (String) enumeration.nextElement();
                log.trace(String.format("HeaderName: %s", headerName));
            }
        }
    }
    
    /**
     * Return server name.
     * 
     * @see HttpServletRequest#getServerName()
     * @return server name
     */
    public static String getServerName() {
        HttpServletRequest request = getRequest();
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
        } catch (Exception e) {
            log.debug("Exception occurred obtaining localhost IP address", e);
            return null;
        }
    }
    
    /**
     * Return client's ip address. Returns null if invoked outside scope of Execution/ServletRequest
     * <p>
     * This considers header X-FORWARDED-FOR (i.e. useful if behind a proxy)
     * 
     * @return the client's IP
     */
    public static String getRemoteAddress() {
        HttpServletRequest request = getRequest();
        String ipAddress = null;
        if (request != null) {
            ipAddress = request.getHeader("x-forwarded-for");
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
                log.trace(
                    String.format("Remote address: %s , obtained from X-FORWARDED_FOR header?", ipAddress, ipFromHeader));
            }
        }
        return ipAddress;
    }
    
    /**
     * Get current request's session id or null if session has not yet been created or if invoked
     * outside the scope of an Execution/ServletRequest.
     * 
     * @return String representing session id or null if session has not yet been created
     */
    public static String getSessionId() {
        HttpSession session = getSession(getRequest());
        return session == null ? null : session.getId();
    }
    
    /**
     * Return request, throwing IllegalStateException if invoked outside the scope of an
     * Execution/ServletRequest
     * 
     * @return HttpServletRequest
     * @throws IllegalStateException if called outside scope of an HttpServletRequest
     */
    public static HttpServletRequest assertRequest() {
        HttpServletRequest request = getRequest();
        Assert.state(request != null, "Method must be invoked within the scope of an Execution/ServletRequest");
        return request;
    }
    
    private static boolean isEmpty(String s) {
        return StringUtils.isEmpty(s) || "unknown".equalsIgnoreCase(s);
    }
    
    /**
     * <p>
     * As convenience, constructs the following diagnostic context, as an ordered List.
     * <ul>
     * <li>Session ID</li>
     * <li>Authentication Principal Username</li>
     * <li>CWF Page ID</li>
     * <li>Client Remote Address</li>
     * <li>Server Name</li>
     * </ul>
     * 
     * @return order List of Strings representing the diagnostic context
     */
    public static List<String> getStandardDiagnosticContext() {
        IUser user = SecurityUtil.getAuthenticatedUser();
        Page page = ExecutionContext.getPage();
        List<String> dc = new ArrayList<>();
        dc.add(getSessionId());
        dc.add(user == null ? "Unknown user" : user.getLoginName());
        dc.add(page == null ? "Unknown" : page.getId());
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
