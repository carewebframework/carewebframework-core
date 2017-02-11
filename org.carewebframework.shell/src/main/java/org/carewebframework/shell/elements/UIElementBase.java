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
package org.carewebframework.shell.elements;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.carewebframework.api.event.EventManager;
import org.carewebframework.api.event.IEventManager;
import org.carewebframework.shell.AboutDialog;
import org.carewebframework.shell.CareWebShell;
import org.carewebframework.shell.CareWebUtil;
import org.carewebframework.shell.ancillary.INotificationListener;
import org.carewebframework.shell.ancillary.NotificationListeners;
import org.carewebframework.shell.ancillary.RelatedClassMap;
import org.carewebframework.shell.ancillary.SavedState;
import org.carewebframework.shell.ancillary.UIException;
import org.carewebframework.shell.designer.DesignContextMenu;
import org.carewebframework.shell.designer.DesignMask;
import org.carewebframework.shell.designer.DesignMask.MaskMode;
import org.carewebframework.shell.designer.PropertyGrid;
import org.carewebframework.shell.layout.LayoutConstants;
import org.carewebframework.shell.plugins.IPluginResource;
import org.carewebframework.shell.plugins.PluginDefinition;
import org.carewebframework.shell.plugins.PluginRegistry;
import org.carewebframework.shell.property.PropertyInfo;
import org.carewebframework.ui.dialog.DialogUtil;
import org.carewebframework.ui.util.CWFUtil;
import org.carewebframework.web.component.BaseComponent;
import org.carewebframework.web.component.BaseLabeledComponent;
import org.carewebframework.web.component.BaseUIComponent;
import org.carewebframework.web.component.Menupopup;
import org.carewebframework.web.page.PageUtil;

/**
 * This is the base class for all UI elements supported by the CareWeb framework.
 */
public abstract class UIElementBase {
    
    protected static final Log log = LogFactory.getLog(UIElementBase.class);
    
    private static final String ATTR_PREFIX = UIElementBase.class.getName() + ".";
    
    private static final String ASSOC_ELEMENT = ATTR_PREFIX + "AssociatedUIElement";
    
    private static final String CONTEXT_MENU = ATTR_PREFIX + "ContextMenu";
    
    private static final RelatedClassMap allowedParentClasses = new RelatedClassMap();
    
    private static final RelatedClassMap allowedChildClasses = new RelatedClassMap();
    
    private final NotificationListeners parentListeners = new NotificationListeners();
    
    private final NotificationListeners childListeners = new NotificationListeners();
    
    private final List<UIElementBase> children = new ArrayList<>();
    
    protected int maxChildren = 1;
    
    protected boolean autoHide = true;
    
    protected boolean autoEnable = true;
    
    private UIElementBase parent;
    
    private boolean locked;
    
    private boolean enabled = true;
    
    private boolean activated;
    
    private boolean visible = true;
    
    private PluginDefinition definition;
    
    private boolean designMode;
    
    private final DesignMask mask;
    
    private BaseUIComponent innerComponent;
    
    private BaseUIComponent outerComponent;
    
    private String rejectReason;
    
    private String hint;
    
    private String color;
    
    private IEventManager eventManager;
    
    /**
     * A UIElementBase subclass should call this in its static initializer block to register any
     * subclasses that may act as a parent.
     * 
     * @param clazz Class whose valid parent classes are to be registered.
     * @param parentClass Class that may act as a parent to clazz.
     */
    protected static synchronized void registerAllowedParentClass(Class<? extends UIElementBase> clazz,
                                                                  Class<? extends UIElementBase> parentClass) {
        allowedParentClasses.addRelated(clazz, parentClass);
    }
    
    /**
     * A UIElementBase subclass should call this in its static initializer block to register any
     * subclasses that may be a child.
     * 
     * @param clazz Class whose valid child classes are to be registered.
     * @param childClass Class that may be a child of clazz.
     */
    protected static synchronized void registerAllowedChildClass(Class<? extends UIElementBase> clazz,
                                                                 Class<? extends UIElementBase> childClass) {
        allowedChildClasses.addRelated(clazz, childClass);
    }
    
    /**
     * Returns true if childClass can be a child of the parentClass.
     * 
     * @param parentClass Parent class
     * @param childClass Child class
     * @return True if childClass can be a child of the parentClass.
     */
    public static boolean canAcceptChild(Class<? extends UIElementBase> parentClass,
                                         Class<? extends UIElementBase> childClass) {
        return allowedChildClasses.isRelated(parentClass, childClass);
    }
    
    /**
     * Returns true if parentClass can be a parent of childClass.
     * 
     * @param childClass The child class.
     * @param parentClass The parent class.
     * @return True if parentClass can be a parent of childClass.
     */
    public static boolean canAcceptParent(Class<? extends UIElementBase> childClass,
                                          Class<? extends UIElementBase> parentClass) {
        return allowedParentClasses.isRelated(childClass, parentClass);
    }
    
    /**
     * Returns the UI element that registered the CWF component.
     * 
     * @param component The CWF component of interest.
     * @return The associated UI element.
     */
    public static UIElementBase getAssociatedUIElement(BaseComponent component) {
        return component == null ? null : (UIElementBase) component.getAttribute(ASSOC_ELEMENT);
    }
    
