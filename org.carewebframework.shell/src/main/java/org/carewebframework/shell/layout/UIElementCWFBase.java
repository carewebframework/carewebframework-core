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

import org.apache.commons.lang.StringUtils;
import org.carewebframework.shell.designer.DesignContextMenu;
import org.carewebframework.shell.designer.DesignMask;
import org.carewebframework.shell.designer.DesignMask.MaskMode;
import org.carewebframework.shell.designer.PropertyGrid;
import org.carewebframework.ui.core.CWFUtil;
import org.carewebframework.ui.dialog.DialogUtil;
import org.carewebframework.ui.zk.ZKUtil;
import org.carewebframework.web.component.BaseComponent;
import org.carewebframework.web.component.BaseUIComponent;
import org.carewebframework.web.component.Menupopup;
import org.carewebframework.web.component.Popup;
import org.carewebframework.web.page.PageUtil;

/**
 * This is an abstract class from which all ZK-based UI elements must derive.
 */
public abstract class UIElementCWFBase extends UIElementBase {
    
    /**
     * Saves various states of a component prior to configuring it for design mode. The restore
     * method then restores to the saved state.
     */
    private static class SavedState {
        
        final BaseUIComponent component;
        
        final String tooltipText;
        
        final Popup contextMenu;
        
        public SavedState(BaseUIComponent component) {
            this.component = component;
            tooltipText = component.getHint();
            contextMenu = component.getContext();
            component.setAttribute(SAVED_STATE, this);
            component.addClass("cwf-designmode-active");
        }
        
        private void restore() {
            component.setHint(tooltipText);
            component.setContext(contextMenu);
            component.removeAttribute(SAVED_STATE);
            component.removeClass("cwf-designmode-active");
        }
        
        public static void restore(BaseUIComponent comp) {
            SavedState ss = (SavedState) comp.getAttribute(SAVED_STATE);
            
            if (ss != null) {
                ss.restore();
            }
        }
    }
    
    private static final String ATTR_PREFIX = UIElementCWFBase.class.getName() + ".";
    
    private static final String ASSOC_ELEMENT = ATTR_PREFIX + "AssociatedUIElement";
    
    private static final String SAVED_STATE = ATTR_PREFIX + "SavedState";
    
    private static final String CONTEXT_MENU = ATTR_PREFIX + "ContextMenu";
    
    private final DesignMask mask;
    
    /**
     * Returns the UI element that registered the ZK component.
     * 
     * @param component The ZK component of interest.
     * @return The associated UI element.
     */
    public static UIElementBase getAssociatedUIElement(BaseComponent component) {
        return component == null ? null : (UIElementBase) component.getAttribute(ASSOC_ELEMENT);
    }
    
    /**
     * Returns the design context menu currently bound to the component.
     * 
     * @param component The ZK component of interest.
     * @return The associated design context menu, or null if none.
     */
    public static Menupopup getDesignContextMenu(BaseComponent component) {
        return component == null ? null : (Menupopup) component.getAttribute(CONTEXT_MENU);
    }
    
    public UIElementCWFBase() {
        mask = new DesignMask(this);
    }
    
    /**
     * Associates the specified ZK component with this UI element.
     * 
     * @param component ZK component to associate.
     */
    public void associateComponent(BaseComponent component) {
        if (component != null) {
            component.setAttribute(ASSOC_ELEMENT, this);
        }
    }
    
    /**
     * Augments the parent method by automatically associating the component with this UI element.
     */
    @Override
    public void setOuterComponent(Object value) {
        super.setOuterComponent(value);
        associateComponent((BaseComponent) value);
    }
    
    /**
     * Augments the parent method by automatically associating the component with this UI element.
     */
    @Override
    public void setInnerComponent(Object value) {
        super.setInnerComponent(value);
        associateComponent((BaseComponent) value);
    }
    
    /**
     * Provides a default implementation for ZK-derived UI components by setting the visibility of
     * the outer component.
     */
    @Override
    protected void updateVisibility(boolean visible, boolean activated) {
        if (!getDefinition().isInternal()) {
            getOuterComponent().setVisible(visible && activated);
        }
    }
    
    /**
     * Returns the URL of the default template to use in createFromTemplate. Override this method to
     * provide an alternate default URL.
     * 
     * @return The template URL.
     */
    protected String getTemplateUrl() {
        return "~./" + getClass().getName().replace(".", "/") + ".cwf";
    }
    
    /**
     * Create wrapped component(s) from a template (a zul page). Performs autowiring of variables
     * and events. The template URL is derived from the class name. For example, if the class is
     * "org.carewebframework.xxx.Clazz", the template URL is assumed to be
     * "~./org/carewebframework/xxx/Clazz.cwf".
     * 
     * @return Top level component.
     */
    protected BaseComponent createFromTemplate() {
        return createFromTemplate(null);
    }
    
    /**
     * Create wrapped component(s) from specified template (a zul page). Performs autowiring of
     * variables and events.
     * 
     * @param template URL of zul page that will serve as a template. If a path is not specified, it
     *            is derived from the package. If the URL is not specified, the template name is
     *            obtained from getTemplateUrl.
     * @return Top level component.
     */
    protected BaseComponent createFromTemplate(String template) {
        return createFromTemplate(template, null, this);
    }
    
