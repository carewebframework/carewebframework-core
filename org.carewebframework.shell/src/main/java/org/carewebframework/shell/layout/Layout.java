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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.fujion.common.MiscUtil;
import org.fujion.common.XMLUtil;
import org.carewebframework.shell.designer.IClipboardAware;
import org.carewebframework.shell.elements.ElementBase;
import org.carewebframework.shell.elements.ElementDesktop;
import org.carewebframework.shell.elements.ElementTrigger;
import org.carewebframework.shell.elements.ElementUI;
import org.carewebframework.shell.layout.LayoutElement.LayoutRoot;
import org.carewebframework.shell.plugins.PluginDefinition;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Represents the layout of the visual interface.
 */
public class Layout implements IClipboardAware<Layout> {

    private static final Log log = LogFactory.getLog(Layout.class);

    public static final String LAYOUT_VERSION = "4.0";
    
    private LayoutRoot root;

    private String layoutName;

    public Layout() {
    }

    public Layout(Layout layout) {
        this(layout.root);
    }

    public Layout(String layoutName) {
        setName(layoutName);
    }

    public Layout(LayoutRoot root) {
        init(root);
    }

    /**
     * Materializes the layout, under the specified parent, starting from the layout origin.
     *
     * @param parent Parent UI element at this level of the hierarchy. May be null.
     * @return The UI element created during this pass.
     */
    public ElementUI materialize(ElementUI parent) {
        boolean isDesktop = parent instanceof ElementDesktop;

        if (isDesktop) {
            parent.getDefinition().initElement(parent, root);
        }

        materializeChildren(parent, root, !isDesktop);
        ElementUI element = parent.getLastVisibleChild();
        
        if (element != null) {
            element.getRoot().activate(true);
        }

        return element;
    }

    /**
     * Materializes the layout, under the specified parent.
     *
     * @param parent Parent UI element at this level of the hierarchy. May be null.
     * @param node The current layout element.
     * @param ignoreInternal Ignore internal elements.
     */
    private void materializeChildren(ElementBase parent, LayoutElement node, boolean ignoreInternal) {
        for (LayoutNode child : node.getChildren()) {
            PluginDefinition def = child.getDefinition();
            ElementBase element = ignoreInternal && def.isInternal() ? null : createElement(parent, child);

            if (element != null) {
                materializeChildren(element, (LayoutElement) child, false);
            }
        }

        for (LayoutTrigger trigger : node.getTriggers()) {
            ElementTrigger trg = new ElementTrigger();
            trg.addTarget((ElementUI) parent);
            createElement(trg, trigger.getChild(LayoutTriggerCondition.class));
            createElement(trg, trigger.getChild(LayoutTriggerAction.class));
            ((ElementUI) parent).addTrigger(trg);
        }
    }
    
    private ElementBase createElement(ElementBase parent, LayoutNode layoutNode) {
        return layoutNode == null ? null : layoutNode.getDefinition().createElement(parent, layoutNode, true);
    }
    
    /**
     * Returns the name of the currently loaded layout, or null if none loaded.
     *
     * @return The layout name.
     */
    public String getName() {
        return layoutName;
    }

    /**
     * Sets the name of the current layout.
     *
     * @param value New name for the layout.
     */
    public void setName(String value) {
        layoutName = value;
        
        if (root != null) {
            root.getAttributes().put("name", value);
        }
    }

    /**
     * Performs some simple validation of the newly loaded layout.
     *
     * @param root The root layout element.
     */
    private void init(LayoutRoot root) {
        this.root = root;
        layoutName = root.getProperty("name");
    }

    /**
     * Reset the layout to not loaded state.
     */
    public void clear() {
        root = null;
        layoutName = null;
    }

    /**
     * Saves the layout as a property value using the specified identifier.
     *
     * @param layoutId Layout identifier
     * @return True if operation succeeded.
     */
    public boolean saveToProperty(LayoutIdentifier layoutId) {
        setName(layoutId.name);

        try {
            LayoutUtil.saveLayout(layoutId, toString());
        } catch (Exception e) {
            log.error("Error saving application layout.", e);
            return false;
        }

        return true;
    }

    /**
     * Returns the class of the element at the root of the layout.
     *
     * @return Class of the element at the root of the layout, or null if none.
     */
    public Class<? extends ElementBase> getRootClass() {
        LayoutElement top = root == null ? null : root.getChild(LayoutElement.class);
        return top == null ? null : top.getDefinition().getClazz();
    }

    /**
     * Returns true if the layout has no content.
     *
     * @return True if the layout has no content.
     */
    public boolean isEmpty() {
        return root == null || root.getChildren().isEmpty();
    }

    /**
     * Returns the layout as an xml-formatted string.
     */
    @Override
    public String toString() {
        if (root == null) {
            return "";
        }
        
        try {
            Document doc = XMLUtil.parseXMLFromString("<layout/>");
            Element node = doc.getDocumentElement();
            buildDocument(node, root);
            LayoutUtil.copyAttributes(root.getAttributes(), node);
            node.setAttribute("version", LAYOUT_VERSION);
            node.setAttribute("name", layoutName);
            return XMLUtil.toString(doc);
        } catch (Exception e) {
            throw MiscUtil.toUnchecked(e);
        }
    }
    
    private void buildDocument(Element node, LayoutNode ele) {
        
        if (ele instanceof LayoutElement) {
            for (LayoutTrigger trigger : ((LayoutElement) ele).getTriggers()) {
                Element child = createDOMNode(trigger, node);
                buildDocument(child, trigger);
            }
        }

        for (LayoutNode element : ele.getChildren()) {
            Element child = createDOMNode(element, node);
            child.setAttribute("_type", element.getDefinition().getId());
            buildDocument(child, element);
        }
    }

    private Element createDOMNode(LayoutNode layoutNode, Element parentDOMNode) {
        Element child = parentDOMNode.getOwnerDocument().createElement(layoutNode.getTagName());
        LayoutUtil.copyAttributes(layoutNode.getAttributes(), child);
        parentDOMNode.appendChild(child);
        return child;
    }

    /**
     * Converts to clipboard format.
     */
    @Override
    public String toClipboard() {
        return toString();
    }

    /**
     * Converts from clipboard format.
     */
    @Override
    public Layout fromClipboard(String data) {
        init(LayoutParser.parseText(data).root);
        return this;
    }
}