    /**
     * Returns the design context menu currently bound to the component.
     * 
     * @param component The CWF component of interest.
     * @return The associated design context menu, or null if none.
     */
    public static Menupopup getDesignContextMenu(BaseComponent component) {
        return component == null ? null : (Menupopup) component.getAttribute(CONTEXT_MENU);
    }
    
    public UIElementBase() {
        mask = new DesignMask(this);
    }
    
    /**
     * Returns the URL of the default template to use in createFromTemplate. Override this method to
     * provide an alternate default URL.
     * 
     * @return The template URL.
     */
    protected String getTemplateUrl() {
        return "web/" + getClass().getName().replace(".", "/") + ".cwf";
    }
    
    /**
     * Create wrapped component(s) from a template (a cwf page). Performs autowiring of variables
     * and events. The template URL is derived from the class name. For example, if the class is
     * "org.carewebframework.xxx.Clazz", the template URL is assumed to be
     * "web/org/carewebframework/xxx/Clazz.cwf".
     * 
     * @return Top level component.
     */
    protected BaseUIComponent createFromTemplate() {
        return createFromTemplate(null);
    }
    
    /**
     * Create wrapped component(s) from specified template (a cwf page). Performs autowiring of
     * variables and events.
     * 
     * @param template URL of cwf page that will serve as a template. If a path is not specified, it
     *            is derived from the package. If the URL is not specified, the template name is
     *            obtained from getTemplateUrl.
     * @return Top level component.
     */
    protected BaseUIComponent createFromTemplate(String template) {
        return createFromTemplate(template, null, this);
    }
    
    /**
     * Create wrapped component(s) from specified template (a cwf page).
     * 
     * @param template URL of cwf page that will serve as a template. If the URL is not specified,
     *            the template name is obtained from getTemplateUrl.
     * @param parent The component that will become the parent.
     * @param controller If specified, events and variables are autowired to the controller.
     * @return Top level component.
     */
    protected BaseUIComponent createFromTemplate(String template, BaseComponent parent, Object controller) {
        if (StringUtils.isEmpty(template)) {
            template = getTemplateUrl();
        } else if (!template.startsWith("web/")) {
            template = CWFUtil.getResourcePath(getClass()) + template;
        }
        
        BaseUIComponent top = null;
        
        try {
            top = (BaseUIComponent) PageUtil.createPage(template, parent).get(0);
            top.wireController(controller);
        } catch (Exception e) {
            UIException.raise("Error creating element from template.", e);
        }
        
        return top;
    }
    
    /**
     * Associates the specified CWF component with this UI element.
     * 
     * @param component CWF component to associate.
     */
    public void associateComponent(BaseComponent component) {
        if (component != null) {
            component.setAttribute(ASSOC_ELEMENT, this);
        }
    }
    
    /**
     * Returns design mode status.
     * 
     * @return True if design mode is active.
     */
    public boolean isDesignMode() {
        return designMode;
    }
    
    /**
     * Sets design mode status for this component and all its children.
     * 
     * @param designMode The design mode flag.
     */
    public void setDesignMode(boolean designMode) {
        this.designMode = designMode;
        setDesignContextMenu(designMode ? DesignContextMenu.getInstance().getMenupopup() : null);
        
        for (UIElementBase child : children) {
            child.setDesignMode(designMode);
        }
        
        updateState();
        mask.update();
    }
    
    /**
     * Apply/remove the design context menu to/from CWF components. This default implementation
     * applies the design context menu to the outer CWF component only. It may be overridden to
     * modify this default behavior.
     * 
     * @param contextMenu The design menu if design mode is activated, or null if it is not.
     */
    protected void setDesignContextMenu(Menupopup contextMenu) {
        setDesignContextMenu(getOuterComponent(), contextMenu);
    }
    
    /**
     * Apply/remove the design context menu to/from the specified component. If applying the design
     * context menu, any existing context menu is saved. When removing the context menu, any saved
     * context menu is restored.
     * 
     * @param component Component to which to apply/remove the design context menu.
     * @param contextMenu The design menu if design mode is activated, or null if it is not.
     */
    protected void setDesignContextMenu(BaseUIComponent component, Menupopup contextMenu) {
        component.setAttribute(CONTEXT_MENU, contextMenu);
        
        if (contextMenu == null) {
            SavedState.restore(component);
            applyHint();
        } else {
            new SavedState(component);
            component.setContext(contextMenu);
            component.setHint(getDefinition().getName());
        }
    }
    
    /**
     * Returns the component that will receive the design mode mask. Override if necessary.
     * 
     * @return The component that will receive the design mode mask.
     */
    public BaseUIComponent getMaskTarget() {
        return getOuterComponent();
    }
    
    /**
     * Updates mask for this element and its children.
     */
    private void updateMasks() {
        mask.update();
        
        for (UIElementBase child : getChildren()) {
            child.updateMasks();
        }
    }
    
    /**
     * Adds the specified child element. The validity of the operation is first tested and an
     * exception thrown if the element is not a valid child for this parent.
     * 
     * @param child Element to add as a child.
     */
    public void addChild(UIElementBase child) {
        addChild(child, true);
    }
    
