/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.shell.layout;

import java.io.InputStream;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.carewebframework.common.XMLUtil;
import org.carewebframework.shell.designer.IClipboardAware;
import org.carewebframework.shell.plugins.PluginDefinition;
import org.carewebframework.shell.property.IPropertyProvider;
import org.carewebframework.shell.property.PropertyInfo;

import org.zkoss.zk.ui.Executions;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * Represents the layout of the visual interface.
 */
public class UILayout implements IPropertyProvider, IClipboardAware<UILayout> {
    
    private static final Log log = LogFactory.getLog(UILayout.class);
    
    private static final String NULL_VALUE = "\\null\\";
    
    private Document document;
    
    private Node currentNode;
    
    private String layoutName;
    
    private String version;
    
    /**
     * Serializes the UI element hierarchy under and including the specified element.
     * 
     * @param parent Top level element to be serialized.
     * @return A UI layout representing the serialized hierarchy.
     * @throws Exception
     */
    public static UILayout serialize(UIElementBase parent) throws Exception {
        UILayout layout = new UILayout();
        layout.internalSerialize(parent);
        return layout;
    }
    
    public UILayout() {
        super();
        clear();
    }
    
    /**
     * Deserializes the layout, under the specified parent, starting from the layout origin.
     * 
     * @param parent Parent UI element at this level of the hierarchy. May be null.
     * @return The UI element created during this pass.
     * @throws Exception
     */
    public UIElementBase deserialize(UIElementBase parent) throws Exception {
        moveTop();
        moveDown();
        UIElementBase element = internalDeserialize(parent, !(parent instanceof UIElementDesktop));
        
        if (element != null) {
            element.getRoot().activate(true);
        }
        
        return element;
    }
    
    /**
     * Deserializes the layout, under the specified parent. This method manipulates the current
     * position within the layout and is called recursively in a depth-first traversal of the XML
     * hierarchy.
     * 
     * @param parent Parent UI element at this level of the hierarchy. May be null.
     * @param ignoreInternal Ignore internal elements.
     * @return The UI element created during this pass.
     * @throws Exception
     */
    private UIElementBase internalDeserialize(UIElementBase parent, boolean ignoreInternal) throws Exception {
        String id = getObjectName();
        PluginDefinition def = PluginDefinition.getDefinition(id);
        
        if (def == null) {
            log.error("Unrecognized tag '" + id + "' encountered in layout.");
        }
        
        UIElementBase element = def == null ? null : ignoreInternal && def.isInternal() ? null : def.createElement(parent,
            this);
        
        if (element != null && moveDown()) {
            internalDeserialize(element, false);
            moveUp();
        }
        
        while (moveNext()) {
            internalDeserialize(parent, ignoreInternal);
        }
        return element;
    }
    
    /**
     * Serializes the specified UI element (parent). This is called recursively for the specified
     * element and all its subordinates.
     * 
     * @param parent UI element to be serialized.
     * @throws Exception
     */
    private void internalSerialize(UIElementBase parent) throws Exception {
        PluginDefinition def = parent.getDefinition();
        boolean isRoot = parent.getParent() == null;
        
        if (!isRoot) {
            newChild(def.getId());
        }
        
        for (PropertyInfo propInfo : def.getProperties()) {
            Object value = propInfo.isSerializable() ? propInfo.getPropertyValue(parent) : null;
            String val = value == null ? null : value.toString();
            
            if (!ObjectUtils.equals(value, propInfo.getDefault())) {
                writeString(propInfo.getId(), val);
            }
        }
        
        for (UIElementBase child : parent.getSerializableChildren()) {
            internalSerialize(child);
        }
        
        if (!isRoot) {
            moveUp();
        }
    }
    
    /**
     * Returns the object name (i.e., the element tag) of the currently selected node.
     * 
     * @return The object name.
     */
    public String getObjectName() {
        return currentNode.getNodeName();
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
        setAttributeValue("name", value, document.getDocumentElement());
    }
    
    /**
     * Returns the version of the layout.
     * 
     * @return The layout version.
     */
    public String getVersion() {
        return version;
    }
    
