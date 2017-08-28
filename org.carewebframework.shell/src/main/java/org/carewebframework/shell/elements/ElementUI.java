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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.carewebframework.shell.Constants;
import org.carewebframework.shell.ancillary.SavedState;
import org.carewebframework.shell.ancillary.CWFException;
import org.carewebframework.shell.designer.DesignContextMenu;
import org.carewebframework.shell.designer.DesignMask;
import org.carewebframework.shell.designer.DesignMask.MaskMode;
import org.carewebframework.shell.designer.PropertyEditorTriggers;
import org.carewebframework.shell.property.PropertyTypeRegistry;
import org.carewebframework.ui.util.CWFUtil;
import org.fujion.component.BaseComponent;
import org.fujion.component.BaseLabeledComponent;
import org.fujion.component.BaseUIComponent;
import org.fujion.component.Menupopup;
import org.fujion.page.PageUtil;

/**
 * This is the base class for all layout elements supported by the CareWeb framework.
 */
public abstract class ElementUI extends ElementBase {
    
    static {
        PropertyTypeRegistry.register("triggers", PropertyEditorTriggers.class);
    }
    
    protected static final Log log = LogFactory.getLog(ElementUI.class);
    
    public static final String EVENT_ELEMENT_ACTIVATE = Constants.EVENT_PREFIX + ".ELEMENT.ACTIVATE";

    public static final String EVENT_ELEMENT_INACTIVATE = Constants.EVENT_PREFIX + ".ELEMENT.INACTIVATE";

    private static final String ATTR_PREFIX = ElementUI.class.getName() + ".";
    
    private static final String ASSOC_ELEMENT = ATTR_PREFIX + "AssociatedElement";
    
    private static final String CONTEXT_MENU = ATTR_PREFIX + "ContextMenu";
    
    private final Set<ElementTrigger> triggers = new HashSet<>();

    protected boolean autoHide = true;
    
    protected boolean autoEnable = true;
    
    private boolean activated;
    
    private boolean visible = true;
    
    private final DesignMask mask;
    
    private BaseUIComponent innerComponent;
    
    private BaseUIComponent outerComponent;
    
    private String hint;
    
    private String color;
    
