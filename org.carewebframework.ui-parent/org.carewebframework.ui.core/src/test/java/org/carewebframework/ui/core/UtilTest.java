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
package org.carewebframework.ui.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.carewebframework.ui.util.CWFUtil;
import org.carewebframework.web.component.BaseComponent;
import org.carewebframework.web.component.Div;
import org.carewebframework.web.component.Html;
import org.carewebframework.web.component.Hyperlink;
import org.carewebframework.web.component.Label;
import org.junit.Test;

public class UtilTest {
    
    private static final String ATTR_TEST = "ATTR_TEST";
    
    private static final String ATTR_DUMMY = "ATTR_DUMMY";
    
    private static final String ATTR_NULL = "ATTR_NULL";
    
    @Test
    public void attributeTests() {
        Div cmpt = new Div();
        cmpt.setAttribute(ATTR_DUMMY, new Object());
        cmpt.setAttribute(ATTR_TEST, 1234);
        assertTrue(1234 == cmpt.getAttribute(ATTR_TEST, 0));
        assertTrue(4321 == cmpt.getAttribute(ATTR_DUMMY, 4321));
        assertTrue(5678 == cmpt.getAttribute(ATTR_NULL, 5678));
        cmpt.setAttribute(ATTR_TEST, true);
        assertTrue(cmpt.getAttribute(ATTR_TEST, Boolean.class));
        cmpt.setAttribute(ATTR_TEST, "TRUE");
        assertTrue(cmpt.getAttribute(ATTR_TEST, Boolean.class));
        cmpt.setAttribute(ATTR_TEST, "ANYTHING BUT TRUE");
        assertFalse(cmpt.getAttribute(ATTR_TEST, Boolean.class));
        assertFalse(cmpt.getAttribute(ATTR_DUMMY, Boolean.class));
        assertTrue(cmpt.getAttribute(ATTR_NULL, Boolean.class));
        cmpt.setAttribute(ATTR_TEST, ATTR_TEST);
        assertEquals(ATTR_TEST, cmpt.getAttribute(ATTR_TEST, String.class));
        assertTrue("".equals(cmpt.getAttribute(ATTR_DUMMY, String.class)));
        assertTrue("".equals(cmpt.getAttribute(ATTR_NULL, String.class)));
        List<Boolean> list = new ArrayList<>();
        cmpt.setAttribute(ATTR_TEST, list);
        assertSame(list, cmpt.getAttribute(ATTR_TEST, List.class));
        assertNull(cmpt.getAttribute(ATTR_DUMMY));
        assertNull(cmpt.getAttribute(ATTR_NULL));
        cmpt.setAttribute(ATTR_TEST, cmpt);
        assertSame(cmpt, cmpt.getAttribute(ATTR_TEST, Div.class));
        assertNull(cmpt.getAttribute(ATTR_DUMMY, Div.class));
    }
    
    public interface ArgumentMapTest {
        
        void doAssertions();
        
        void setTest2Variable(String value);
    }
    
    @Test
    public void wireArgumentMapTest() {
        Map<Object, Object> map = new HashMap<>();
        map.put("test1Variable", 123);
        map.put("test2Variable", "testing");
        ArgumentMapTest controller = new ArgumentMapTest() {
            
            public int test1Variable;
            
            public String test2;
            
            @Override
            public void setTest2Variable(String value) {
                test2 = value;
            }
            
            @Override
            public void doAssertions() {
                assertEquals(123, test1Variable);
                assertEquals("testing", test2);
            }
        };
        
        CWFUtil.wireController(map, controller);
        controller.doAssertions();
    }
    
    @Test
    public void getTextComponentTest() {
        BaseComponent cmp;
        cmp = CWFUtil.getTextComponent("general text");
        assertTrue(cmp instanceof Label);
        cmp = CWFUtil.getTextComponent("<html>html text</html>");
        assertTrue(cmp instanceof Html);
        cmp = CWFUtil.getTextComponent("https://url");
        assertTrue(cmp instanceof Hyperlink);
        cmp = CWFUtil.getTextComponent("http://url");
        assertTrue(cmp instanceof Hyperlink);
    }
    
    @Test
    public void getResourcePathTest() {
        assertEquals("web/org/carewebframework/ui/core/", CWFUtil.getResourcePath(UtilTest.class));
        assertEquals("web/org/carewebframework/ui/", CWFUtil.getResourcePath(UtilTest.class, 1));
        assertEquals("web/org/carewebframework/ui/core/", CWFUtil.getResourcePath(UtilTest.class.getPackage()));
        assertEquals("web/org/carewebframework/", CWFUtil.getResourcePath(UtilTest.class.getPackage(), 2));
        assertEquals("web/org/carewebframework/ui/core/", CWFUtil.getResourcePath("org.carewebframework.ui.core"));
        assertEquals("web/org/carewebframework/ui/core/", CWFUtil.getResourcePath("org.carewebframework.ui.core", -2));
        assertEquals("web/org/carewebframework/ui/", CWFUtil.getResourcePath("org.carewebframework.ui.core", 1));
    }
}
