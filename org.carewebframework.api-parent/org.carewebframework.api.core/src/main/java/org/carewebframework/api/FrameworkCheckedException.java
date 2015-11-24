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
import org.springframework.core.NestedCheckedException;

/**
 * Superclass for all checked exceptions thrown in the framework
 * <p>
 * This class is <code>abstract</code> to force the programmer to extend the class.
 * <code>getMessage</code> will include nested exception information; <code>printStackTrace</code>
 * and other like methods will delegate to the wrapped exception, if any.
 * </p>
 * <p>
 * Extends Spring's handy <code>NestedCheckedException</code> to wrap runtime exceptions with a root
 * cause.
 * </p>
 */
public class FrameworkCheckedException extends NestedCheckedException implements IThrowableContext, ErrorCoded {
    
    private static final long serialVersionUID = 1L;
    
    private String errorCode;
    
    private final String throwableContext;
    
    public FrameworkCheckedException(String msg) {
        this(msg, null, null);
    }
    
    public FrameworkCheckedException(String msg, Throwable cause, String throwableContext) {
        this(msg, cause, throwableContext, (Object[]) null);
    }
    
    public FrameworkCheckedException(String msg, Throwable cause, String throwableContext, Object... params) {
        super(StrUtil.formatMessage(msg, params), cause);
        this.throwableContext = throwableContext;
        
        if (msg.startsWith("@")) {
            errorCode = msg.substring(1);
        }
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
    
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        
        if (!(other instanceof FrameworkCheckedException)) {
            return false;
        }
        
        FrameworkCheckedException otherBe = (FrameworkCheckedException) other;
        return getMessage().equals(otherBe.getMessage()) && ObjectUtils.equals(getCause(), otherBe.getCause());
    }
    
    @Override
    public final int hashCode() {
        return getMessage().hashCode();
    }
    
    @Override
    public final String getErrorCode() {
        return errorCode;
    }
    
    @Override
    public final String getThrowableContext() {
        return throwableContext;
    }
}
