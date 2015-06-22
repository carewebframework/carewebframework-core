/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.ui.zk;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.carewebframework.ui.test.CommonTest;

import org.zkoss.zk.ui.Component;

import org.junit.Test;

/**
 * Unit tests for ZK functions that require mock framework.
 */
public class ZKTest extends CommonTest {
    
    private static String RESOURCE_PATH = "~./org/carewebframework/ui/test/";
    
    @Test
    public void focusFirstTest() {
        Component root = ZKUtil.loadZulPage(RESOURCE_PATH + "testFocusFirst.zul", null);
        focusFirstTest(root, "test1", "test1_correct");
        focusFirstTest(root, "test2", "test2_correct");
        focusFirstTest(root, "test3", null);
    }
    
    private void focusFirstTest(Component root, String testId, String expectedId) {
        Component test = root.getFellow(testId);
        assertNotNull(test);
        Component focus = ZKUtil.focusFirst(test, true);
        
        if (expectedId == null) {
            assertNull(focus);
        } else {
            assertNotNull(focus);
            assertEquals(expectedId, focus.getId());
        }
    }
}
