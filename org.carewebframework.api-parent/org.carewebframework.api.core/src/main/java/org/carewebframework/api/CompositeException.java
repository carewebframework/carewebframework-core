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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Allows collecting exceptions generated during an iterative process and reporting them as a single
 * single composite exception.
 */
public class CompositeException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;
    
    private final List<Throwable> exceptions = new ArrayList<>();
    
    public CompositeException(String msg) {
        super(msg);
    }
    
    /**
     * Adds an exception.
     * 
     * @param exception Exception to add.
     */
    public void add(Throwable exception) {
        exceptions.add(exception);
    }
    
    /**
     * Returns true if this instance contains any exceptions.
     * 
     * @return True if exceptions have been added.
     */
    public boolean hasExceptions() {
        return !exceptions.isEmpty();
    }
    
    /**
     * Returns true if this instance contains an exception of the given type.
     * 
     * @param type The exception class sought.
     * @return True if the exception class is present.
     */
    public boolean hasException(Class<? extends Throwable> type) {
        for (Throwable exception : exceptions) {
            if (type.isInstance(exception)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Throws the exception if it is not empty.
     */
    public void throwIfExceptions() {
        if (hasExceptions()) {
            throw this;
        }
    }
    
    /**
     * Creates composite exception message
     */
    @Override
    public String getMessage() {
        return getMessage(false);
    }
    
    /**
     * Creates composite exception message
     */
    @Override
    public String getLocalizedMessage() {
        return getMessage(true);
    }
    
    /**
     * Creates composite exception message (localized or not).
     * 
     * @param localized If true, return the localized version.
     * @return A composite of all exception messages.
     */
    private String getMessage(boolean localized) {
        String msg = localized ? super.getLocalizedMessage() : super.getMessage();
        StringBuilder sb = new StringBuilder(msg == null ? "" : msg);
        
        for (Throwable exception : exceptions) {
            msg = localized ? exception.getLocalizedMessage() : exception.getMessage();
            
            if (msg != null) {
                sb.append(sb.length() == 0 ? "" : "\n\n").append(msg);
            }
        }
        
        return sb.toString();
    }
    
    /**
     * Returns the stack trace, which is the union of all stack traces of contained exceptions.
     */
    @Override
    public StackTraceElement[] getStackTrace() {
        ArrayList<StackTraceElement> stackTrace = new ArrayList<>();
        
        for (Throwable exception : exceptions) {
            stackTrace.addAll(Arrays.asList(exception.getStackTrace()));
        }
        
        return stackTrace.toArray(new StackTraceElement[stackTrace.size()]);
    }
    
    @Override
    public void setStackTrace(StackTraceElement[] stackTrace) {
        throw new UnsupportedOperationException();
    }
    
    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object object) {
        return object instanceof CompositeException && ((CompositeException) object).exceptions.equals(exceptions);
    }
    
    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public final int hashCode() {
        return exceptions.hashCode();
    }
    
}
