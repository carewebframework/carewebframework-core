/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.ui.mockup;

import java.io.IOException;

import org.apache.commons.lang.StringUtils;

import org.carewebframework.shell.plugins.PluginContainer;
import org.carewebframework.shell.plugins.PluginController;

import org.zkoss.zul.Iframe;

/**
 * Simple component to display third-party UI mockups.
 */
public class MainController extends PluginController {
    
    private static final long serialVersionUID = 1L;
    
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
    public void onLoad(PluginContainer container) {
        super.onLoad(container);
        container.registerProperties(this, "mockupType", "mockupId");
    }
    
    /**
     * Refreshes the iframe content.
     */
    @Override
    public void refresh() {
        String url = mockupTypes.getUrl(mockupType);
        
        if (mockupId == null || url == null) {
            iframe.setContent(null);
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
    }
    
}
