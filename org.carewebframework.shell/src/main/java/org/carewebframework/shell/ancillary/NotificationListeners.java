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
package org.carewebframework.shell.ancillary;

import java.util.HashMap;
import java.util.Map;

import org.carewebframework.shell.elements.UIElementBase;

/**
 * Allows a UI element to listen to notifications from its parent or children.
 */
public class NotificationListeners {
    
    private Map<String, INotificationListener> listeners;
    
    public void register(String eventName, INotificationListener listener) {
        if (listener == null) {
            if (listeners != null) {
                listeners.remove(eventName);
            }
        } else {
            if (listeners == null) {
                listeners = new HashMap<>();
            }
            
            listeners.put(eventName, listener);
        }
    }
    
    public boolean notify(UIElementBase sender, String eventName, Object eventData) {
        INotificationListener listener = listeners == null ? null : listeners.get(eventName);
        return listener == null || listener.onNotification(sender, eventName, eventData);
    }
}
