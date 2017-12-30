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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;

import org.fujion.common.MiscUtil;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;

/**
 * Base class for configurators that derive values from the application context's property store via
 * field-based annotations.
 */
public abstract class PropertyBasedConfigurator implements ApplicationContextAware {

    private static final String NULL_VALUE = "@@null@@";

    /**
     * Used to annotate fields for injection.
     */
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface Param {

        /**
         * Name of the property containing the value.
         *
         * @return The property name.
         */
        String property();

        /**
         * True if the property is required.
         *
         * @return True if the property is required.
         */
        boolean required() default false;
        
        /**
         * The default value if no property exists.
         *
         * @return The default value.
         */
        String defaultValue() default NULL_VALUE;
    }

    /**
     * Forms the full property name. The default implementation simply returns the original name.
     * Override if the provided name needs to be transformed in some way.
     *
     * @param name The property name specified in the annotation.
     * @return The expanded property name.
     */
    protected String expandPropertyName(String name) {
        return name;
    }

    /**
     * Inject all annotated class members.
     *
     * @see org.springframework.context.ApplicationContextAware#setApplicationContext(org.springframework.context.ApplicationContext)
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        PropertyProvider propertyProvider = new PropertyProvider(applicationContext);
        ConversionService conversionService = DefaultConversionService.getSharedInstance();
        Class<?> clazz = getClass();

        while (clazz != PropertyBasedConfigurator.class) {
            for (Field field : clazz.getDeclaredFields()) {
                field.setAccessible(true);
                Param annot = field.getAnnotation(Param.class);

                if (annot != null) {
                    String propName = expandPropertyName(annot.property());
                    String value = propertyProvider.getProperty(propName);
                    
                    if (value == null || value.isEmpty()) {
                        value = annot.defaultValue();
                        value = NULL_VALUE.equals(value) ? null : value;
                    }
                    
                    if (annot.required() && value == null) {
                        throw new RuntimeException("Required configuration property not specified: " + propName);
                    }

                    try {
                        field.set(this, conversionService.convert(value, field.getType()));
                    } catch (Exception e) {
                        throw MiscUtil.toUnchecked(e);
                    }
                }
            }

            clazz = clazz.getSuperclass();
        }
    }

}
