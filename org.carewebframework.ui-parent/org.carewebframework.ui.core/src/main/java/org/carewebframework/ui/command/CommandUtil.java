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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import org.carewebframework.ui.action.IAction;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.KeyEvent;
import org.zkoss.zul.impl.XulElement;

/**
 * Static utility class.
 */
public class CommandUtil {
    
    private static final Map<Integer, String> specialKeys = new HashMap<>();
    
    /*
     * Initializes map for converting key code to symbolic form.
     */
    static {
        specialKeys.put(KeyEvent.F1, "f1");
        specialKeys.put(KeyEvent.F2, "f2");
        specialKeys.put(KeyEvent.F3, "f3");
        specialKeys.put(KeyEvent.F4, "f4");
        specialKeys.put(KeyEvent.F5, "f5");
        specialKeys.put(KeyEvent.F6, "f6");
        specialKeys.put(KeyEvent.F7, "f7");
        specialKeys.put(KeyEvent.F8, "f8");
        specialKeys.put(KeyEvent.F9, "f9");
        specialKeys.put(KeyEvent.F10, "f10");
        specialKeys.put(KeyEvent.F11, "f11");
        specialKeys.put(KeyEvent.F12, "f12");
        specialKeys.put(8, "bak");
        specialKeys.put(KeyEvent.DELETE, "del");
        specialKeys.put(KeyEvent.DOWN, "down");
        specialKeys.put(KeyEvent.END, "end");
        specialKeys.put(KeyEvent.HOME, "home");
        specialKeys.put(KeyEvent.INSERT, "ins");
        specialKeys.put(KeyEvent.LEFT, "left");
        specialKeys.put(KeyEvent.PAGE_DOWN, "pgdn");
        specialKeys.put(KeyEvent.PAGE_UP, "pgup");
        specialKeys.put(KeyEvent.RIGHT, "right");
        specialKeys.put(KeyEvent.UP, "up");
    }
    
    /**
     * Given a key event, returns the symbolic representation of the typed key.
     * 
     * @param event The key press event.
     * @return The symbolic representation of the typed key.
     */
    public static String getShortcut(KeyEvent event) {
        StringBuilder sb = new StringBuilder();
        
        if (event.isAltKey()) {
            sb.append('@');
        }
        
        if (event.isCtrlKey()) {
            sb.append('^');
        }
        
        if (event.isShiftKey()) {
            sb.append('$');
        }
        
        String specialKey = specialKeys.get(event.getKeyCode());
        
        if (specialKey != null) {
            sb.append('#').append(specialKey);
        } else {
            sb.append(Character.toLowerCase(event.getKeyCode()));
        }
        
        return sb.toString();
    }
    
    /**
     * Updates a components associated shortcuts by adding or removing the specified set of
     * shortcuts.
     * 
     * @param component The component whose ctrlKeys property is to be updated.
     * @param shortcuts The list of shortcuts to be added or removed.
     * @param remove If true, the specified shortcuts are removed from the component's ctrlKeys
     *            property. If false, they are added.
     */
    /*package*/static void updateShortcuts(XulElement component, String shortcuts, boolean remove) {
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
    /*package*/static void updateShortcuts(XulElement component, Set<String> shortcuts, boolean remove) {
        Set<String> currentShortcuts = CommandUtil.parseShortcuts(component.getCtrlKeys(), null);
        
        if (remove) {
            currentShortcuts.removeAll(shortcuts);
        } else {
            currentShortcuts.addAll(shortcuts);
        }
        
        component.setCtrlKeys(CommandUtil.concatShortcuts(currentShortcuts));
    }
    
    /**
     * Returns true if the shortcut is a valid symbolic representation of a supported key press.
     * 
     * @param shortcut Symbolic representation of a shortcut.
     * @return True if the shortcut is valid.
     */
    /*package*/static boolean validateShortcut(String shortcut) {
        if (shortcut.startsWith("$")) {
            if (shortcut.startsWith("$#")) {
                shortcut = shortcut.substring(1);
            } else {
                return false;
            }
        }
        
        if (shortcut.startsWith("^") || shortcut.startsWith("@")) {
            shortcut = shortcut.substring(1);
            
            if (shortcut.length() == 1 && StringUtils.isAlphanumeric(shortcut)) {
                return true;
            }
        }
        
        if (!shortcut.startsWith("#")) {
            return false;
        }
        
        shortcut = shortcut.substring(1);
        return specialKeys.values().contains(shortcut);
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
            if (validateShortcut(shortcut)) {
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
     * Associates a ZK component with a command.
     * 
     * @param commandName Name of the command.
     * @param component Component to be associated.
     */
    public static void associateCommand(String commandName, XulElement component) {
        associateCommand(commandName, component, (Component) null);
    }
    
    /**
     * Associates a ZK component with a command.
     * 
     * @param commandName Name of the command.
     * @param component Component to be associated.
     * @param commandTarget The target of the command event. A null value indicates that the
     *            component itself will be the target.
     */
    public static void associateCommand(String commandName, XulElement component, Component commandTarget) {
        getCommand(commandName, true).bind(component, commandTarget);
    }
    
    /**
     * Associates a ZK component with a command and action.
     * 
     * @param commandName Name of the command.
     * @param component Component to be associated.
     * @param action Action to be executed when the command is invoked.
     */
    public static void associateCommand(String commandName, XulElement component, IAction action) {
        getCommand(commandName, true).bind(component, action);
    }
    
    /**
     * Dissociate a ZK component with a command.
     * 
     * @param commandName Name of the command.
     * @param component Component to be associated.
     */
    public static void dissociateCommand(String commandName, XulElement component) {
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
    public static void dissociateAll(XulElement component) {
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
            String shortcut = sb.toString();
            
            if (validateShortcut(shortcut)) {
                shortcuts.add(shortcut);
            }
            
            sb.delete(0, sb.length());
        }
    }
    
    /**
     * Enforces static class.
     */
    private CommandUtil() {
    };
}
