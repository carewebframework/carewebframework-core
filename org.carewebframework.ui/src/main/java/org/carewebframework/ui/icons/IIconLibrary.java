/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.ui.icons;

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
