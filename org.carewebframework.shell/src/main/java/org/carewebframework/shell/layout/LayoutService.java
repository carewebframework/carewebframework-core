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
     * @param layout The layout identifier.
     * @return True if layout exists.
     */
    @Override
    public boolean layoutExists(LayoutIdentifier layout) {
        return getLayouts(layout.shared).contains(layout.name);
    }
    
    /**
     * Saves a layout with the specified name and content.
     * 
     * @param layout The layout identifier.
     * @param content The layout content.
     */
    @Override
    public void saveLayout(LayoutIdentifier layout, String content) {
        propertyService.saveValue(getPropertyName(layout.shared), layout.name, layout.shared, content);
    }
    
    /**
     * Rename a layout.
     * 
     * @param layout The original layout identifier.
     * @param newName The new layout name.
     */
    @Override
    public void renameLayout(LayoutIdentifier layout, String newName) {
        String text = getLayoutContent(layout);
        saveLayout(new LayoutIdentifier(newName, layout.shared), text);
        deleteLayout(layout);
    }
    
    /**
     * Clone a layout.
     * 
     * @param layout The original layout identifier.
     * @param layout2 The new layout identifier.
     */
    @Override
    public void cloneLayout(LayoutIdentifier layout, LayoutIdentifier layout2) {
        String text = getLayoutContent(layout);
        saveLayout(layout2, text);
    }
    
    /**
     * Delete a layout.
     * 
     * @param layout The layout identifier.
     */
    @Override
    public void deleteLayout(LayoutIdentifier layout) {
        saveLayout(layout, null);
    }
    
    /**
     * Returns the layout content.
     * 
     * @param layout The layout identifier.
     * @return The layout content.
     */
    @Override
    public String getLayoutContent(LayoutIdentifier layout) {
        return propertyService.getValue(getPropertyName(layout.shared), layout.name);
    }
    
    /**
     * Load the layout associated with the specified application id.
     * 
     * @param appId An application id.
     * @return The layout content.
     */
    @Override
    public String getLayoutContentByAppId(String appId) {
        String value = propertyService.getValue(LayoutConstants.PROPERTY_LAYOUT_ASSOCIATION, appId);
        return value == null ? null : getLayoutContent(new LayoutIdentifier(value, true));
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
     * @return Name of property for storing layouts.
     */
    private String getPropertyName(boolean shared) {
        return shared ? LayoutConstants.PROPERTY_LAYOUT_SHARED : LayoutConstants.PROPERTY_LAYOUT_PRIVATE;
    }
    
}
