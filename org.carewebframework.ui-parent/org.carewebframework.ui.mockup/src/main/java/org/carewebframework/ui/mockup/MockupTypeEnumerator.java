package org.carewebframework.ui.mockup;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Properties;

import org.apache.commons.io.IOUtils;

import org.springframework.core.io.Resource;

/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of
 * the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * This Source Code Form is also subject to the terms of the Health-Related Additional Disclaimer of
 * Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
public class MockupTypeEnumerator implements Iterable<String> {
    
    private final Properties mockupTypes = new Properties();
    
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
    
    public String getUrl(String mockupType) {
        return mockupType == null ? null : mockupTypes.getProperty(mockupType);
    }
    
    @Override
    public Iterator<String> iterator() {
        return mockupTypes.stringPropertyNames().iterator();
    }
    
}
