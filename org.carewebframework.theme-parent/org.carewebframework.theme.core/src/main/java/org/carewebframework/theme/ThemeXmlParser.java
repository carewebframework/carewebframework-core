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
package org.carewebframework.theme;

import org.carewebframework.api.spring.BaseXmlParser;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;

import org.w3c.dom.Element;

/**
 * Spring xml configuration file parser extension. Supports the definition of theme modules within
 * the configuration file in a much more abbreviated fashion than would be required without the
 * extension.
 */
public class ThemeXmlParser extends BaseXmlParser {
    
    @Override
    protected Class<?> getBeanClass(Element element) {
        return ThemeDefinition.class;
    }
    
    @Override
    protected void doParse(Element element, BeanDefinitionBuilder builder) {
        addProperties(element, builder);
    }
    
    /**
     * Parses a theme definition from an xml string.
     * 
     * @param xml XML containing theme definition.
     * @return A theme definition instance.
     * @throws Exception Unspecified exception.
     */
    public static ThemeDefinition fromXml(String xml) throws Exception {
        return (ThemeDefinition) new ThemeXmlParser().fromXml(xml, "theme");
    }
    
}
