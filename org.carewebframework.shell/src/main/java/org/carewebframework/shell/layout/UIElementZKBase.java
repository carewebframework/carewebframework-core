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

import org.apache.commons.lang.StringUtils;

import org.carewebframework.shell.designer.DesignContextMenu;
import org.carewebframework.shell.designer.PropertyGrid;
import org.carewebframework.ui.zk.PromptDialog;
import org.carewebframework.ui.zk.ZKUtil;

import org.zkoss.zk.au.out.AuInvoke;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.HtmlBasedComponent;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Menupopup;
import org.zkoss.zul.impl.XulElement;

/**
 * This is an abstract class from which all ZK-based UI elements must derive.
 */
public abstract class UIElementZKBase extends UIElementBase {
    
    private static final String ATTR_PREFIX = UIElementZKBase.class.getName() + ".";
    
    private static final String ASSOC_ELEMENT = ATTR_PREFIX + "AssociatedUIElement";
    
    private static final String SAVED_CONTEXT_MENU = ATTR_PREFIX + "SavedContextMenu";
    
    protected static final String CUSTOM_COLOR_OVERRIDE = "setCustomColor";
    
    private boolean enableDesignModeMask;
    
    private Event maskEvent;
    
    /**
     * Handles the onMask event, which is posted with a low priority to ensure that the target
     * widget states have been properly set.
     */
    private final EventListener<Event> maskListener = new EventListener<Event>() {
        
        @Override
        public void onEvent(Event event) throws Exception {
            Component target = event.getTarget();
            Menupopup contextMenu = (Menupopup) event.getData();
            ZKUtil.addMask(target, getDisplayName(), contextMenu);
        }
        
    };
    
    /**
     * Returns the UI element that registered the ZK component.
     * 
     * @param component The ZK component of interest.
     * @return The associated UI element.
     */
    public static UIElementBase getAssociatedUIElement(Component component) {
        return component == null ? null : (UIElementBase) component.getAttribute(ASSOC_ELEMENT);
    }
    
    /**
     * Associates the specified ZK component with this UI element.
     * 
     * @param component ZK component to associate.
     */
    public void associateComponent(Component component) {
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
        associateComponent((Component) value);
    }
    
