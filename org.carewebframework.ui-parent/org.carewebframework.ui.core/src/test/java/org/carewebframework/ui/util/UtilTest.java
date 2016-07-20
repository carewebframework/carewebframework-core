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
