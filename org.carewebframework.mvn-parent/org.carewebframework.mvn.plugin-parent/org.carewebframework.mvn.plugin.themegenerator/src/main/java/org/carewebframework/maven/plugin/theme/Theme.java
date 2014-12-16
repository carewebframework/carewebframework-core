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
    
    private String cssMapper;
    
    private String themeVersion;
    
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
     * @return The version of this theme.
     */
    public String getThemeVersion() {
        return themeVersion;
    }
    
    /**
     * Sets the theme version.
     * 
     * @param themeVersion The theme version.
     */
    protected void setThemeVersion(String themeVersion) {
        this.themeVersion = themeVersion;
    }
    
    /**
     * @return The style mapper control file.
     */
    public String getCSSMapper() {
        return cssMapper;
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