    /**
     * Augments the parent method by automatically associating the component with this UI element.
     */
    @Override
    public void setInnerComponent(Object value) {
        super.setInnerComponent(value);
        associateComponent((Component) value);
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
     * Returns the url of the default template to use in createFromTemplate. Override this method to
     * provide an alternate default url.
     * 
     * @return
     */
    protected String getTemplateUrl() {
        return "~./" + getClass().getName().replace(".", "/") + ".zul";
    }
    
    /**
     * Create wrapped component(s) from a template (a zul page). Performs autowiring of variables
     * and events. The template url is derived from the class name. For example, if the class is
     * "org.carewebframework.xxx.Clazz", the template url is assumed to be
     * "~./org/carewebframework/xxx/Clazz.zul".
     * 
     * @return Top level component.
     */
    protected Component createFromTemplate() {
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
    protected Component createFromTemplate(String template) {
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
    protected Component createFromTemplate(String template, Component parent, Object controller) {
        if (StringUtils.isEmpty(template)) {
            template = getTemplateUrl();
        } else if (!template.startsWith("~")) {
            template = ZKUtil.getResourcePath(getClass()) + template;
        }
        
        Component top = null;
        
        try {
            top = Executions.createComponents(ZKUtil.loadCachedPageDefinition(template), parent, null);
            ZKUtil.wireController(top, controller);
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
    protected void moveChild(Component child, int index) {
        ZKUtil.moveChild(child, index);
    }
    
    /**
     * Swaps the position of the two child components.
     * 
     * @param child1
     * @param child2
     */
    protected void swapChildren(Component child1, Component child2) {
        ZKUtil.swapChildren(child1, child2);
    }
    
    /**
     * Provides a default behavior for resequencing this element relative to its siblings. This
     * assumes there is one wrapped ZK component and it is that component whose position is to be
     * changed.
     */
    @Override
    protected void afterMoveTo(int index) {
        moveChild(getOuterComponent(), index);
        applyMask(true);
    }
    
    /**
     * Set width and height of a component to 100%.
     * 
     * @param cmpt Component
     */
    protected void fullSize(HtmlBasedComponent cmpt) {
        cmpt.setWidth("100%");
        cmpt.setHeight("100%");
    }
    
    /**
     * Checks for a pending mask operation when a UI element is activated.
     */
    @Override
    public void activate(boolean activate) {
        super.activate(activate);
        
        if (activate) {
            applyMask(false);
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
        } else {
            applyMask(false);
        }
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
     * @param component Component to which to apply/remove the design context menu.
     * @param contextMenu The design menu if design mode is activated, or null if it is not.
     */
    protected void setDesignContextMenu(Component component, Menupopup contextMenu) {
        if (component instanceof XulElement) {
            XulElement comp = (XulElement) component;
            
            if (contextMenu == null) {
                if (enableDesignModeMask) {
                    maskEvent = null;
                    comp.removeEventListener("onMask", maskListener);
                    ZKUtil.removeMask(comp);
                } else {
                    comp.setContext((String) comp.removeAttribute(SAVED_CONTEXT_MENU));
                }
            } else {
                if (enableDesignModeMask) {
                    comp.addEventListener("onMask", maskListener);
                    maskEvent = new Event("onMask", comp, contextMenu);
                    applyMask(false);
                } else {
                    comp.setAttribute(SAVED_CONTEXT_MENU, comp.getContext());
                    comp.setContext(contextMenu);
                }
            }
        }
    }
    
    /**
     * Applies the mask if one is present. Does this by firing the onMask event to the target
     * component.
     * 
     * @param recurse If true, recurse over all children as well.
     */
    private void applyMask(boolean recurse) {
        if (maskEvent != null && isActivated()) {
            Events.postEvent(-9999, maskEvent);
        }
        
        if (recurse) {
            for (UIElementBase child : getChildren()) {
                if (child instanceof UIElementZKBase) {
                    ((UIElementZKBase) child).applyMask(recurse);
                }
            }
        }
    }
    
    @Override
    protected void bind() {
        getParent().getInnerComponent().appendChild(getOuterComponent());
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
            PromptDialog.showError("Displaying property grid: \r\n" + e.toString());
        }
    }
    
    /**
     * Returns the innermost wrapped ZK component.
     * 
     * @return The inner ZK component.
     */
    @Override
    public Component getInnerComponent() {
        return (Component) super.getInnerComponent();
    }
    
    /**
     * Returns the outermost wrapped ZK component.
     * 
     * @return The outer ZK component.
     */
    @Override
    public Component getOuterComponent() {
        return (Component) super.getOuterComponent();
    }
    
    @Override
    public UIElementZKBase getParent() {
        return (UIElementZKBase) super.getParent();
    }
    
    /**
     * Applies the current color setting to the target component. If the target implements a custom
     * method for performing this operation, that method will be invoked. Otherwise, the background
     * color of the target is set. Override this method to provide alternate implementations.
     * 
     * @param cmpt Component to receive the color setting.
     */
    @Override
    protected void applyColor(Object cmpt) {
        if (cmpt instanceof HtmlBasedComponent) {
            HtmlBasedComponent comp = (HtmlBasedComponent) cmpt;
            
            if (comp.getWidgetOverride(CUSTOM_COLOR_OVERRIDE) != null) {
                Executions.getCurrent().addAuResponse(new AuInvoke(comp, CUSTOM_COLOR_OVERRIDE, getColor()));
            } else {
                ZKUtil.updateStyle(comp, "background-color", getColor());
            }
        }
    }
    
    /**
     * Applies the current hint text to the target component.
     * 
     * @param cmpt Component to receive the hint text.
     */
    @Override
    protected void applyHint(Object cmpt) {
        if (cmpt instanceof HtmlBasedComponent) {
            ((HtmlBasedComponent) cmpt).setTooltiptext(getHint());
        }
    }
    
    /**
     * Returns true if the design mode mask is to be used. This mask is used to cover the underlying
     * outer component when in design mode.
     * 
     * @return True if the design mode mask is enabled.
     */
    protected boolean getEnableDesignModeMask() {
        return enableDesignModeMask;
    }
    
    /**
     * Sets whether to use the design mode mask.
     * 
     * @param value True to enable the design mode mask.
     */
    protected void setEnableDesignModeMask(boolean value) {
        this.enableDesignModeMask = value;
    }
    
    /**
     * Returns true if any associated UI elements in the component subtree are visible.
     * 
     * @param comp Component subtree to examine.
     * @return True if any associated UI element in the subtree is visible.
     */
    protected boolean hasVisibleElements(Component comp) {
        for (Component child : comp.getChildren()) {
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
