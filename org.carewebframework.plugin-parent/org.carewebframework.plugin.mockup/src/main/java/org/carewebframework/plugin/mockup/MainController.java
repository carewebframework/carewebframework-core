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
package org.carewebframework.plugin.mockup;

import java.io.IOException;

import org.apache.commons.lang.StringUtils;
import org.carewebframework.shell.elements.UIElementPlugin;
import org.carewebframework.shell.plugins.PluginController;
import org.carewebframework.web.annotation.WiredComponent;
import org.carewebframework.web.component.Iframe;

/**
 * Simple component to display third-party UI mockups.
 */
public class MainController extends PluginController {
    
    @WiredComponent
    private Iframe iframe;
    
    private String mockupId;
    
    private String mockupType;
    
    private final MockupTypeEnumerator mockupTypes;
    
    public MainController(MockupTypeEnumerator mockupTypes) throws IOException {
        this.mockupTypes = mockupTypes;
    }
    
    /**
     * Register published properties.
     */
    @Override
    public void onLoad(UIElementPlugin plugin) {
        super.onLoad(plugin);
        plugin.registerProperties(this, "mockupType", "mockupId");
    }
    
    /**
     * Refreshes the iframe content.
     */
    @Override
    public void refresh() {
        String url = mockupTypes.getUrl(mockupType);
        
        if (mockupId == null || url == null) {
            iframe.setSrc(null);
            return;
        }
        
        iframe.setSrc(String.format(url, mockupId, System.currentTimeMillis()));
    }
    
    /**
     * Returns the unique identifier for the mockup within the selected framework.
     * 
     * @return The unique mockup identifier.
     */
    public String getMockupId() {
        return mockupId;
    }
    
    /**
     * Sets the unique identifier for the mockup within the selected framework.
     * 
     * @param mockupId The unique mockup identifier.
     */
    public void setMockupId(String mockupId) {
        this.mockupId = StringUtils.trimToNull(mockupId);
        refresh();
    }
    
    /**
     * Returns the mockup framework type.
     * 
     * @return The mockup framework type.
     */
    public String getMockupType() {
        return mockupType;
    }
    
    /**
     * Sets the mockup framework type.
     * 
     * @param mockupType The mockup framework type.
     */
    public void setMockupType(String mockupType) {
        this.mockupType = mockupType;
        refresh();
    }
    
}
