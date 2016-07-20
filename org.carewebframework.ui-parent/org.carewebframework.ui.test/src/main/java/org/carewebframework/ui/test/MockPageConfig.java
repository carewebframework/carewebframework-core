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
