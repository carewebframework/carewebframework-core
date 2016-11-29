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
package org.carewebframework.ui.command;

import java.util.HashSet;
import java.util.Set;

import org.carewebframework.ui.action.ActionUtil;
import org.carewebframework.ui.action.IAction;
import org.carewebframework.web.component.BaseComponent;
import org.carewebframework.web.component.BaseUIComponent;
import org.carewebframework.web.component.Div;
import org.carewebframework.web.event.Event;
import org.carewebframework.web.event.EventUtil;
import org.carewebframework.web.event.IEventListener;
import org.carewebframework.web.event.KeyEvent;

/**
 * Represents an intermediary between an event handler and a keyboard shortcut. A command provides
 * an indirect linkage between an event handler and an associated keyboard shortcut. In this way, a
 * component can bind directly to a command without dictating which keyboard shortcut(s) are bound
 * to that command. The binding of keyboard shortcuts to commands can then be managed centrally to
 * minimize conflicts and ensure consistency.
 */
public class Command {
    
    private static final String ATTR_TARGET = Command.class.getName() + ".target.";
    
    private static final String ATTR_DUMMY = Command.class.getName() + ".dummy";
    
    /**
     * Each command has a control key event listener to process shortcut key presses.
     */
    private class CtrlKeyListener implements IEventListener {
        
        /**
         * Respond to the control key press event by sending an onCommand event to the target
         * component. Note that because the target may be bound to more than one command, we must
         * verify that the triggering shortcut is bound to this command and ignore the event if it
         * is not.
         */
        @Override
        public void onEvent(Event event) {
            KeyEvent keyEvent = (KeyEvent) event;
            String shortcut = keyEvent.getKeycapture();
            
            if (isBound(shortcut)) {
                fire(keyEvent.getTarget(), keyEvent);
            }
        }
        
        public void registerComponent(BaseComponent component, boolean register) {
            if (register) {
                component.registerEventListener("keypress", this);
            } else {
                component.unregisterEventListener("keypress", this);
            }
        }
    }
    
    private final String name;
    
    private final Set<String> shortcutBindings = new HashSet<>();
    
    private final Set<BaseUIComponent> componentBindings = new HashSet<>();
    
    private final CtrlKeyListener keyEventListener = new CtrlKeyListener();
    
    /**
     * Creates a command with the specified name.
     * 
     * @param name The command name.
     */
    /*package*/ Command(String name) {
        this.name = name;
    }
    
    /**
     * Returns the command's associated name.
     * 
     * @return The associated name.
     */
    public String getName() {
        return name;
    }
    
    /**
     * Bind a component to this command.
     * 
     * @param component The component to be bound.
     * @param commandTarget Optional component that will be the target of the onCommand event. If
     *            null, the bound component will be the target.
     */
    /*package*/void bind(BaseUIComponent component, BaseUIComponent commandTarget) {
        if (componentBindings.add(component)) {
            setCommandTarget(component, commandTarget);
            CommandUtil.updateShortcuts(component, shortcutBindings, false);
            keyEventListener.registerComponent(component, true);
        }
    }
    
    /**
     * Bind a component to this command and action.
     * 
     * @param component The component to be bound.
     * @param action The action to be executed when the command is invoked.
     */
    /*package*/void bind(BaseUIComponent component, IAction action) {
        BaseUIComponent dummy = new Div();
        dummy.setAttribute(ATTR_DUMMY, true);
        dummy.setVisible(false);
        dummy.setParent(component.getPage());
        ActionUtil.addAction(dummy, action, CommandEvent.EVENT_NAME);
        bind(component, dummy);
    }
    
    /**
     * Bind a keyboard shortcut to this command.
     * 
     * @param shortcut Shortcut specifier in key capture format.
     */
    /*package*/void bind(String shortcut) {
        String normalized = CommandUtil.validateShortcut(shortcut);
        
        if (normalized == null) {
            throw new IllegalArgumentException("Invalid shortcut specifier: " + shortcut);
        }
        
        if (shortcutBindings.add(normalized)) {
            shortcutChanged(normalized, false);
        }
    }
    
