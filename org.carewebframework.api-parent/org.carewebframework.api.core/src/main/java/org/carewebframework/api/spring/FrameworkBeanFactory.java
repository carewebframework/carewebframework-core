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

import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.PlaceholderConfigurerSupport;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.BeanDefinitionDecorator;
import org.springframework.beans.factory.xml.NamespaceHandlerSupport;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.context.ApplicationContext;
import org.w3c.dom.Attr;
import org.w3c.dom.Node;

/**
 * Implementation of DefaultListableBeanFactory that supports namespace extensions to bean
 * declarations. Specifically, it supports:
 * <p>
 * The <b>cwf:override</b> attribute that can explicitly allow or disallow the overriding of a bean
 * definition, independent of the settings for the application context. It should be applied to the
 * overriding bean definition.
 * <p>
 * The <b>cwf:final</b> attribute that can prevent a bean definition from being overridden under any
 * circumstances. If set to true, it takes precedence over the application context settings and the
 * cwf:override setting of any overriding bean definition. The default attribute value is false.
 */
public class FrameworkBeanFactory extends DefaultListableBeanFactory {
    
    private static final String CAN_OVERRIDE_ATTR = FrameworkBeanFactory.class.getName() + ".canOverride";
    
    private static final String IS_FINAL_ATTR = FrameworkBeanFactory.class.getName() + ".isFinal";
    
    private enum BeanOverride {
        DEFAULT, // Override according to application context setting.
        NEVER, // Never override.  An exception is raised if an existing bean definition is found.
        ALWAYS, // Always override.  Any existing bean definition is replaced.
        IGNORE // Ignore if a bean definition already exists.  No exception is raised.
    }
    
    /**
     * This is a decorator for the transferring attribute values from the custom namespace to a
     * specified metadata attribute on the bean definition.
     */
    private static class Decorator implements BeanDefinitionDecorator {
        
        private final String attributeName;
        
        /**
         * Constructor
         *
         * @param attributeName The name of the bean definition attribute under which the custom
         *            namespace attribute value will be stored.
         */
        private Decorator(String attributeName) {
            this.attributeName = attributeName;
        }
        
        @Override
        public BeanDefinitionHolder decorate(Node source, BeanDefinitionHolder holder, ParserContext ctx) {
            holder.getBeanDefinition().setAttribute(attributeName, ((Attr) source).getValue());
            return holder;
        }
        
    }
    
    /**
     * Namespace handler registers the decorator for the cwf:override attribute.
     */
    public static class NamespaceHandler extends NamespaceHandlerSupport {
        
        @Override
        public void init() {
            super.registerBeanDefinitionDecoratorForAttribute("final", new Decorator(IS_FINAL_ATTR));
            super.registerBeanDefinitionDecoratorForAttribute("override", new Decorator(CAN_OVERRIDE_ATTR));
        }
    }
    
    // Default override behavior as dictated by the application context.
    private boolean defaultOverriding = true;
    
    /**
     * Creates a bean factory.
     *
     * @param parentContext Parent application context, if any. When specified, any placeholder
     *            configurers found in the parent context will be registered in this bean factory.
     * @param parentBeanFactory The parent bean factory, if any.
     */
    public FrameworkBeanFactory(ApplicationContext parentContext, BeanFactory parentBeanFactory) {
        super(parentBeanFactory);
        int i = 0;
        
        if (parentContext != null) {
            for (PlaceholderConfigurerSupport configurer : parentContext
                    .getBeansOfType(PlaceholderConfigurerSupport.class, false, false).values()) {
                registerSingleton("_placeholderconfigurer" + ++i, configurer);
            }
        }
    }
    
    /**
     * The application context will call this to set the override behavior of the bean factory. We
     * intercept this to set this as the default behavior for the bean factory. This setting is used
     * when no cwf:override attribute is specified.
     */
    @Override
    public void setAllowBeanDefinitionOverriding(boolean allowBeanDefinitionOverriding) {
        super.setAllowBeanDefinitionOverriding(allowBeanDefinitionOverriding);
        this.defaultOverriding = allowBeanDefinitionOverriding;
    }
    
    /**
     * Intercepts the call set the override behavior of the bean factory prior to registering the
     * bean. The override behavior is set to the cwf:override setting of the bean definition or, in
     * the absence of this setting, to the default override behavior for the bean factory and by the
     * cwf:final setting which can be used to prevent a bean declaration from ever being overridden.
     */
    @Override
    public void registerBeanDefinition(String beanName, BeanDefinition beanDefinition) throws BeanDefinitionStoreException {
        boolean canOverride = defaultOverriding;
        BeanDefinition oldBeanDefinition = containsBeanDefinition(beanName) ? getBeanDefinition(beanName) : null;
        String override = oldBeanDefinition == null ? null : getAttribute(beanDefinition, CAN_OVERRIDE_ATTR);
        String isfinal = oldBeanDefinition == null ? null : getAttribute(oldBeanDefinition, IS_FINAL_ATTR);
        
        if (override != null) {
            switch (BeanOverride.valueOf(override.toUpperCase())) {
                case ALWAYS:
                    canOverride = true;
                    break;
                
                case NEVER:
                    canOverride = false;
                    break;
                
                case IGNORE:
                    return;

                case DEFAULT:
                    break;
            }
        }
        
        if (canOverride && "true".equalsIgnoreCase(isfinal)) {
            canOverride = false;
        }
        
        super.setAllowBeanDefinitionOverriding(canOverride);
        super.registerBeanDefinition(beanName, beanDefinition);
    }
    
    /**
     * Searches this bean definition and all originating bean definitions until it finds the
     * requested attribute.
     *
     * @param beanDefinition Bean definition.
     * @param attributeName Attribute to locate.
     * @return The value of the attribute, or null if not found.
     */
    private String getAttribute(BeanDefinition beanDefinition, String attributeName) {
        String value = null;
        
        while (beanDefinition != null) {
            value = (String) beanDefinition.getAttribute(attributeName);
            
            if (value != null) {
                break;
            }
            
            beanDefinition = beanDefinition.getOriginatingBeanDefinition();
        }
        
        return value;
    }
}