    /**
     * Adds the specified child element. The validity of the operation is first tested and an
     * exception thrown if the element is not a valid child for this parent.
     * 
     * @param child Element to add as a child.
     * @param doEvent Fires the add child events if true.
     */
    protected void addChild(UIElementBase child, boolean doEvent) {
        if (!child.canAcceptParent(this)) {
            UIException.raise(child.rejectReason);
        }
        
        if (!canAcceptChild(child)) {
            UIException.raise(rejectReason);
        }
        
        if (doEvent) {
            beforeAddChild(child);
        }
        
        if (child.getParent() != null) {
            child.getParent().removeChild(child, false);
        }
        
        children.add(child);
        child.updateParent(this);
        
        if (doEvent) {
            afterAddChild(child);
        }
    }
    
    /**
     * Called after a child is logically added to the parent.
     * 
     * @param child The child element added.
     */
    protected void afterAddChild(UIElementBase child) {
        mask.update();
    }
    
    /**
     * Called before a child is logically added to the parent.
     * 
     * @param child The new child element.
     */
    protected void beforeAddChild(UIElementBase child) {
    }
    
    /**
     * Removes the specified element as a child of this parent.
     * 
     * @param child Child element to remove.
     * @param destroy If true the child is explicitly destroyed.
     */
    public void removeChild(UIElementBase child, boolean destroy) {
        if (!children.contains(child)) {
            return;
        }
        
        boolean isLocked = child.isLocked() || child.getDefinition().isInternal();
        
        if (destroy) {
            child.removeChildren();
            
            if (!isLocked) {
                child.destroy();
            }
        }
        
        if (!isLocked) {
            beforeRemoveChild(child);
            children.remove(child);
            child.updateParent(null);
            afterRemoveChild(child);
        }
    }
    
    /**
     * Called after a child is logically removed from the parent.
     * 
     * @param child The child UI element.
     */
    protected void afterRemoveChild(UIElementBase child) {
        mask.update();
    }
    
    /**
     * Called before a child is logically removed from the parent.
     * 
     * @param child The child UI element.
     */
    protected void beforeRemoveChild(UIElementBase child) {
    }
    
    /**
     * Changes the assigned parent, firing parent changed events if appropriate.
     * 
     * @param newParent The new parent.
     */
    private void updateParent(UIElementBase newParent) {
        UIElementBase oldParent = this.parent;
        
        if (oldParent != newParent) {
            beforeParentChanged(newParent);
            
            if (parent != null && !getDefinition().isInternal()) {
                unbind();
            }
            
            this.parent = newParent;
            
            if (oldParent != null) {
                oldParent.updateState();
            }
            
            if (newParent != null) {
                if (parent != null && !getDefinition().isInternal()) {
                    bind();
                }
                
                afterParentChanged(oldParent);
                newParent.updateState();
                setDesignMode(newParent.isDesignMode());
            }
        }
    }
    
    /**
     * Called after the parent has been changed.
     * 
     * @param oldParent The value of the parent property prior to the change.
     */
    protected void afterParentChanged(UIElementBase oldParent) {
    }
    
    /**
     * Called before the parent has been changed.
     * 
     * @param newParent The value of the parent property prior to the change.
     */
    protected void beforeParentChanged(UIElementBase newParent) {
    }
    
    /**
     * Removes this element from its parent and optionally destroys it.
     * 
     * @param destroy If true, the element is also destroyed.
     */
    public void remove(boolean destroy) {
        if (parent != null) {
            parent.removeChild(this, destroy);
        }
    }
    
    /**
     * Remove and destroy all children associated with this element.
     */
    public void removeChildren() {
        for (int i = children.size() - 1; i >= 0; i--) {
            removeChild(children.get(i), true);
        }
    }
    
    /**
     * Override to implement special cleanup when an object is destroyed.
     */
    public void destroy() {
        unbind();
        processResources(false);
    }
    
    /**
     * Override to bind wrapped components to the UI.
     */
    protected void bind() {
        getParent().getInnerComponent().addChild(getOuterComponent());
    }
    
    /**
     * Override to unbind wrapped components from the UI.
     */
    protected void unbind() {
        getOuterComponent().destroy();
    }
    
    /**
     * Rebind any children. This may be called if the wrapped UI component is recreated.
     */
    protected void rebindChildren() {
        for (UIElementBase child : getChildren()) {
            child.unbind();
            child.bind();
        }
    }
    
    /**
     * Returns the innermost wrapped UI component. For UI elements that may host child elements,
     * this would be the wrapped UI component that can host the child components. For UI elements
     * that wrap a single UI component, getInnerComponent and getOuterComponent should return the
     * same value.
     * 
     * @return The inner component.
     */
    public BaseUIComponent getInnerComponent() {
        return innerComponent == null ? outerComponent : innerComponent;
    }
    
    /**
     * Sets the innermost wrapped UI component.
     * 
     * @param value The innermost wrapped UI component.
     */
    protected void setInnerComponent(BaseUIComponent value) {
        innerComponent = value;
        associateComponent(value);
    }
    
    /**
     * Returns the outermost wrapped UI component. This represents the wrapped UI component that
     * will be the direct child of the UI component wrapped by the parent element. For UI elements
     * that wrap a single UI component, getInnerComponent and getOuterComponent should return the
     * same value.
     * 
     * @return The outer component.
     */
    public BaseUIComponent getOuterComponent() {
        return outerComponent == null ? innerComponent : outerComponent;
    }
    
