/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.api.context;

/**
 * Interface for access to public context manager services.
 */
public interface IContextManager {
    
    /**
     * Initiates a local context change.
     * 
     * @param managedContext The context that is changing.
     * @throws ContextException
     */
    void localChangeBegin(IManagedContext managedContext) throws ContextException;
    
    /**
     * Completes a local context change. When all requested context changes have been completed,
     * initiates the context change sequence for all pending changes.
     * 
     * @param managedContext The context that is changing.
     * @return Reason if change was rejected.
     * @throws ContextException
     */
    String localChangeEnd(IManagedContext managedContext) throws ContextException;
    
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
     * @throws ContextException
     */
    ContextMarshaller getContextMarshaller(String keyStoreName) throws ContextException;
    
    /**
     * Resets all managed contexts to a null state.
     * 
     * @param silent If true, no user interaction is permitted.
     * @return True if the operation completed successfully.
     */
    boolean reset(boolean silent);
}
