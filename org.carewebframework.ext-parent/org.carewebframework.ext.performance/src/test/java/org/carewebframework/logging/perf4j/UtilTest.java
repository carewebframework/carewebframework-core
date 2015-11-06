/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.logging.perf4j;

import static org.junit.Assert.assertEquals;

import java.util.Map;
import java.util.TreeMap;

import org.junit.Test;

public class UtilTest {
    
    @Test
    public void test() {
        Map<String, Object> map = new TreeMap<>();
        map.put("item1", "item1.value");
        map.put("item2", 2222);
        String s = Util.formatForLogging("tagname", 1234, 5678, map);
        assertEquals(s, "tag[tagname] start[1234] time[5678] message[{item1=item1.value, item2=2222}]");
    }
    
}
