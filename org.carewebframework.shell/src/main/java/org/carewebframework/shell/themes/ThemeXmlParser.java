/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.shell.themes;

import org.carewebframework.shell.BaseXmlParser;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;

import org.w3c.dom.Element;

/**
 * Spring xml configuration file parser extension. Supports the definition of help modules within
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
        builder.setInitMethodName("init");
        builder.setDestroyMethodName("destroy");
        builder.addDependsOn("themeRegistry");
        builder.addPropertyReference("themeRegistry", "themeRegistry");
        addProperties(element, builder);
    }
    
    /**
     * Parses a theme definition from an xml string.
     * 
     * @param xml XML containing theme definition.
     * @return A theme definition instance.
     * @throws Exception
     */
    public static ThemeDefinition fromXml(String xml) throws Exception {
        return (ThemeDefinition) new ThemeXmlParser().fromXml(xml, "theme");
    }
    
}
