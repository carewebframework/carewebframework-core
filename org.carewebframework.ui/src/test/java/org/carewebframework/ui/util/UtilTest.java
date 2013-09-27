/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.ui.util;

import static org.junit.Assert.assertEquals;

import java.util.Map;

import org.carewebframework.ui.FrameworkWebSupport;

import org.junit.Test;

public class UtilTest {
    
    @Test
    public void queryStringToMapTest() {
        Map<String, String> map = FrameworkWebSupport.queryStringToMap("name1=value1&name2=value2&name3=value3");
        
        assert (map.size() == 3);
        
        for (int i = 1; i <= 3; i++) {
            assertEquals(map.get("name" + i), "value" + i);
        }
        
        map = FrameworkWebSupport.queryStringToMap(
            "?name1=value1&name2=value2&name3=value3&name1=value4&name2=value5&name3=value6", "; ");
        
        assert (map.size() == 3);
        
        for (int i = 1; i <= 3; i++) {
            assertEquals(map.get("name" + i), "value" + i + "; value" + (i + 3));
        }
        
        map = FrameworkWebSupport.queryStringToMap("name=this+is+a+test%26");
        
        assert (map.size() == 1);
        assertEquals(map.get("name"), "this is a test&");
    }
    
}
