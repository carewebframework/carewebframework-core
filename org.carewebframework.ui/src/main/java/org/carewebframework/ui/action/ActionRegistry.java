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

import org.carewebframework.api.AbstractGlobalRegistry;
import org.carewebframework.api.FrameworkUtil;

/**
 * Global (shared across application instances) and local (restricted to current desktop) registry
 * for actions. Local entries take precedence over global.
 */
public class ActionRegistry extends AbstractGlobalRegistry<String, IAction> {
    
    private static final String ATTR_LOCAL_MAP = ActionRegistry.class.getName() + ".localmap";
    
    private static final ActionRegistry instance = new ActionRegistry();
    
    public enum ActionScope {
        BOTH, GLOBAL, LOCAL
    };
    
    public static ActionRegistry getInstance() {
        return instance;
    }
    
    /**
     * Adds an action to the global registry.
     * 
     * @param action Action to add.
     * @return The added action.
     */
    public static IAction addGlobalAction(IAction action) {
        getInstance().add(action);
        return action;
    }
    
    /**
     * Adds an action to the global registry.
     * 
     * @param label Action's label.
     * @param script Action's script.
     * @return The added action.
     */
    public static IAction addGlobalAction(String label, String script) {
        return addGlobalAction(ActionUtil.createAction(label, script));
    }
    
    /**
     * Removes an action from the global registry.
     * 
     * @param action Action to remove.
     */
    public static void removeGlobalAction(IAction action) {
        getInstance().remove(action);
    }
    
    /**
     * Removes an action from the global registry.
     * 
     * @param label Action's associated label.
     */
    public static void removeGlobalAction(String label) {
        getInstance().removeByKey(label);
    }
    
    /**
     * Adds an action to the local registry.
     * 
     * @param action Action to add.
     * @return The added action.
     */
    public static IAction addLocalAction(IAction action) {
        String key = action.getLabel();
        Map<String, IAction> map = getLocalMap();
        checkDuplicate(key, action, map);
        map.put(key, action);
        return action;
    }
    
    /**
     * Adds an action to the local registry.
     * 
     * @param label Action's associated label.
     * @param script Action's script.
     * @return The added action.
     */
    public static IAction addLocalAction(String label, String script) {
        return addLocalAction(ActionUtil.createAction(label, script));
    }
    
    /**
     * Removes an action from the local registry.
     * 
     * @param action Action to remove.
     */
    public static void removeLocalAction(IAction action) {
        removeLocalAction(action.getLabel());
    }
    
    /**
     * Removes an action from the local registry.
     * 
     * @param label Action's associated label.
     */
    public static void removeLocalAction(String label) {
        getLocalMap().remove(label);
    }
    
    /**
     * Returns a reference to the map for local actions.
     * 
     * @return Map for local actions.
     */
    private static Map<String, IAction> getLocalMap() {
        @SuppressWarnings("unchecked")
        Map<String, IAction> map = (Map<String, IAction>) FrameworkUtil.getAttribute(ATTR_LOCAL_MAP);
        
        if (map == null) {
            FrameworkUtil.setAttribute(ATTR_LOCAL_MAP, map = new HashMap<String, IAction>());
        }
        
        return map;
    }
    
    /**
     * Don't allow duplicates.
     */
    private ActionRegistry() {
        super(false);
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
            actions.putAll(globalMap);
        }
        
        if (scope == ActionScope.BOTH || scope == ActionScope.LOCAL) {
            actions.putAll(getLocalMap());
        }
        
        return actions.values();
    }
    
    @Override
    protected String getKey(IAction action) {
        return action.getLabel();
    }
    
    /**
     * Attempt to locate in local registry first, then global.
     */
    @Override
    public IAction get(String key) {
        IAction action = getLocalMap().get(key);
        return action == null ? super.get(key) : action;
    }
    
    /**
     * The iterator is a merged version of global and local entries.
     */
    @Override
    public Iterator<IAction> iterator() {
        return getRegisteredActions(ActionScope.BOTH).iterator();
    }
    
}