    /**
     * Sets the outermost wrapped UI component.
     * 
     * @param value The outermost wrapped UI component.
     */
    protected void setOuterComponent(BaseUIComponent value) {
        outerComponent = value;
        associateComponent(value);
    }
    
    /**
     * Return the definition used to create this instance.
     * 
     * @return The plugin definition.
     */
    public final PluginDefinition getDefinition() {
        if (definition == null) {
            setDefinition(getClass());
        }
        
        return definition;
    }
    
    /**
     * Sets the plugin definition for this element.
     * 
     * @param definition The plugin definition.
     */
    public void setDefinition(PluginDefinition definition) {
        if (this.definition != null) {
            if (this.definition == definition) {
                return;
            }
            
            UIException.raise("Cannot modify plugin definition.");
        }
        
        this.definition = definition;
        
        // Assign any default property values.
        if (definition != null) {
            for (PropertyInfo propInfo : definition.getProperties()) {
                String dflt = propInfo.getDefault();
                
                if (dflt != null) {
                    try {
                        propInfo.setPropertyValue(this, dflt);
                    } catch (Exception e) {
                        log.error("Error setting default value for property " + propInfo.getName(), e);
                    }
                }
            }
        }
    }
    
    /**
     * Sets the plugin definition based on the specified class. Typically this would be the same
     * class as the element itself, but in certain cases (as in the UIElementProxy class) it is not.
     * 
     * @param clazz The UI element class.
     */
    public void setDefinition(Class<? extends UIElementBase> clazz) {
        setDefinition(PluginRegistry.getInstance().get(clazz));
    }
    
    /**
     * Displays an about dialog for the UI element.
     */
    public void about() {
        AboutDialog.execute(this);
    }
    
    /**
     * Invokes the property grid with this element as its target.
     */
    public void editProperties() {
        try {
            PropertyGrid.create(this, null);
        } catch (Exception e) {
            DialogUtil.showError("Displaying property grid: \r\n" + e.toString());
        }
    }
    
    /**
     * Set width and height of a component to 100%.
     * 
     * @param component BaseComponent
     */
    protected void fullSize(BaseUIComponent component) {
        component.setWidth("100%");
        component.setHeight("100%");
    }
    
    /**
     * Activates or inactivates a UI element. In general, this method should not be overridden to
     * introduce new behavior. Rather, if a UI element must change its visual state in response to a
     * change in activation state, it should override the updateVisibility method. If a UI element
     * requires special activation logic for its children (e.g., if it allows only one child to be
     * active at a time), it should override the activateChildren method.
     * 
     * @param activate The activate status.
     */
    public void activate(boolean activate) {
        activateChildren(activate);
        activated = activate;
        updateVisibility();
        getEventManager().fireLocalEvent(
            activate ? LayoutConstants.EVENT_ELEMENT_ACTIVATE : LayoutConstants.EVENT_ELEMENT_INACTIVATE, this);
        
        if (activate) {
            mask.update();
        }
    }
    
    /**
     * Default behavior is to pass activation/inactivation event to children. Override to restrict
     * propagation of the event.
     * 
     * @param activate The activate status.
     */
    protected void activateChildren(boolean activate) {
        for (UIElementBase child : children) {
            child.activate(activate);
        }
    }
    
    /**
     * Returns the activation status of the element.
     * 
     * @return The activation status.
     */
    public final boolean isActivated() {
        return activated;
    }
    
    /**
     * Returns instance of the event manager.
     * 
     * @return Event manager instance.
     */
    protected IEventManager getEventManager() {
        if (eventManager == null) {
            eventManager = EventManager.getInstance();
        }
        
        return eventManager;
    }
    
    /**
     * Gets the UI element child at the specified index.
     * 
     * @param index Index of the child to retrieve.
     * @return The child at the specified index.
     */
    public UIElementBase getChild(int index) {
        return children.get(index);
    }
    
    /**
     * Locates and returns a child that is an instance of the specified class. If none is found,
     * returns null.
     * 
     * @param <T> The type of child being sought.
     * @param clazz Class of the child being sought.
     * @param last If specified, the search begins after this child. If null, the search begins with
     *            the first child.
     * @return The requested child or null if none found.
     */
    @SuppressWarnings("unchecked")
    public <T extends UIElementBase> T getChild(Class<T> clazz, UIElementBase last) {
        int i = last == null ? -1 : children.indexOf(last);
        
        for (i++; i < children.size(); i++) {
            if (clazz.isInstance(children.get(i))) {
                return (T) children.get(i);
            }
        }
        
        return null;
    }
    
    /**
     * Returns an iterable of this component's children.
     * 
     * @return Iterable of this component's children.
     */
    public Iterable<UIElementBase> getChildren() {
        return children;
    }
    
    /**
     * Returns an iterable of this component's children.
     * 
     * @param clazz Expected class of children.
     * @return Iterable of this component's children.
     */
    @SuppressWarnings("unchecked")
    public <T extends UIElementBase> Iterable<T> getChildren(Class<T> clazz) {
        return (Iterable<T>) children;
    }
    
    /**
     * Returns an iterable of this component's serializable children. By default, this calls
     * getChildren() but may be overridden to accommodate specialized serialization needs.
     * 
     * @return Iterable of this component's serializable children.
     */
    public Iterable<UIElementBase> getSerializableChildren() {
        return getChildren();
    }
    
