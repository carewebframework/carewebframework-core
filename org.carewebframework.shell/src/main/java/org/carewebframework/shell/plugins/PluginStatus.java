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
package org.carewebframework.shell.plugins;

import java.util.ArrayList;
import java.util.List;

import org.carewebframework.api.FrameworkUtil;
import org.carewebframework.shell.elements.ElementPlugin;

/**
 * Base class for defining logic that determines when a plugin should be enabled. Plugin authors may
 * implement the logic in a subclass.
 */
public abstract class PluginStatus implements IPluginEventListener {
    
    private final List<ElementPlugin> plugins = new ArrayList<>();
    
    private boolean disabled;
    
    private boolean initialized;
    
    /**
     * Register with the framework to connect any context change listeners.
     */
    public PluginStatus() {
        FrameworkUtil.getAppFramework().registerObject(this);
    }
    
    /**
     * Return disabled status.
     * 
     * @return True if disabled.
     */
    public boolean isDisabled() {
        return disabled;
    }
    
    /**
     * Override to provide logic to determine when the plugin should be disabled.
     * 
     * @return True if the plugin should be disabled.
     */
    protected boolean checkDisabled() {
        return disabled;
    }
    
    /**
     * Set the disabled status. Notifies subscribed containers of the change.
     * 
     * @param disabled True to disable the plugin.
     */
    private void setDisabled(boolean disabled) {
        if (disabled != this.disabled) {
            this.disabled = disabled;
            updateStatus();
        }
    }
    
    /**
     * Call to update the disabled status for the bean. If the status has changed, all subscribing
     * containers will be notified.
     */
    protected void updateDisabled() {
        initialized = true;
        setDisabled(checkDisabled());
    }
    
    /**
     * Initialize disabled flag.
     */
    private void init() {
        if (!initialized) {
            initialized = true;
            disabled = checkDisabled();
        }
    }
    
    /**
     * Subscribe/unsubscribe container.
     */
    @Override
    public void onPluginEvent(PluginEvent event) {
        ElementPlugin plugin = event.getPlugin();
        
        switch (event.getAction()) {
            case SUBSCRIBE:
                init();
                plugins.add(plugin);
                plugin.setDisabled(isDisabled());
                break;
            
            case UNSUBSCRIBE:
                plugins.remove(plugin);
                plugin.setDisabled(false);
                break;
        }
    }
    
    /**
     * Updates the disabled status of all registered containers.
     */
    protected void updateStatus() {
        if (!plugins.isEmpty()) {
            boolean disabled = isDisabled();
            
            for (ElementPlugin container : plugins) {
                container.setDisabled(disabled);
            }
        }
    }
    
}
