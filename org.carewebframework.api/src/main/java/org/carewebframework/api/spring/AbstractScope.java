/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.api.spring;

import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.Scope;

/**
 * Abstract base scope for custom scopes.
 */
public abstract class AbstractScope implements Scope {
    
    /**
     * Implement this to retrieve the container for this scope.
     * 
     * @return
     */
    protected abstract ScopeContainer getContainer();
    
    @Override
    public Object get(String name, ObjectFactory<?> objectFactory) {
        return getContainer().get(name, objectFactory);
    }
    
    @Override
    public Object remove(String name) {
        return getContainer().remove(name);
    }
    
    @Override
    public void registerDestructionCallback(String name, Runnable callback) {
        getContainer().registerDestructionCallback(name, callback);
    }
    
    @Override
    public Object resolveContextualObject(String key) {
        return getContainer().resolveContextualObject(key);
    }
    
    @Override
    public String getConversationId() {
        ScopeContainer container = getContainer();
        return container == null ? null : container.getConversationId();
    }
    
}
