/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.ui;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import org.carewebframework.api.FrameworkUtil;
import org.carewebframework.api.property.PropertyUtil;
import org.carewebframework.common.StrUtil;
import org.carewebframework.ui.util.RequestUtil;

import org.zkoss.zk.ui.Desktop;
import org.zkoss.zk.ui.Execution;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Session;

/**
 * Class to manage instantiation of framework containers.
 */
public class FrameworkWebSupport {
    
    private static final String REQUEST_PARAMS = "_CWFRequestParams";
    
    private static final String REQUEST_URL = "_CWFRequestUrl";
    
    private static final ThreadLocal<Desktop> _desktops = new ThreadLocal<Desktop>();
    
    /**
     * Converts the queryString to a map.
     * 
     * @param queryString The query string (leading "?" is optional)
     * @return A map containing the parameters from the query string. This may return null if the
     *         queryString is empty. Multiple values for a parameter are separated by commas.
     */
    public static Map<String, String> queryStringToMap(String queryString) {
        return queryStringToMap(queryString, ",");
    }
    
    /**
     * Converts the queryString to a map.
     * 
     * @param queryString The query string (leading "?" is optional)
     * @param valueDelimiter String to use to delimit multiple values for a parameter. May be null.
     * @return A map containing the arguments from the query string. This may return null if the
     *         queryString is empty.
     */
    public static Map<String, String> queryStringToMap(String queryString, String valueDelimiter) {
        if (queryString == null || queryString.isEmpty()) {
            return null;
        }
        
        try {
            valueDelimiter = valueDelimiter == null ? "" : valueDelimiter;
            URI uri = new URI(queryString.startsWith("?") ? queryString : ("?" + queryString));
            List<NameValuePair> params = URLEncodedUtils.parse(uri, StrUtil.CHARSET);
            
            Map<String, String> result = new HashMap<String, String>();
            
            for (NameValuePair nvp : params) {
                String value = result.get(nvp.getName());
                result.put(nvp.getName(), (value == null ? "" : value + valueDelimiter) + nvp.getValue());
            }
            
            return result;
        } catch (URISyntaxException e) {
            return null;
        }
    }
    
    /**
     * Returns the current desktop. Returns null if the current thread has not been associated with
     * a desktop..
     * 
     * @return Current desktop or null.
     */
    public static Desktop getDesktop() {
        Execution exec = Executions.getCurrent();
        return exec == null ? _desktops.get() : exec.getDesktop();
    }
    
    /**
     * Return current Desktop ID or null if unavailable.
     * 
     * @return Desktop ID or null if unavailable.
     */
    public static String getDesktopId() {
        Desktop desktop = getDesktop();
        return desktop == null ? null : desktop.getId();
    }
    
    /**
     * Associates the specified desktop with the current thread. Used for background threads that
     * need to reference the desktop.
     * 
     * @param desktop If not null, associates this desktop with the current thread. If null, removes
     *            any existing association.
     */
    public static void associateDesktop(Desktop desktop) {
        if (desktop != null) {
            _desktops.set(desktop);
        } else {
            _desktops.remove();
        }
    }
    
    /**
     * Adds the specified query string to the url.
     * 
     * @param url url to receive the query string.
     * @param queryString Query string to add.
     * @return The updated url.
     * @throws IllegalArgumentException if url is null
     */
    public static String addQueryString(String url, String queryString) {
        Validate.notNull(url, "The url must not be null");
        
        if (!StringUtils.isEmpty(queryString)) {
            if (url.endsWith("?")) {
                url += queryString;
            } else if (url.contains("?")) {
                url += "&" + queryString;
            } else {
                url += "?" + queryString;
            }
        }
        
        return url;
    }
    
    /**
     * Returns the original request parameters from the current request.
     * 
     * @return the String value for key represented by constant {@link #REQUEST_PARAMS}.
     */
    public static String getRequestParams() {
        return (String) FrameworkUtil.getAttribute(REQUEST_PARAMS);
    }
    
    /**
     * Save the request parameters as an attribute of the framework
     * 
     * @param value String value to set for key represented by constant {@link #REQUEST_PARAMS}.
     */
    public static void setRequestParams(String value) {
        FrameworkUtil.setAttribute(REQUEST_PARAMS, value);
    }
    
