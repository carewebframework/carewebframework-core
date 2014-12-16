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

import org.apache.commons.lang.ArrayUtils;

import org.carewebframework.maven.plugin.processor.AbstractProcessor;

/**
 * Generates a new theme from a base theme by using specialized processors to transform individual
 * theme elements.
 */
public abstract class AbstractThemeProcessor extends AbstractProcessor<ThemeGeneratorMojo> {
    
    private static final String THEME_NAME_REGEX = "^[\\w\\-]+$";
    
    private final Theme theme;
    
    /**
     * @param theme The theme.
     * @param mojo The theme generator mojo.
     * @throws Exception if error occurs initializing generator
     */
    public AbstractThemeProcessor(Theme theme, ThemeGeneratorMojo mojo) throws Exception {
        super(mojo);
        this.theme = theme;
        
        if (theme.getThemeName() == null || !theme.getThemeName().matches(THEME_NAME_REGEX)) {
            throw new Exception("Theme names must be alphanumeric with no blanks, conforming to regexp: " + THEME_NAME_REGEX);
        }
    }
    
    protected void addConfigEntry(String insertionTag, String... params) {
        params = (String[]) ArrayUtils.addAll(
            new String[] { theme.getThemeName(), theme.getThemeVersion(), getThemeBase() }, params);
        mojo.addConfigEntry(insertionTag, params);
    }
    
    protected String getThemeBase() {
        return mojo.getThemeBase();
    }
    
    /**
     * @return The theme.
     */
    public Theme getTheme() {
        return theme;
    }
    
}
