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

import org.zkoss.zk.ui.Component;
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
    
    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
    }
    
    @Override
    public void onLoad(PluginContainer container) {
        super.onLoad(container);
        container.registerProperties(this, "mockupType", "mockupId");
    }
    
    @Override
    public void refresh() {
        String url = mockupTypes.getUrl(mockupType);
        
        if (mockupId == null || url == null) {
            iframe.setContent(null);
            return;
        }
        
        iframe.setSrc(String.format(url, mockupId));
    }
    
    public String getMockupId() {
        return mockupId;
    }
    
    public void setMockupId(String mockupId) {
        this.mockupId = StringUtils.trimToNull(mockupId);
        refresh();
    }
    
    public String getMockupType() {
        return mockupType;
    }
    
    public void setMockupType(String mockupType) {
        this.mockupType = mockupType;
    }
    
}
