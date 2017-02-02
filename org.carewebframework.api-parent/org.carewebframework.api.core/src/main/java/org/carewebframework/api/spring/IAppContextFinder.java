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

import org.springframework.context.ApplicationContext;

/**
 * Implementors must define the logic for locating the framework's application context.
 */
public interface IAppContextFinder {
    
    /**
     * Returns the application context for the current scope. If the application implements
     * hierarchical contexts, this will return the lowest order application context in the
     * hierarchy. Otherwise, this will return the same value as getRootAppContext.
     * 
     * @return The application context.
     */
    ApplicationContext getChildAppContext();
    
    /**
     * Returns the application root context for the current scope. If the application implements
     * hierarchical contexts, this will return the highest order application context in the
     * hierarchy.
     * 
     * @return The root application context.
     */
    ApplicationContext getRootAppContext();
}
