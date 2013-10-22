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
 * Utility class for Icon support
 */
public class IconUtil {
    
    /**
     * <p>
     * Returns the path to the icon resource.
     * </p>
     * <p>
     * For example: getIconPath("help.png") returns
     * ~./org/carewebframework/ui/icons/16x16/silk/help.png
     * </p>
     * <p>
     * <i>Note: This method signature is exposed as an EL function</i>
     * </p>
     * 
     * @param iconName Name of the icon in question
     * @return Path to icon resource (i.e. ~./org/carewebframework/ui/icons/16x16/silk/help.png)
     */
    public static String getIconPath(final String iconName) {
        return getIconPath(iconName, null, null);
    }
    
    /**
     * Returns the paths to matching icon resources given name, dimensions, and library name, any
     * one of which may contain wildcard characters.
     * 
     * @param iconName Name of the requested icon (e.g., "help.png").
     * @param dimensions Dimensions of the requested icon (e.g., "16x16"). Specify null to use
     *            default.
     * @param library Library name containing the icon (e.g., "silk"). Specify null to use default.
     * @return The icon path.
     */
    public static List<String> getMatching(final String iconName, final String dimensions, final String library) {
        return IconLibraryRegistry.getInstance().getMatching(library, iconName, dimensions);
    }
    
    /**
     * Returns the path to the icon resource given its name, dimensions, and library name.
     * 
     * @param iconName Name of the requested icon (e.g., "help.png").
     * @param dimensions Dimensions of the requested icon (e.g., "16x16"). Specify null to use
     *            default.
     * @param library Library name containing the icon (e.g., "silk"). Specify null to use default.
     * @return The icon path.
     */
    public static String getIconPath(final String iconName, final String dimensions, final String library) {
        IIconLibrary lib = IconLibraryRegistry.getInstance().get(library);
        return lib == null ? null : lib.getIconUrl(iconName, dimensions);
    }
    
    /**
     * Enforce static class.
     */
    private IconUtil() {
    }
}
