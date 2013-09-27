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

import java.util.Map;

import org.carewebframework.common.StopWatchFactory.IStopWatch;

/**
 * Wrapper class for perf4j implementation of StopWatch.
 */
public class StopWatch implements IStopWatch {
    
    private org.perf4j.StopWatch sw;
    
    private Map<String, Object> data;
    
    @Override
    public void init(String tag, Map<String, Object> data) {
        sw = new org.perf4j.commonslog.CommonsLogStopWatch(tag);
        this.data = data;
    }
    
    @Override
    public void start() {
        sw.start();
    }
    
    @Override
    public void stop() {
        sw.setMessage(data == null ? null : data.toString());
        sw.stop();
    }
    
    @Override
    public String toString() {
        sw.setMessage(data == null ? null : data.toString());
        return sw.toString();
    }
}
