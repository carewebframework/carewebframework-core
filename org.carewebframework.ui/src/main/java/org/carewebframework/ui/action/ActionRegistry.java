/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.ui.action;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.carewebframework.api.FrameworkUtil;
import org.carewebframework.common.RegistryMap;
import org.carewebframework.common.RegistryMap.DuplicateAction;

/**
 * Global (shared across application instances) and local (restricted to current desktop) registry
 * for actions. Local entries take precedence over global.
 */
public class ActionRegistry implements Iterable<IAction> {
    
    private static final String ATTR_LOCAL_MAP = ActionRegistry.class.getName() + ".localmap";
    
    private static final ActionRegistry instance = new ActionRegistry();
    
    private final RegistryMap<String, IAction> globalMap = new RegistryMap<String, IAction>(DuplicateAction.ERROR);
    
    public enum ActionScope {
        BOTH, GLOBAL, LOCAL
    };
    
    public static ActionRegistry getInstance() {
        return instance;
    }
    
    /**
     * Adds an action to the global or local registry.
     * 
     * @param asGlobal If true, register as global action; if false, local action.
     * @param id Unique id.
     * @param action Action to add.
     * @return The added action.
     */
    public static IAction register(boolean asGlobal, String id, IAction action) {
        instance.getMap(asGlobal).put(id, action);
        return action;
    }
    
    /**
     * Adds an action to the global or local registry.
     * 
     * @param asGlobal If true, register as global action; if false, local action.
     * @param id Unique id.
     * @param label Action's label.
     * @param script Action's script.
     * @return The added action.
     */
    public static IAction register(boolean asGlobal, String id, String label, String script) {
        return register(asGlobal, id, ActionUtil.createAction(label, script));
    }
    
    /**
     * Removes an action from the global or local registry.
     * 
     * @param asGlobal If true, unregister global action; if false, local action.
     * @param id Unique id.
     */
    public static void unregister(boolean asGlobal, String id) {
        instance.getMap(asGlobal).remove(id);
    }
    
    /**
     * Enforce singleton instance.
     */
    private ActionRegistry() {
    }
    
    /**
     * Returns a collection of actions registered to the specified scope.
     * 
     * @param scope Action scope from which to retrieve.
     * @return
     */
    public Collection<IAction> getRegisteredActions(ActionScope scope) {
        Map<String, IAction> actions = new HashMap<String, IAction>();
        
        if (scope == ActionScope.BOTH || scope == ActionScope.GLOBAL) {
            actions.putAll(getMap(true));
        }
        
        if (scope == ActionScope.BOTH || scope == ActionScope.LOCAL) {
            actions.putAll(getMap(false));
        }
        
        return actions.values();
    }
    
    /**
     * Attempt to locate in local registry first, then global.
     * 
     * @param id
     * @return
     */
    public IAction get(String id) {
        IAction action = getMap(false).get(id);
        return action == null ? getMap(true).get(id) : action;
    }
    
    /**
     * The iterator is a merged version of global and local entries.
     */
    @Override
    public Iterator<IAction> iterator() {
        return getRegisteredActions(ActionScope.BOTH).iterator();
    }
    
    /**
     * Returns a reference to the map for global or local actions.
     * 
     * @param global If true, return the global map; if false, the local map.
     * @return Map for local actions.
     */
    private RegistryMap<String, IAction> getMap(boolean global) {
        if (global) {
            return globalMap;
        }
        
        @SuppressWarnings("unchecked")
        RegistryMap<String, IAction> map = (RegistryMap<String, IAction>) FrameworkUtil.getAttribute(ATTR_LOCAL_MAP);
        
        if (map == null) {
            FrameworkUtil.setAttribute(ATTR_LOCAL_MAP, map = new RegistryMap<String, IAction>());
        }
        
        return map;
    }
    
}
