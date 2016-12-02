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
package org.carewebframework.ui.spring;

import org.carewebframework.api.spring.FrameworkBeanFactory;
import org.carewebframework.api.spring.ResourceCache;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.web.context.support.XmlWebApplicationContext;

/**
 * Subclass application context to implement resource caching and to customize bean definition
 * override capability.
 */
public class FrameworkAppContext extends XmlWebApplicationContext {
    
    private static ResourceCache resourceCache;
    
    /**
     * Override to return a custom bean factory. Registers any placeholder configurers found in
     * parent context with the child context (which allows resolution of placeholder values in the
     * child context using a common configurer).
     */
    @Override
    public DefaultListableBeanFactory createBeanFactory() {
        return new FrameworkBeanFactory(getParent(), getInternalParentBeanFactory());
    }
    
    /**
     * Use cached resource resolver.
     */
    @Override
    protected ResourcePatternResolver getResourcePatternResolver() {
        return resourceCache == null ? resourceCache = new ResourceCache(this) : resourceCache;
    }
    
}
