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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.carewebframework.api.event.EventManager;
import org.carewebframework.api.event.IEventManager;
import org.carewebframework.common.MiscUtil;
import org.carewebframework.shell.AboutDialog;
import org.carewebframework.shell.CareWebShell;
import org.carewebframework.shell.CareWebUtil;
import org.carewebframework.shell.ancillary.INotificationListener;
import org.carewebframework.shell.ancillary.NotificationListeners;
import org.carewebframework.shell.ancillary.RelatedClassMap;
import org.carewebframework.shell.ancillary.RelatedClassMap.Cardinality;
import org.carewebframework.shell.ancillary.CWFException;
import org.carewebframework.shell.designer.PropertyGrid;
import org.carewebframework.shell.plugins.IPluginResource;
import org.carewebframework.shell.plugins.PluginDefinition;
import org.carewebframework.shell.plugins.PluginRegistry;
import org.carewebframework.shell.property.PropertyInfo;
import org.carewebframework.ui.dialog.DialogUtil;
import org.carewebframework.web.component.BaseUIComponent;

/**
 * This is the base class for all layout elements supported by the CareWeb framework.
 */
public abstract class ElementBase {
    
    protected static final Log log = LogFactory.getLog(ElementBase.class);
    
    private static final RelatedClassMap allowedParentClasses = new RelatedClassMap();
    
    private static final RelatedClassMap allowedChildClasses = new RelatedClassMap();
    
    private final NotificationListeners parentListeners = new NotificationListeners();
    
    private final NotificationListeners childListeners = new NotificationListeners();
    
    private final List<ElementBase> children = new ArrayList<>();
    
    private final int maxChildren;
    
    private ElementBase parent;
    
    private boolean designMode;
    
    private boolean locked;
    
    private boolean enabled = true;
    
    private PluginDefinition definition;
    
    private String rejectReason;
    
    private IEventManager eventManager;
    
    /**
     * A ElementBase subclass should call this in its static initializer block to register any
     * subclasses that may act as a parent.
     *
     * @param clazz Class whose valid parent classes are to be registered.
     * @param parentClass Class that may act as a parent to clazz.
     */
    protected static synchronized void registerAllowedParentClass(Class<? extends ElementBase> clazz,
                                                                  Class<? extends ElementBase> parentClass) {
        allowedParentClasses.addCardinality(clazz, parentClass, 1);
    }
    
    /**
     * A ElementBase subclass should call this in its static initializer block to register any
     * subclasses that may be a child.
     *
     * @param clazz Class whose valid child classes are to be registered.
     * @param childClass Class that may be a child of clazz.
     * @param maxOccurrences Maximum occurrences for the child class.
     */
    protected static synchronized void registerAllowedChildClass(Class<? extends ElementBase> clazz,
                                                                 Class<? extends ElementBase> childClass,
                                                                 int maxOccurrences) {
        allowedChildClasses.addCardinality(clazz, childClass, maxOccurrences);
    }
    
    /**
     * Returns true if childClass can be a child of the parentClass.
     *
     * @param parentClass Parent class
     * @param childClass Child class
     * @return True if childClass can be a child of the parentClass.
     */
    public static boolean canAcceptChild(Class<? extends ElementBase> parentClass, Class<? extends ElementBase> childClass) {
        return allowedChildClasses.isRelated(parentClass, childClass);
    }
    
    /**
     * Returns true if parentClass can be a parent of childClass.
     *
     * @param childClass The child class.
     * @param parentClass The parent class.
     * @return True if parentClass can be a parent of childClass.
     */
    public static boolean canAcceptParent(Class<? extends ElementBase> childClass,
                                          Class<? extends ElementBase> parentClass) {
        return allowedParentClasses.isRelated(childClass, parentClass);
    }
    
    public ElementBase() {
        maxChildren = allowedChildClasses.getTotalCardinality(getClass());
    }
    
    /**
     * Adds the specified child element. The validity of the operation is first tested and an
     * exception thrown if the element is not a valid child for this parent.
     *
     * @param child Element to add as a child.
     */
    public void addChild(ElementBase child) {
        addChild(child, true);
    }
    
