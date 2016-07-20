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
package org.carewebframework.ui.test;

import java.util.LinkedList;
import java.util.Queue;

import org.zkoss.zk.ui.Desktop;
import org.zkoss.zk.ui.DesktopUnavailableException;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.sys.Scheduler;
import org.zkoss.zk.ui.sys.ServerPush;

/**
 * Mock server push.
 */
public class MockServerPush implements ServerPush {
    
    private static class ScheduledEvent<T extends Event> {
        
        final EventListener<T> task;
        
        final T event;
        
        public ScheduledEvent(EventListener<T> task, T event) {
            this.task = task;
            this.event = event;
        }
        
        public void fire() throws Exception {
            task.onEvent(event);
        }
    }
    
    private final Queue<ScheduledEvent<?>> queue = new LinkedList<>();
    
    @Override
    public boolean isActive() {
        return true;
    }
    
    @Override
    public void start(Desktop desktop) {
    }
    
    @Override
    public void stop() {
    }
    
    @Override
    public <T extends Event> void schedule(EventListener<T> task, T event, Scheduler<T> scheduler) {
        queue.add(new ScheduledEvent<T>(task, event));
    }
    
    @Override
    public boolean activate(long timeout) throws InterruptedException, DesktopUnavailableException {
        return true;
    }
    
    @Override
    public boolean deactivate(boolean stop) {
        return true;
    }
    
    @Override
    public void onPiggyback() {
    }
    
    /**
     * Flushes the queue of all scheduled server push events.
     * 
     * @return True if any events were flushed.
     */
    public boolean flush() {
        ScheduledEvent<?> scheduledEvent;
        boolean result = false;
        
        while ((scheduledEvent = queue.poll()) != null) {
            try {
                result = true;
                scheduledEvent.fire();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        return result;
    }
    
    @Override
    public void resume() {
    }
}
