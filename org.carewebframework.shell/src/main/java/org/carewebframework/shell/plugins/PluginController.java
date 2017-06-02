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

import org.carewebframework.api.thread.IAbortable;
import org.carewebframework.shell.elements.ElementBase;
import org.carewebframework.shell.elements.ElementPlugin;
import org.carewebframework.shell.elements.ElementPlugin.PluginContainer;
import org.carewebframework.ui.controller.FrameworkController;
import org.carewebframework.web.ancillary.IAutoWired;
import org.carewebframework.web.component.BaseComponent;

/**
 * Base controller for plugins. Offers convenience methods for determining activation state,
 * accessing the plugin plugin, and managing background threads.
 */
public class PluginController extends FrameworkController implements IPluginController, IPluginEvent {
    
    private boolean isActive;
    
    private ElementPlugin plugin;
    
    /**
     * Wire controller from toolbar components first, then from plugin.
     */
    @Override
    public void afterInitialized(BaseComponent comp) {
        super.afterInitialized(comp);
        PluginContainer container = comp.getAncestor(PluginContainer.class, true);
        plugin = (ElementPlugin) ElementBase.getAssociatedElement(container);
    }
    
    /**
     * Displays/clears a busy message.
     * 
     * @param message The message.
     */
    public void showBusy(String message) {
        plugin.setBusy(message);
    }
    
    @Override
    public void onLoad(ElementPlugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public void onActivate() {
        isActive = true;
    }
    
    @Override
    public void onInactivate() {
        isActive = false;
    }
    
    @Override
    public void onUnload() {
        abortBackgroundThreads();
    }
    
    public boolean isActive() {
        return isActive;
    }
    
    /**
     * Attaches a controller to the specified component and registers any recognized listeners to
     * the plugin.
     * 
     * @param comp Target component.
     * @param controller Controller to attach.
     */
    public void attachController(BaseComponent comp, IAutoWired controller) {
        plugin.tryRegisterListener(controller, true);
        comp.wireController(controller);
    }
    
    public ElementPlugin getPlugin() {
        return plugin;
    }
    
    /**
     * Remove a thread from the active list. Clears the busy state if this was the last active
     * thread.
     * 
     * @param thread Thread to remove.
     * @return The thread that was removed.
     */
    @Override
    protected IAbortable removeThread(IAbortable thread) {
        super.removeThread(thread);
        
        if (!hasActiveThreads()) {
            showBusy(null);
        }
        
        return thread;
    }
    
}
