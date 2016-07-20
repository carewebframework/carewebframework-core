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
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.IOUtils;

import org.springframework.core.io.Resource;

/**
 * An enumerator for supported UI mockup frameworks. These are loaded from one or more property
 * files named "ui-mockup.properties".
 */
public class MockupTypeEnumerator implements Iterable<String> {
    
    private final Properties mockupTypes = new Properties();
    
    /**
     * Processes all UI mockup property files.
     * 
     * @param resources Array of property file resources.
     * @throws IOException If an error processing a property file.
     */
    public MockupTypeEnumerator(Resource[] resources) throws IOException {
        for (Resource resource : resources) {
            InputStream is = null;
            
            try {
                is = resource.getInputStream();
                mockupTypes.load(is);
            } finally {
                IOUtils.closeQuietly(is);
            }
        }
    }
    
    /**
     * Returns the url given the mockup type.
     * 
     * @param mockupType The mockup type.
     * @return The url pattern, or null if not found.
     */
    public String getUrl(String mockupType) {
        return mockupType == null ? null : mockupTypes.getProperty(mockupType);
    }
    
    /**
     * Returns an alphabetically sorted iterator of recognized mockup framework types.
     */
    @Override
    public Iterator<String> iterator() {
        List<String> list = new ArrayList<>(mockupTypes.stringPropertyNames());
        Collections.sort(list, String.CASE_INSENSITIVE_ORDER);
        return list.iterator();
    }
    
}
