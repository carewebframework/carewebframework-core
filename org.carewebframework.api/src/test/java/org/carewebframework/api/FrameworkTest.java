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

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

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
}