    /**
     * Create wrapped component(s) from specified template (a zul page).
     * 
     * @param template URL of zul page that will serve as a template. If the URL is not specified,
     *            the template name is obtained from getTemplateUrl.
     * @param parent The component that will become the parent.
     * @param controller If specified, events and variables are autowired to the controller.
     * @return Top level component.
     */
    protected BaseComponent createFromTemplate(String template, BaseComponent parent, Object controller) {
        if (StringUtils.isEmpty(template)) {
            template = getTemplateUrl();
        } else if (!template.startsWith("~")) {
            template = CWFUtil.getResourcePath(getClass()) + template;
        }
        
        BaseComponent top = null;
        
        try {
            top = PageUtil.createPage(template, parent).get(0);
            top.wireController(controller);
        } catch (Exception e) {
            raise("Error creating element from template.", e);
        }
        
        return top;
    }
    
    /**
     * Moves a child to the position specified by an index.
     * 
     * @param child Child to move
     * @param index Move child to this position.
     */
    protected void moveChild(BaseComponent child, int index) {
        ZKUtil.moveChild(child, index);
    }
    
    /**
     * Swaps the position of the two child components.
     * 
     * @param child1 The first child.
     * @param child2 The second child.
     */
    protected void swapChildren(BaseComponent child1, BaseComponent child2) {
        child1.getParent().swapChildren(child1.indexOf(), child2.indexOf());
    }
    
    /**
     * Provides a default behavior for resequencing this element relative to its siblings. This
     * assumes there is one wrapped ZK component and it is that component whose position is to be
     * changed.
     */
    @Override
    protected void afterMoveTo(int index) {
        moveChild(getOuterComponent(), index);
        updateMasks();
    }
    
    @Override
    protected void afterAddChild(UIElementBase child) {
        mask.update();
        super.afterAddChild(child);
    }
    
    @Override
    protected void afterRemoveChild(UIElementBase child) {
        mask.update();
        super.afterRemoveChild(child);
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
     * Checks for a pending mask operation when a UI element is activated.
     */
    @Override
    public void activate(boolean activate) {
        super.activate(activate);
        
        if (activate) {
            mask.update();
        }
    }
    
    /**
     * Augments the parent method by activating the associated design context menu when design mode
     * is activated.
     * 
     * @param designMode If true, design mode is activated.
     */
    @Override
    public void setDesignMode(boolean designMode) {
        if (isDesignMode() != designMode) {
            super.setDesignMode(designMode);
            setDesignContextMenu(designMode ? DesignContextMenu.getInstance() : null);
        }
        
        mask.update();
    }
    
    /**
     * Apply/remove the design context menu to/from ZK components. This default implementation
     * applies the design context menu to the outer ZK component only. It may be overridden to
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
     * @param component BaseComponent to which to apply/remove the design context menu.
     * @param contextMenu The design menu if design mode is activated, or null if it is not.
     */
    protected void setDesignContextMenu(BaseComponent component, Menupopup contextMenu) {
        if (component instanceof BaseUIComponent) {
            BaseUIComponent comp = (BaseUIComponent) component;
            comp.setAttribute(CONTEXT_MENU, contextMenu);
            
            if (contextMenu == null) {
                SavedState.restore(comp);
                applyHint();
            } else {
                new SavedState(comp);
                comp.setContext(contextMenu);
                comp.setHint(getDefinition().getName());
            }
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
            if (child instanceof UIElementCWFBase) {
                ((UIElementCWFBase) child).updateMasks();
            }
        }
    }
    
    @Override
    protected void bind() {
        getParent().getInnerComponent().addChild(getOuterComponent());
    }
    
    @Override
    protected void unbind() {
        getOuterComponent().detach();
    }
    
    /**
     * Invokes the property grid with this element as its target.
     */
    @Override
    public void editProperties() {
        try {
            PropertyGrid.create(this, null);
        } catch (Exception e) {
            DialogUtil.showError("Displaying property grid: \r\n" + e.toString());
        }
    }
    
    /**
     * Returns the innermost wrapped ZK component.
     * 
     * @return The inner ZK component.
     */
    @Override
    public BaseComponent getInnerComponent() {
        return (BaseComponent) super.getInnerComponent();
    }
    
    /**
     * Returns the outermost wrapped ZK component.
     * 
     * @return The outer ZK component.
     */
    @Override
    public BaseUIComponent getOuterComponent() {
        return (BaseUIComponent) super.getOuterComponent();
    }
    
    @Override
    public UIElementCWFBase getParent() {
        return (UIElementCWFBase) super.getParent();
    }
    
    /**
     * Applies the current color setting to the target component. If the target implements a custom
     * method for performing this operation, that method will be invoked. Otherwise, the background
     * color of the target is set. Override this method to provide alternate implementations.
     * 
     * @param component BaseComponent to receive the color setting.
     */
    @Override
    protected void applyColor(Object component) {
        if (component instanceof BaseUIComponent) {
            //TODO: ZKUtil.applyColor((BaseUIComponent) component, getColor());
        }
    }
    
    /**
     * Applies the current hint text to the target component.
     * 
     * @param component BaseComponent to receive the hint text.
     */
    @Override
    protected void applyHint(Object component) {
        if (component instanceof BaseUIComponent) {
            ((BaseUIComponent) component).setHint(getHint());
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
     * @param component BaseComponent subtree to examine.
     * @return True if any associated UI element in the subtree is visible.
     */
    protected boolean hasVisibleElements(BaseComponent component) {
        for (BaseComponent child : component.getChildren()) {
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
    
}
