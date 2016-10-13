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
package org.carewebframework.ui.zk;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.Popup;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang.reflect.FieldUtils;
import org.carewebframework.common.MiscUtil;
import org.carewebframework.common.StrUtil;
import org.carewebframework.web.annotation.EventHandlerScanner;
import org.carewebframework.web.annotation.WiredComponentScanner;
import org.carewebframework.web.client.ClientUtil;
import org.carewebframework.web.component.BaseComponent;
import org.carewebframework.web.component.BaseInputboxComponent;
import org.carewebframework.web.component.BaseUIComponent;
import org.carewebframework.web.component.Checkbox;
import org.carewebframework.web.component.Combobox;
import org.carewebframework.web.component.Html;
import org.carewebframework.web.component.Hyperlink;
import org.carewebframework.web.component.Label;
import org.carewebframework.web.component.Page;
import org.carewebframework.web.core.ExecutionContext;
import org.carewebframework.web.event.ChangeEvent;
import org.carewebframework.web.event.Event;
import org.carewebframework.web.event.EventUtil;
import org.carewebframework.web.event.IEventListener;
import org.carewebframework.web.event.SelectEvent;
import org.carewebframework.web.page.PageDefinition;
import org.carewebframework.web.page.PageDefinitionCache;
import org.springframework.core.io.ClassPathResource;

/**
 * General purpose ZK extensions and convenience methods.
 */
public class ZKUtil {
    
    /**
     * Resource prefix for resources within this package
     */
    public static final String RESOURCE_PREFIX = getResourcePath(ZKUtil.class);
    
    private static final String MASK_ANCHOR = "maskTarget";
    
    private static final IEventListener deferredDelivery = new IEventListener() {
        
        @Override
        public void onEvent(Event event) {
            EventUtil.send(event);
        }
        
    };
    
    /**
     * Possible match modes for hierarchical tree search.
     */
    public enum MatchMode {
        AUTO, // Autodetect index vs label
        INDEX, // By node index.
        CASE_SENSITIVE, // Case sensitive by node label.
        CASE_INSENSITIVE // Case insensitive by node label.
    };
    
    /**
     * Returns the ZK resource path for the specified class.
     * 
     * @param clazz Class to evaluate
     * @return String representing resource path
     */
    public static final String getResourcePath(Class<?> clazz) {
        return getResourcePath(clazz.getPackage());
    }
    
    /**
     * Returns the ZK resource path for the specified class.
     * 
     * @param clazz Class to evaluate
     * @param up Number of path levels to remove
     * @return String representing resource path
     */
    public static final String getResourcePath(Class<?> clazz, int up) {
        return getResourcePath(clazz.getPackage(), up);
    }
    
    /**
     * Returns the ZK resource path for the specified package.
     * 
     * @param pkg Package to evaluate
     * @return String representing resource path
     */
    public static final String getResourcePath(Package pkg) {
        return getResourcePath(pkg.getName());
    }
    
    /**
     * Returns the ZK resource path for the specified package.
     * 
     * @param pkg Package to evaluate
     * @param up Number of path levels to remove
     * @return String representing resource path
     */
    public static final String getResourcePath(Package pkg, int up) {
        return getResourcePath(pkg.getName(), up);
    }
    
    /**
     * Returns the ZK resource path for the package name.
     * 
     * @param name Package name
     * @return String representing resource path
     */
    public static final String getResourcePath(String name) {
        return getResourcePath(name, 0);
    }
    
    /**
     * Returns the ZK resource path for the package name.
     * 
     * @param name Package name
     * @param up Number of path levels to remove
     * @return String representing resource path
     */
    public static final String getResourcePath(String name, int up) {
        String path = StringUtils.chomp(name.replace('.', '/'), "/");
        
        while (up > 0) {
            int i = path.lastIndexOf("/");
            
            if (i <= 0) {
                break;
            } else {
                path = path.substring(0, i);
                up--;
            }
        }
        
        return "~./" + path + "/";
    }
    
    /**
     * Loads a page definition from a cwf page. If the page is an embedded resource, prefix the file
     * name with the owning class name followed by a colon. For example,
     * "org.carewebframework.ui.component.PatientSelection:patientSelection.cwf"
     * 
     * @param filename File name pointing to the cwf page.
     * @return A page definition representing the cwf page.
     */
    public static PageDefinition loadPageDefinition(String filename) {
        Class<?> containingClass = null;
        int i = filename.indexOf(":");
        
        try {
            if (i > 0) {
                containingClass = Class.forName(filename.substring(0, i));
                filename = filename.substring(i + 1);
            }
            
            if (containingClass != null) {
                filename = new ClassPathResource(filename, containingClass).getURL().toString();
            }
            
            return PageDefinitionCache.getInstance().get(filename);
        } catch (Exception e) {
            throw MiscUtil.toUnchecked(e);
        }
    }
    
