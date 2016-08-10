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
package org.carewebframework.ui.xml;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.StringUtils;
import org.carewebframework.common.XMLUtil;
import org.carewebframework.web.annotation.ComponentDefinition;
import org.carewebframework.web.component.BaseComponent;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * Converts a ZK component tree to XML format.
 */
public class CWF2XML {
    
    private final Set<String> exclude = new HashSet<>();
    
    private final Document doc;
    
    /**
     * Returns an XML document that mirrors the ZK component tree starting at the specified root.
     * 
     * @param root BaseComponent whose subtree is to be traversed.
     * @param excludedProperties An optional list of properties that should be excluded from the
     *            output. These may either be the property name (e.g., "uuid") or a property name
     *            qualified by a component name (e.g., "window.uuid"). Optionally, an entry may be
     *            followed by an "=" and a value to exclude matches with a specific value. Note that
     *            "innerAttrs" and "outerAttrs" are always excluded.
     * @return An XML document that represents the component subtree.
     */
    public static Document toDocument(BaseComponent root, String... excludedProperties) {
        try {
            CWF2XML instance = new CWF2XML(excludedProperties);
            instance.toXML(root, instance.doc);
            return instance.doc;
        } catch (ParserConfigurationException e) {
            return null;
        }
    }
    
    /**
     * Returns an XML-formatted string that mirrors the ZK component tree starting at the specified
     * root.
     * 
     * @param root BaseComponent whose subtree is to be traversed.
     * @param excludedProperties An optional list of properties that should be excluded from the
     *            output. These may either be the property name (e.g., "uuid") or a property name
     *            qualified by a component name (e.g., "window.uuid"). Optionally, an entry may be
     *            followed by an "=" and a value to exclude matches with a specific value. Note that
     *            "innerAttrs" and "outerAttrs" are always excluded.
     * @return The XML text representation of the component subtree.
     */
    public static String toXML(BaseComponent root, String... excludedProperties) {
        return XMLUtil.toString(toDocument(root, excludedProperties));
    }
    
    /**
     * Private constructor to limit access to static methods.
     * 
     * @param excludedProperties An optional list of properties that should be excluded from the
     *            output. These may either be the property name (e.g., "uuid") or a property name
     *            qualified by a component name (e.g., "window.uuid"). Optionally, an entry may be
     *            followed by an "=" and a value to exclude matches with a specific value. Note that
     *            "innerAttrs" and "outerAttrs" are always excluded.
     * @throws ParserConfigurationException On parser exception.
     */
    private CWF2XML(String[] excludedProperties) throws ParserConfigurationException {
        doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        
        if (excludedProperties != null) {
            exclude.addAll(Arrays.asList(excludedProperties));
        }
        
        exclude.add("innerAttrs");
        exclude.add("outerAttrs");
    }
    
    /**
     * Adds the root component to the XML document at the current level along with all bean
     * properties that return String or primitive types. Then, recurses over all of the root
     * component's children.
     * 
     * @param root The root component.
     * @param parent The parent XML node.
     */
    private void toXML(BaseComponent root, Node parent) {
        TreeMap<String, String> properties = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        Class<?> clazz = root.getClass();
        ComponentDefinition def = root.getDefinition();
        String cmpname = def.getTag();
        
        if (def.getClazz() != clazz) {
            properties.put("use", clazz.getName());
        }
        
        //if (root.getController() != null) {
        //    properties.put("apply", def.getApply());
        //}
        
        Node child = doc.createElement(cmpname);
        parent.appendChild(child);
        
        for (PropertyDescriptor propDx : PropertyUtils.getPropertyDescriptors(root)) {
            Method getter = propDx.getReadMethod();
            Method setter = propDx.getWriteMethod();
            String name = propDx.getName();
            
            if (getter != null && setter != null && !isExcluded(name, cmpname, null)
                    && !setter.isAnnotationPresent(Deprecated.class)
                    && (getter.getReturnType() == String.class || getter.getReturnType().isPrimitive())) {
                try {
                    Object raw = getter.invoke(root);
                    String value = raw == null ? null : raw.toString();
                    
                    if (StringUtils.isEmpty(value) || ("id".equals(name) && value.startsWith("z_"))
                            || isExcluded(name, cmpname, value)) {
                        continue;
                    }
                    
                    properties.put(name, value.toString());
                } catch (Exception e) {}
            }
        }
        
        for (Entry<String, String> entry : properties.entrySet()) {
            Attr attr = doc.createAttribute(entry.getKey());
            child.getAttributes().setNamedItem(attr);
            attr.setValue(entry.getValue());
        }
        
        properties = null;
        
        for (BaseComponent cmp : root.getChildren()) {
            toXML(cmp, child);
        }
    }
    
    /**
     * Returns true if the property is to be excluded.
     * 
     * @param name The property name.
     * @param cmpname The component name (may be null).
     * @param value The property value (may be null).
     * @return True if the property should be excluded.
     */
    private boolean isExcluded(String name, String cmpname, String value) {
        return exclude.contains(excludeKey(name, null, value)) || exclude.contains(excludeKey(name, cmpname, value));
    }
    
    /**
     * Returns the exclusion lookup key for the property.
     * 
     * @param name The property name.
     * @param cmpname The component name (may be null).
     * @param value The property value (may be null).
     * @return The exclusion lookup key.
     */
    private String excludeKey(String name, String cmpname, String value) {
        StringBuilder sb = new StringBuilder(cmpname == null ? "" : cmpname + ".");
        sb.append(name).append(value == null ? "" : "=" + value);
        return sb.toString();
    }
}
