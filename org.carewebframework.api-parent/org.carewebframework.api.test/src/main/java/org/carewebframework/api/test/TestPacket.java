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
package org.carewebframework.api.test;

import java.io.Serializable;

public class TestPacket implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private String eventName;
    
    private int eventId;
    
    private boolean shouldReceive;
    
    private boolean remote;
    
    public TestPacket() {
        
    }
    
    public TestPacket(int eventId, String eventName, boolean shouldReceive, boolean remote) {
        this.eventName = eventName;
        this.eventId = eventId;
        this.shouldReceive = shouldReceive;
        this.remote = remote;
    }
    
    public String getEventName() {
        return eventName;
    }
    
    public void setEventName(String eventName) {
        this.eventName = eventName;
    }
    
    public int getEventId() {
        return eventId;
    }
    
    public void setEventId(int eventId) {
        this.eventId = eventId;
    }
    
    public boolean isShouldReceive() {
        return shouldReceive;
    }
    
    public void setShouldReceive(boolean shouldReceive) {
        this.shouldReceive = shouldReceive;
    }
    
    public boolean isRemote() {
        return remote;
    }
    
    public void setRemote(boolean remote) {
        this.remote = remote;
    }
    
    @Override
    public String toString() {
        return "Event id = " + eventId + ", name = " + eventName + ", should receive = " + shouldReceive + ", mode = "
                + (remote ? "remote" : "local");
    }
    
    @Override
    public boolean equals(Object object) {
        return object instanceof TestPacket && ((TestPacket) object).eventId == eventId;
    }
}
