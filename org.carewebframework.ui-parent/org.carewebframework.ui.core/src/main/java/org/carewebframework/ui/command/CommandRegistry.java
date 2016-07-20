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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.carewebframework.api.spring.SpringUtil;

import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.KeyEvent;
import org.zkoss.zul.impl.XulElement;

/**
 * Registry for commands.
 */
public class CommandRegistry implements Iterable<Command> {
    
    private final Map<String, Command> commands = new HashMap<>();
    
    /**
     * Returns the singleton instance of the registry.
     * 
     * @return Command registry instance.
     */
    public static final CommandRegistry getInstance() {
        return SpringUtil.getBean("commandRegistry", CommandRegistry.class);
    }
    
    /**
     * Creates the command registry using the specified shortcut mappings.
     * 
     * @param shortcuts A map of shortcut mappings, where the key is the command name and the value
     *            contains the associated shortcut(s) in ZK format.
     */
    public CommandRegistry(Map<Object, Object> shortcuts) {
        super();
        bindShortcuts(shortcuts);
    }
    
    public void add(Command command) {
        if (commands.containsValue(command)) {
            throw new IllegalArgumentException("Command already exists: " + command.getName());
        }
        
        commands.put(command.getName(), command);
    }
    
    public Command get(String commandName) {
        return get(commandName, false);
    }
    
    /**
     * Retrieves the command associated with the specified name from the registry.
     * 
     * @param commandName Name of the command sought.
     * @param forceCreate If true and the command does not exist, one is created and added to the
     *            registry.
     * @return The associated command, or null if it does not exist (and forceCreate is false).
     */
    public Command get(String commandName, boolean forceCreate) {
        Command command = commands.get(commandName);
        
        if (command == null && forceCreate) {
            command = new Command(commandName);
            add(command);
        }
        
        return command;
    }
    
    /**
     * Binds the shortcuts specified in the map to the associated commands. The map key is the
     * command name and the associated value is a list of shortcuts bound to the command.
     * 
     * @param shortcuts Shortcut map.
     */
    private void bindShortcuts(Map<Object, Object> shortcuts) {
        for (Object commandName : shortcuts.keySet()) {
            bindShortcuts(commandName.toString(), shortcuts.get(commandName).toString());
        }
    }
    
    private void bindShortcuts(String commandName, String shortcuts) {
        Command command = get(commandName, true);
        
        for (String shortcut : CommandUtil.parseShortcuts(shortcuts, null)) {
            command.bind(shortcut);
        }
    }
    
    public void process(KeyEvent event) {
        if (event.getReference() instanceof XulElement) {
            String shortcut = CommandUtil.getShortcut(event);
            fireCommands(shortcut, event, (XulElement) event.getReference());
        }
    }
    
    public void fireCommands(String shortcut, Event triggerEvent, Iterable<? extends XulElement> targets) {
        for (Command command : this) {
            if (command.isBound(shortcut)) {
                for (XulElement target : targets) {
                    if (command.isBound(target)) {
                        if (!command.fire(target, triggerEvent)) {
                            break;
                        }
                    }
                }
            }
        }
    }
    
    public void fireCommands(String shortcut, Event triggerEvent, XulElement target) {
        for (Command command : this) {
            if (command.isBound(shortcut) && command.isBound(target)) {
                command.fire(target, triggerEvent);
            }
        }
    }
    
    @Override
    public Iterator<Command> iterator() {
        return commands.values().iterator();
    }
}