    /**
     * Unbind a component from this command.
     * 
     * @param component The component to unbind.
     */
    public void unbind(BaseUIComponent component) {
        if (componentBindings.remove(component)) {
            keyEventListener.registerComponent(component, false);
            CommandUtil.updateShortcuts(component, shortcutBindings, true);
            setCommandTarget(component, null);
        }
    }
    
    /**
     * Unbind a keyboard shortcut from this command.
     * 
     * @param shortcut Shortcut specifier in ZK format.
     */
    /*package*/void unbind(String shortcut) {
        if (shortcutBindings.remove(shortcut)) {
            shortcutChanged(shortcut, true);
        }
    }
    
    /**
     * Called when a shortcut is bound or unbound.
     * 
     * @param shortcut The shortcut that has been bound or unbound.
     * @param unbind If true, the shortcut is being unbound.
     */
    private void shortcutChanged(String shortcut, boolean unbind) {
        Set<String> bindings = new HashSet<>();
        bindings.add(shortcut);
        
        for (BaseUIComponent component : componentBindings) {
            CommandUtil.updateShortcuts(component, bindings, unbind);
        }
    }
    
    /**
     * Returns true if the specified component is bound to this command.
     * 
     * @param component The component of interest.
     * @return True if the component is bound to this command.
     */
    public boolean isBound(BaseComponent component) {
        return componentBindings.contains(component);
    }
    
    /**
     * Returns true if the specified shortcut is bound to this command.
     * 
     * @param shortcut The shortcut of interest.
     * @return True if the shortcut is bound to this command.
     */
    public boolean isBound(String shortcut) {
        return shortcutBindings.contains(shortcut);
    }
    
    /**
     * Returns an iterable of shortcut bindings for this command.
     * 
     * @return Iterable of shortcut bindings.
     */
    public Iterable<String> getShortcutBindings() {
        return shortcutBindings;
    }
    
    /**
     * Returns an iterable of component bindings for this command.
     * 
     * @return Iterable of component bindings.
     */
    public Iterable<BaseUIComponent> getComponentBindings() {
        return componentBindings;
    }
    
    /**
     * Returns the name of the attribute used to store the command target in the bound component.
     * 
     * @return The attribute name.
     */
    private String getTargetAttributeName() {
        return ATTR_TARGET + name;
    }
    
    /**
     * Sets or removes the command target for the specified component.
     * 
     * @param component The bound component whose command target is being modified.
     * @param commandTarget If null, any associated command target is removed. Otherwise, this value
     *            is set as the command target.
     */
    private void setCommandTarget(BaseComponent component, BaseComponent commandTarget) {
        if (commandTarget == null) {
            commandTarget = (BaseComponent) component.removeAttribute(getTargetAttributeName());
            
            if (commandTarget != null && commandTarget.hasAttribute(ATTR_DUMMY)) {
                commandTarget.detach();
            }
        } else {
            component.setAttribute(getTargetAttributeName(), commandTarget);
        }
    }
    
    /**
     * Returns the command target associated with the specified component.
     * 
     * @param component The component whose command target is sought.
     * @return The associated command target. If there is no associated command target, returns the
     *         component itself (i.e., the component is the command target).
     */
    private BaseComponent getCommandTarget(BaseComponent component) {
        BaseComponent commandTarget = (BaseComponent) component.getAttribute(getTargetAttributeName());
        return commandTarget == null ? component : commandTarget;
    }
    
    /**
     * Fire the onCommand event to all bound components.
     * 
     * @param triggerEvent An optional trigger event.
     */
    public void fire(Event triggerEvent) {
        for (BaseComponent target : componentBindings) {
            if (!fire(target, triggerEvent)) {
                break;
            }
        }
    }
    
    /**
     * Fire the onCommand event to the specified target (or its associated command target).
     * 
     * @param target The target component.
     * @param triggerEvent The trigger event.
     * @return If false, do not propagate the event further.
     */
    public boolean fire(BaseComponent target, Event triggerEvent) {
        CommandEvent event = new CommandEvent(name, triggerEvent, getCommandTarget(target));
        EventUtil.post(event);
        return !event.isStopped();
    }
    
    /**
     * Two commands are equal if their names are the same (ignoring case).
     */
    @Override
    public boolean equals(Object command) {
        return command instanceof Command ? name.equalsIgnoreCase(((Command) command).name) : false;
    }
}
