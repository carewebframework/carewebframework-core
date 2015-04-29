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

import java.util.HashSet;
import java.util.Set;

import org.carewebframework.ui.action.ActionListener;
import org.carewebframework.ui.action.IAction;
import org.carewebframework.ui.zk.ZKUtil;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.KeyEvent;
import org.zkoss.zul.Div;
import org.zkoss.zul.impl.XulElement;

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
    private class CtrlKeyListener implements EventListener<Event> {
        
        /**
         * Respond to the control key press event by sending an onCommand event to the target
         * component. Note that because the target may be bound to more than one command, we must
         * verify that the triggering shortcut is bound to this command and ignore the event if it
         * is not.
         */
        @Override
        public void onEvent(Event event) {
            KeyEvent keyEvent = (KeyEvent) ZKUtil.getEventOrigin(event);
            String shortcut = CommandUtil.getShortcut(keyEvent);
            
            if (isBound(shortcut)) {
                fire(keyEvent.getTarget(), keyEvent);
            }
        }
        
        public void registerComponent(XulElement component, boolean register) {
            if (register) {
                component.addEventListener(Events.ON_CTRL_KEY, this);
            } else {
                component.removeEventListener(Events.ON_CTRL_KEY, this);
            }
        }
    }
    
    private final String name;
    
    private final Set<String> shortcutBindings = new HashSet<String>();
    
    private final Set<XulElement> componentBindings = new HashSet<XulElement>();
    
    private final CtrlKeyListener keyEventListener = new CtrlKeyListener();
    
    /**
     * Creates a command with the specified name.
     * 
     * @param name The command name.
     */
    /*package*/Command(final String name) {
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
    /*package*/void bind(final XulElement component, final Component commandTarget) {
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
    /*package*/void bind(final XulElement component, final IAction action) {
        Component dummy = new Div();
        dummy.setAttribute(ATTR_DUMMY, true);
        dummy.setVisible(false);
        dummy.setPage(component.getPage());
        ActionListener.addAction(dummy, action, CommandEvent.EVENT_NAME);
        bind(component, dummy);
    }
    
    /**
     * Bind a keyboard shortcut to this command.
     * 
     * @param shortcut Shortcut specifier in ZK format.
     */
    /*package*/void bind(final String shortcut) {
        if (!CommandUtil.validateShortcut(shortcut)) {
            throw new IllegalArgumentException("Invalid shortcut specifier: " + shortcut);
        }
        
        if (shortcutBindings.add(shortcut)) {
            shortcutChanged(shortcut, false);
        }
    }
    
    /**
     * Unbind a component from this command.
     * 
     * @param component The component to unbind.
     */
    public void unbind(final XulElement component) {
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
    /*package*/void unbind(final String shortcut) {
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
    private void shortcutChanged(final String shortcut, final boolean unbind) {
        Set<String> bindings = new HashSet<String>();
        bindings.add(shortcut);
        
        for (XulElement component : componentBindings) {
            CommandUtil.updateShortcuts(component, bindings, unbind);
        }
    }
    
    /**
     * Returns true if the specified component is bound to this command.
     * 
     * @param component The component of interest.
     * @return True if the component is bound to this command.
     */
    public boolean isBound(final XulElement component) {
        return componentBindings.contains(component);
    }
    
    /**
     * Returns true if the specified shortcut is bound to this command.
     * 
     * @param shortcut The shortcut of interest.
     * @return True if the shortcut is bound to this command.
     */
    public boolean isBound(final String shortcut) {
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
    public Iterable<XulElement> getComponentBindings() {
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
    private void setCommandTarget(Component component, Component commandTarget) {
        if (commandTarget == null) {
            commandTarget = (Component) component.removeAttribute(getTargetAttributeName());
            
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
    private Component getCommandTarget(Component component) {
        Component commandTarget = (Component) component.getAttribute(getTargetAttributeName());
        return commandTarget == null ? component : commandTarget;
    }
    
    /**
     * Fire the onCommand event to all bound components.
     * 
     * @param triggerEvent An optional trigger event.
     */
    public void fire(Event triggerEvent) {
        for (XulElement target : componentBindings) {
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
    public boolean fire(Component target, Event triggerEvent) {
        CommandEvent event = new CommandEvent(name, triggerEvent, getCommandTarget(target));
        Events.postEvent(event);
        return event.isPropagatable();
    }
    
    /**
     * Two commands are equal if their names are the same (ignoring case).
     */
    @Override
    public boolean equals(Object command) {
        return command instanceof Command ? name.equalsIgnoreCase(((Command) command).name) : false;
    }
}
