/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.maven.plugin.theme;

import org.apache.commons.lang.StringUtils;

/**
 *
 */
public class Theme {
    
    private String themeName;
    
    private String baseColor;
    
    private String themeUri;
    
    /**
     * @return The theme name.
     */
    public String getThemeName() {
        return themeName;
    }
    
    /**
     * @return The base color (for ZK themes).
     */
    public String getBaseColor() {
        return baseColor;
    }
    
    /**
     * @return The source uri (for Bootstrap themes).
     */
    public String getThemeUri() {
        return themeUri;
    }
    
    /**
     * @return Display friendly representation.
     */
    @Override
    public String toString() {
        String type = baseColor != null ? baseColor : themeUri;
        return StringUtils.trimToEmpty(this.themeName) + ":" + StringUtils.trimToEmpty(type);
    }
    
}