    /**
     * Loads a cwf page. May contain a query string which is passed as an argument map.
     * 
     * @param filename File name pointing to the cwf page.
     * @param parent BaseComponent to become the parent of the created page.
     * @return The top level component of the created cwf page.
     */
    public static BaseComponent loadPage(String filename, BaseComponent parent) {
        try {
            return loadPageDefinition(filename).materialize(parent);
        } catch (Exception e) {
            throw MiscUtil.toUnchecked(e);
        }
    }
    
    /**
     * Detaches all children from a parent.
     * 
     * @param parent Parent component.
     */
    public static void detachChildren(BaseComponent parent) {
        detachChildren(parent, null);
    }
    
    /**
     * Detaches children from a parent. If exclusions is not null, any exclusions will not be
     * removed.
     * 
     * @param parent Parent component.
     * @param exclusions List of child components to be excluded, or null to detach all children.
     */
    public static void detachChildren(BaseComponent parent, List<? extends BaseComponent> exclusions) {
        detach(parent.getChildren(), exclusions);
    }
    
    /**
     * Detaches a list of components.
     * 
     * @param components List of components to detach.
     */
    public static void detach(List<? extends BaseComponent> components) {
        detach(components, null);
    }
    
    /**
     * Detaches a list of components. If exclusions is not null, any exclusions will not be removed.
     * 
     * @param components List of components to detach.
     * @param exclusions List of components to be excluded, or null to detach all.
     */
    public static void detach(List<? extends BaseComponent> components, List<? extends BaseComponent> exclusions) {
        for (int i = components.size() - 1; i >= 0; i--) {
            BaseComponent comp = components.get(i);
            
            if (exclusions == null || !exclusions.contains(comp)) {
                comp.detach();
            }
        }
    }
    
    /**
     * Recurses over all children of specified component and enables or disables them.
     * 
     * @param parent Parent whose children's disable status is to be modified.
     * @param disable The disable status for the children.
     */
    public static void disableChildren(BaseComponent parent, boolean disable) {
        List<BaseComponent> children = parent.getChildren();
        
        for (int i = children.size() - 1; i >= 0; i--) {
            BaseComponent comp = children.get(i);
            
            if (comp instanceof BaseUIComponent) {
                ((BaseUIComponent) comp).setDisabled(disable);
            }
            
            disableChildren(comp, disable);
        }
    }
    
    /**
     * Finds the first child component of the specified class, or null if none found;
     * 
     * @param <T> The child class.
     * @param parent Parent whose children are to be searched.
     * @param childClass Class of the child component being sought.
     * @return Child component matching the specified class, or null if not found.
     */
    public static <T extends BaseComponent> T findChild(BaseComponent parent, Class<T> childClass) {
        return findChild(parent, childClass, null);
    }
    
    /**
     * Finds a child component of the specified class, or null if none found;
     * 
     * @param <T> The child class.
     * @param parent Parent whose children are to be searched.
     * @param childClass Class of the child component being sought.
     * @param lastChild Child after which search will begin, or null to begin with first child.
     * @return Child component matching the specified class, or null if not found.
     */
    @SuppressWarnings("unchecked")
    public static <T extends BaseComponent> T findChild(BaseComponent parent, Class<T> childClass, BaseComponent lastChild) {
        BaseComponent child = parent == null ? null
                : lastChild == null ? parent.getFirstChild() : lastChild.getNextSibling();
        
        while (child != null && !childClass.isInstance(child)) {
            child = child.getNextSibling();
        }
        
        return (T) child;
    }
    
    /**
     * Finds the first ancestor that belongs to the specified class.
     * 
     * @param <T> The ancestor class.
     * @param child Child whose ancestors are to be searched.
     * @param ancestorClass Class being sought.
     * @return Ancestor matching requested class, or null if not found.
     */
    @SuppressWarnings("unchecked")
    public static <T extends BaseComponent> T findAncestor(BaseComponent child, Class<T> ancestorClass) {
        BaseComponent ancestor = child == null ? null : child.getParent();
        
        while (ancestor != null && !ancestorClass.isInstance(ancestor)) {
            ancestor = ancestor.getParent();
        }
        
        return (T) ancestor;
    }
    
