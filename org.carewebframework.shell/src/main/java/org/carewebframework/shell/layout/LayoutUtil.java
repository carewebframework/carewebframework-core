/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.shell.layout;

import java.util.List;

import org.carewebframework.api.spring.SpringUtil;

/**
 * Static utility class for accessing layout services.
 */
public class LayoutUtil {
    
    private static ILayoutService layoutService;
    
    public static ILayoutService getLayoutService() {
        if (layoutService == null) {
            layoutService = SpringUtil.getBean("layoutService", ILayoutService.class);
        }
        
        return layoutService;
    }
    
    /**
     * Validates a layout name.
     * 
     * @param name Layout name to validate.
     * @return True if the name is valid.
     */
    public static boolean validateName(String name) {
        return getLayoutService().validateName(name);
    }
    
    /**
     * Returns true if the specified layout exists.
     * 
     * @param name
     * @return True if layout exists.
     * @param shared Shared or personal layout.
     */
    public static boolean layoutExists(String name, boolean shared) {
        return getLayoutService().layoutExists(name, shared);
    }
    
    /**
     * Saves a layout with the specified name and content.
     * 
     * @param name The layout name.
     * @param content The layout content.
     * @param shared If true, save as a shared layout; otherwise, as a personal layout.
     */
    public static void saveLayout(String name, String content, boolean shared) {
        getLayoutService().saveLayout(name, content, shared);
    }
    
    /**
     * Rename a layout.
     * 
     * @param oldName The original layout name.
     * @param newName The new layout name.
     * @param shared Shared or personal layout.
     */
    public static void renameLayout(String oldName, String newName, boolean shared) {
        getLayoutService().renameLayout(oldName, newName, shared);
    }
    
    /**
     * Clone a layout.
     * 
     * @param oldName The original layout name.
     * @param newName The new layout name.
     * @param shared Shared or personal layout.
     */
    public static void cloneLayout(String oldName, String newName, boolean shared) {
        getLayoutService().cloneLayout(oldName, newName, shared);
    }
    
    /**
     * Delete a layout.
     * 
     * @param name The layout name.
     * @param shared Shared or personal layout.
     */
    public static void deleteLayout(String name, boolean shared) {
        getLayoutService().deleteLayout(name, shared);
    }
    
    /**
     * Returns the layout content.
     * 
     * @param name The layout name.
     * @param shared If true, returns a shared layout; otherwise, a personal layout.
     * @return The layout content.
     */
    public static String getLayout(String name, boolean shared) {
        return getLayoutService().getLayout(name, shared);
    }
    
    /**
     * Load the layout associated with the specified application id.
     * 
     * @param appId An application id.
     * @return The layout content.
     */
    public static String getLayoutByAppId(String appId) {
        return getLayoutService().getLayoutByAppId(appId);
    }
    
    /**
     * Returns a list of saved layouts.
     * 
     * @param shared If true, return shared layouts; otherwise, return personal layouts.
     * @return List of saved layouts.
     */
    public static List<String> getLayouts(boolean shared) {
        return getLayoutService().getLayouts(shared);
    }
    
    /**
     * Enforce static class.
     */
    private LayoutUtil() {
    };
}
