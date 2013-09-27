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

/**
 * CareWeb plugins may implement this interface to respond to container-based events.
 */
public interface IPluginEvent {
    
    /**
     * Fired when the plugin is first loaded
     * 
     * @param container PluginContainer
     */
    void onLoad(PluginContainer container); // 
    
    /**
     * Fired when the plugin is activated in the UI
     */
    void onActivate();
    
    /**
     * Fired when the plugin is inactivated in the UI
     */
    void onInactivate();
    
    /**
     * Fired when the plugin is unloaded
     */
    void onUnload();
}
