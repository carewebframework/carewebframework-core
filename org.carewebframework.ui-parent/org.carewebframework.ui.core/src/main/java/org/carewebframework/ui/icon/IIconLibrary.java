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
package org.carewebframework.ui.icon;

import java.util.List;

/**
 * Interface for plug-in icon libraries.
 */
public interface IIconLibrary {
    
    /**
     * Returns the unique identifier for the library.
     * 
     * @return The unique identifier.
     */
    String getId();
    
    /**
     * Returns the url for the requested icon.
     * 
     * @param iconName The icon file name.
     * @param dimensions The desired dimensions (for example, 16x16). May be null.
     * @return The full url referencing the requested icon.
     */
    String getIconUrl(String iconName, String dimensions);
    
    /**
     * Returns a list of URLs for icons that match the criteria. Criteria may include wildcard
     * characters. For example, <code>getMatching("weather*", "*x16")</code>
     * 
     * @param iconName Name of icon to match.
     * @param dimensions Desired dimensions to match.
     * @return List of matching URLs.
     */
    List<String> getMatching(String iconName, String dimensions);
    
    /**
     * Returns an array of dimensions supported by this library.
     * 
     * @return Array of supported dimensions.
     */
    String[] supportedDimensions();
}
