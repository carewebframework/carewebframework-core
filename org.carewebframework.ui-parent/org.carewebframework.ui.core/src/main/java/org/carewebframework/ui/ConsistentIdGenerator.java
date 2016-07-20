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
package org.carewebframework.ui;

import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.http.HttpServletResponse;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Desktop;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.metainfo.ComponentInfo;
import org.zkoss.zk.ui.sys.IdGenerator;

/**
 * Generates consistent desktop and component id's for purposes of automated testing.
 * <p>
 * To enable this, add the following lines to the zk.xml configuration file:
 * 
 * <pre>
 * {@literal
 *    <system-config>
 *         <id-generator-class>org.carewebframework.ui.ConsistentIdGenerator</id-generator-class>
 *  </system-config>
 * }
 * </pre>
 */
public class ConsistentIdGenerator implements IdGenerator {
    
    private static final String ATTRIBUTE_GENERATED_ID = "generatedId";
    
    private static final String HEADER_DESKTOP = "Desktop";
    
    private static final String ZK_DESKTOP_PREFIX = "zk_dt_";
    
    private static final String ZK_PAGE_PREFIX = "zk_page_";
    
    private static final String ZK_COMPONENT_PREFIX = "zk_comp_";
    
    private static AtomicInteger desktopCounter = new AtomicInteger();
    
    private static AtomicInteger pageCounter = new AtomicInteger();
    
    @Override
    public String nextComponentUuid(Desktop desktop, Component comp, ComponentInfo info) {
        if (!desktop.hasAttribute(ATTRIBUTE_GENERATED_ID)) {
            desktop.setAttribute(ATTRIBUTE_GENERATED_ID, new AtomicInteger());
        }
        
        AtomicInteger componentCounter = (AtomicInteger) desktop.getAttribute(ATTRIBUTE_GENERATED_ID);
        return formatId(ZK_COMPONENT_PREFIX, componentCounter);
    }
    
    @Override
    public String nextPageUuid(Page page) {
        return formatId(ZK_PAGE_PREFIX, pageCounter);
    }
    
    @Override
    public String nextDesktopId(Desktop desktop) {
        String dtid = formatId(ZK_DESKTOP_PREFIX, desktopCounter);
        HttpServletResponse response = (HttpServletResponse) desktop.getExecution().getNativeResponse();
        response.addHeader(HEADER_DESKTOP, dtid);
        return dtid;
    }
    
    private String formatId(String prefix, AtomicInteger counter) {
        return prefix + Integer.toHexString(counter.getAndIncrement());
    }
}