    /**
     * Returns the first visible child, if any.
     * 
     * @param parent The parent component.
     * @param recurse If true, all descendant levels are also searched using a breadth first
     *            strategy.
     * @return The first visible child encountered, or null if not found.
     */
    public static BaseComponent firstVisibleChild(BaseComponent parent, boolean recurse) {
        return firstVisibleChild(parent, BaseUIComponent.class, recurse);
    }
    
    /**
     * Returns the first visible child of a given class, if any.
     * 
     * @param <T> The child class.
     * @param parent The parent component.
     * @param clazz The child class to consider.
     * @param recurse If true, all descendant levels are also searched using a breadth first
     *            strategy.
     * @return The first visible child encountered, or null if not found.
     */
    @SuppressWarnings("unchecked")
    public static <T extends BaseUIComponent> T firstVisibleChild(BaseComponent parent, Class<T> clazz, boolean recurse) {
        for (BaseComponent child : parent.getChildren()) {
            if (clazz.isInstance(child) && ((BaseUIComponent) child).isVisible()) {
                return (T) child;
            }
        }
        
        if (recurse) {
            for (BaseComponent child : parent.getChildren()) {
                T comp = firstVisibleChild(child, clazz, recurse);
                
                if (comp != null) {
                    return comp;
                }
            }
        }
        
        return null;
    }
    
    /**
     * Sets focus to first input element under the parent that is capable of receiving focus.
     * 
     * @param parent Parent component.
     * @param select If true, select contents after setting focus.
     * @return The input element that received focus, or null if focus was not set.
     */
    public static BaseInputboxComponent focusFirst(BaseComponent parent, boolean select) {
        for (BaseComponent child : parent.getChildren()) {
            BaseInputboxComponent ele;
            
            if (child instanceof BaseInputboxComponent) {
                ele = (BaseInputboxComponent) child;
                
                if (ele.isVisible() && !ele.isDisabled() && !ele.isReadonly()) {
                    ele.focus();
                    
                    if (select) {
                        ele.select();
                    }
                    
                    return ele;
                }
            } else if ((ele = focusFirst(child, select)) != null) {
                return ele;
            }
        }
        
        return null;
    }
    
    /**
     * Moves a child to the position specified by an index.
     * 
     * @param child Child to move
     * @param index Move child to this position.
     */
    public static void moveChild(BaseComponent child, int index) {
        if (child != null && child.getParent() != null) {
            child.getParent().addChild(child, index);
        }
    }
    
    /**
     * Swaps the position of the two child components.
     * 
     * @param child1 The first child.
     * @param child2 The second child.
     */
    public static void swapChildren(BaseComponent child1, BaseComponent child2) {
        BaseComponent parent = child1.getParent();
        
        if (parent == null || parent != child2.getParent()) {
            throw new IllegalArgumentException("Components do not have the same parent.");
        }
        
        if (child1 != child2) {
            int idx1 = child1.getNextSibling().indexOf();
            int idx2 = child2.getNextSibling().indexOf();
            parent.addChild(child1, idx2);
            parent.addChild(child2, idx1);
        }
    }
    
    /**
     * Returns named boolean attribute from the specified component.
     * 
     * @param c The component containing the desired attribute.
     * @param attr The name of the attribute.
     * @return Value of named attribute, or null if not found.
     */
    public static boolean getAttributeBoolean(BaseComponent c, String attr) {
        Boolean val = getAttribute(c, attr, Boolean.class);
        return val != null ? val : BooleanUtils.toBoolean(getAttributeString(c, attr));
    }
    
    /**
     * Returns named String attribute from the specified component.
     * 
     * @param c The component containing the desired attribute.
     * @param attr The name of the attribute.
     * @return Value of named attribute, or null if not found.
     */
    public static String getAttributeString(BaseComponent c, String attr) {
        String val = getAttribute(c, attr, String.class);
        return val == null ? "" : val;
    }
    
    /**
     * Returns named integer attribute from the specified component.
     * 
     * @param c The component containing the desired attribute.
     * @param attr The name of the attribute.
     * @param defaultVal The default value
     * @return Value of named attribute, or null if not found.
     */
    public static int getAttributeInt(BaseComponent c, String attr, int defaultVal) {
        Integer val = getAttribute(c, attr, Integer.class);
        return val != null ? val : defaultVal;
    }
    
