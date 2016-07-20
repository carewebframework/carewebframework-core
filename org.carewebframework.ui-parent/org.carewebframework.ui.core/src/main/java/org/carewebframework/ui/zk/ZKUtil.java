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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang.reflect.FieldUtils;
import org.carewebframework.common.MiscUtil;
import org.carewebframework.common.StrUtil;
import org.carewebframework.ui.FrameworkWebSupport;
import org.zkoss.json.JavaScriptValue;
import org.zkoss.lang.Exceptions;
import org.zkoss.zk.au.AuResponse;
import org.zkoss.zk.au.out.AuInvoke;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.ComponentNotFoundException;
import org.zkoss.zk.ui.Desktop;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.HtmlBasedComponent;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.ForwardEvent;
import org.zkoss.zk.ui.ext.Disable;
import org.zkoss.zk.ui.metainfo.PageDefinition;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zk.ui.util.ConventionWires;
import org.zkoss.zul.A;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Html;
import org.zkoss.zul.Label;
import org.zkoss.zul.Popup;
import org.zkoss.zul.impl.InputElement;
import org.zkoss.zul.impl.MeshElement;
import org.zkoss.zul.impl.XulElement;

/**
 * General purpose ZK extensions and convenience methods.
 */
public class ZKUtil {
    
    /**
     * Resource prefix for resources within this package
     */
    public static final String RESOURCE_PREFIX = getResourcePath(ZKUtil.class);
    
    private static final String CUSTOM_COLOR_OVERRIDE = "setCustomColor";
    
    private static final String MASK_ANCHOR = "maskTarget";
    
