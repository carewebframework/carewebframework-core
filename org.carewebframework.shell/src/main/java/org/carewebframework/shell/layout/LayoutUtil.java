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
     * @param layoutId The layout identifier.
     * @return True if layout exists.
     */
    public static boolean layoutExists(LayoutIdentifier layoutId) {
        return getLayoutService().layoutExists(layoutId);
    }
    
    /**
     * Saves a layout with the specified name and content.
     * 
     * @param layoutId The layout identifier.
     * @param content The layout content.
     */
    public static void saveLayout(LayoutIdentifier layoutId, String content) {
        getLayoutService().saveLayout(layoutId, content);
    }
    
    /**
     * Rename a layout.
     * 
     * @param layoutId The original layout.
     * @param newName The new layout name.
     */
    public static void renameLayout(LayoutIdentifier layoutId, String newName) {
        getLayoutService().renameLayout(layoutId, newName);
    }
    
    /**
     * Clone a layout.
     * 
     * @param layoutId1 The original layout identifier.
     * @param layoutId2 The new layout identifier.
     */
    public static void cloneLayout(LayoutIdentifier layoutId1, LayoutIdentifier layoutId2) {
        getLayoutService().cloneLayout(layoutId1, layoutId2);
    }
    
    /**
     * Delete a layout.
     * 
     * @param layoutId The layout identifier.
     */
    public static void deleteLayout(LayoutIdentifier layoutId) {
        getLayoutService().deleteLayout(layoutId);
    }
    
    /**
     * Returns the layout content.
     * 
     * @param layoutId The layout identifier.
     * @return The layout content.
     */
    public static String getLayoutContent(LayoutIdentifier layoutId) {
        return getLayoutService().getLayoutContent(layoutId);
    }
    
    /**
     * Load the layout associated with the specified application id.
     * 
     * @param appId An application id.
     * @return The layout content.
     */
    public static String getLayoutContentByAppId(String appId) {
        return getLayoutService().getLayoutContentByAppId(appId);
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