    /**
     * Returns named List attribute from the specified component.
     * 
     * @param c The component containing the desired attribute.
     * @param attr The name of the attribute.
     * @return Value of named attribute, or null if not found.
     */
    public static List<?> getAttributeList(BaseComponent c, String attr) {
        return getAttribute(c, attr, List.class);
    }
    
    /**
     * Returns named List attribute from the specified component.
     * 
     * @param <T> Class of the list elements.
     * @param c The component containing the desired attribute.
     * @param attr The name of the attribute.
     * @param elementClass The expected class of the list elements.
     * @return Value of named attribute, or null if not found.
     */
    @SuppressWarnings("unchecked")
    public static <T> List<T> getAttributeList(BaseComponent c, String attr, Class<T> elementClass) {
        return getAttribute(c, attr, List.class);
    }
    
    /**
     * Returns named BaseComponent attribute from the specified component.
     * 
     * @param c The component containing the desired attribute.
     * @param attr The name of the attribute.
     * @return Value of named attribute, or null if not found.
     */
    public static BaseComponent getAttributeComponent(BaseComponent c, String attr) {
        return getAttribute(c, attr, BaseComponent.class);
    }
    
    /**
     * Returns named attribute of the specified type from the specified component.
     * 
     * @param <T> Class of the attribute value.
     * @param c The component containing the desired attribute.
     * @param attr The name of the attribute.
     * @param clazz The expected type of the attribute value.
     * @return Value of named attribute, or null if not found.
     */
    @SuppressWarnings("unchecked")
    public static <T> T getAttribute(BaseComponent c, String attr, Class<T> clazz) {
        Object val = c == null || attr == null ? null : c.getAttribute(attr);
        return clazz.isInstance(val) ? (T) val : null;
    }
    
    /**
     * Places a semi-transparent mask over the specified component to disable user interaction.
     * 
     * @param c BaseComponent to be disabled.
     * @param caption Caption text to appear over disabled component.
     */
    public static void addMask(BaseComponent c, String caption) {
        addMask(c, caption, null, null);
    }
    
    /**
     * Places a semi-transparent mask over the specified component to disable user interaction.
     * 
     * @param c BaseComponent to be disabled.
     * @param caption Caption text to appear over disabled component.
     * @param popup Optional popup to display when context menu is invoked.
     */
    public static void addMask(BaseComponent c, String caption, Popup popup) {
        addMask(c, caption, popup, null);
    }
    
    /**
     * Places a semi-transparent mask over the specified component to disable user interaction.
     * 
     * @param component BaseComponent to be disabled.
     * @param caption Caption text to appear over disabled component.
     * @param popup Optional popup to display when context menu is invoked.
     * @param hint Optional tooltip text.
     */
    public static void addMask(BaseComponent component, String caption, Popup popup, String hint) {
        String anchor = getMaskAnchor(component);
        ClientUtil.invoke("cwf_addMask", component, new Object[] { component, caption, popup, hint, anchor });
    }
    
    /**
     * Removes any disable mask over a component.
     * 
     * @param component The component.
     */
    public static void removeMask(BaseComponent component) {
        ClientUtil.invoke("cwf_removeMask", component, new Object[] { component });
    }
    
    /**
     * Returns the uuid of the mask's anchor..
     * 
     * @param component BaseComponent
     * @return The subid of the mask anchor, or null if none specified.
     */
    private static String getMaskAnchor(BaseComponent component) {
        return (String) component.getAttribute(MASK_ANCHOR);
    }
    
    /**
     * Sets the subid of the real mask anchor.
     * 
     * @param component BaseComponent
     * @param target The subid of the real anchor, or null to remove existing anchor.
     */
    public static void setMaskAnchor(BaseComponent component, String target) {
        component.setAttribute(MASK_ANCHOR, target);
    }
    
    /**
     * Adds or removes a badge to/from an HTML element.
     * 
     * @param selector Selector for the target element.
     * @param label Text for the badge. Null value removes any existing badge.
     * @param classes Optional additional CSS classes for badge.
     */
    public static void setBadge(String selector, String label, String classes) {
        ClientUtil.invoke("cwf.setBadge", new Object[] { selector, label, classes });
    }
    
    /**
     * Wires a controller (events and component references) for the specified component where the
     * component serves as its own controller.
     * 
     * @param component The source component.
     */
    public static void wireController(BaseComponent component) {
        wireController(component, component);
    }
    