    /**
     * Returns the number of children.
     * 
     * @return Number of children.
     */
    public int getChildCount() {
        return children.size();
    }
    
    /**
     * Returns the first child, or null if there are no children.
     * 
     * @return First child, or null if none.
     */
    public UIElementBase getFirstChild() {
        return getChildCount() == 0 ? null : getChild(0);
    }
    
    /**
     * Returns the first visible child.
     * 
     * @return First visible child, or null if none;
     */
    public UIElementBase getFirstVisibleChild() {
        return getVisibleChild(true);
    }
    
    /**
     * Returns the last child, or null if there are no children.
     * 
     * @return Last child, or null if none.
     */
    public UIElementBase getLastChild() {
        return getChildCount() == 0 ? null : getChild(getChildCount() - 1);
    }
    
    /**
     * Returns the last visible child.
     * 
     * @return Last visible child, or null if none;
     */
    public UIElementBase getLastVisibleChild() {
        return getVisibleChild(false);
    }
    
    /**
     * Returns first or last visible child.
     * 
     * @param first If true, find first visible child; if false, last visible child.
     * @return Visible child, or null if none found.
     */
    private UIElementBase getVisibleChild(boolean first) {
        int start = first ? 0 : getChildCount() - 1;
        int end = first ? getChildCount() - 1 : 0;
        int inc = first ? 1 : -1;
        
        for (int i = start; i <= end; i += inc) {
            if (getChild(i).isVisible()) {
                return getChild(i);
            }
        }
        
        return null;
    }
    
    /**
     * Returns this element's next sibling.
     * 
     * @param visibleOnly If true, skip any non-visible siblings.
     * @return The next sibling, or null if none.
     */
    public UIElementBase getNextSibling(boolean visibleOnly) {
        List<UIElementBase> sibs = parent == null ? null : parent.children;
        
        if (sibs != null) {
            for (int i = sibs.indexOf(this) + 1; i < sibs.size(); i++) {
                if (!visibleOnly || sibs.get(i).isVisible()) {
                    return sibs.get(i);
                }
            }
        }
        
        return null;
    }
    
    /**
     * Returns the index of the specified child. If the specified component is not a child, -1 is
     * returned.
     * 
     * @param child The child component whose index is sought.
     * @return The child's index or -1 if not found.
     */
    public int indexOfChild(UIElementBase child) {
        return children.indexOf(child);
    }
    
    /**
     * Returns true if the specified element is a child of this element.
     * 
     * @param element The UI element to test.
     * @return True if the element is a child.
     */
    public boolean hasChild(UIElementBase element) {
        return indexOfChild(element) > -1;
    }
    
    /**
     * Recurses the component subtree for a child belonging to the specified class.
     * 
     * @param <T> The type of child being sought.
     * @param clazz Class of child being sought.
     * @return A child of the specified class, or null if not found.
     */
    @SuppressWarnings("unchecked")
    public <T extends UIElementBase> T findChildElement(Class<T> clazz) {
        for (UIElementBase child : getChildren()) {
            if (clazz.isInstance(child)) {
                return (T) child;
            }
        }
        
        for (UIElementBase child : getChildren()) {
            T child2 = child.findChildElement(clazz);
            
            if (child2 != null) {
                return child2;
            }
        }
        
        return null;
    }
    
    /**
     * Returns true if specified element is an ancestor of this element.
     * 
     * @param element A UI element.
     * @return True if the specified element is an ancestor of this element.
     */
    public boolean hasAncestor(UIElementBase element) {
        UIElementBase child = this;
        
        while (child != null) {
            if (element.hasChild(child)) {
                return true;
            }
            child = child.getParent();
        }
        
        return false;
    }
    
    /**
     * Returns this element's index in its parent's list of children. If this element has no parent,
     * returns -1.
     * 
     * @return This element's index.
     */
    public int getIndex() {
        return parent == null ? -1 : parent.indexOfChild(this);
    }
    
    /**
     * Moves a child from one position to another under the same parent.
     * 
     * @param from Current position of child.
     * @param to New position of child.
     */
    public void moveChild(int from, int to) {
        if (from != to) {
            UIElementBase child = children.get(from);
            UIElementBase ref = children.get(to);
            children.remove(from);
            to = children.indexOf(ref);
            children.add(to, child);
            afterMoveChild(child, ref);
            updateMasks();
        }
    }
    
    /**
     * Element has been moved to a different position under this parent. Adjust wrapped components
     * accordingly.
     * 
     * @param child Child element that was moved.
     * @param before Child element was moved before this one.
     */
    protected void afterMoveChild(UIElementBase child, UIElementBase before) {
        moveChild(child.getOuterComponent(), before.getOuterComponent());
    }
    
    /**
     * Sets this element's index to the specified value. This effectively changes the position of
     * the element relative to its siblings.
     * 
     * @param index The index.
     */
    public void setIndex(int index) {
        UIElementBase parent = getParent();
        
        if (parent == null) {
            UIException.raise("Element has no parent.");
        }
        
        int currentIndex = parent.children.indexOf(this);
        
        if (currentIndex < 0 || currentIndex == index) {
            return;
        }
        
        parent.moveChild(currentIndex, index);
    }
    
