/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.ui.test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Mock page configuration for unit testing.
 */
public class MockPageConfig implements org.zkoss.zk.ui.sys.PageConfig {
    
    private String id;
    
    private String uuid;
    
    private String title;
    
    private String style;
    
    private String viewport;
    
    private String beforeHeadTags;
    
    private String afterHeadTags;
    
    private final List<Object[]> responseHeaders = new ArrayList<>();
    
    @Override
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    @Override
    public String getUuid() {
        return uuid;
    }
    
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
    
    @Override
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    @Override
    public String getStyle() {
        return style;
    }
    
    public void setStyle(String style) {
        this.style = style;
    }
    
    @Override
    public String getViewport() {
        return viewport;
    }
    
    public void setViewport(String viewport) {
        this.viewport = viewport;
    }
    
    @Override
    public String getBeforeHeadTags() {
        return beforeHeadTags;
    }
    
    public void setBeforeHeadTags(String beforeHeadTags) {
        this.beforeHeadTags = beforeHeadTags;
    }
    
    @Override
    public String getAfterHeadTags() {
        return afterHeadTags;
    }
    
    public void getAfterHeadTags(String afterHeadTags) {
        this.afterHeadTags = afterHeadTags;
    }
    
    @Override
    public Collection<Object[]> getResponseHeaders() {
        return responseHeaders;
    }
    
    public void addResponseHeader(Object[] responseHeader) {
        responseHeaders.add(responseHeader);
    }
}
