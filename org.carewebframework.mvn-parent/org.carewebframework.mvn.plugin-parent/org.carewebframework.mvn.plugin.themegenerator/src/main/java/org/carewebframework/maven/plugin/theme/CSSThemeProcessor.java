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
        addConfigEntry("css", resource.getSourcePath());
        transform(resource);
    }
    
}
