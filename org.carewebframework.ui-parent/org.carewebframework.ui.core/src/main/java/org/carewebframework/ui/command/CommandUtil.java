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

import org.carewebframework.ui.action.IAction;
import org.fujion.component.BaseUIComponent;
import org.fujion.event.KeyCode;

/**
 * Static utility class.
 */
public class CommandUtil {
    
    /**
     * Updates a components associated shortcuts by adding or removing the specified set of
     * shortcuts.
     * 
     * @param component The component whose ctrlKeys property is to be updated.
     * @param shortcuts The list of shortcuts to be added or removed.
     * @param remove If true, the specified shortcuts are removed from the component's ctrlKeys
     *            property. If false, they are added.
     */
    /*package*/static void updateShortcuts(BaseUIComponent component, String shortcuts, boolean remove) {
        updateShortcuts(component, parseShortcuts(shortcuts, null), remove);
    }
    
    /**
     * Updates a components associated shortcuts by adding or removing the specified set of
     * shortcuts.
     * 
     * @param component The component whose ctrlKeys property is to be updated.
     * @param shortcuts The set of shortcuts to be added or removed.
     * @param remove If true, the specified shortcuts are removed from the component's ctrlKeys
     *            property. If false, they are added.
     */
    /*package*/static void updateShortcuts(BaseUIComponent component, Set<String> shortcuts, boolean remove) {
        Set<String> currentShortcuts = CommandUtil.parseShortcuts(component.getKeycapture(), null);
        
        if (remove) {
            currentShortcuts.removeAll(shortcuts);
        } else {
            currentShortcuts.addAll(shortcuts);
        }
        
        component.setKeycapture(CommandUtil.concatShortcuts(currentShortcuts));
    }
    
    /**
     * Returns a normalized version of the shortcut, or null if it is not a valid shortcut.
     * 
     * @param shortcut Symbolic representation of a shortcut.
     * @return The normalized shortcut, or null if not valid.
     */
    /*package*/static String validateShortcut(String shortcut) {
        try {
            shortcut = KeyCode.normalizeKeyCapture(shortcut);
        } catch (Exception e) {
            shortcut = null;
        }
        
        return shortcut;
    }
    
    /**
     * Returns a concatenated list of shortcuts from a set.
     * 
     * @param result Set of shortcut entries.
     * @return Concatenated list of validated shortcut entries.
     */
    /*package*/static String concatShortcuts(Set<String> result) {
        StringBuilder sb = new StringBuilder();
        
        for (String shortcut : result) {
            shortcut = validateShortcut(shortcut);
            
            if (shortcut != null) {
                sb.append(shortcut);
            }
        }
        
        return sb.toString();
    }
    
    /**
     * Parses a concatenated list of shortcuts into a collection of individual shortcuts.
     * 
     * @param shortcuts Concatenated list of shortcuts. Commas and spaces are ignored.
     * @param result Set to which parsed shortcuts will be added, or null to create a new one.
     * @return The set of parsed shortcuts.
     */
    /*package*/static Set<String> parseShortcuts(String shortcuts, Set<String> result) {
        if (result == null) {
            result = new HashSet<>();
        }
        
        if (shortcuts == null || shortcuts.isEmpty()) {
            return result;
        }
        
        int pos = 0;
        int len = shortcuts.length();
        boolean symbolic = false;
        StringBuilder sb = new StringBuilder();
        
        while (pos < len) {
            char c = shortcuts.charAt(pos++);
            
            switch (c) {
                case '$':
                case '^':
                case '@':
                    addShortcut(sb, result);
                    sb.append(c);
                    symbolic = false;
                    break;
                
                case '#':
                    if (symbolic) {
                        addShortcut(sb, result);
                    }
                    
                    sb.append(c);
                    symbolic = true;
                    break;
                
                case ',':
                case ' ':
                    break;
                
                default:
                    sb.append(c);
                    
                    if (!symbolic) {
                        addShortcut(sb, result);
                    }
                    
                    break;
            }
        }
        addShortcut(sb, result);
        return result;
    }
    
    /**
     * Associates a UI component with a command.
     * 
     * @param commandName Name of the command.
     * @param component Component to be associated.
     */
    public static void associateCommand(String commandName, BaseUIComponent component) {
        associateCommand(commandName, component, (BaseUIComponent) null);
    }
    
    /**
     * Associates a UI component with a command.
     * 
     * @param commandName Name of the command.
     * @param component Component to be associated.
     * @param commandTarget The target of the command event. A null value indicates that the
     *            component itself will be the target.
     */
    public static void associateCommand(String commandName, BaseUIComponent component, BaseUIComponent commandTarget) {
        getCommand(commandName, true).bind(component, commandTarget);
    }
    
    /**
     * Associates a UI component with a command and action.
     * 
     * @param commandName Name of the command.
     * @param component Component to be associated.
     * @param action Action to be executed when the command is invoked.
     */
    public static void associateCommand(String commandName, BaseUIComponent component, IAction action) {
        getCommand(commandName, true).bind(component, action);
    }
    
    /**
     * Dissociate a UI component with a command.
     * 
     * @param commandName Name of the command.
     * @param component Component to be associated.
     */
    public static void dissociateCommand(String commandName, BaseUIComponent component) {
        Command command = getCommand(commandName, false);
        
        if (command != null) {
            command.unbind(component);
        }
    }
    
    /**
     * Removes all command bindings for a component.
     * 
     * @param component The component.
     */
    public static void dissociateAll(BaseUIComponent component) {
        for (Command command : CommandRegistry.getInstance()) {
            command.unbind(component);
        }
    }
    
    /**
     * Returns a command from the command registry.
     * 
     * @param commandName Name of the command.
     * @param forceCreate If true and the named command does not exist, one will be created and
     *            added to the command registry.
     * @return The command object corresponding to the specified command name.
     */
    public static Command getCommand(String commandName, boolean forceCreate) {
        return CommandRegistry.getInstance().get(commandName, forceCreate);
    }
    
    /**
     * Adds a parsed shortcut to the set of shortcuts. Only valid shortcuts are added.
     * 
     * @param sb String builder containing a parsed shortcut.
     * @param shortcuts Set to receive parsed shortcut.
     */
    private static void addShortcut(StringBuilder sb, Set<String> shortcuts) {
        if (sb.length() > 0) {
            String shortcut = validateShortcut(sb.toString());
            
            if (shortcut != null) {
                shortcuts.add(shortcut);
            }
            
            sb.delete(0, sb.length());
        }
    }
    
    /**
     * Enforces static class.
     */
    private CommandUtil() {
    }
}
