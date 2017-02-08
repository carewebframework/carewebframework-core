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

import org.carewebframework.shell.AboutDialog;
import org.carewebframework.shell.designer.DesignMask.MaskMode;
import org.carewebframework.shell.plugins.PluginContainer;
import org.carewebframework.shell.plugins.PluginDefinition;
import org.carewebframework.shell.property.IPropertyAccessor;
import org.carewebframework.shell.property.PropertyInfo;
import org.carewebframework.web.ancillary.IDisable;

/**
 * This class is used for all container-hosted plugins.
 */
public class UIElementPlugin extends UIElementCWFBase implements IDisable, IPropertyAccessor {
    
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
        setMaskMode(MaskMode.ENABLE);
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
     * @see org.carewebframework.shell.layout.UIElementCWFBase#setDesignMode
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
    
    @Override
    public boolean isDisabled() {
        return !isEnabled();
    }
    
    @Override
    public void setDisabled(boolean disabled) {
        setEnabled(!disabled);
    }
    
}
