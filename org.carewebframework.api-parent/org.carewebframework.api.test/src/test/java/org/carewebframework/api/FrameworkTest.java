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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import org.carewebframework.api.spring.SpringUtil;
import org.carewebframework.api.test.CommonTest;

import org.junit.Test;

public class FrameworkTest extends CommonTest {
    
    private class TestBean {};
    
    @Test
    public void testObjectRegistration() {
        // Testing simple object registration/unregistration
        TestBean testBean = new TestBean();
        assertNull(appFramework.findObject(TestBean.class, null));
        appFramework.registerObject(testBean);
        assertSame(appFramework.findObject(TestBean.class, null), testBean);
        appFramework.unregisterObject(testBean);
        assertNull(appFramework.findObject(TestBean.class, null));
        // Testing weak reference implementation of object registration
        appFramework.registerObject(testBean);
        assertSame(appFramework.findObject(TestBean.class, null), testBean);
        testBean = null;
        System.gc();
        assertNull(appFramework.findObject(TestBean.class, null));
    }
    
    @Test
    public void testPropertyFetch() {
        assertEquals("keypass", SpringUtil.getProperty("org.carewebframework.keystore.private"));
        assertNotNull(SpringUtil.getProperty("path"));
    }
}
