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

import java.io.File;

import org.apache.maven.project.MavenProject;

import org.carewebframework.maven.plugin.resource.IResource;
import org.carewebframework.maven.plugin.transform.AbstractTransform;
import org.carewebframework.maven.plugin.transform.CopyTransform;

import org.codehaus.plexus.util.FileUtils;

/**
 * Generates a new theme directly from a CSS file.
 */
public class CSSThemeProcessor extends AbstractThemeProcessor {
    
    /**
     * @param theme The theme.
     * @param mojo The theme generator mojo.
     * @throws Exception if error occurs initializing generator
     */
    public CSSThemeProcessor(Theme theme, ThemeGeneratorMojo mojo) throws Exception {
        super(theme, mojo);
        String mapper = theme.getCSSMapper();
        AbstractTransform transform = mapper == null ? new CopyTransform(mojo) : new CSSTransform(mojo);
        registerTransform("*.css", transform);
    }
    
    @Override
    public String relocateResource(String resourceName) {
        return "web/" + getResourceBase() + getTheme().getThemeName() + "-" + FileUtils.filename(resourceName);
    }
    
    @Override
    public String getResourceBase() {
        return getThemeBase() + "css/";
    }
    
    @Override
    public void transform() throws Exception {
        Theme theme = getTheme();
        MavenProject mavenProject = mojo.getMavenProject();
        String mapper = theme.getCSSMapper();
        File file = new File(mavenProject.getBasedir(), theme.getThemeUri());
        File map = mapper == null ? null : new File(mavenProject.getBasedir(), mapper);
        IResource resource = new CSSResource(file, map);
        addConfigEntry("css", resource.getRelativePath());
        transform(resource);
    }
    
}
