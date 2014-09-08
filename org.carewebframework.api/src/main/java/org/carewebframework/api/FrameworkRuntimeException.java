/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.api;

import org.apache.commons.lang.ObjectUtils;

import org.carewebframework.common.StrUtil;

import org.springframework.core.ErrorCoded;
import org.springframework.core.NestedRuntimeException;

/**
 * Superclass for all 'fatal' exceptions thrown in the framework
 * <p>
 * This class is <code>abstract</code> to force the programmer to extend the class.
 * <code>getMessage</code> will include nested exception information; <code>printStackTrace</code>
 * and other like methods will delegate to the wrapped exception, if any.
 * </p>
 * <p>
 * Extends Spring's handy <code>NestedRuntimeException</code> to wrap runtime exceptions with a root
 * cause.
 * </p>
 */
public class FrameworkRuntimeException extends NestedRuntimeException implements IThrowableContext, ErrorCoded {
    
    private static final long serialVersionUID = 1L;
    
    private String errorCode;
    
    private final String throwableContext;
    
    public FrameworkRuntimeException(String msg) {
        this(msg, null);
    }
    
    public FrameworkRuntimeException(String msg, Throwable cause) {
        this(msg, cause, null, (Object[]) null);
    }
    
    public FrameworkRuntimeException(String msg, Throwable cause, String throwableContext, Object... args) {
        super(StrUtil.formatMessage(msg, null, args), cause);
        this.throwableContext = throwableContext;
        
        if (msg.startsWith("@")) {
            errorCode = msg.substring(1);
        }
    }
    
    /**
     * Override to provide any special message formatting.
     * 
     * @param msg Message to format.
     * @return Formatted message.
     */
    protected String formatMessage(String msg) {
        return msg;
    }
    
    /**
     * Appends nested exception message
     * 
     * @see org.springframework.core.NestedExceptionUtils#buildMessage(String, Throwable)
     */
    @Override
    public final String getMessage() {
        return super.getMessage();
    }
    
    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        
        if (!(other instanceof FrameworkRuntimeException)) {
            return false;
        }
        
        FrameworkRuntimeException otherBe = (FrameworkRuntimeException) other;
        return getMessage().equals(otherBe.getMessage()) && ObjectUtils.equals(getCause(), otherBe.getCause());
    }
    
    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public final int hashCode() {
        return getMessage().hashCode();
    }
    
    @Override
    public String getErrorCode() {
        return errorCode;
    }
    
    @Override
    public String getThrowableContext() {
        return throwableContext;
    }
}
