/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.ui.spring;

import org.carewebframework.ui.LabelFinder;

import org.springframework.context.ApplicationContext;
import org.springframework.core.env.PropertySource;

import org.zkoss.util.resource.Labels;

/**
 * Allows ZK label names to be resolved within Spring.
 */
public class LabelPropertySource extends PropertySource<Object> {
    
    private static final String LABEL_PREFIX = "labels.";
    
    private ApplicationContext appContext;
    
    public LabelPropertySource(ApplicationContext appContext) {
        super("ZK Labels");
        this.appContext = appContext;
    }
    
    /**
     * Label names must be prefixed with "labels." to be recognized as such.
     */
    @Override
    public String getProperty(String name) {
        initLabels();
        return name.startsWith(LABEL_PREFIX) ? Labels.getLabel(name.substring(LABEL_PREFIX.length())) : null;
    }
    
    private void initLabels() {
        if (appContext != null) {
            new LabelFinder().init(appContext);
            appContext = null;
        }
    }
}
