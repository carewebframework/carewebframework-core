/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.logging.log4j;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.carewebframework.common.StopWatchFactory;
import org.carewebframework.common.StopWatchFactory.IStopWatch;
import org.carewebframework.logging.perf4j.StopWatch;

import org.junit.Test;

public class LoggingTest {
    
    /**
     * Testing StatusMessageLayout with value of:
     * <p>
     * tag:%P{tag}, time:%P{time}, start:%P{start}, task:%P{message.task}, other:%P{message.other}
     */
    @Test
    public void testLayout() {
        StopWatchFactory.createFactory(StopWatch.class);
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("task", "Task name");
        map.put("other", "Other value");
        IStopWatch sw = StopWatchFactory.create("test", map);
        sw.start();
        sw.stop();
        
        List<String> logMessages = TestAppender.getMessages();
        assertFalse(logMessages.isEmpty());
        assertTrue(logMessages.get(0).matches("^tag:test, time:\\d*, start:\\d*, task:Task name, other:Other value\\W*$"));
    }
    
}
