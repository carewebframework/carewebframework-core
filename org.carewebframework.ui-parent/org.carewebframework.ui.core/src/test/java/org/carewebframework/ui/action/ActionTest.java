/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.ui.action;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ActionTest {
    
    @Test
    public void testActionFormats() {
        assertEquals(ActionType.ZSCRIPT, ActionType.getType("zscript: xyz"));
        assertEquals(ActionType.JSCRIPT, ActionType.getType("jscript: alert('hi');"));
        assertEquals(ActionType.JSCRIPT, ActionType.getType("javascript: alert('hi');"));
        assertEquals(ActionType.URL, ActionType.getType("http://www.regenstrief.org"));
        assertEquals(ActionType.URL, ActionType.getType("https://www.regenstrief.org"));
        assertEquals(ActionType.ZUL, ActionType.getType("~./org/regenstrief/test/test.zul"));
        assertEquals(ActionType.UNKNOWN, ActionType.getType("unknown type"));
    }
    
}