    /**
     * Wires a controller (events and component references) for the specified component.
     * 
     * @param component The source component.
     * @param controller The controller to be wired.
     */
    public static void wireController(BaseComponent component, Object controller) {
        if (controller != null) {
            WiredComponentScanner.wire(controller, component);
            EventHandlerScanner.wire(controller, component);
        }
    }
    
    /**
     * Wires variables from a map into a controller. Useful to inject parameters passed in an
     * argument map.
     * 
     * @param map The argument map.
     * @param controller The controller to be wired.
     */
    public static void wireController(Map<?, ?> map, Object controller) {
        if (map == null || map.isEmpty() || controller == null) {
            return;
        }
        
        for (Entry<?, ?> entry : map.entrySet()) {
            String key = entry.getKey().toString();
            Object value = entry.getValue();
            
            try {
                PropertyUtils.setProperty(controller, key, value);
            } catch (Exception e) {
                try {
                    FieldUtils.writeField(controller, key, value, true);
                } catch (Exception e1) {}
            }
            
        }
    }
    
    /**
     * Fires an event, deferring delivery if the desktop of the target is not currently active.
     * 
     * @param event The event to fire.
     */
    public static void fireEvent(Event event) {
        fireEvent(event, deferredDelivery);
    }
    
    /**
     * Fires an event to the specified listener, deferring delivery if the desktop of the target is
     * not currently active.
     * 
     * @param event The event to fire.
     * @param listener The listener to receive the event.
     */
    public static void fireEvent(Event event, IEventListener listener) {
        Page page = event.getTarget() == null ? null : event.getTarget().getPage();
        
        if (page != null && !inEventThread(page)) {
            Executions.schedule(dtp, listener, event);
        } else {
            try {
                listener.onEvent(event);
            } catch (Exception e) {
                throw MiscUtil.toUnchecked(e);
            }
        }
    }
    
    /**
     * Returns true if in the specified page's event thread.
     * 
     * @param page Page instance.
     * @return True if the current event thread is the page's event thread.
     */
    public static boolean inEventThread(Page page) {
        return ExecutionContext.getPage() == page;
    }
    
    /**
     * Formats an exception for display.
     * 
     * @param exc Exception to format.
     * @return The displayable form of the exception.
     */
    public static String formatExceptionForDisplay(Throwable exc) {
        return exc == null ? null : ExceptionUtils.getMessage(ExceptionUtils.getRootCause(exc));
    }
    
    /**
     * Send a print request to the browser client.
     * 
     * @param selectors List of selectors whose content shall be printed.
     * @param styleSheets List of stylesheets to be applied before printing.
     * @param preview If true, open in preview mode. If false, submit directly for printing.
     */
    public static void printToClient(List<String> selectors, List<String> styleSheets, boolean preview) {
        ClientUtil.invoke("cwf_print", null, selectors, styleSheets, preview);
    }
    
    /**
     * Send a print request to the browser client.
     * 
     * @param selectors Comma-delimited list of selectors whose content shall be printed.
     * @param styleSheets Comma-delimited list of stylesheets to be applied before printing.
     * @param preview If true, open in preview mode. If false, submit directly for printing.
     */
    public static void printToClient(String selectors, String styleSheets, boolean preview) {
        ClientUtil.invoke("cwf_print", null, selectors, styleSheets, preview);
    }
    
    /**
     * Provides a more convenient method signature for getting a ZK label with arguments. Also
     * recognizes line continuation with backslash characters.
     * 
     * @param key Label key.
     * @param args Optional argument list.
     * @return The key value or null if not found.
     * @deprecated Use StrUtil.getLabel
     */
    @Deprecated
    public static String getLabel(String key, Object... args) {
        return StrUtil.getLabel(key, args);
    }
    
    /**
     * Returns a component of a type suitable for displaying the specified text. For text that is a
     * URL, returns an anchor. For text that begins with &lt;html&gt;, returns an HTML component.
     * All other text returns a label.
     * 
     * @param text Text to be displayed.
     * @return BaseComponent of the appropriate type.
     */
    public static BaseComponent getTextComponent(String text) {
        String frag = text == null ? "" : StringUtils.substring(text, 0, 20).toLowerCase();
        
        if (frag.contains("<html>")) {
            return new Html(text);
        }
        
        if (frag.matches("^https?:\\/\\/.+$")) {
            Hyperlink anchor = new Hyperlink();
            anchor.setHref(text);
            anchor.setTarget("_blank");
            return anchor;
        }
        
        return new Label(text);
    }
    
