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
package org.carewebframework.shell.layout;

import java.io.InputStream;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.ObjectUtils;
import org.carewebframework.shell.ancillary.CWFException;
import org.carewebframework.shell.elements.ElementBase;
import org.carewebframework.shell.elements.ElementDesktop;
import org.carewebframework.shell.layout.LayoutElement.LayoutRoot;
import org.carewebframework.shell.plugins.PluginDefinition;
import org.carewebframework.shell.plugins.PluginRegistry;
import org.carewebframework.shell.property.PropertyInfo;
import org.fujion.client.ExecutionContext;
import org.fujion.common.MiscUtil;
import org.fujion.common.Version;
import org.fujion.common.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Parses an XML layout. A number of data sources are supported.
 */
public class LayoutParser {
    
    private static final LayoutParser instance = new LayoutParser();
    
    private enum Tag {
        LAYOUT(false), ELEMENT(true), TRIGGER(true), CONDITION(false), ACTION(false);

        private final boolean allowMultiple;

        Tag(boolean allowMultiple) {
            this.allowMultiple = allowMultiple;
        }

    }

    private final Version newVersion = new Version("4.0");
    
    /**
     * Loads a layout from the specified resource, using a registered layout loader or, failing
     * that, using the default loadFromUrl method.
     *
     * @param resource The resource to be loaded.
     * @return The loaded layout.
     */
    public static Layout parseResource(String resource) {
        int i = resource.indexOf(":");

        if (i > 0) {
            String loaderId = resource.substring(0, i);
            ILayoutLoader layoutLoader = LayoutLoaderRegistry.getInstance().get(loaderId);

            if (layoutLoader != null) {
                String name = resource.substring(i + 1);
                return layoutLoader.loadLayout(name);
            }
        }

        InputStream strm = ExecutionContext.getSession().getServletContext().getResourceAsStream(resource);
        
        if (strm == null) {
            throw new CWFException("Unable to locate layout resource: " + resource);
        }
        
        return parseStream(strm);
    }

    /**
     * Parse the layout from XML content.
     *
     * @param xml The XML content to parse.
     * @return The root layout element.
     */
    public static Layout parseText(String xml) {
        try {
            return parseDocument(XMLUtil.parseXMLFromString(xml));
        } catch (Exception e) {
            throw MiscUtil.toUnchecked(e);
        }
    }

    /**
     * Parse layout from an input stream.
     *
     * @param stream The input stream.
     * @return The root layout element.
     */
    public static Layout parseStream(InputStream stream) {
        try (InputStream is = stream) {
            return parseDocument(XMLUtil.parseXMLFromStream(is));
        } catch (Exception e) {
            throw MiscUtil.toUnchecked(e);
        }
    }

    /**
     * Parse the layout from a stored layout.
     *
     * @param layoutId Layout identifier.
     * @return The root layout element.
     */
    public static Layout parseProperty(LayoutIdentifier layoutId) {
        return parseText(LayoutUtil.getLayoutContent(layoutId));
    }
    
    /**
     * Parse the layout associated with the specified application id.
     *
     * @param appId An application id.
     * @return The root layout element.
     */
    public static Layout parseAppId(String appId) {
        return parseText(LayoutUtil.getLayoutContentByAppId(appId));
    }
    
    /**
     * Parse the layout from an XML document.
     *
     * @param document An XML document.
     * @return The root layout element.
     */
    public static Layout parseDocument(Document document) {
        return new Layout(instance.parseChildren(document, null, Tag.LAYOUT));
    }
    
    /**
     * Parse the layout from the UI.
     *
     * @param root Root UI element.
     * @return The root layout element.
     */
    public static Layout parseElement(ElementBase root) {
        return new Layout(instance.parseUI(root));
    }
    
    private LayoutParser() {
    }
    
    private LayoutRoot parseChildren(Node parentNode, LayoutNode parent, Tag... tags) {
        Element node = getFirstChild(parentNode);
        
        while (node != null) {
            Tag tag = getTag(node, tags);
            
            switch (tag) {
                case LAYOUT:
                    return parseLayout(node);
                
                case ELEMENT:
                    parseElement(node, (LayoutElement) parent);
                    break;
                
                case TRIGGER:
                    parseTrigger(node, (LayoutElement) parent);
                    break;

                case ACTION:
                    parseAction(node, (LayoutTrigger) parent);
                    break;

                case CONDITION:
                    parseCondition(node, (LayoutTrigger) parent);
                    break;
            }
            
            node = getNextSibling(node);
        }
        
        return null;
    }
    
    private boolean isElementNode(Node node) {
        return node.getNodeType() == Node.ELEMENT_NODE;
    }
    
    private Element getFirstChild(Node parent) {
        Node child = parent.getFirstChild();
        return child == null ? null : isElementNode(child) ? (Element) child : getNextSibling(child);
    }
    
    private Element getNextSibling(Node node) {
        Node sib = node;
        
        do {
            sib = sib.getNextSibling();
        } while (sib != null && !isElementNode(sib));

        return (Element) sib;
    }
    
    private LayoutRoot parseLayout(Element node) {
        LayoutRoot root = new LayoutRoot();
        LayoutUtil.copyAttributes(node, root.getAttributes());
        Version version = new Version(getRequiredAttribute(node, "version"));
        
        if (version.compareTo(newVersion) >= 0) {
            parseChildren(node, root, Tag.ELEMENT);
        } else {
            parseLegacy(node, root);
        }

        return root;
    }

