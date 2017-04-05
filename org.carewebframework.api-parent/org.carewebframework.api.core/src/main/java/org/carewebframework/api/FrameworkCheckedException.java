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
package org.carewebframework.api;

import org.apache.commons.lang.ObjectUtils;
import org.carewebframework.common.StrUtil;
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
public class FrameworkCheckedException extends NestedCheckedException implements IThrowableContext {
    
    private static final long serialVersionUID = 1L;
    
    private final String errorCode;
    
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
        errorCode = msg.startsWith("@") ? msg.substring(1) : null;
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
    
    public final String getErrorCode() {
        return errorCode;
    }
    
    @Override
    public final String getThrowableContext() {
        return throwableContext;
    }
}