    /**
     * Returns the original request url from the current request.
     * 
     * @return the String value for key represented by constant {@link #REQUEST_URL}
     */
    public static String getRequestUrl() {
        return (String) FrameworkUtil.getAttribute(REQUEST_URL);
    }
    
    /**
     * Save the request url.
     * 
     * @param value String value to set for key represented by constant {@link #REQUEST_URL}.
     */
    public static void setRequestUrl(String value) {
        FrameworkUtil.setAttribute(REQUEST_URL, value);
    }
    
    /**
     * Returns the HttpServletRequest object for the current execution.
     * 
     * @return the HttpServletRequest
     */
    public static HttpServletRequest getHttpServletRequest() {
        return RequestUtil.getRequest();
    }
    
    /**
     * Returns the HttpServletResponse object for the current execution.
     * 
     * @return the HttpServletResponse
     */
    public static HttpServletResponse getHttpServletResponse() {
        Execution exec = Executions.getCurrent();
        return exec == null ? null : (HttpServletResponse) exec.getNativeResponse();
    }
    
    /**
     * Returns the named cookie from the current request.
     * 
     * @param cookieName Name of cookie.
     * @see #getCookie(String, HttpServletRequest)
     * @return A cookie, or null if not found.
     * @throws IllegalArgumentException if argument is null or {@link #getHttpServletRequest()}
     *             returns null
     */
    public static Cookie getCookie(String cookieName) {
        return getCookie(cookieName, getHttpServletRequest());
    }
    
    /**
     * Returns the named cookie from the specified request. When values are retrieved, they should
     * be decoded.
     * 
     * @see #decodeCookieValue(String)
     * @param cookieName Name of cookie
     * @param httpRequest Request containing cookie.
     * @return A cookie, or null if not found.
     * @throws IllegalArgumentException if arguments are null
     */
    public static Cookie getCookie(String cookieName, HttpServletRequest httpRequest) {
        Validate.notNull(cookieName, "The cookieName must not be null");
        Validate.notNull(httpRequest, "The httpRequest must not be null");
        Cookie[] cookies = httpRequest.getCookies();
        
        if (cookies != null) {
            for (Cookie cookie : httpRequest.getCookies()) {
                if (cookieName.equals(cookie.getName())) {
                    return cookie;
                }
            }
        }
        
        return null;
    }
    
    /**
     * Returns the value from the named cookie from the specified request. The value is decoded with
     * for security and consistency (Version 0+ of Cookies and web containers)
     * 
     * @see #getCookie(String, HttpServletRequest)
     * @see #decodeCookieValue(String)
     * @param cookieName Name of cookie
     * @param httpRequest Request containing cookie. If null, it will check
     *            {@link Execution#getNativeRequest()}
     * @return A cookie value, or null if not found.
     * @throws IllegalArgumentException if arguments are null
     */
    public static String getCookieValue(String cookieName, HttpServletRequest httpRequest) {
        Cookie cookie = getCookie(cookieName, httpRequest);
        return cookie == null ? null : decodeCookieValue(cookie.getValue());
    }
    
    /**
     * <p>
     * Encodes a plain text cookie value.
     * </p>
     * <i>Note: The direction to use a two-phase encode/decode process (i.e. instead of the Base64
     * class URL_SAFE option) was intentional</i>
     * 
     * @see URLEncoder#encode(String, String)
     * @see Base64#encodeBase64String(byte[])
     * @param cookieValuePlainText The plain text to encode
     * @return encoded cookie value
     * @throws IllegalArgumentException If the argument is null
     */
    public static String encodeCookieValue(String cookieValuePlainText) {
        Validate.notNull(cookieValuePlainText, "The cookieValuePlainText must not be null");
        try {
            return URLEncoder.encode(Base64.encodeBase64String(cookieValuePlainText.getBytes()), StrUtil.CHARSET);
        } catch (Exception e) {
            throw new RuntimeException("Unexpected exception occurred encoding cookie value", e);
        }
    }
    
