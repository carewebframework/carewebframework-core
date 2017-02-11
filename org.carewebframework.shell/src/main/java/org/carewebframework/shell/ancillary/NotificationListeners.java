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
