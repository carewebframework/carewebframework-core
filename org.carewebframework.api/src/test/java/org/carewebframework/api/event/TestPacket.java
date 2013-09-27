/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.api.event;

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