    /**
     * <p>
     * Decodes an encoded cookie value
     * </p>
     * <i>Note: The direction to use a two-phase encode/decode process (i.e. instead of the Base64
     * class URL_SAFE option) was intentional</i>
     * 
     * @see URLDecoder#decode(String, String)
     * @see Base64#decode(String)
     * @param encodedCookieValue The encoded cookie value
     * @return decoded cookie value
     * @throws IllegalArgumentException If the argument is null
     */
    public static String decodeCookieValue(String encodedCookieValue) {
        Validate.notNull(encodedCookieValue, "The encodedCookieValue must not be null");
        try {
            return new String(Base64.decodeBase64(URLDecoder.decode(encodedCookieValue, StrUtil.CHARSET)));
        } catch (Exception e) {
            throw new RuntimeException("Unexpected exception occurred decoding cookie value", e);
        }
    }
    
    /**
     * Returns the value from the named cookie from the specified request. The value is decoded.
     * 
     * @see #getCookieValue(String, HttpServletRequest)
     * @param cookieName Name of cookie
     * @return A cookie value, or null if not found.
     * @throws IllegalArgumentException if argument is null or if underlying HttpServletRequest is
     *             null
     */
    public static String getCookieValue(String cookieName) {
        return getCookieValue(cookieName, getHttpServletRequest());
    }
    
    /**
     * Sets a cookie into the current response.
     * 
     * @see #setCookie(String, String, HttpServletResponse, HttpServletRequest)
     * @param cookieName Name of cookie.
     * @param value Value of cookie.
     * @return Newly created cookie.
     * @throws IllegalArgumentException if {@link #getHttpServletResponse()} is null
     */
    public static Cookie setCookie(String cookieName, String value) {
        return setCookie(cookieName, value, getHttpServletResponse(), getHttpServletRequest());
    }
    
    /**
     * Sets a cookie into the response. Cookies are URLEncoded for consistency (Version 0+ of
     * Cookies)
     * 
     * @param cookieName Name of cookie.
     * @param value Value of cookie. If null, the cookie is removed from the client if it exists.
     * @param httpResponse Response object.
     * @param httpRequest Request object.
     * @return Newly created cookie.
     * @throws IllegalArgumentException if cookieName, httpResponse, or httpRequest arguments are
     *             null
     */
    public static Cookie setCookie(String cookieName, String value, HttpServletResponse httpResponse,
                                   HttpServletRequest httpRequest) {
        Validate.notNull(httpResponse, "The httpResponse must not be null");
        Cookie cookie = getCookie(cookieName, httpRequest);
        if (value != null) {
            value = encodeCookieValue(value);
        }
        
        if (cookie == null) {
            if (value == null) {
                return null;
            }
            cookie = new Cookie(cookieName, value);
        } else if (value == null) {
            cookie.setMaxAge(0);
        } else {
            cookie.setValue(value);
        }
        
        if (httpRequest.isSecure()) {
            cookie.setSecure(true);
        }
        
        httpResponse.addCookie(cookie);
        return cookie;
    }
    
    /**
     * Returns a session attribute value, removing it from the session.
     * 
     * @param session Session containing attributes of interest.
     * @param name Name of attribute to retrieve.
     * @return Value of attribute or null if it does not exist.
     */
    public static Object extractSessionAttribute(Session session, String name) {
        return extractSessionAttribute(session, name, null);
    }
    
    /**
     * Returns a session attribute value, removing it from the session.
     * 
     * @param session Session containing attributes of interest.
     * @param name Name of attribute to retrieve.
     * @param dflt Default value if none exists.
     * @return Value of attribute.
     */
    public static Object extractSessionAttribute(Session session, String name, Object dflt) {
        Object value = session.removeAttribute(name);
        return value == null ? dflt : value;
    }
    
    /**
     * Returns framework property first by looking for an HTTP request parameter and then by looking
     * at the property store. Assumes the property and the request parameter names are the same.
     * 
     * @param propertyName Property name.
     * @return Property value.
     */
    public static String getFrameworkProperty(String propertyName) {
        return getFrameworkProperty(propertyName, propertyName);
    }
    
    /**
     * Returns framework property first by looking for an HTTP request parameter and then by looking
     * at the property store.
     * 
     * @param parameterName Name of parameter in execution.
     * @param propertyName Name of property in property store.
     * @return Property value.
     */
    public static String getFrameworkProperty(String parameterName, String propertyName) {
        Execution exec = Executions.getCurrent();
        String value = exec == null ? null : exec.getParameter(parameterName);
        return value == null ? PropertyUtil.getValue(propertyName, null) : value;
    }
    
    /**
     * Enforce static class.
     */
    private FrameworkWebSupport() {
    };
}
