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

/**
 * Interface for implementing layout management.
 */
public interface ILayoutService {
    
    /**
     * Validates a layout name.
     * 
     * @param name Layout name to validate.
     * @return True if the name is valid.
     */
    boolean validateName(String name);
    
    /**
     * Returns true if the specified layout exists.
     * 
     * @param name
     * @return True if layout exists.
     * @param shared Shared or personal layout.
     */
    boolean layoutExists(String name, boolean shared);
    
    /**
     * Saves a layout with the specified name and content.
     * 
     * @param name The layout name.
     * @param content The layout content.
     * @param shared If true, save as a shared layout; otherwise, as a personal layout.
     */
    void saveLayout(String name, String content, boolean shared);
    
    /**
     * Rename a layout.
     * 
     * @param oldName The original layout name.
     * @param newName The new layout name.
     * @param shared Shared or personal layout.
     */
    void renameLayout(String oldName, String newName, boolean shared);
    
    /**
     * Clone a layout.
     * 
     * @param oldName The original layout name.
     * @param newName The new layout name.
     * @param shared Shared or personal layout.
     */
    void cloneLayout(String oldName, String newName, boolean shared);
    
    /**
     * Delete a layout.
     * 
     * @param name The layout name.
     * @param shared Shared or personal layout.
     */
    void deleteLayout(String name, boolean shared);
    
    /**
     * Returns the layout content.
     * 
     * @param name The layout name.
     * @param shared If true, save as a shared layout; otherwise, as a personal layout.
     * @return The layout content.
     */
    String getLayout(String name, boolean shared);
    
    /**
     * Returns the layout associated with the specified application id.
     * 
     * @param appId An application id.
     * @return The layout content.
     */
    String getLayoutByAppId(String appId);
    
    /**
     * Returns a list of saved layouts.
     * 
     * @param shared If true, return shared layouts; otherwise, return personal layouts.
     * @return List of saved layouts.
     */
    List<String> getLayouts(boolean shared);
    
}
