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
package org.carewebframework.logging.perf4j;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.carewebframework.web.client.ExecutionContext;
import org.carewebframework.web.component.BaseComponent;
import org.carewebframework.web.component.Page;
import org.carewebframework.web.event.Event;

/**
 * Performance event interceptor for timing events. The monitor copies event timing data into the
 * EventLog of the current PerformanceData object associated with the page making the request. The
 * monitor must be registered as a listener in the zk.xml configuration file as follows:
 * 
 * <pre>
 * {@literal
 * <listener>
 *      <description>Performance monitor</description>
 *      <listener-class>org.carewebframework.logging.perf4j.PerformanceEventInterceptor</listener-class>
 * </listener>}
 * </pre>
 */
public class PerformanceEventInterceptor implements EventInterceptor {
    
    private static final Log log = LogFactory.getLog(PerformanceEventInterceptor.class);
    
    /**
     * Constructs a PerformanceEventInterceptor.
     */
    public PerformanceEventInterceptor() {
        log.debug("PerformanceEventInterceptor listener instantiated");
    }
    
    /**
     * @see org.zkoss.zk.ui.util.EventInterceptor#beforeSendEvent(org.zkoss.zk.ui.event.Event)
     */
    @Override
    public Event beforeSendEvent(Event event) {
        return event;
    }
    
    /**
     * @see org.zkoss.zk.ui.util.EventInterceptor#beforePostEvent(org.zkoss.zk.ui.event.Event)
     */
    @Override
    public Event beforePostEvent(Event event) {
        return event;
    }
    
    /**
     * @see org.zkoss.zk.ui.util.EventInterceptor#beforeProcessEvent(org.zkoss.zk.ui.event.Event)
     */
    @Override
    public Event beforeProcessEvent(Event event) {
        EventLog eventLog = getEventLog(event);
        
        if (eventLog != null) {
            EventInfo ei = new EventInfo(event, eventLog.getTag(), eventLog.getPageId(), eventLog.getRequestId());
            eventLog.put(event, ei);
        }
        
        return event;
    }
    
    /**
     * @see org.zkoss.zk.ui.util.EventInterceptor#afterProcessEvent(org.zkoss.zk.ui.event.Event)
     */
    @Override
    public void afterProcessEvent(Event event) {
        EventLog eventLog = getEventLog(event);
        
        if (eventLog != null) {
            EventInfo ei = eventLog.get(event);
            if (ei != null) {
                ei.end();
            }
        }
    }
    
    private EventLog getEventLog(Event event) {
        BaseComponent target = event.getTarget();
        if (target != null) {
            Page page = target.getPage();
            if (page == null) {
                page = ExecutionContext.getPage();
            }
            if (page != null) {
                PerformanceData pd = (PerformanceData) page.getAttribute(PerformanceData.ATTR_PERF_DATA);
                if (pd != null) {
                    return pd.getEventLog(event);
                }
            }
        }
        
        return null;
    }
}
