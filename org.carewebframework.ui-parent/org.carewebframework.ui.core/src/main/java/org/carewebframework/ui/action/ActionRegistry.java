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
import java.util.Map;

import org.carewebframework.api.FrameworkUtil;
import org.carewebframework.common.AbstractRegistry;

/**
 * Global (shared across application instances) and local (restricted to current desktop) registry
 * for actions. Local entries take precedence over global.
 */
public class ActionRegistry extends AbstractRegistry<String, ActionEntry> {
    
    private static final String ATTR_LOCAL_REGISTRY = ActionRegistry.class.getName() + ".local";
    
    private static final ActionRegistry instance = new ActionRegistry();
    
    public enum ActionScope {
        BOTH, GLOBAL, LOCAL
    };
    
    /**
     * Adds an action to the global or local registry.
     * 
     * @param asGlobal If true, register as global action; if false, local action.
     * @param id Unique id.
     * @param action Action to add.
     * @return The added action.
     */
    public static ActionEntry register(boolean asGlobal, String id, IAction action) {
        ActionEntry actionEntry = new ActionEntry(id, action);
        getRegistry(asGlobal).register(actionEntry);
        return actionEntry;
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
    public static ActionEntry register(boolean asGlobal, String id, String label, String script) {
        return register(asGlobal, id, new ActionEntry(id, label, script));
    }
    
    /**
     * Removes an action from the global or local registry.
     * 
     * @param asGlobal If true, unregister global action; if false, local action.
     * @param id Unique id.
     */
    public static void unregister(boolean asGlobal, String id) {
        getRegistry(asGlobal).unregisterByKey(id);
    }
    
    /**
     * Attempt to locate in local registry first, then global.
     * 
     * @param id The action id.
     * @return The action entry (possibly null).
     */
    public static ActionEntry getRegisteredAction(String id) {
        ActionEntry action = getRegistry(false).get(id);
        return action == null ? getRegistry(true).get(id) : action;
    }
    
    /**
     * Returns a collection of actions registered to the specified scope.
     * 
     * @param scope Action scope from which to retrieve.
     * @return Actions associated with specified scope.
     */
    public static Collection<ActionEntry> getRegisteredActions(ActionScope scope) {
        Map<String, ActionEntry> actions = new HashMap<>();
        
        if (scope == ActionScope.BOTH || scope == ActionScope.GLOBAL) {
            actions.putAll(getRegistry(true).map);
        }
        
        if (scope == ActionScope.BOTH || scope == ActionScope.LOCAL) {
            actions.putAll(getRegistry(false).map);
        }
        
        return actions.values();
    }
    
    /**
     * Returns a reference to the registry for global or local actions.
     * 
     * @param global If true, return the global registry; if false, the local registry.
     * @return An action registry.
     */
    private static ActionRegistry getRegistry(boolean global) {
        if (global) {
            return instance;
        }
        
        ActionRegistry registry = (ActionRegistry) FrameworkUtil.getAttribute(ATTR_LOCAL_REGISTRY);
        
        if (registry == null) {
            FrameworkUtil.setAttribute(ATTR_LOCAL_REGISTRY, registry = new ActionRegistry());
        }
        
        return registry;
    }
    
    /**
     * Create global or local action registry.
     */
    private ActionRegistry() {
        super();
    }
    
    /**
     * Action entry id is the key.
     */
    @Override
    protected String getKey(ActionEntry item) {
        return item.getId();
    }
    
}
