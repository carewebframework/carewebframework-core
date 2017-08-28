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
import org.fujion.common.StrUtil;
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
public class FrameworkRuntimeException extends NestedRuntimeException implements IThrowableContext {

    private static final long serialVersionUID = 1L;

    private final String errorCode;

    private final String throwableContext;

    public FrameworkRuntimeException(String msg) {
        this(msg, null);
    }

    public FrameworkRuntimeException(String msg, Throwable cause) {
        this(msg, cause, null, (Object[]) null);
    }

    public FrameworkRuntimeException(String msg, Throwable cause, String throwableContext, Object... args) {
        super(StrUtil.formatMessage(msg, args), cause);
        this.throwableContext = throwableContext;
        errorCode = msg.startsWith("@") ? msg.substring(1) : null;
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

    public String getErrorCode() {
        return errorCode;
    }

    @Override
    public String getThrowableContext() {
        return throwableContext;
    }
}
