/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.ui.command;

import org.apache.commons.beanutils.PropertyUtils;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;

/**
 * Event object for sending commands to components.
 */
public class CommandEvent extends Event {
    
    private static final long serialVersionUID = 1L;
    
    public static final String EVENT_NAME = "onCommand";
    
    private final Event triggerEvent;
    
    private final String commandName;
    
    public CommandEvent(String commandName, Event triggerEvent, Component target) {
        super(EVENT_NAME, target);
        this.commandName = commandName;
        this.triggerEvent = triggerEvent;
    }
    
    public String getCommandName() {
        return commandName;
    }
    
    public Event getTriggerEvent() {
        return triggerEvent;
    }
    
    public Component getReference() {
        try {
            Object ref = triggerEvent == null ? null : PropertyUtils.getProperty(triggerEvent, "reference");
            return ref instanceof Component ? (Component) ref : null;
        } catch (Exception e) {
            return null;
        }
    }
}
