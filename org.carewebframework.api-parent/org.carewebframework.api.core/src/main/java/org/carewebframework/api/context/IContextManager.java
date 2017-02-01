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
package org.carewebframework.api.context;

import java.util.List;

import org.carewebframework.api.context.ISurveyResponse.ISurveyCallback;

/**
 * Interface for access to public context manager services.
 */
public interface IContextManager {
    
    /**
     * Initiates a local context change.
     * 
     * @param managedContext The context that is changing.
     * @throws ContextException Context exception.
     */
    void localChangeBegin(IManagedContext<?> managedContext) throws ContextException;
    
    /**
     * Completes a local context change. When all requested context changes have been completed,
     * initiates the context change sequence for all pending changes.
     * 
     * @param managedContext The context that is changing.
     * @param callback Callback to report response to context change request (may be null).
     * @throws ContextException Context exception.
     */
    void localChangeEnd(IManagedContext<?> managedContext, ISurveyCallback callback) throws ContextException;
    
    /**
     * Returns a list of all registered shared contexts.
     * 
     * @return A list of all registered shared contexts.
     */
    List<ISharedContext<?>> getSharedContexts();
    
    /**
     * Gets the shared context corresponding to the named class. If the context object has not yet
     * been registered, it is instantiated and registered.
     * 
     * @param className Name of the class for the requested context object.
     * @return An instance of the specified context object.
     */
    ISharedContext<?> getSharedContext(String className);
    
    /**
     * Returns a context marshaler using the specified keystore for authentication.
     * 
     * @param keyStoreName The name of the keystore.
     * @return The context marshaler.
     * @throws ContextException Context exception.
     */
    ContextMarshaller getContextMarshaller(String keyStoreName) throws ContextException;
    
    /**
     * Resets all managed contexts to a null state.
     * 
     * @param silent If true, no user interaction is permitted.
     * @param callback Callback to report response to context change request (may be null).
     */
    void reset(boolean silent, ISurveyCallback callback);
}
