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
package org.carewebframework.ui.cwf;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.carewebframework.ui.test.CommonTest;
import org.carewebframework.ui.zk.ZKUtil;
import org.carewebframework.web.component.BaseComponent;
import org.carewebframework.web.page.PageUtil;
import org.junit.Test;

/**
 * Unit tests for ZK functions that require mock framework.
 */
public class TestCWF extends CommonTest {
    
    private static String RESOURCE_PATH = "~./org/carewebframework/ui/test/";
    
    @Test
    public void focusFirstTest() {
        BaseComponent root = PageUtil.createPage(RESOURCE_PATH + "testFocusFirst.cwf", null).get(0);
        focusFirstTest(root, "test1", "test1_correct");
        focusFirstTest(root, "test2", "test2_correct");
        focusFirstTest(root, "test3", null);
    }
    
    private void focusFirstTest(BaseComponent root, String testId, String expectedId) {
        BaseComponent test = root.findByName(testId);
        assertNotNull(test);
        BaseComponent focus = ZKUtil.focusFirst(test, true);
        
        if (expectedId == null) {
            assertNull(focus);
        } else {
            assertNotNull(focus);
            assertEquals(expectedId, focus.getId());
        }
    }
}