    private static final EventListener<Event> deferredDelivery = new EventListener<Event>() {
        
        @Override
        public void onEvent(Event event) throws Exception {
            Events.sendEvent(event);
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
     * Loads a page definition from the cache. If not present in the cache, the page is created and
     * placed in the cache.
     * 
     * @param filename Filename of the zul page.
     * @return A page definition.
     */
    public static PageDefinition loadCachedPageDefinition(String filename) {
        Validate.notNull(filename, "'filename' argument must not be null");
        return ZulGlobalCache.getInstance().get(filename);
    }
    
    /**
     * Loads a page definition from a zul page. If the page is an embedded resource, prefix the file
     * name with the owning class name followed by a colon. For example,
     * "org.carewebframework.ui.component.PatientSelection:patientSelection.zul"
     * 
     * @param filename File name pointing to the zul page.
     * @return A page definition representing the zul page.
     */
    public static PageDefinition loadZulPageDefinition(String filename) {
        InputStream is = null;
        InputStreamReader reader = null;
        Class<?> containingClass = null;
        int i = filename.indexOf(":");
        
        try {
            if (i > 0) {
                containingClass = Class.forName(filename.substring(0, i));
                filename = filename.substring(i + 1);
            }
            
            if (containingClass == null) {
                return Executions.getCurrent().getPageDefinition(filename.replace("/zkau/web/", "~./"));
            }
            
            is = containingClass.getResourceAsStream(filename);
            
            if (is == null) {
                throw new IOException("Zul page not found: " + filename);
            }
            
            reader = new InputStreamReader(is);
            return Executions.getCurrent().getPageDefinitionDirectly(reader, null);
        } catch (Exception e) {
            IOUtils.closeQuietly(reader);
            IOUtils.closeQuietly(is);
            throw MiscUtil.toUnchecked(e);
        }
    }
    
    /**
     * Loads a page definition from a zul page. If the page is an embedded resource, prefix the file
     * name with the owning class name followed by a colon. For example,
     * "org.carewebframework.ui.component.PatientSelection:patientSelection.zul" Also, extracts any
     * query parameters present and returns them in the specified map.
     * 
     * @param url url of the zul page and, optionally, may include query parameters.
     * @param args Upon return, will be populated by any query parameters present in the url.
     * @return A page definition representing the zul page.
     */
    public static PageDefinition loadZulPageDefinition(String url, Map<Object, Object> args) {
        String pcs[] = url.split("\\?", 2);
        
        if (args != null) {
            Map<String, String> queryArgs = (pcs.length < 2 ? null : FrameworkWebSupport.queryStringToMap(pcs[1]));
            
            if (queryArgs != null) {
                args.putAll(queryArgs);
            }
        }
        
        return loadZulPageDefinition(pcs[0]);
    }
    
    /**
     * Loads a zul page. May contain a query string which is passed as an argument map.
     * 
     * @param filename File name pointing to the zul page.
     * @param parent Component to become the parent of the created page.
     * @return The top level component of the created zul page.
     */
    public static Component loadZulPage(String filename, Component parent) {
        Map<Object, Object> args = new HashMap<>();
        PageDefinition pageDefinition;
        try {
            pageDefinition = loadZulPageDefinition(filename, args);
        } catch (Exception e) {
            throw MiscUtil.toUnchecked(e);
        }
        return Executions.getCurrent().createComponents(pageDefinition, parent, args);
    }
    
    /**
     * Loads a zul page with additional arguments passed as an argument map.
     * 
     * @param filename File name pointing to the zul page.
     * @param parent Component to become the parent of the created page.
     * @param args Arguments to pass to zul page.
     * @return The top level component of the created zul page.
     */
    public static Component loadZulPage(String filename, Component parent, Map<Object, Object> args) {
        return loadZulPage(filename, parent, args, null);
    }
    
    /**
     * Loads a zul page with additional arguments passed as an argument map.
     * 
     * @param filename File name pointing to the zul page.
     * @param parent Component to become the parent of the created page.
     * @param args Arguments to pass to zul page.
     * @param controller If specified, the zul page is autowired to the controller.
     * @return The top level component of the created zul page.
     */
    public static Component loadZulPage(String filename, Component parent, Map<Object, Object> args, Object controller) {
        PageDefinition pageDefinition = loadZulPageDefinition(filename);
        Component top = Executions.getCurrent().createComponents(pageDefinition, parent, args);
        wireController(top, controller);
        return top;
    }
    
    /**
     * Creates a dialog for the specified class. Autowires variables and forwards and applies the
     * onMove event listener to restrict movement to the browser view port. This makes several
     * assumptions. First, the zul page backed by the class must be named the same as the class with
     * a lowercase first character. Second, the zul page must be located in a subfolder named the
     * same as the fully qualified package name and located under the web folder. Third, the top
     * level component of the zul page must correspond to the specified class. For example, if the
     * class is org.carewebframework.sample.MyClass, the corresponding zul page must be
     * web/org/carewebframework/sample/myClass.zul and the top level component in the zul page must
     * be of the type myClass.
     * 
     * @param <T> Type
     * @param clazz Class backing the dialog.
     * @return Instance of the dialog.
     * @throws Exception if problem occurs creating dialog
     */
    @SuppressWarnings("unchecked")
    public static <T extends Component> T createDialog(Class<T> clazz) throws Exception {
        String zul = getResourcePath(clazz) + StringUtils.uncapitalize(clazz.getSimpleName()) + ".zul";
        T dialog = (T) ZKUtil.loadZulPage(zul, null);
        MoveEventListener.add(dialog);
        wireController(dialog);
        return dialog;
    }
    
    /**
     * Detaches all children from a parent.
     * 
     * @param parent Parent component.
     */
    public static void detachChildren(Component parent) {
        detachChildren(parent, null);
    }
    
    /**
     * Detaches children from a parent. If exclusions is not null, any exclusions will not be
     * removed.
     * 
     * @param parent Parent component.
     * @param exclusions List of child components to be excluded, or null to detach all children.
     */
    public static void detachChildren(Component parent, List<? extends Component> exclusions) {
        detach(parent.getChildren(), exclusions);
    }
    
    /**
     * Detaches a list of components.
     * 
     * @param components List of components to detach.
     */
    public static void detach(List<? extends Component> components) {
        detach(components, null);
    }
    
    /**
     * Detaches a list of components. If exclusions is not null, any exclusions will not be removed.
     * 
     * @param components List of components to detach.
     * @param exclusions List of components to be excluded, or null to detach all.
     */
    public static void detach(List<? extends Component> components, List<? extends Component> exclusions) {
        for (int i = components.size() - 1; i >= 0; i--) {
            Component comp = components.get(i);
            
            if (exclusions == null || !exclusions.contains(comp)) {
                comp.detach();
            }
        }
    }
    
    /**
     * Recurses over all children of specified component that implement the Disable interface or a
     * "disabled" property and enables or disables them.
     * 
     * @param parent Parent whose children's disable status is to be modified.
     * @param disable The disable status for the children.
     */
    public static void disableChildren(Component parent, boolean disable) {
        List<Component> children = parent.getChildren();
        
        for (int i = children.size() - 1; i >= 0; i--) {
            Component comp = children.get(i);
            
            if (comp instanceof Disable) {
                ((Disable) comp).setDisabled(disable);
            } else {
                try {
                    PropertyUtils.setSimpleProperty(comp, "disabled", disable);
                } catch (Exception e) {}
            }
            
            disableChildren(comp, disable);
        }
    }
    
    /**
     * Provides a means to suppress the display of the default browser context menu when
     * right-clicking on a xul element that has not specified its own context menu. It sets the xul
     * element's context to a dummy popup component with no children and styled to be invisible.
     * 
     * @param component Element to receive dummy context menu.
     */
    public static void suppressContextMenu(XulElement component) {
        suppressContextMenu(component, false);
    }
    
    /**
     * Provides a means to suppress the display of the default browser context menu when
     * right-clicking on a xul element that has not specified its own context menu. It sets the xul
     * element's context to a dummy popup component with no children and styled to be invisible.
     * 
     * @param component Element to receive dummy context menu.
     * @param noReplace If true and a context menu exists for the component, do not replace it.
     */
    public static void suppressContextMenu(XulElement component, boolean noReplace) {
        if (noReplace && component.getContext() != null) {
            return;
        }
        
        Popup popup = new Popup();
        popup.setPage(component.getPage());
        popup.setStyle("height:0;width:0;overflow:hidden");
        component.setContext(popup);
    }
    
    /**
     * Returns the original event. Walks the chain of forwarded events until it encounters the
     * original event.
     * 
     * @param event The event whose origin is sought.
     * @return The original event in a chain of zero or more forwarded events.
     */
    public static Event getEventOrigin(Event event) {
        while (event instanceof ForwardEvent) {
            event = ((ForwardEvent) event).getOrigin();
        }
        
        return event;
    }
    
    /**
     * Removes event listeners with the specified event name from a component.
     * 
     * @param component Component whose event listeners are to be removed.
     * @param evtnm The name of the event.
     */
    public static void removeEventListeners(Component component, String evtnm) {
        removeEventListeners(component, evtnm, null);
    }
    
    /**
     * Removes event listeners with the specified event name and, optionally, of the specified
     * class, from a component.
     * 
     * @param component Component whose event listeners are to be removed.
     * @param evtnm The name of the event.
     * @param elClass Class of the event listener to be removed (null to ignore).
     */
    public static void removeEventListeners(Component component, String evtnm, Class<?> elClass) {
        List<EventListener<?>> list = new ArrayList<>();
        
        for (EventListener<?> el : component.getEventListeners(evtnm)) {
            if (elClass == null || elClass.equals(el.getClass())) {
                list.add(el);
            }
        }
        
        for (EventListener<?> el : list) {
            component.removeEventListener(evtnm, el);
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
    public static <T extends Component> T findChild(Component parent, Class<T> childClass) {
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
    public static <T extends Component> T findChild(Component parent, Class<T> childClass, Component lastChild) {
        Component child = parent == null ? null : lastChild == null ? parent.getFirstChild() : lastChild.getNextSibling();
        
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
    public static <T extends Component> T findAncestor(Component child, Class<T> ancestorClass) {
        Component ancestor = child == null ? null : child.getParent();
        
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
    public static Component firstVisibleChild(Component parent, boolean recurse) {
        return firstVisibleChild(parent, Component.class, recurse);
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
    public static <T extends Component> T firstVisibleChild(Component parent, Class<T> clazz, boolean recurse) {
        for (Component child : parent.getChildren()) {
            if (clazz.isInstance(child) && child.isVisible()) {
                return (T) child;
            }
        }
        
        if (recurse) {
            for (Component child : parent.getChildren()) {
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
    public static InputElement focusFirst(Component parent, boolean select) {
        for (Component child : parent.getChildren()) {
            InputElement ele;
            
            if (child instanceof InputElement) {
                ele = (InputElement) child;
                
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
    public static void moveChild(Component child, int index) {
        if (child == null) {
            return;
        }
        
        Component parent = child.getParent();
        Component refChild = parent.getChildren().get(index);
        
        if (child != refChild) {
            parent.insertBefore(child, refChild);
        }
    }
    
    /**
     * Swaps the position of the two child components.
     * 
     * @param child1 The first child.
     * @param child2 The second child.
     */
    public static void swapChildren(Component child1, Component child2) {
        Component parent = child1.getParent();
        
        if (parent == null || parent != child2.getParent()) {
            throw new IllegalArgumentException("Components do not have the same parent.");
        }
        
        if (child1 != child2) {
            Component ref1 = child1.getNextSibling();
            Component ref2 = child2.getNextSibling();
            parent.insertBefore(child1, ref2);
            parent.insertBefore(child2, ref1);
        }
    }
    
    /**
     * Returns a component of the specified ID in the same ID space. Components in the same ID space
     * are called fellows. This is mainly used in cases where you can't call a method/function (e.g.
     * EL in zul). This may be deprecated after we move to ZK5, which should provide what we need
     * via <a href="http://docs.zkoss.org/wiki/Zk.Widget#Overview:_zk.Widget">ZK Widget</a>.
     * 
     * @see org.zkoss.zk.ui.Component
     * @param component Component to search for fellow
     * @param id fellow component's id
     * @return Component fellow component or null if not found
     * @throws org.zkoss.zk.ui.ComponentNotFoundException When no fellow component found
     */
    public static Component getFellow(Component component, String id) throws ComponentNotFoundException {
        return component.getFellow(id);
    }
    
    /**
     * Returns a component of the specified ID in the same ID space. Components in the same ID space
     * are called fellows. This is mainly used in cases where you can't call a method/function (e.g.
     * EL in zul). This may be deprecated after we move to ZK5, which should provide what we need
     * via <a href="http://docs.zkoss.org/wiki/Zk.Widget#Overview:_zk.Widget">ZK Widget</a>.
     * 
     * @see org.zkoss.zk.ui.Component
     * @param component Component to search for fellow
     * @param id fellow component's id
     * @return Component fellow component or null if not found
     */
    public static Component getFellowIfAny(Component component, String id) {
        return component.getFellowIfAny(id);
    }
    
    /**
     * Adds, removes, or replaces a style in a component.
     * 
     * @param component The component whose style is to be modified.
     * @param styleName The name of the style to modify.
     * @param styleValue The new value for the style. If null or empty, the style is removed if it
     *            exists. Otherwise, if the style does not already exist, it is added with this
     *            value. If the style already exists, its value is replaced.
     * @return The previous value for the style. Returns null if the style did not previously exist.
     */
    public static String updateStyle(HtmlBasedComponent component, String styleName, String styleValue) {
        String style = component.getStyle();
        
        if (StringUtils.isEmpty(style) && StringUtils.isEmpty(styleValue)) {
            return null;
        }
        
        String[] styles = style == null ? null : style.split("\\;");
        StringBuilder sb = new StringBuilder();
        String oldValue = null;
        boolean found = false;
        
        if (styles != null) {
            for (String aStyle : styles) {
                String[] nvp = aStyle.split("\\:", 2);
                
                if (nvp.length == 0) {
                    continue;
                }
                
                String name = nvp[0].trim();
                String val = nvp.length < 2 ? "" : nvp[1];
                
                if (name.equals(styleName)) {
                    found = true;
                    oldValue = val;
                    val = styleValue;
                }
                
                if (!StringUtils.isEmpty(val)) {
                    sb.append(name).append(':').append(val).append(';');
                }
            }
        }
        
        if (!found && !StringUtils.isEmpty(styleValue)) {
            sb.append(styleName).append(':').append(styleValue).append(';');
        }
        
        component.setStyle(sb.length() == 0 ? null : sb.toString());
        return oldValue;
    }
    
    /**
     * Adds or removes a class from a component's sclass property, preserving any other class names
     * that may be present.
     * 
     * @param component Component whose sclass will be modified.
     * @param className The class name to add or remove.
     * @param remove If true, the class is removed; otherwise, it is appended.
     * @return Returns the original sclass property value.
     */
    public static String updateSclass(HtmlBasedComponent component, String className, boolean remove) {
        String oclass = component.getSclass();
        component.setSclass(updateCSSclass(oclass, className, remove));
        return oclass;
    }
    
    /**
     * Adds or removes a class from a component's zclass property, preserving any other class names
     * that may be present.
     * 
     * @param component Component whose zclass will be modified.
     * @param className The class name to add or remove.
     * @param remove If true, the class is removed; otherwise, it is appended.
     * @return Returns the original zclass property value.
     */
    public static String updateZclass(HtmlBasedComponent component, String className, boolean remove) {
        String oclass = component.getZclass();
        component.setZclass(updateCSSclass(oclass, className, remove));
        return oclass;
    }
    
    /**
     * Adds or removes a class from a list of style classes, preserving any other class names that
     * may be present.
     * 
     * @param oclass Current style classes.
     * @param className The class name to add or remove.
     * @param remove If true, the class is removed; otherwise, it is appended.
     * @return Returns the new class values.
     */
    public static String updateCSSclass(String oclass, String className, boolean remove) {
        if (StringUtils.isEmpty(oclass)) {
            return remove ? null : className;
        }
        
        String nclass = " " + oclass + " ";
        className = " " + className + " ";
        boolean exists = nclass.contains(className);
        
        if (exists == remove) {
            if (remove) {
                nclass = nclass.replace(className, " ");
            } else {
                nclass = oclass + className;
            }
            
            return StringUtils.trimToNull(nclass);
        }
        
        return oclass;
    }
    
    /**
     * Toggles between two mutually exclusive sclass values, depending on the given boolean value.
     * 
     * @param component Component whose sclass will be modified.
     * @param classIfTrue Style class to be applied if value is true.
     * @param classIfFalse Style class to be applied if value is false.
     * @param value Value to determine which class is added and which is removed.
     * @return Returns the original sclass property value.
     */
    public static String toggleSclass(HtmlBasedComponent component, String classIfTrue, String classIfFalse, boolean value) {
        String oclass = component.getSclass();
        updateSclass(component, classIfTrue, !value);
        updateSclass(component, classIfFalse, value);
        return oclass;
    }
    
    /**
     * Returns named boolean attribute from the specified component.
     * 
     * @param c The component containing the desired attribute.
     * @param attr The name of the attribute.
     * @return Value of named attribute, or null if not found.
     */
    public static boolean getAttributeBoolean(Component c, String attr) {
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
    public static String getAttributeString(Component c, String attr) {
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
    public static int getAttributeInt(Component c, String attr, int defaultVal) {
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
    public static List<?> getAttributeList(Component c, String attr) {
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
    public static <T> List<T> getAttributeList(Component c, String attr, Class<T> elementClass) {
        return getAttribute(c, attr, List.class);
    }
    
    /**
     * Returns named XulElement attribute from the specified component.
     * 
     * @param c The component containing the desired attribute.
     * @param attr The name of the attribute.
     * @return Value of named attribute, or null if not found.
     */
    public static XulElement getAttributeXulElement(Component c, String attr) {
        return getAttribute(c, attr, XulElement.class);
    }
    
    /**
     * Returns named Component attribute from the specified component.
     * 
     * @param c The component containing the desired attribute.
     * @param attr The name of the attribute.
     * @return Value of named attribute, or null if not found.
     */
    public static Component getAttributeComponent(Component c, String attr) {
        return getAttribute(c, attr, Component.class);
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
    public static <T> T getAttribute(Component c, String attr, Class<T> clazz) {
        Object val = c == null || attr == null ? null : c.getAttribute(attr);
        return clazz.isInstance(val) ? (T) val : null;
    }
    
    /**
     * Invokes a JavaScript function on the client.
     * 
     * @param functionName The function name.
     * @param depends Component on which function execution depends. If the component is removed
     *            before the execution request is sent, the request will be removed. May be null to
     *            indicate no dependency.
     * @param args List of arguments for the function.
     */
    public static void invokeJS(String functionName, Component depends, Object... args) {
        AuResponse rsp = new AuResponse(functionName, depends, args);
        Clients.response(rsp);
    }
    
    /**
     * Adds watermark text to the specified component.
     * 
     * @param c Component to receive watermark text.
     * @param watermark The watermark text.
     */
    public static void addWatermark(Component c, String watermark) {
        addWatermark(c, watermark, "gray", null, false);
    }
    
    /**
     * Adds watermark (placeholder) text to the specified component.
     * 
     * @param c Component to receive watermark text.
     * @param watermark The watermark text.
     * @param color Optional color for watermark text (may be null).
     * @param font Optional font for watermark text (may be null).
     * @param hideOnFocus True sets visibility to true on component focus
     */
    public static void addWatermark(Component c, String watermark, String color, String font, boolean hideOnFocus) {
        invokeJS("cwf_addWatermark", c, c.getUuid(), watermark, color, font, hideOnFocus);
    }
    
    /**
     * Removes a watermark from the specified component.
     * 
     * @param c Component whose watermark is to be removed.
     */
    public static void removeWatermark(Component c) {
        addWatermark(c, null);
    }
    
    /**
     * Places a semi-transparent mask over the specified component to disable user interaction.
     * 
     * @param c Component to be disabled.
     * @param caption Caption text to appear over disabled component.
     */
    public static void addMask(Component c, String caption) {
        addMask(c, caption, null, null);
    }
    
    /**
     * Places a semi-transparent mask over the specified component to disable user interaction.
     * 
     * @param c Component to be disabled.
     * @param caption Caption text to appear over disabled component.
     * @param popup Optional popup to display when context menu is invoked.
     */
    public static void addMask(Component c, String caption, Popup popup) {
        addMask(c, caption, popup, null);
    }
    
    /**
     * Places a semi-transparent mask over the specified component to disable user interaction.
     * 
     * @param component Component to be disabled.
     * @param caption Caption text to appear over disabled component.
     * @param popup Optional popup to display when context menu is invoked.
     * @param hint Optional tooltip text.
     */
    public static void addMask(Component component, String caption, Popup popup, String hint) {
        String anchor = getMaskAnchor(component);
        AuResponse rsp = new AuResponse("cwf_addMask", component, new Object[] { component, caption, popup, hint, anchor });
        Clients.response(rsp);
    }
    
    /**
     * Removes any disable mask over a component.
     * 
     * @param component The component.
     */
    public static void removeMask(Component component) {
        AuResponse rsp = new AuResponse("cwf_removeMask", component, new Object[] { component });
        Clients.response(rsp);
    }
    
    /**
     * Returns the uuid of the mask's anchor..
     * 
     * @param component Component
     * @return The subid of the mask anchor, or null if none specified.
     */
    private static String getMaskAnchor(Component component) {
        return (String) component.getAttribute(MASK_ANCHOR);
    }
    
    /**
     * Sets the subid of the real mask anchor.
     * 
     * @param component Component
     * @param target The subid of the real anchor, or null to remove existing anchor.
     */
    public static void setMaskAnchor(Component component, String target) {
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
        AuResponse rsp = new AuInvoke("cwf.setBadge", new Object[] { selector, label, classes });
        Clients.response(rsp);
    }
    
    /**
     * Wires a controller (events and component references) for the specified component where the
     * component serves as its own controller.
     * 
     * @param component The source component.
     */
    public static void wireController(Component component) {
        wireController(component, component);
    }
    
    /**
     * Wires a controller (events and component references) for the specified component.
     * 
     * @param component The source component.
     * @param controller The controller to be wired.
     */
    public static void wireController(Component component, Object controller) {
        if (controller != null) {
            ConventionWires.wireVariables(component, controller);
            ConventionWires.addForwards(component, controller);
            
            if (!(controller instanceof Component)) {
                Events.addEventListeners(component, controller);
            }
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
    public static void fireEvent(Event event, EventListener<Event> listener) {
        Desktop dtp = event.getTarget() == null ? null : event.getTarget().getDesktop();
        
        if (dtp != null && !inEventThread(dtp)) {
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
     * Returns true if in the specified desktop's event thread.
     * 
     * @param dtp Desktop instance.
     * @return True if the current event thread is the desktop's event thread.
     */
    public static boolean inEventThread(Desktop dtp) {
        return dtp.getExecution() != null && dtp.getExecution() == Executions.getCurrent();
    }
    
    /**
     * Formats an exception for display.
     * 
     * @param exc Exception to format.
     * @return The displayable form of the exception.
     */
    public static String formatExceptionForDisplay(Throwable exc) {
        return exc == null ? null : Exceptions.getMessage(Exceptions.getRealCause(exc));
    }
    
    /**
     * Send a print request to the browser client.
     * 
     * @param selectors List of selectors whose content shall be printed.
     * @param styleSheets List of stylesheets to be applied before printing.
     * @param preview If true, open in preview mode. If false, submit directly for printing.
     */
    public static void printToClient(List<String> selectors, List<String> styleSheets, boolean preview) {
        invokeJS("cwf_print", null, selectors, styleSheets, preview);
    }
    
    /**
     * Send a print request to the browser client.
     * 
     * @param selectors Comma-delimited list of selectors whose content shall be printed.
     * @param styleSheets Comma-delimited list of stylesheets to be applied before printing.
     * @param preview If true, open in preview mode. If false, submit directly for printing.
     */
    public static void printToClient(String selectors, String styleSheets, boolean preview) {
        invokeJS("cwf_print", null, selectors, styleSheets, preview);
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
     * @return Component of the appropriate type.
     */
    public static XulElement getTextComponent(String text) {
        String frag = text == null ? "" : StringUtils.substring(text, 0, 20).toLowerCase();
        
        if (frag.contains("<html>")) {
            return new Html(text);
        }
        
        if (frag.matches("^https?:\\/\\/.+$")) {
            A anchor = new A(text);
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
    public static <ANCHOR extends Component, NODE extends Component> NODE findNode(Component root, Class<ANCHOR> anchorClass,
                                                                                   Class<NODE> nodeClass, String path,
                                                                                   boolean create, MatchMode matchMode) {
        String[] pcs = path.split("\\\\");
        Component node = null;
        
        try {
            for (String pc : pcs) {
                if (pc.isEmpty()) {
                    continue;
                }
                
                Component parent = node == null ? root : ZKUtil.findChild(node, anchorClass);
                
                if (parent == null) {
                    if (!create) {
                        return null;
                    }
                    node.appendChild(parent = anchorClass.newInstance());
                }
                
                node = null;
                int index = matchMode == MatchMode.INDEX || matchMode == MatchMode.AUTO ? NumberUtils.toInt(pc, -1) : -1;
                MatchMode mode = matchMode != MatchMode.AUTO ? matchMode
                        : index >= 0 ? MatchMode.INDEX : MatchMode.CASE_INSENSITIVE;
                List<Component> children = parent.getChildren();
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
                        parent.appendChild(nodeClass.newInstance());
                    }
                    node = children.get(index);
                    
                } else {
                    for (Component child : children) {
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
                        parent.appendChild(node);
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
    public static JavaScriptValue toJavaScriptValue(String snippet) {
        return snippet == null ? null
                : new JavaScriptValue(snippet.startsWith("function") ? snippet : "function() {" + snippet + "}");
    }
    
    /**
     * Applies the specified color setting to the target component. If the target implements a
     * custom method for performing this operation, that method will be invoked. Otherwise, the
     * background color of the target is set.
     * 
     * @param comp Component to receive the color setting.
     * @param color Color value.
     */
    public static void applyColor(HtmlBasedComponent comp, String color) {
        if (comp.getWidgetOverride(CUSTOM_COLOR_OVERRIDE) != null) {
            Executions.getCurrent().addAuResponse(new AuInvoke(comp, CUSTOM_COLOR_OVERRIDE, color));
        } else {
            updateStyle(comp, "background-color", color);
        }
    }
    
    /**
     * Sets the JS code for applying a custom color to a component.
     * 
     * @param comp Target component.
     * @param snippet The JS snippet. If a function body is not supplied, a single argument function
     *            wrapper will be applied.
     */
    public static void setCustomColorLogic(HtmlBasedComponent comp, String snippet) {
        snippet = snippet == null ? null : snippet.startsWith("function") ? snippet : "function(value) {" + snippet + "}";
        comp.setWidgetOverride(CUSTOM_COLOR_OVERRIDE, snippet);
    }
    
    /**
     * Recursively wires input elements to a common event handler for the detection of changes.
     * 
     * @param parent The parent component.
     * @param targetComponent The component to receive the change events.
     * @param targetEvent The name of the event to send to the target component.
     */
    public static void wireChangeEvents(Component parent, Component targetComponent, String targetEvent) {
        for (Component child : parent.getChildren()) {
            String sourceEvents = null;
            
            if (child instanceof Combobox) {
                sourceEvents = Events.ON_CHANGING + "," + Events.ON_SELECT;
            } else if (child instanceof InputElement) {
                if (((InputElement) child).getInstant()) {
                    sourceEvents = Events.ON_CHANGE;
                } else {
                    sourceEvents = Events.ON_CHANGE + "," + Events.ON_CHANGING;
                }
            } else if (child instanceof Checkbox) {
                sourceEvents = Events.ON_CHECK;
            } else if (child instanceof MeshElement) {
                sourceEvents = Events.ON_SELECT;
            }
            
            if (sourceEvents != null) {
                for (String eventName : sourceEvents.split("\\,")) {
                    child.addForward(eventName, targetComponent, targetEvent);
                }
            }
            
            wireChangeEvents(child, targetComponent, targetEvent);
        }
        
    }
    
    /**
     * Enforces static class.
     */
    private ZKUtil() {
    };
}