    /**
     * Brings this element to the front of the user interface.
     */
    public void bringToFront() {
        if (parent != null) {
            parent.bringToFront();
        } else {
            activate(true);
        }
    }
    
    /**
     * Returns the display name of this element. By default, the definition name is returned, but
     * subclasses may override this to return some other name suitable for display in the design UI.
     * 
     * @return The display name.
     */
    public String getDisplayName() {
        return getDefinition().getName();
    }
    
    /**
     * Returns the instance name of this element. By default, this is the same as the display name,
     * but subclasses may override this to provide additional information that would distinguish
     * multiple instances of the same UI element.
     * 
     * @return The instance name.
     */
    public String getInstanceName() {
        return getDisplayName();
    }
    
    /**
     * Returns the class of the property editor associated with this UI element. Null means no
     * property editor exists.
     * 
     * @return The class of the associated property editor.
     */
    public Class<? extends Object> getPropEditClass() {
        return null;
    }
    
    /**
     * Returns true if the element is locked. When an element is locked, it may not be manipulated
     * within the designer.
     * 
     * @return True if the element is locked.
     */
    public boolean isLocked() {
        return locked;
    }
    
    /**
     * Sets the locked status of the element. When an element is locked, it may not be manipulated
     * within the designer.
     * 
     * @param locked The locked status.
     */
    public void setLocked(boolean locked) {
        this.locked = locked;
    }
    
    /**
     * Returns the parent of this element. May be null.
     * 
     * @return This element's parent.
     */
    public UIElementBase getParent() {
        return parent;
    }
    
    /**
     * Sets the parent of this element, subject to the parent/child constraints applicable to each.
     * 
     * @param parent The new parent.
     */
    public final void setParent(UIElementBase parent) {
        UIElementBase oldParent = this.parent;
        
        if (oldParent == parent) {
            return;
        }
        
        if (oldParent != null) {
            oldParent.removeChild(this, false);
        }
        
        if (parent != null) {
            parent.addChild(this);
        }
    }
    
    /**
     * Sets the enabled state of the component. This base implementation only sets the internal flag
     * and notifies the parent of the state change. Each UI element is responsible for overriding
     * this method and reflecting the enabled state in their wrapped UI components.
     * 
     * @param enabled The enabled state.
     */
    public void setEnabled(boolean enabled) {
        if (this.enabled != enabled) {
            this.enabled = enabled;
            updateParentState();
        }
    }
    
    /**
     * Returns the enabled state of the UI element.
     * 
     * @return True if the UI element is enabled.
     */
    public final boolean isEnabled() {
        return enabled;
    }
    
    /**
     * Sets the visibility state of the component. This base implementation only sets the internal
     * flag and notifies the parent of the state change. Each UI element is responsible for
     * overriding this method and reflecting the visibility state in their wrapped UI components.
     * 
     * @param visible The visibility state.
     */
    public final void setVisible(boolean visible) {
        if (this.visible != visible) {
            this.visible = visible;
            updateVisibility();
            updateParentState();
        }
    }
    
    /**
     * Calls updateVisibility with current settings.
     */
    protected final void updateVisibility() {
        updateVisibility(visible, activated);
    }
    
    /**
     * Override to set the visibility of wrapped components. Invoked when visibility or activation
     * states change.
     * 
     * @param visible The current visibility state.
     * @param activated The current activation state.
     */
    protected void updateVisibility(boolean visible, boolean activated) {
        if (!getDefinition().isInternal()) {
            getOuterComponent().setVisible(visible && activated);
        }
    }
    
    /**
     * Moves a child to before another component.
     * 
     * @param child Child to move
     * @param before Move child to this component.
     */
    protected void moveChild(BaseUIComponent child, BaseUIComponent before) {
        child.getParent().addChild(child, before);
    }
    
    /**
     * Returns the visible state of the UI element.
     * 
     * @return True if the UI element is visible.
     */
    public final boolean isVisible() {
        return visible;
    }
    
    /**
     * Returns the color (as an HTML-formatted RGB string) for this element.
     * 
     * @return An HTML-formatted color specification (e.g., #0F134E). May be null.
     */
    public final String getColor() {
        return color;
    }
    
    /**
     * Provides a default implementation for setting the color of a UI element. This is provided to
     * allow components to easily expose a color property in the property editor. It may not be
     * appropriate for all subclasses. To change which UI elements are affected, override the
     * applyColor() method.
     * 
     * @param value A correctly formatted HTML color specification.
     */
    public final void setColor(String value) {
        color = value;
        applyColor();
    }
    
    /**
     * Provides a default implementation for setting the color of a UI element. Sets the color of
     * the inner and outer components. Override to modify this default behavior.
     */
    protected void applyColor() {
        applyColor(getOuterComponent());
        
        if (getInnerComponent() != getOuterComponent()) {
            applyColor(getInnerComponent());
        }
    }
    
    /**
     * Applies the current color setting to the target component. If the target implements a custom
     * method for performing this operation, that method will be invoked. Otherwise, the background
     * color of the target is set. Override this method to provide alternate implementations.
     * 
     * @param comp Component to receive the color setting.
     */
    protected void applyColor(BaseUIComponent comp) {
        if (comp instanceof BaseLabeledComponent) {
            comp.invoke(comp.sub("lbl"), "css", "color", getColor());
        } else {
            comp.addStyle("background-color", getColor());
        }
    }
    