    /**
     * Sets the version of the current layout.
     * 
     * @param value Version of the layout.
     */
    public void setVersion(String value) {
        version = value;
        setAttributeValue("version", value, document.getDocumentElement());
    }
    
    /**
     * Performs some simple validation of the newly loaded layout.
     * 
     * @throws Exception
     */
    private void validateDocument() throws Exception {
        currentNode = document.getDocumentElement();
        
        if (!LayoutConstants.LAYOUT_ROOT.equals(currentNode.getNodeName())) {
            throw new Exception("Expected signature not found.");
        }
        
        layoutName = readString("name", "");
        version = readString("version", "");
        moveDown();
    }
    
    /**
     * Reset the layout to not loaded state.
     */
    private void reset() {
        document = null;
        currentNode = null;
        layoutName = null;
        version = null;
    }
    
    /**
     * Load the layout from a file.
     * 
     * @param url Resource path.
     * @throws Exception when problem retrieving resource via uri path
     */
    public void loadFromUrl(String url) throws Exception {
        InputStream strm = null;
        
        try {
            reset();
            strm = Executions.getCurrent().getDesktop().getWebApp().getResourceAsStream(url);
            
            if (strm == null) {
                throw new UIException("Unable to locate layout resource: " + url);
            }
            
            document = XMLUtil.parseXMLFromStream(strm);
            validateDocument();
        } catch (Exception e) {
            reset();
            throw e;
        } finally {
            if (strm != null) {
                strm.close();
            }
        }
    }
    
    /**
     * Load the layout from a string.
     * 
     * @param text
     * @return This layout (for chaining).
     * @throws Exception
     */
    public UILayout loadFromText(String text) throws Exception {
        try {
            reset();
            document = XMLUtil.parseXMLFromString(text);
            validateDocument();
            return this;
        } catch (Exception e) {
            reset();
            throw e;
        }
    }
    
    /**
     * Load the layout from a stored property.
     * 
     * @param layoutName A named layout.
     * @param shared If true, return a shared layout; otherwise, a private layout.
     * @return This layout (for chaining).
     * @throws Exception
     */
    public UILayout loadFromProperty(String layoutName, boolean shared) throws Exception {
        String xml = LayoutUtil.getLayout(layoutName, shared);
        loadFromText(xml);
        this.layoutName = layoutName;
        return this;
    }
    
    /**
     * Load the layout associated with the specified application id.
     * 
     * @param appId An application id.
     * @return True if the operation succeeded.
     * @throws Exception
     */
    public UILayout loadByAppId(String appId) throws Exception {
        String xml = LayoutUtil.getLayoutByAppId(appId);
        return loadFromText(xml);
    }
    
    /**
     * Saves the layout as a property value using the specified name.
     * 
     * @param layoutName Name of layout.
     * @param shared If true, save as a shared layout; otherwise, a private layout.
     * @return True if operation succeeded.
     */
    public boolean saveToProperty(String layoutName, boolean shared) {
        setName(layoutName);
        setVersion(LayoutConstants.LAYOUT_VERSION);
        
        try {
            LayoutUtil.saveLayout(layoutName, toString(), shared);
        } catch (Exception e) {
            log.error("Error saving application layout.", e);
            return false;
        }
        
        return true;
    }
    
    /**
     * Sets the current node to the specified value. If the value is not an element node, sets the
     * current node to the first sibling node that is an element.
     * 
     * @param node Node to become current node.
     * @return True if the current node was successfully set.
     */
    private boolean setCurrentNode(Node node) {
        while (node != null) {
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                currentNode = node;
                return true;
            }
            node = node.getNextSibling();
        }
        
