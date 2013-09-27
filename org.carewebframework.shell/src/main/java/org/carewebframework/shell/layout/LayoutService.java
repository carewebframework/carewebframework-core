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

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import org.carewebframework.api.property.IPropertyService;

/**
 * Default layout service implementation using property service for persistence.
 */
public class LayoutService implements ILayoutService {
    
    private final IPropertyService propertyService;
    
    public LayoutService(IPropertyService propertyService) {
        this.propertyService = propertyService;
    }
    
    /**
     * Validates a layout name.
     * 
     * @param name Layout name to validate.
     * @return True if the name is valid.
     */
    @Override
    public boolean validateName(String name) {
        return name != null && !name.isEmpty() && StringUtils.isAlphanumericSpace(name.replace('_', ' '));
    }
    
    /**
     * Returns true if the specified layout exists.
     * 
     * @param name
     * @return True if layout exists.
     * @param shared Shared or personal layout.
     */
    @Override
    public boolean layoutExists(String name, boolean shared) {
        return getLayouts(shared).contains(name);
    }
    
    /**
     * Saves a layout with the specified name and content.
     * 
     * @param name The layout name.
     * @param content The layout content.
     * @param shared If true, save as a shared layout; otherwise, as a personal layout.
     */
    @Override
    public void saveLayout(String name, String content, boolean shared) {
        propertyService.saveValue(getPropertyName(shared), name, shared, content);
    }
    
    /**
     * Rename a layout.
     * 
     * @param oldName The original layout name.
     * @param newName The new layout name.
     * @param shared Shared or personal layout.
     */
    @Override
    public void renameLayout(String oldName, String newName, boolean shared) {
        String text = getLayout(oldName, shared);
        saveLayout(newName, text, shared);
        deleteLayout(oldName, shared);
    }
    
    /**
     * Clone a layout.
     * 
     * @param oldName The original layout name.
     * @param newName The new layout name.
     * @param shared Shared or personal layout.
     */
    @Override
    public void cloneLayout(String oldName, String newName, boolean shared) {
        String text = getLayout(oldName, shared);
        saveLayout(newName, text, shared);
    }
    
    /**
     * Delete a layout.
     * 
     * @param name The layout name.
     * @param shared Shared or personal layout.
     */
    @Override
    public void deleteLayout(String name, boolean shared) {
        saveLayout(name, null, shared);
    }
    
    /**
     * Returns the layout content.
     * 
     * @param name The layout name.
     * @param shared If true, save as a shared layout; otherwise, as a personal layout.
     * @return The layout content.
     */
    @Override
    public String getLayout(String name, boolean shared) {
        return propertyService.getValue(getPropertyName(shared), name);
    }
    
    /**
     * Load the layout associated with the specified application id.
     * 
     * @param appId An application id.
     * @return The layout content.
     */
    @Override
    public String getLayoutByAppId(String appId) {
        String value = propertyService.getValue(LayoutConstants.PROPERTY_LAYOUT_ASSOCIATION, appId);
        return value == null ? null : getLayout(value, true);
    }
    
    /**
     * Returns a list of saved layouts.
     * 
     * @param shared If true, return shared layouts; otherwise, return personal layouts.
     * @return List of saved layouts.
     */
    @Override
    public List<String> getLayouts(boolean shared) {
        List<String> layouts = propertyService.getInstances(getPropertyName(shared), shared);
        Collections.sort(layouts, String.CASE_INSENSITIVE_ORDER);
        return layouts;
    }
    
    /**
     * Returns the name of the property to use.
     * 
     * @param shared Shared or personal layout.
     * @return
     */
    private String getPropertyName(boolean shared) {
        return shared ? LayoutConstants.PROPERTY_LAYOUT_SHARED : LayoutConstants.PROPERTY_LAYOUT_PRIVATE;
    }
    
}
