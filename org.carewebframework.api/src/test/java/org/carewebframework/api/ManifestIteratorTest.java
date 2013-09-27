/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.api;

import static org.junit.Assert.assertNotNull;

import java.util.Map.Entry;
import java.util.jar.Attributes;
import java.util.jar.Attributes.Name;
import java.util.jar.Manifest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.carewebframework.api.test.CommonTest;

import org.junit.Test;

public class ManifestIteratorTest extends CommonTest {
    
    private static final Log log = LogFactory.getLog(ManifestIteratorTest.class);
    
    @Test
    public void testIterator() {
        ManifestIterator manifests = (ManifestIterator) appContext.getBean("manifestIterator");
        
        for (Manifest manifest : manifests) {
            Attributes attributes = manifest.getMainAttributes();
            
            for (Entry<?, ?> entry : attributes.entrySet()) {
                Name name = (Name) entry.getKey();
                log.info(name + ": " + entry.getValue().toString());
            }
        }
    }
    
    @Test
    public void testFindByPath() {
        ManifestIterator manifests = (ManifestIterator) appContext.getBean("manifestIterator");
        String path = ManifestIterator.class.getClassLoader().getResource(ManifestIterator.MANIFEST_PATH).getPath();
        assertNotNull(path);
        assertNotNull(manifests.findByPath(path));
    }
}