    /**
     * Returns the tool tip text.
     * 
     * @return The tool tip text.
     */
    public final String getHint() {
        return hint;
    }
    
    /**
     * Sets the tool tip text.
     * 
     * @param value The tool tip text.
     */
    public final void setHint(String value) {
        this.hint = value;
        applyHint();
    }
    
    /**
     * Provides a default implementation for setting the hint text of a UI element. Sets the hint
     * text of the inner and outer components. Override to modify this default behavior.
     */
    protected void applyHint() {
        if (isDesignMode()) {
            return;
        }
        
        applyHint(getOuterComponent());
        
        if (getInnerComponent() != getOuterComponent()) {
            applyHint(getInnerComponent());
        }
    }
    
    /**
     * Applies the current hint text to the target component.
     * 
     * @param comp Component to receive the hint text.
     */
    protected void applyHint(BaseUIComponent comp) {
        comp.setHint(getHint());
    }
    
    /**
     * Calls updateState on the parent if one exists.
     */
    protected final void updateParentState() {
        if (parent != null) {
            parent.updateState();
        }
    }
    
    /**
     * Update a UI element based on the state of its children. The default implementation acts on
     * container elements only and has the following behavior:
     * <ul>
     * <li>If all children are disabled, the parent is also disabled (set autoEnable to false to
     * turn off).</li>
     * <li>If all children are hidden or there are no children and design mode is not active, the
     * parent is also hidden (set autoHide to false to turn off).
     * </ul>
     */
    protected void updateState() {
        if (!isContainer()) {
            return;
        }
        
        boolean anyEnabled = !autoEnable || getChildCount() == 0;
        boolean anyVisible = !autoHide || designMode;
        
        for (UIElementBase child : children) {
            if (anyEnabled && anyVisible) {
                break;
            }
            
            anyEnabled |= child.isEnabled();
            anyVisible |= child.isVisible();
        }
        
        setEnabled(anyEnabled);
        setVisible(anyVisible);
    }
    
    /**
     * Returns true if this UI element can contain other UI elements.
     * 
     * @return True if this UI element can contain other UI elements.
     */
    public boolean isContainer() {
        return allowedChildClasses.hasRelated(getClass());
    }
    
    /**
     * Returns true if this element may accept a child. Updates the reject reason with the result.
     * 
     * @return True if this element may accept a child. Updates the reject reason with the result.
     */
    public boolean canAcceptChild() {
        if (!isContainer()) {
            rejectReason = getDisplayName() + " does not accept any child components.";
        } else if (getChildCount() >= maxChildren) {
            rejectReason = getDisplayName() + " may accept at most " + maxChildren + " child component(s).";
        } else {
            rejectReason = null;
        }
        
        return rejectReason == null;
    }
    
    /**
     * Returns true if this element may accept a child of the specified class. Updates the reject
     * reason with the result.
     * 
     * @param clazz Child class to test.
     * @return True if this element may accept a child of the specified class.
     */
    public boolean canAcceptChild(Class<? extends UIElementBase> clazz) {
        if (!canAcceptChild()) {
            return false;
        }
        
        if (!canAcceptChild(getClass(), clazz)) {
            rejectReason = getDisplayName() + " does not accept " + clazz.getSimpleName() + " as a child.";
        } else {
            rejectReason = null;
        }
        
        return rejectReason == null;
    }
    
    /**
     * Returns true if this element may accept the specified child. Updates the reject reason with
     * the result.
     * 
     * @param child Child instance to test.
     * @return True if this element may accept the specified child.
     */
    public boolean canAcceptChild(UIElementBase child) {
        if (!canAcceptChild()) {
            return false;
        }
        
        if (!canAcceptChild(getClass(), child.getClass())) {
            rejectReason = getDisplayName() + " does not accept " + child.getDisplayName() + " as a child.";
        } else {
            rejectReason = null;
        }
        
        return rejectReason == null;
    }
    
    /**
     * Returns true if this element may accept a parent. Updates the reject reason with the result.
     * 
     * @return True if this element may accept a parent.
     */
    public boolean canAcceptParent() {
        rejectReason = !allowedParentClasses.hasRelated(getClass())
                ? getDisplayName() + " does not accept any parent component." : null;
        return rejectReason == null;
    }
    
    /**
     * Returns true if this element may accept a parent of the specified class. Updates the reject
     * reason with the result.
     * 
     * @param clazz Parent class to test.
     * @return True if this element may accept a parent of the specified class.
     */
    public boolean canAcceptParent(Class<? extends UIElementBase> clazz) {
        if (!canAcceptParent(getClass(), clazz)) {
            rejectReason = getDisplayName() + " does not accept " + clazz.getSimpleName() + " as a parent.";
        } else {
            rejectReason = null;
        }
        
        return rejectReason == null;
    }
    
    /**
     * Returns true if this element may accept the specified element as a parent. Updates the reject
     * reason with the result.
     * 
     * @param parent Parent instance to test.
     * @return True if this element may accept the specified element as a parent.
     */
    public boolean canAcceptParent(UIElementBase parent) {
        if (!canAcceptParent()) {
            return false;
        }
        
        if (!canAcceptParent(getClass(), parent.getClass())) {
            rejectReason = getDisplayName() + " does not accept " + parent.getDisplayName() + " as a parent.";
        } else {
            rejectReason = null;
        }
        
        return rejectReason == null;
    }
    
