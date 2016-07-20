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
     * @return Container for this scope.
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
