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

import java.util.ArrayList;
import java.util.List;

import org.carewebframework.api.spring.SpringUtil;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.MoveEvent;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Window;

/**
 * Manages popups created by framework.
 */
public class PopupManager {
    
    private static final String ATTR_OFFSET = "_popupoffset";
    
    /**
     * Move listener catches modal dialog immediately after becoming visible to allow adjustment of
     * position.
     */
    private class PopupMoveEventListener extends MoveEventListener {
        
        private boolean first = true;
        
        @Override
        public void onEvent(MoveEvent event) throws Exception {
            if (first) {
                first = false;
                addOffset((Window) event.getTarget());
            } else {
                super.onEvent(event);
            }
        }
        
    }
    
    /**
     * Close event removes window from manager.
     */
    private class PopupCloseEventListener implements EventListener<Event> {
        
        @Override
        public void onEvent(Event event) throws Exception {
            unregisterPopup((Window) event.getTarget());
        }
    }
    
    private final List<Window> active = new ArrayList<>();
    
    /*
     * Set the offset size to 22 pixels so that the text of the title bars of nested
     * popups can be read on all popups in the stack.
     */
    private final int increment = 22;
    
    private boolean disableScript;
    
    /**
     * Returns an instance of the popup manager.
     * 
     * @return PopupManager
     */
    public static PopupManager getInstance() {
        return SpringUtil.getBean("popupManager", PopupManager.class);
    }
    
    /**
     * No-args constructor
     */
    public PopupManager() {
        super();
    }
    
    /**
     * Register a window with the popup manager.
     * 
     * @param window Window to register
     */
    public void registerPopup(Window window) {
        window.addEventListener(Events.ON_MOVE, new PopupMoveEventListener());
        window.addEventListener(Events.ON_CLOSE, new PopupCloseEventListener());
    }
    
    /**
     * Unregister a window from the event manager.
     * 
     * @param window Window to unregister/remove
     */
    public void unregisterPopup(Window window) {
        active.remove(window);
    }
    
    /**
     * Destroy method can be called by IOC container.
     */
    public void destroy() {
        active.clear();
    }
    
    /**
     * Add the next offset to the window's top and left positions.
     * 
     * @param window The window.
     */
    private void addOffset(Window window) {
        if (!disableScript && (!window.inEmbedded())) {
            int offset = getNextOffset();
            active.add(window);
            window.setAttribute(ATTR_OFFSET, offset);
            
            if (offset != 0) {
                try {
                    Clients.evalJavaScript("cwf.offset_window('" + window.getUuid() + "'," + (offset * increment) + ");");
                } catch (Throwable e) {
                    disableScript = true;
                }
            }
        }
    }
    
    /**
     * Returns the next offset to be used.
     * 
     * @return The next offset.
     */
    private int getNextOffset() {
        int offset = -1;
        
        for (int i = active.size() - 1; i >= 0; i--) {
            Window window = active.get(i);
            Integer thisOffset = (Integer) window.getAttribute(ATTR_OFFSET);
            
            if (window.getPage() == null || thisOffset == null) {
                active.remove(i);
            } else if (window.isVisible()) {
                offset = thisOffset;
                break;
            }
        }
        
        return offset > 9 ? 0 : offset + 1;
    }
}
