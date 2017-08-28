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

import java.util.List;

import org.carewebframework.api.event.EventManager;
import org.carewebframework.api.event.IEventManager;
import org.carewebframework.api.event.IGenericEvent;
import org.fujion.common.StrUtil;
import org.carewebframework.shell.elements.ElementPlugin;

/**
 * This can serve as a bean resource for a plugin to insure that a plugin is loaded when a specific
 * generic event is received. To configure properly, declare it as a prototype bean and reference
 * its bean id as a bean resource in the plugin definition.
 */
public class PluginWakeOnMessage implements IPluginEventListener, IGenericEvent<Object> {
    
    final private List<String> eventNames;
    
    private ElementPlugin plugin;
    
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
                plugin = event.getPlugin();
                doSubscribe(true);
                break;
            
            case LOAD: // Stop listening once loaded.
                plugin.unregisterListener(this);
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
        plugin.load();
    }
    
}
