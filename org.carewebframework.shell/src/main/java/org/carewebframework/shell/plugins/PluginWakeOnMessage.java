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

import java.util.List;

import org.carewebframework.api.event.EventManager;
import org.carewebframework.api.event.IEventManager;
import org.carewebframework.api.event.IGenericEvent;
import org.carewebframework.common.StrUtil;

/**
 * This can serve as a bean resource for a plugin to insure that a plugin is loaded when a specific
 * generic event is received. To configure properly, declare it as a prototype bean and reference
 * its bean id as a bean resource in the plugin definition.
 */
public class PluginWakeOnMessage implements IPluginEventListener, IGenericEvent<Object> {
    
    final private List<String> eventNames;
    
    private PluginContainer container;
    
    private final IEventManager eventManager = EventManager.getInstance();
    
    /**
     * Sets the monitored event.
     * 
     * @param eventNames The names of one or more events to monitor. Separate multiple event names
     *            with commas.
     */
    public PluginWakeOnMessage(String eventNames) {
        this.eventNames = StrUtil.toList(eventNames, ",");
    }
    
    /**
     * Subscribe/unsubscribe to/from specified events.
     * 
     * @param subscribe If true, subscribe; if false, unsubscribe.
     */
    private void doSubscribe(boolean subscribe) {
        for (String eventName : eventNames) {
            if (subscribe) {
                eventManager.subscribe(eventName, this);
            } else {
                eventManager.unsubscribe(eventName, this);
            }
        }
    }
    
    /**
     * Listen for plugin lifecycle events.
     * 
     * @see org.carewebframework.shell.plugins.IPluginEventListener#onPluginEvent(org.carewebframework.shell.plugins.PluginEvent)
     */
    @Override
    public void onPluginEvent(PluginEvent event) {
        switch (event.getAction()) {
            case SUBSCRIBE: // Upon initial subscription, begin listening for specified generic events.
                container = event.getContainer();
                doSubscribe(true);
                break;
            
            case LOAD: // Stop listening once loaded.
                container.unregisterListener(this);
                break;
            
            case UNSUBSCRIBE: // Stop listening for generic events once unsubscribed from plugin events.
                doSubscribe(false);
                break;
        }
    }
    
    /**
     * When one of the subscribed events is published, force the container to load.
     * 
     * @see org.carewebframework.api.event.IGenericEvent#eventCallback(java.lang.String,
     *      java.lang.Object)
     */
    @Override
    public void eventCallback(String eventName, Object eventData) {
        container.load();
    }
    
}
