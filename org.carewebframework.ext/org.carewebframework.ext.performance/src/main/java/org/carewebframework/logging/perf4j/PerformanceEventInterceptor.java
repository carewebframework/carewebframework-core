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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Desktop;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.util.EventInterceptor;

/**
 * Performance event interceptor for timing events. The monitor copies event timing data into the
 * EventLog of the current PerformanceData object associated with the desktop making the request.
 * The monitor must be registered as a listener in the zk.xml configuration file as follows:
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
            EventInfo ei = new EventInfo(event, eventLog.getTag(), eventLog.getDesktopId(), eventLog.getRequestId());
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
        Component target = event.getTarget();
        if (target != null) {
            Desktop desktop = target.getDesktop();
            if (desktop == null) {
                desktop = Executions.getCurrent().getDesktop();
            }
            if (desktop != null) {
                PerformanceData pd = (PerformanceData) desktop.getAttribute(PerformanceData.ATTR_PERF_DATA);
                if (pd != null) {
                    return pd.getEventLog(event);
                }
            }
        }
        
        return null;
    }
}
