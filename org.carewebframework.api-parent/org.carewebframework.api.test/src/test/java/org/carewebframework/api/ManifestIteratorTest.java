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
package org.carewebframework.api;

import static org.junit.Assert.assertNotNull;

import java.util.Map.Entry;
import java.util.jar.Attributes;
import java.util.jar.Attributes.Name;
import java.util.jar.Manifest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.carewebframework.api.ManifestIterator;
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
