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
package org.carewebframework.ui.zk;

import org.carewebframework.web.component.BaseComponent;
import org.carewebframework.web.component.BaseUIComponent;
import org.carewebframework.web.event.Event;
import org.carewebframework.web.event.IEventListener;

/**
 * Used to prevent a modal window from being moved outside the view port.
 */
public class MoveEventListener implements IEventListener {
    
    private static final MoveEventListener moveEventListener = new MoveEventListener();
    
    /**
     * Adds a move event listener to the specified component.
     * 
     * @param comp The component to receive the event listener.
     */
    public static void add(BaseUIComponent comp) {
        comp.registerEventListener("move", moveEventListener);
    }
    
    /**
     * Removes a move event listener from the specified component.
     * 
     * @param comp The component from which to remove the event listener.
     */
    public static void remove(BaseComponent comp) {
        comp.unregisterEventListener("move", moveEventListener);
    }
    
    @Override
    public void onEvent(Event moveEvent) {
        // Prevent movement outside of browser margins where we can't move it back.
        BaseUIComponent target = (BaseUIComponent) moveEvent.getTarget();
        
        if (target.getTop().startsWith("-")) {
            target.setTop("0px");
        }
        
        if (target.getLeft().startsWith("-")) {
            target.setLeft("0px");
        }
    }
}