    /**
     * Returns the node associated with the specified \-delimited path.
     * 
     * @param <ANCHOR> Class of the anchor component.
     * @param <NODE> Class of the node component.
     * @param root Root component of hierarchy.
     * @param anchorClass Class of the anchor component.
     * @param nodeClass Class of the node component.
     * @param path \-delimited path to search.
     * @param create If true, nodes are created as needed.
     * @param matchMode The match mode.
     * @return The node corresponding to the specified path, or null if not found.
     */
    @SuppressWarnings("unchecked")
    public static <ANCHOR extends BaseComponent, NODE extends BaseComponent> NODE findNode(BaseComponent root,
                                                                                           Class<ANCHOR> anchorClass,
                                                                                           Class<NODE> nodeClass,
                                                                                           String path, boolean create,
                                                                                           MatchMode matchMode) {
        String[] pcs = path.split("\\\\");
        BaseComponent node = null;
        
        try {
            for (String pc : pcs) {
                if (pc.isEmpty()) {
                    continue;
                }
                
                BaseComponent parent = node == null ? root : ZKUtil.findChild(node, anchorClass);
                
                if (parent == null) {
                    if (!create) {
                        return null;
                    }
                    node.addChild(parent = anchorClass.newInstance());
                }
                
                node = null;
                int index = matchMode == MatchMode.INDEX || matchMode == MatchMode.AUTO ? NumberUtils.toInt(pc, -1) : -1;
                MatchMode mode = matchMode != MatchMode.AUTO ? matchMode
                        : index >= 0 ? MatchMode.INDEX : MatchMode.CASE_INSENSITIVE;
                List<BaseComponent> children = parent.getChildren();
                int size = children.size();
                
                if (mode == MatchMode.INDEX) {
                    
                    if (index < 0) {
                        index = size;
                    }
                    
                    int deficit = index - size;
                    
                    if (!create && deficit >= 0) {
                        return null;
                    }
                    
                    while (deficit-- >= 0) {
                        parent.addChild(nodeClass.newInstance());
                    }
                    node = children.get(index);
                    
                } else {
                    for (BaseComponent child : children) {
                        String label = BeanUtils.getProperty(child, "label");
                        
                        if (mode == MatchMode.CASE_SENSITIVE ? pc.equals(label) : pc.equalsIgnoreCase(label)) {
                            node = child;
                            break;
                        }
                    }
                    
                    if (node == null) {
                        if (!create) {
                            return null;
                        }
                        node = nodeClass.newInstance();
                        parent.addChild(node);
                        BeanUtils.setProperty(node, "label", pc);
                    }
                }
                
                if (node == null) {
                    break;
                }
            }
        } catch (Exception e) {
            throw MiscUtil.toUnchecked(e);
        }
        
        return (NODE) node;
    }
    
    /**
     * Converts a JavaScript snippet to serializable form. If the snippet does not have a function
     * wrapper, a no-argument wrapper will be added.
     * 
     * @param snippet JS code snippet.
     * @return A JavaScriptValue object or null if the input was null.
     */
    public static String toJavaScriptValue(String snippet) {
        return snippet == null ? null : snippet.startsWith("function") ? snippet : "function() {" + snippet + "}";
    }
    
    /**
     * Recursively wires input elements to a common event handler for the detection of changes.
     * 
     * @param parent The parent component.
     * @param targetComponent The component to receive the change events.
     * @param targetEvent The name of the event to send to the target component.
     */
    public static void wireChangeEvents(BaseComponent parent, BaseComponent targetComponent, String targetEvent) {
        for (BaseComponent child : parent.getChildren()) {
            String sourceEvents = null;
            
            if (child instanceof Combobox) {
                sourceEvents = ChangeEvent.TYPE + "," + SelectEvent.TYPE;
            } else if (child instanceof BaseInputboxComponent) {
                sourceEvents = ChangeEvent.TYPE;
            } else if (child instanceof Checkbox) {
                sourceEvents = ChangeEvent.TYPE;
            }
            
            if (sourceEvents != null) {
                for (String eventName : sourceEvents.split("\\,")) {
                    child.registerEventListener(eventName, new IEventListener() {
                        
                        @Override
                        public void onEvent(Event event) {
                            event = new Event(targetEvent, targetComponent);
                            EventUtil.post(event);
                        }
                        
                    });
                }
            }
            
            wireChangeEvents(child, targetComponent, targetEvent);
        }
        
    }
    
    /**
     * Enforces static class.
     */
    private ZKUtil() {
    }
}
