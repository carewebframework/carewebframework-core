/*
 * #%L
 * carewebframework
 * %%
 * Copyright (C) 2008 - 2016 Regenstrief Institute, Inc.
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related
 * Additional Disclaimer of Warranty and Limitation of Liability available at
 *
 *      http://www.carewebframework.org/licensing/disclaimer.
 *
 * #L%
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
     * @param layout The layout identifier.
     * @return True if layout exists.
     */
    boolean layoutExists(LayoutIdentifier layout);
    
    /**
     * Saves a layout with the specified name and content.
     * 
     * @param layout The layout identifier.
     * @param content The layout content.
     */
    void saveLayout(LayoutIdentifier layout, String content);
    
    /**
     * Rename a layout.
     * 
     * @param layout The original layout identifier.
     * @param newName The new layout name.
     */
    void renameLayout(LayoutIdentifier layout, String newName);
    
    /**
     * Clone a layout.
     * 
     * @param layout The original layout identifier.
     * @param layout2 The new layout identifier.
     */
    void cloneLayout(LayoutIdentifier layout, LayoutIdentifier layout2);
    
    /**
     * Delete a layout.
     * 
     * @param layout The layout identifier.
     */
    void deleteLayout(LayoutIdentifier layout);
    
    /**
     * Returns the layout content.
     * 
     * @param layout The layout identifier.
     * @return The layout content.
     */
    String getLayoutContent(LayoutIdentifier layout);
    
    /**
     * Returns content of the layout associated with the specified application id.
     * 
     * @param appId An application id.
     * @return The layout content.
     */
    String getLayoutContentByAppId(String appId);
    
    /**
     * Returns a list of saved layouts.
     * 
     * @param shared If true, return shared layouts; otherwise, return personal layouts.
     * @return List of saved layouts.
     */
    List<String> getLayouts(boolean shared);
    
}
