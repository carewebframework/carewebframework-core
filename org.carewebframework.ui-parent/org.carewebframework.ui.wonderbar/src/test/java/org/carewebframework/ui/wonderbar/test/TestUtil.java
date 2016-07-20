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
package org.carewebframework.ui.wonderbar.test;

import static org.junit.Assert.assertEquals;

import org.carewebframework.ui.wonderbar.Wonderbar.MatchMode;
import org.carewebframework.ui.wonderbar.WonderbarUtil;

import org.junit.Test;

public class TestUtil {
    
    private static final String TEXT = "Now is the time for all good men to come to the aid of their country.";
    
    private MatchMode mode;
    
    @Test
    public void test() {
        mode = MatchMode.ANY_ORDER;
        testMatch("NOW IS", true);
        testMatch("now is the the cou", true);
        testMatch("is the now time .cou", true);
        testMatch("time the the the the", false);
        testMatch("aid", true);
        testMatch("now", true);
        testMatch("zzz", false);
        testMatch("co come", true);
        mode = MatchMode.SAME_ORDER;
        testMatch("NOW IS", true);
        testMatch("now is the the cou", true);
        testMatch("is the now time .cou", false);
        testMatch("time the the the the", false);
        testMatch("aid", true);
        testMatch("now", true);
        testMatch("zzz", false);
        testMatch("co come", false);
        mode = MatchMode.ADJACENT;
        testMatch("NOW IS", true);
        testMatch("now is the time", true);
        testMatch("for all good men", true);
        testMatch("time for good men", false);
        testMatch("aid", true);
        testMatch("now", true);
        testMatch("zzz", false);
        testMatch("co come", false);
        mode = MatchMode.FROM_START;
        testMatch("NOW IS", true);
        testMatch("now is the time", true);
        testMatch("for all good men", false);
        testMatch("time for good men", false);
        testMatch("aid", false);
        testMatch("now", true);
        testMatch("zzz", false);
        testMatch("co come", false);
    }
    
    private void testMatch(String value, boolean result) {
        assertEquals(WonderbarUtil.matches(value, TEXT, mode), result);
    }
}
