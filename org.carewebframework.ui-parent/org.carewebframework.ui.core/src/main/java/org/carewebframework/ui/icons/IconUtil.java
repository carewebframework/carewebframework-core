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
package org.carewebframework.ui.icons;

import java.util.List;

/**
 * Utility class for Icon support
 */
public class IconUtil {
    
    private static class IconParams {
        
        final String name;
        
        final String dimensions;
        
        final String library;
        
        /**
         * Create icon parameters from name and defaults.
         * 
         * @param name Icon name. May be the name only, or library:dimensions:name.
         * @param defltDimensions Default dimensions.
         * @param defltLibrary Default library.
         */
        IconParams(String name, String defltDimensions, String defltLibrary) {
            String pcs[] = name.split("\\:", 3);
            int len = pcs.length;
            this.name = getElement(pcs, --len, null);
            this.dimensions = getElement(pcs, --len, defltDimensions);
            this.library = getElement(pcs, --len, defltLibrary);
        }
        
        private String getElement(String[] pcs, int index, String deflt) {
            String element = index >= 0 ? pcs[index] : "";
            return element.isEmpty() ? deflt : element;
        }
    }
    
    /**
     * <p>
     * Returns the path to the icon resource.
     * </p>
     * <p>
     * For example: getIconPath("help.png") returns
     * ~./org/carewebframework/ui/icons/silk/16x16/help.png
     * </p>
     * <p>
     * <i>Note: This method signature is exposed as an EL function</i>
     * </p>
     * 
     * @param iconName Name of the icon in question
     * @return Path to icon resource (i.e. ~./org/carewebframework/ui/icons/silk/16x16/help.png)
     */
    public static String getIconPath(String iconName) {
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
    public static List<String> getMatching(String iconName, String dimensions, String library) {
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
    public static String getIconPath(String iconName, String dimensions, String library) {
        IconParams icon = new IconParams(iconName, dimensions, library);
        IIconLibrary lib = IconLibraryRegistry.getInstance().get(icon.library);
        return lib == null ? null : lib.getIconUrl(icon.name, icon.dimensions);
    }
    
    /**
     * Enforce static class.
     */
    private IconUtil() {
    }
}
