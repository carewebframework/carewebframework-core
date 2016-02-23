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

import org.carewebframework.shell.AboutDialog;
import org.carewebframework.shell.plugins.PluginContainer;
import org.carewebframework.shell.plugins.PluginDefinition;
import org.carewebframework.shell.plugins.PluginResourceBean;
import org.carewebframework.shell.plugins.PluginResourceButton;
import org.carewebframework.shell.plugins.PluginResourceMenu;
import org.carewebframework.shell.property.IPropertyAccessor;
import org.carewebframework.shell.property.PropertyInfo;
import org.carewebframework.ui.action.ActionListener;

import org.zkoss.zk.ui.ext.Disable;
import org.zkoss.zul.Button;
import org.zkoss.zul.Menu;

/**
 * This class is used for all container-hosted plugins.
 */
public class UIElementPlugin extends UIElementZKBase implements Disable, IPropertyAccessor {
    
    static {
        registerAllowedParentClass(UIElementPlugin.class, UIElementBase.class);
    }
    
    private final PluginContainer container = new PluginContainer();
    
    /**
     * Sets the container as the wrapped component and registers itself to receive action
     * notifications from the container.
     */
    public UIElementPlugin() {
        super();
        setOuterComponent(container);
        container.registerAction(this);
    }
    
    /**
     * Also passes the plugin definition to the container.
     * 
     * @see org.carewebframework.shell.layout.UIElementBase#setDefinition(org.carewebframework.shell.plugins.PluginDefinition)
     */
    @Override
    public void setDefinition(PluginDefinition definition) {
        super.setDefinition(definition);
        container.setPluginDefinition(definition);
    }
    
    /**
     * Returns the container wrapped by this UI element.
     * 
     * @return The container.
     */
    public PluginContainer getContainer() {
        return container;
    }
    
    /**
     * @see org.carewebframework.shell.layout.UIElementBase#about()
     */
    @Override
    public void about() {
        AboutDialog.execute(getDefinition());
    }
    
    /**
     * Passes the activation request to the container.
     * 
     * @see org.carewebframework.shell.layout.UIElementBase#activateChildren(boolean)
     */
    @Override
    public void activateChildren(boolean active) {
        if (active != isActivated()) {
            if (active) {
                container.activate();
            } else {
                container.inactivate();
            }
        }
    }
    
    /**
     * Additional processing of the plugin after it is initialized.
     * 
     * @throws Exception Unspecified exception.
     * @see org.carewebframework.shell.layout.UIElementBase#afterInitialize
     */
    @Override
    public void afterInitialize(boolean deserializing) throws Exception {
        super.afterInitialize(deserializing);
        
        if (!getDefinition().isLazyLoad()) {
            container.load();
        }
    }
    
    /**
     * Passes the destroy event to the container.
     * 
     * @see org.carewebframework.shell.layout.UIElementBase#destroy()
     */
    @Override
    public void destroy() {
        container.destroy();
        super.destroy();
    }
    
    /**
     * Passes design mode setting to the container.
     * 
     * @see org.carewebframework.shell.layout.UIElementZKBase#setDesignMode
     */
    @Override
    public void setDesignMode(boolean designMode) {
        super.setDesignMode(designMode);
        container.setDesignMode(designMode);
    }
    
    /**
     * Delegates retrieval of property values to the container.
     * 
     * @see org.carewebframework.shell.property.IPropertyAccessor#getPropertyValue
     */
    @Override
    public Object getPropertyValue(PropertyInfo propInfo) throws Exception {
        return container.getPropertyValue(propInfo);
    }
    
    /**
     * Delegates setting of property values to the container.
     * 
     * @see org.carewebframework.shell.property.IPropertyAccessor#setPropertyValue
     */
    @Override
    public void setPropertyValue(PropertyInfo propInfo, Object value) throws Exception {
        container.setPropertyValue(propInfo, value);
    }
    
    /**
     * @see org.zkoss.zk.ui.ext.Disable#isDisabled()
     */
    @Override
    public boolean isDisabled() {
        return !isEnabled();
    }
    
    /**
     * @see org.zkoss.zk.ui.ext.Disable#setDisabled(boolean)
     */
    @Override
    public void setDisabled(boolean disabled) {
        setEnabled(!disabled);
    }
    
    public void registerResource(PluginResourceBean resource) {
        container.registerBean(resource.getBean(), resource.isRequired());
    }
    
    public void registerResource(PluginResourceButton resource) {
        Button button = new Button(resource.getCaption());
        button.setId(resource.getId());
        button.setTooltiptext(resource.getTooltip());
        button.setImage(resource.getIcon());
        ActionListener.addAction(button, resource.getAction());
        container.addToolbarComponent(button);
        container.registerId(resource.getId(), button);
    }
    
    /**
     * Registers the menu resource with the container.
     * 
     * @param resource A menu resource.
     */
    public void registerResource(PluginResourceMenu resource) {
        Menu menu = container.getShell().addMenu(resource.getPath(), resource.getAction());
        container.registerComponent(menu);
        container.registerId(resource.getId(), menu);
    }
}