    /**
     * Adds the specified child element. The validity of the operation is first tested and an
     * exception thrown if the element is not a valid child for this parent.
     *
     * @param child Element to add as a child.
     * @param doEvent Fires the add child events if true.
     */
    protected void addChild(ElementBase child, boolean doEvent) {
        if (!child.canAcceptParent(this)) {
            CWFException.raise(child.rejectReason);
        }
        
        if (!canAcceptChild(child)) {
            CWFException.raise(rejectReason);
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
    protected void afterAddChild(ElementBase child) {
    }
    
    /**
     * Called before a child is logically added to the parent.
     *
     * @param child The new child element.
     */
    protected void beforeAddChild(ElementBase child) {
    }
    
    /**
     * Removes the specified element as a child of this parent.
     *
     * @param child Child element to remove.
     * @param destroy If true the child is explicitly destroyed.
     */
    public void removeChild(ElementBase child, boolean destroy) {
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
    protected void afterRemoveChild(ElementBase child) {
    }
    
    /**
     * Called before a child is logically removed from the parent.
     *
     * @param child The child UI element.
     */
    protected void beforeRemoveChild(ElementBase child) {
    }
    
    /**
     * Changes the assigned parent, firing parent changed events if appropriate.
     *
     * @param newParent The new parent.
     */
    private void updateParent(ElementBase newParent) {
        ElementBase oldParent = this.parent;
        
        if (oldParent != newParent) {
            beforeParentChanged(newParent);
            this.parent = newParent;
            
            if (oldParent != null) {
                oldParent.updateState();
            }
            
            if (newParent != null) {
                afterParentChanged(oldParent);
                newParent.updateState();
            }
        }
    }
    
    /**
     * Called after the parent has been changed.
     *
     * @param oldParent The value of the parent property prior to the change.
     */
    protected void afterParentChanged(ElementBase oldParent) {
    }
    
    /**
     * Called before the parent has been changed.
     *
     * @param newParent The value of the parent property prior to the change.
     */
    protected void beforeParentChanged(ElementBase newParent) {
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
        processResources(false);
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
            
            CWFException.raise("Cannot modify plugin definition.");
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
     * class as the element itself, but in certain cases (as in the ElementProxy class) it is not.
     *
     * @param clazz The UI element class.
     */
    public void setDefinition(Class<? extends ElementBase> clazz) {
        setDefinition(PluginRegistry.getInstance().get(clazz));
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
        
        for (ElementBase child : children) {
            child.setDesignMode(designMode);
        }
        
        updateState();
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
    public ElementBase getChild(int index) {
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
    public <T extends ElementBase> T getChild(Class<T> clazz, ElementBase last) {
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
    public Iterable<ElementBase> getChildren() {
        return children;
    }
    
    /**
     * Returns an iterable of this component's children restricted to the specified type.
     *
     * @param clazz Restrict to children of this type.
     * @return Iterable of this component's children.
     */
    public <T extends ElementBase> Iterable<T> getChildren(Class<T> clazz) {
        return MiscUtil.iterableForType(children, clazz);
    }
    
    /**
     * Returns an iterable of this component's serializable children. By default, this calls
     * getChildren() but may be overridden to accommodate specialized serialization needs.
     *
     * @return Iterable of this component's serializable children.
     */
    public Iterable<ElementBase> getSerializableChildren() {
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
     * Returns the number of children.
     *
     * @param clazz Restrict to children of this type.
     * @return Number of children.
     */
    public int getChildCount(Class<? extends ElementBase> clazz) {
        if (clazz == ElementBase.class) {
            return getChildCount();
        }
        
        int count = 0;
        
        for (ElementBase child : children) {
            if (clazz.isInstance(child)) {
                count++;
            }
        }
        
        return count;
    }
    
    /**
     * Returns the first child, or null if there are no children.
     *
     * @return First child, or null if none.
     */
    public ElementBase getFirstChild() {
        return getChildCount() == 0 ? null : getChild(0);
    }
    
    /**
     * Returns the last child, or null if there are no children.
     *
     * @return Last child, or null if none.
     */
    public ElementBase getLastChild() {
        return getChildCount() == 0 ? null : getChild(getChildCount() - 1);
    }
    
    /**
     * Returns the index of the specified child. If the specified component is not a child, -1 is
     * returned.
     *
     * @param child The child component whose index is sought.
     * @return The child's index or -1 if not found.
     */
    public int indexOfChild(ElementBase child) {
        return children.indexOf(child);
    }
    
    /**
     * Returns true if the specified element is a child of this element.
     *
     * @param element The UI element to test.
     * @return True if the element is a child.
     */
    public boolean hasChild(ElementBase element) {
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
    public <T extends ElementBase> T findChildElement(Class<T> clazz) {
        for (ElementBase child : getChildren()) {
            if (clazz.isInstance(child)) {
                return (T) child;
            }
        }
        
        for (ElementBase child : getChildren()) {
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
    public boolean hasAncestor(ElementBase element) {
        ElementBase child = this;
        
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
            ElementBase child = children.get(from);
            ElementBase ref = children.get(to);
            children.remove(from);
            to = children.indexOf(ref);
            children.add(to, child);
            afterMoveChild(child, ref);
        }
    }
    
    /**
     * Called after an element has been moved to a different position under this parent.
     *
     * @param child Child element that was moved.
     * @param before Child element was moved before this one.
     */
    protected void afterMoveChild(ElementBase child, ElementBase before) {
    }
    
    /**
     * Sets this element's index to the specified value. This effectively changes the position of
     * the element relative to its siblings.
     *
     * @param index The index.
     */
    public void setIndex(int index) {
        ElementBase parent = getParent();
        
        if (parent == null) {
            CWFException.raise("Element has no parent.");
        }
        
        int currentIndex = parent.children.indexOf(this);
        
        if (currentIndex < 0 || currentIndex == index) {
            return;
        }
        
        parent.moveChild(currentIndex, index);
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
    public ElementBase getParent() {
        return parent;
    }
    
    /**
     * Sets the parent of this element, subject to the parent/child constraints applicable to each.
     *
     * @param parent The new parent.
     */
    public final void setParent(ElementBase parent) {
        ElementBase oldParent = this.parent;
        
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
     * Moves a child to before another component.
     *
     * @param child Child to move
     * @param before Move child to this component.
     */
    protected void moveChild(BaseUIComponent child, BaseUIComponent before) {
        child.getParent().addChild(child, before);
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
     * Update an element based on the state of its children.
     */
    protected void updateState() {
    }
    
    /**
     * Returns true if this UI element can contain other UI elements.
     *
     * @return True if this UI element can contain other UI elements.
     */
    public boolean isContainer() {
        return maxChildren > 0;
    }
    
    /**
     * Returns true if this element may accept a child. Updates the reject reason with the result.
     *
     * @return True if this element may accept a child. Updates the reject reason with the result.
     */
    public boolean canAcceptChild() {
        if (maxChildren == 0) {
            rejectReason = getDisplayName() + " does not accept any children.";
        } else if (getChildCount() >= maxChildren) {
            rejectReason = "Maximum child count exceeded for " + getDisplayName() + ".";
        } else {
            rejectReason = null;
        }
        
        return rejectReason == null;
    }
    
    /**
     * Returns true if this element may accept a child of the specified class. Updates the reject
     * reason with the result.
     *
     * @param childClass Child class to test.
     * @return True if this element may accept a child of the specified class.
     */
    public boolean canAcceptChild(Class<? extends ElementBase> childClass) {
        if (!canAcceptChild()) {
            return false;
        }

        Cardinality cardinality = allowedChildClasses.getCardinality(getClass(), childClass);
        int max = cardinality.getMaxOccurrences();
        
        if (max == 0) {
            rejectReason = getDisplayName() + " does not accept " + childClass.getSimpleName() + " as a child.";
        } else if (max != Integer.MAX_VALUE && getChildCount(cardinality.getTargetClass()) >= max) {
            rejectReason = getDisplayName() + " cannot accept more than " + max + " of "
                    + cardinality.getTargetClass().getSimpleName() + ".";
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
    public boolean canAcceptChild(ElementBase child) {
        return canAcceptChild(child.getClass());
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
    public boolean canAcceptParent(Class<? extends ElementBase> clazz) {
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
    public boolean canAcceptParent(ElementBase parent) {
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
    public ElementBase getRoot() {
        ElementBase root = this;
        
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
    public <T extends ElementBase> T getAncestor(Class<T> clazz) {
        ElementBase parent = getParent();
        
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
     * Allows a child element to notify its parent of an event of interest.
     *
     * @param eventName Name of the event.
     * @param eventData Data associated with the event.
     * @param recurse If true, recurse up the parent chain.
     */
    public void notifyParent(String eventName, Object eventData, boolean recurse) {
        ElementBase ele = parent;
        
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
    
    private void notifyChildren(ElementBase sender, String eventName, Object eventData, boolean recurse) {
        for (ElementBase child : getChildren()) {
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