    /**
     * Sets the reject reason to the specified value.
     * 
     * @param rejectReason Reason for rejection.
     */
    public void setRejectReason(String rejectReason) {
        this.rejectReason = rejectReason;
    }
    
    /**
     * Returns the reject reason. This is updated by the canAcceptParent and canAcceptChild calls.
     * 
     * @return The reject reason.
     */
    public String getRejectReason() {
        return rejectReason;
    }
    
    /**
     * Returns the UI element at the root of the component tree.
     * 
     * @return Root UI element.
     */
    public UIElementBase getRoot() {
        UIElementBase root = this;
        
        while (root.getParent() != null) {
            root = root.getParent();
        }
        
        return root;
    }
    
    /**
     * Returns the first ancestor corresponding to the specified class.
     * 
     * @param <T> The type of ancestor sought.
     * @param clazz Class of ancestor sought.
     * @return An ancestor of the specified class or null if not found.
     */
    @SuppressWarnings("unchecked")
    public <T extends UIElementBase> T getAncestor(Class<T> clazz) {
        UIElementBase parent = getParent();
        
        while (parent != null && !clazz.isInstance(parent)) {
            parent = parent.getParent();
        }
        
        return (T) parent;
    }
    
    /**
     * Subclasses may override this to implement any additional operations that are necessary before
     * this element is initialized (i.e., before property values and parent element are set).
     * 
     * @param deserializing If true, initialization is occurring as a result of deserialization.
     * @throws Exception Unspecified exception.
     */
    public void beforeInitialize(boolean deserializing) throws Exception {
        
    }
    
    /**
     * Subclasses may override this to implement any additional operations that are necessary after
     * this element is initialized (i.e., after property values and parent element are set).
     * 
     * @param deserializing If true, initialization is occurring as a result of deserialization.
     * @throws Exception Unspecified exception.
     */
    public void afterInitialize(boolean deserializing) throws Exception {
        processResources(true);
    }
    
    /**
     * Process all associated resources.
     * 
     * @param register If true, resources will be registered; if false, unregistered.
     */
    private void processResources(boolean register) {
        CareWebShell shell = CareWebUtil.getShell();
        
        for (IPluginResource resource : getDefinition().getResources()) {
            resource.register(shell, this, register);
        }
    }
    
    /**
     * Returns true if the design mode mask is to be used. This mask is used to cover the underlying
     * outer component when in design mode.
     * 
     * @return True if the design mode mask is enabled.
     */
    protected MaskMode getMaskMode() {
        return mask.getMode();
    }
    
    /**
     * Sets whether to use the design mode mask.
     * 
     * @param mode True to enable the design mode mask.
     */
    protected void setMaskMode(MaskMode mode) {
        mask.setMode(mode);
    }
    
    /**
     * Returns true if any associated UI elements in the component subtree are visible.
     * 
     * @param component Component subtree to examine.
     * @return True if any associated UI element in the subtree is visible.
     */
    protected boolean hasVisibleElements(BaseUIComponent component) {
        for (BaseUIComponent child : component.getChildren(BaseUIComponent.class)) {
            UIElementBase ele = getAssociatedUIElement(child);
            
            if (ele != null && ele.isVisible()) {
                return true;
            }
            
            if (hasVisibleElements(child)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Allows a child element to notify its parent of an event of interest.
     * 
     * @param eventName Name of the event.
     * @param eventData Data associated with the event.
     * @param recurse If true, recurse up the parent chain.
     */
    public void notifyParent(String eventName, Object eventData, boolean recurse) {
        UIElementBase ele = parent;
        
        while (ele != null) {
            recurse &= ele.parentListeners.notify(this, eventName, eventData);
            ele = recurse ? ele.parent : null;
        }
    }
    
    /**
     * Register/unregister a child notification listener.
     * 
     * @param eventName The event name.
     * @param listener A notification listener. If null, any existing listener is removed. If not
     *            null and a listener is already registered, it will be replaced.
     */
    protected void listenToChild(String eventName, INotificationListener listener) {
        parentListeners.register(eventName, listener);
    }
    
    /**
     * Allows a parent element to notify its children of an event of interest.
     * 
     * @param eventName Name of the event.
     * @param eventData Data associated with the event.
     * @param recurse If true, recurse over all child levels.
     */
    public void notifyChildren(String eventName, Object eventData, boolean recurse) {
        notifyChildren(this, eventName, eventData, recurse);
    }
    
    private void notifyChildren(UIElementBase sender, String eventName, Object eventData, boolean recurse) {
        for (UIElementBase child : getChildren()) {
            if (child.childListeners.notify(sender, eventName, eventData) && recurse) {
                child.notifyChildren(sender, eventName, eventData, recurse);
            }
        }
    }
    
    /**
     * Register/unregister a parent notification listener.
     * 
     * @param eventName The event name.
     * @param listener A notification listener. If null, any existing listener is removed. If not
     *            null and a listener is already registered, it will be replaced.
     */
    protected void listenToParent(String eventName, INotificationListener listener) {
        childListeners.register(eventName, listener);
    }
    
}
