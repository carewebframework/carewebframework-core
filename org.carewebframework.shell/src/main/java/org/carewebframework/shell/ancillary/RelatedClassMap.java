package org.carewebframework.shell.ancillary;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.carewebframework.shell.elements.UIElementBase;

/**
 * Private inner class to support registering allowed parent - child relationships for UI elements.
 */
public class RelatedClassMap {
    
    private final Map<Class<? extends UIElementBase>, Set<Class<? extends UIElementBase>>> map = new HashMap<>();
    
    /**
     * Returns the set of related classes for the specified class. If no set yet exists, one is
     * created.
     * 
     * @param clazz Class whose related class list is sought.
     * @return The set of classes related to the specified class.
     */
    private Set<Class<? extends UIElementBase>> getRelatedClasses(Class<? extends UIElementBase> clazz) {
        Set<Class<? extends UIElementBase>> set = map.get(clazz);
        
        if (set == null) {
            set = new HashSet<>();
            map.put(clazz, set);
        }
        
        return set;
    }
    
    /**
     * Adds clazz2 as a related class to clazz1.
     * 
     * @param clazz1 The primary class.
     * @param clazz2 Class to be registered.
     */
    public void addRelated(Class<? extends UIElementBase> clazz1, Class<? extends UIElementBase> clazz2) {
        getRelatedClasses(clazz1).add(clazz2);
    }
    
    /**
     * Returns true if the specified class has any related classes.
     * 
     * @param clazz The primary class.
     * @return True if the specified class has any related classes.
     */
    public boolean hasRelated(Class<? extends UIElementBase> clazz) {
        Set<Class<? extends UIElementBase>> set = map.get(clazz);
        return set != null && !set.isEmpty();
    }
    
    /**
     * Returns true if class clazz2 or a superclass of clazz2 is related to clazz1.
     * 
     * @param clazz1 The primary class.
     * @param clazz2 The class to test.
     * @return True if class clazz2 or a superclass of clazz2 is related to clazz1.
     */
    public boolean isRelated(Class<? extends UIElementBase> clazz1, Class<? extends UIElementBase> clazz2) {
        Set<Class<? extends UIElementBase>> set = map.get(clazz1);
        
        if (set != null) {
            for (Class<?> clazz : set) {
                if (clazz.isAssignableFrom(clazz2)) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
}
