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
        Map<String, Object> map = new HashMap<>();
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