        return false;
    }
    
    /**
     * Clears the current document.
     */
    public void clear() {
        try {
            reset();
            document = XMLUtil.parseXMLFromString("<" + LayoutConstants.LAYOUT_ROOT + "/>\r\n");
        } catch (Exception e) {
            reset();
        }
        
        currentNode = document.getDocumentElement();
    }
    
    /**
     * Returns the class of the element at the root of the layout.
     * 
     * @return Class of the element at the root of the layout, or null if none.
     */
    public Class<? extends UIElementBase> getRootClass() {
        Node node = document.getDocumentElement();
        node = node.hasChildNodes() ? node.getFirstChild() : null;
        String id = node == null ? null : node.getNodeName();
        PluginDefinition def = id == null ? null : PluginDefinition.getDefinition(id);
        return def == null ? null : def.getClazz();
    }
    
    /**
     * Move current node down one level.
     * 
     * @return True if successful.
     */
    public boolean moveDown() {
        return currentNode != null && currentNode.hasChildNodes() && setCurrentNode(currentNode.getFirstChild());
    }
    
    /**
     * Moves to next sibling node.
     * 
     * @return True if successful.
     */
    public boolean moveNext() {
        return setCurrentNode(currentNode.getNextSibling());
    }
    
    /**
     * Move to the top element in the document.
     * 
     * @return Returns true only if a layout is currently loaded.
     */
    public boolean moveTop() {
        currentNode = document.getDocumentElement();
        return !StringUtils.isEmpty(layoutName);
    }
    
    /**
     * Move up one level.
     * 
     * @return True if successful
     */
    public boolean moveUp() {
        return setCurrentNode(currentNode.getParentNode());
    }
    
    /**
     * Returns value of named attribute as a boolean.
     * 
     * @param name Attribute name.
     * @param deflt Default value if not found.
     * @return Value of the attribute.
     */
    public boolean readBoolean(String name, boolean deflt) {
        return Boolean.parseBoolean(readString(name, Boolean.toString(deflt)));
    }
    
    /**
     * Return value of named attribute as an integer;
     * 
     * @param name Attribute name.
     * @param deflt Default value if not found.
     * @return Value of the attribute.
     */
    public int readInteger(String name, int deflt) {
        return NumberUtils.toInt(readString(name, null), deflt);
    }
    
    /**
     * Return value of named attribute as a string.
     * 
     * @param name Attribute name.
     * @param deflt Default value if not found.
     * @return Value of the attribute.
     */
    public String readString(String name, String deflt) {
        String value = hasProperty(name) ? currentNode.getAttributes().getNamedItem(name).getNodeValue() : deflt;
        return NULL_VALUE.equals(value) ? null : value;
    }
    
    /**
     * Create a new element node as the child of the current node and make it the current node.
     * 
     * @param name
     */
    public void newChild(String name) {
        currentNode = currentNode.appendChild(document.createElement(name));
    }
    
    public void writeBoolean(String name, boolean value) {
        writeString(name, Boolean.toString(value));
    }
    
    public void writeInteger(String name, int value) {
        writeString(name, Integer.toString(value));
    }
    
    /**
     * Sets an attribute value.
     * 
     * @param name
     * @param value
     */
    public void writeString(String name, String value) {
        setAttributeValue(name, value == null ? NULL_VALUE : value, currentNode);
    }
    
    /**
     * Returns true if the layout has no content.
     * 
     * @return True if the layout has no content.
     */
    public boolean isEmpty() {
        Node root = document == null ? null : document.getElementsByTagName(LayoutConstants.LAYOUT_ROOT).item(0);
        return root == null || !root.hasChildNodes();
    }
    
    /**
     * Sets the specified attribute value for the specified element.
     * 
     * @param name
     * @param value
     * @param element
     */
    private void setAttributeValue(String name, String value, Node element) {
        Node node = element.getAttributes().getNamedItem(name);
        
        if (node == null) {
            node = document.createAttribute(name);
            element.getAttributes().setNamedItem(node);
        }
        
        node.setNodeValue(value);
    }
    
    /**
     * Returns the layout as an xml-formatted string.
     */
    @Override
    public String toString() {
        return document == null ? null : XMLUtil.toString(document);
    }
    
    /**
     * @see #org.carewebframework.shell.property.IPropertyProvider.getProperty(String)
     */
    @Override
    public String getProperty(String key) {
        return readString(key, null);
    }
    
    /**
     * Returns true if the specified attribute exists in the current node.
     * 
     * @param name Attribute name.
     * @return True if the attribute exists.
     */
    @Override
    public boolean hasProperty(String name) {
        return currentNode == null ? false : currentNode.getAttributes().getNamedItem(name) != null;
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
     * 
     * @throws Exception
     */
    @Override
    public UILayout fromClipboard(String data) throws Exception {
        return new UILayout().loadFromText(data);
    }
}
