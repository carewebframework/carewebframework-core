/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.shell.plugins;

import org.carewebframework.ui.FrameworkController;
import org.carewebframework.ui.thread.ZKThread;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.util.Composer;

/**
 * Base controller for plugins. Offers convenience methods for determining activation state,
 * accessing the plugin container, and managing background threads.
 */
public class PluginController extends FrameworkController implements IPluginEvent {
    
    private static final long serialVersionUID = 1L;
    
    private boolean isActive;
    
    private PluginContainer container;
    
    /**
     * Wire controller from toolbar components first, then from container.
     */
    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        container = PluginContainer.getContainer(comp);
    }
    
    /**
     * Displays/clears a busy message.
     * 
     * @param message The message.
     */
    public void showBusy(String message) {
        container.setBusy(message);
    }
    
    @Override
    public void onLoad(PluginContainer container) {
        this.container = container;
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
    
    public PluginContainer getContainer() {
        return container;
    }
    
    /**
     * Attaches a controller to the specified component and registers any recognized listeners to
     * the container.
     * 
     * @param comp Target component.
     * @param controller Controller to attach.
     * @throws Exception Unspecified exception.
     */
    public void attachController(Component comp, Composer<Component> controller) throws Exception {
        container.tryRegisterListener(controller, true);
        controller.doAfterCompose(comp);
    }
    
    /**
     * Remove a thread from the active list. Clears the busy state if this was the last active
     * thread.
     * 
     * @param thread Thread to remove.
     * @return The thread that was removed.
     */
    @Override
    protected ZKThread removeThread(ZKThread thread) {
        super.removeThread(thread);
        
        if (!hasActiveThreads()) {
            showBusy(null);
        }
        
        return thread;
    }
    
}