    private void parseLegacy(Element node, LayoutElement parent) {
        Element child = getFirstChild(node);

        while (child != null) {
            LayoutElement ele = newLayoutElement(child, parent, child.getTagName());
            parseLegacy(child, ele);
            child = getNextSibling(child);
        }

    }
    
    private LayoutElement newLayoutElement(Element node, LayoutElement parent, String type) {
        PluginDefinition pluginDefinition = getDefinition(type, node);
        LayoutElement layoutElement = new LayoutElement(pluginDefinition, parent);
        
        if (node != null) {
            LayoutUtil.copyAttributes(node, layoutElement.getAttributes());
        }
        
        return layoutElement;
    }
    
    private PluginDefinition getDefinition(String type, Element node) {
        type = type != null ? type : getRequiredAttribute(node, "_type");
        PluginDefinition pluginDefinition = PluginRegistry.getInstance().get(type);

        if (pluginDefinition == null) {
            throw new IllegalArgumentException("Unrecognized " + node.getTagName() + " type: " + type);
        }

        return pluginDefinition;
    }

    /**
     * Parse a layout element node.
     *
     * @param node The DOM node.
     * @param parent The parent layout element.
     * @return The newly created layout element.
     */
    private LayoutElement parseElement(Element node, LayoutElement parent) {
        LayoutElement layoutElement = newLayoutElement(node, parent, null);
        parseChildren(node, layoutElement, Tag.ELEMENT, Tag.TRIGGER);
        return layoutElement;
    }

    /**
     * Returns the value of the named attribute from a DOM node, throwing an exception if not found.
     *
     * @param node The DOM node.
     * @param name The attribute name.
     * @return The attribute value.
     */
    private String getRequiredAttribute(Element node, String name) {
        String value = node.getAttribute(name);
        
        if (value.isEmpty()) {
            throw new IllegalArgumentException("Missing " + name + " attribute on node: " + node.getTagName());
        }
        
        return value;
    }
    
    /**
     * Parse a trigger node.
     *
     * @param node The DOM node.
     * @param parent The parent layout element.
     */
    private void parseTrigger(Element node, LayoutElement parent) {
        LayoutTrigger trigger = new LayoutTrigger();
        parent.getTriggers().add(trigger);
        parseChildren(node, trigger, Tag.CONDITION, Tag.ACTION);
    }
    
    /**
     * Parse a trigger condition node.
     *
     * @param node The DOM node.
     * @param parent The parent layout trigger.
     */
    private void parseCondition(Element node, LayoutTrigger parent) {
        new LayoutTriggerCondition(parent, getDefinition(null, node));
    }
    
    /**
     * Parse a trigger action node.
     *
     * @param node The DOM node.
     * @param parent The parent layout trigger.
     */
    private void parseAction(Element node, LayoutTrigger parent) {
        new LayoutTriggerAction(parent, getDefinition(null, node));
    }
    
    /**
     * Return and validate the tag type, throwing an exception if the tag is unknown or among the
     * allowable types.
     *
     * @param node The DOM node.
     * @param tags The allowable tag types.
     * @return The tag type.
     */
    private Tag getTag(Element node, Tag... tags) {
        String name = node.getTagName();
        String error = null;
        
        try {
            Tag tag = Tag.valueOf(name.toUpperCase());
            int i = ArrayUtils.indexOf(tags, tag);

            if (i < 0) {
                error = "Tag '%s' is not valid at this location";
            } else {
                if (!tag.allowMultiple) {
                    tags[i] = null;
                }

                return tag;
            }
        } catch (IllegalArgumentException e) {
            error = "Unrecognized tag '%s' in layout";
        }

        throw new IllegalArgumentException(getInvalidTagError(error, name, tags));
    }

    private String getInvalidTagError(String message, String tagName, Tag... tags) {
        message = String.format(message, tagName);
        StringBuilder sb = new StringBuilder();
        int tagCount = 0;

        for (Tag tag : tags) {
            if (tag != null) {
                sb.append(tagCount == 0 ? "" : ", ").append("'").append(tag.name().toLowerCase()).append("'");
                tagCount++;
            }
        }

        sb.insert(0, tagCount == 0 ? "no tags were expected" : tagCount == 1 ? "expected " : "expected one of ");
        return message + "; " + sb.toString();
    }
    
    /**
     * Parse the layout from the UI element tree.
     *
     * @param root The root of the UI element tree.
     * @return The root layout element.
     */
    private LayoutRoot parseUI(ElementBase root) {
        LayoutRoot ele = new LayoutRoot();

        if (root instanceof ElementDesktop) {
            copyAttributes(root, ele);
        }

        parseChildren(root, ele);
        return ele;
    }

    private void parseChildren(ElementBase parentNode, LayoutElement parent) {
        for (ElementBase child : parentNode.getSerializableChildren()) {
            LayoutElement ele = new LayoutElement(child.getDefinition(), parent);
            copyAttributes(child, ele);
            parseChildren(child, ele);
        }
    }

    /**
     * Copy attributes from a UI element to layout element.
     *
     * @param src UI element.
     * @param dest Layout element.
     */
    private void copyAttributes(ElementBase src, LayoutElement dest) {
        for (PropertyInfo propInfo : src.getDefinition().getProperties()) {
            Object value = propInfo.isSerializable() ? propInfo.getPropertyValue(src) : null;
            String val = value == null ? null : propInfo.getPropertyType().getSerializer().serialize(value);
            
            if (!ObjectUtils.equals(value, propInfo.getDefault())) {
                dest.getAttributes().put(propInfo.getId(), val);
            }
        }
    }

}
