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
package org.carewebframework.plugin.settings;

import org.apache.commons.lang.StringUtils;

import org.carewebframework.shell.designer.PropertyGrid;
import org.carewebframework.shell.plugins.PluginController;

import org.zkoss.zk.ui.Component;

/**
 * Controller for settings plugin. The settings plugin is a generic plugin for managing settings of
 * various types using the framework's built-in property editor. It requires an implementation of
 * the ISettingsProvider which provides an interface to the underlying data store for the settings.
 */
public class MainController extends PluginController {
    
    private static final long serialVersionUID = 1L;
    
    private PropertyGrid propertyGrid;
    
    private ISettingsProvider provider;
    
    private String providerBeanId;
    
    private String groupId;
    
    /**
     * Connects an embedded instance of the property grid to the plug-in's root component.
     */
    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        propertyGrid = PropertyGrid.create(null, comp, true);
        getContainer().registerProperties(this, "provider", "group");
    }
    
    /**
     * Returns the id of the bean that implements the ISettingsProvider interface.
     * 
     * @return The settings provider bean id.
     */
    public String getProvider() {
        return providerBeanId;
    }
    
    /**
     * Sets the id of the bean that implements the ISettingsProvider interface.
     * 
     * @param beanId The bean id.
     */
    public void setProvider(String beanId) {
        provider = getAppContext().getBean(beanId, ISettingsProvider.class);
        providerBeanId = beanId;
        init();
    }
    
    /**
     * Returns the unique id of the settings group that is to be loaded.
     * 
     * @return The group id.
     */
    public String getGroup() {
        return groupId;
    }
    
    /**
     * Sets the unique id of the settings group that is to be loaded.
     * 
     * @param groupId The group id.
     */
    public void setGroup(String groupId) {
        this.groupId = groupId;
        init();
    }
    
    /**
     * Activates the property grid once the provider and group ids are set.
     */
    private void init() {
        if (provider != null) {
            propertyGrid.setTarget(StringUtils.isEmpty(groupId) ? null : new Settings(groupId, provider));
        }
    }
}
