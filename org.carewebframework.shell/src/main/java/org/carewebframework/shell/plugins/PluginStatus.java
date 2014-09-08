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

import java.util.ArrayList;
import java.util.List;

import org.carewebframework.api.FrameworkUtil;

/**
 * Base class for defining logic that determines when a plugin should be enabled. Plugin authors may
 * implement the logic in a subclass.
 */
public abstract class PluginStatus implements IPluginEventListener {
    
    private final List<PluginContainer> containers = new ArrayList<PluginContainer>();
    
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
        switch (event.getAction()) {
            case SUBSCRIBE:
                init();
                containers.add(event.getContainer());
                event.getContainer().setDisabled(isDisabled());
                break;
            
            case UNSUBSCRIBE:
                containers.remove(event.getContainer());
                event.getContainer().setDisabled(false);
                break;
        }
    }
    
    /**
     * Updates the disabled status of all registered containers.
     */
    protected void updateStatus() {
        if (!containers.isEmpty()) {
            boolean disabled = isDisabled();
            
            for (PluginContainer container : containers) {
                container.setDisabled(disabled);
            }
        }
    }
    
}