    /**
     * Returns the UI element that registered the CWF component.
     *
     * @param component The CWF component of interest.
     * @return The associated UI element.
     */
    public static ElementUI getAssociatedElement(BaseComponent component) {
        return component == null ? null : (ElementUI) component.getAttribute(ASSOC_ELEMENT);
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
    
    public ElementUI() {
        mask = new DesignMask(this);
    }
    
    /**
     * Returns the URL of the default template to use in createFromTemplate. Override this method to
     * provide an alternate default URL.
     *
     * @return The template URL.
     */
    protected String getTemplateUrl() {
        return "web/" + getClass().getPackage().getName().replace(".", "/") + "/"
                + StringUtils.uncapitalize(getClass().getSimpleName()) + ".fsp";
    }
    
    /**
     * Create wrapped component(s) from a template (a cwf page). Performs autowiring of variables
     * and events. The template URL is derived from the class name. For example, if the class is
     * "org.carewebframework.xxx.Clazz", the template URL is assumed to be
     * "web/org/carewebframework/xxx/clazz.fsp".
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
            CWFException.raise("Error creating element from template.", e);
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
     * Sets design mode status for this component and all its children and triggers.
     *
     * @param designMode The design mode flag.
     */
    @Override
    public void setDesignMode(boolean designMode) {
        super.setDesignMode(designMode);

        for (ElementTrigger trigger : triggers) {
            trigger.setDesignMode(designMode);
        }

        setDesignContextMenu(designMode ? DesignContextMenu.getInstance().getMenupopup() : null);
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
        
        for (ElementUI child : getChildren(ElementUI.class)) {
            child.updateMasks();
        }
    }
    
    /**
     * Called after a child is logically added to the parent.
     *
     * @param child The child element added.
     */
    @Override
    protected void afterAddChild(ElementBase child) {
        mask.update();
    }
    
    /**
     * Called after a child is logically removed from the parent.
     *
     * @param child The child UI element.
     */
    @Override
    protected void afterRemoveChild(ElementBase child) {
        mask.update();
    }
    
    /**
     * Called after the parent has been changed.
     *
     * @param oldParent The value of the parent property prior to the change.
     */
    @Override
    protected void afterParentChanged(ElementBase oldParent) {
        if (getParent() != null) {
            if (!getDefinition().isInternal()) {
                bind();
            }
            
            setDesignMode(getParent().isDesignMode());
        }
    }
    
    /**
     * Called before the parent has been changed.
     *
     * @param newParent The value of the parent property prior to the change.
     */
    @Override
    protected void beforeParentChanged(ElementBase newParent) {
        if (getParent() != null && !getDefinition().isInternal()) {
            unbind();
        }
    }
    
    /**
     * Override to implement special cleanup when an object is destroyed.
     */
    @Override
    public void destroy() {
        unbind();
        super.destroy();
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
        for (ElementUI child : getChildren(ElementUI.class)) {
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
        getEventManager().fireLocalEvent(activate ? EVENT_ELEMENT_ACTIVATE : EVENT_ELEMENT_INACTIVATE, this);
        
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
        for (ElementUI child : getChildren(ElementUI.class)) {
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
     * Returns the first visible child.
     *
     * @return First visible child, or null if none;
     */
    public ElementUI getFirstVisibleChild() {
        return getVisibleChild(true);
    }
    
    /**
     * Returns the last visible child.
     *
     * @return Last visible child, or null if none;
     */
    public ElementUI getLastVisibleChild() {
        return getVisibleChild(false);
    }
    
    /**
     * Returns first or last visible child.
     *
     * @param first If true, find first visible child; if false, last visible child.
     * @return Visible child, or null if none found.
     */
    private ElementUI getVisibleChild(boolean first) {
        int count = getChildCount();
        int start = first ? 0 : count - 1;
        int inc = first ? 1 : -1;
        
        for (int i = start; i >= 0 && i < count; i += inc) {
            ElementUI child = (ElementUI) getChild(i);

            if (child.isVisible()) {
                return child;
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
    public ElementUI getNextSibling(boolean visibleOnly) {
        ElementUI parent = getParent();
        
        if (parent != null) {
            int count = parent.getChildCount();
            
            for (int i = getIndex() + 1; i < count; i++) {
                ElementUI child = (ElementUI) parent.getChild(i);
                
                if (!visibleOnly || child.isVisible()) {
                    return child;
                }
            }
        }
        
        return null;
    }
    
    /**
     * Element has been moved to a different position under this parent. Adjust wrapped components
     * accordingly.
     *
     * @param child Child element that was moved.
     * @param before Child element was moved before this one.
     */
    @Override
    protected void afterMoveChild(ElementBase child, ElementBase before) {
        moveChild(((ElementUI) child).getOuterComponent(), ((ElementUI) before).getOuterComponent());
        updateMasks();
    }
    
    /**
     * Brings this element to the front of the user interface.
     */
    public void bringToFront() {
        if (getParent() != null) {
            getParent().bringToFront();
        } else {
            activate(true);
        }
    }
    
    /**
     * Returns the parent of this element. May be null.
     *
     * @return This element's parent.
     */
    @Override
    public ElementUI getParent() {
        return (ElementUI) super.getParent();
    }
    
    /**
     * Returns the UI element at the root of the component tree.
     *
     * @return Root UI element.
     */
    @Override
    public ElementUI getRoot() {
        return (ElementUI) super.getRoot();
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
        } else if (comp != null) {
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
     * Update a UI element based on the state of its children. The default implementation acts on
     * container elements only and has the following behavior:
     * <ul>
     * <li>If all children are disabled, the parent is also disabled (set autoEnable to false to
     * turn off).</li>
     * <li>If all children are hidden or there are no children and design mode is not active, the
     * parent is also hidden (set autoHide to false to turn off).
     * </ul>
     */
    @Override
    protected void updateState() {
        if (!isContainer()) {
            return;
        }
        
        boolean anyEnabled = !autoEnable || getChildCount() == 0;
        boolean anyVisible = !autoHide || isDesignMode();
        
        for (ElementUI child : getChildren(ElementUI.class)) {
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
            ElementUI ele = getAssociatedElement(child);
            
            if (ele != null && ele.isVisible()) {
                return true;
            }
            
            if (hasVisibleElements(child)) {
                return true;
            }
        }
        
        return false;
    }
    
    public void addTrigger(ElementTrigger trigger) {
        if (triggers.add(trigger)) {
            trigger.setDesignMode(isDesignMode());
            trigger.addTarget(this);
        }
    }

    public void removeTrigger(ElementTrigger trigger) {
        if (triggers.remove(trigger)) {
            trigger.removeTarget(this);
        }
    }

    public Set<ElementTrigger> getTriggers() {
        return Collections.unmodifiableSet(triggers);
    }
}
