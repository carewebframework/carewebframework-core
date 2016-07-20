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
package org.carewebframework.shell.designer;

import org.carewebframework.shell.layout.UIElementZKBase;
import org.carewebframework.ui.zk.ZKUtil;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Menupopup;

/**
 * Implements the mask that covers components when design mode is active.
 */
public class DesignMask implements EventListener<Event> {
    
    public enum MaskMode {
        AUTO, ENABLE, DISABLE
    };
    
    private final UIElementZKBase element;
    
    private MaskMode mode = MaskMode.AUTO;
    
    private Event maskEvent;
    
    private boolean visible;
    
    public DesignMask(UIElementZKBase element) {
        this.element = element;
    }
    
    public MaskMode getMode() {
        return mode;
    }
    
    public void setMode(MaskMode mode) {
        mode = mode == null ? MaskMode.DISABLE : mode;
        
        if (this.mode != mode) {
            this.mode = mode;
            update();
        }
    }
    
    private boolean shouldShow() {
        if (!element.isDesignMode()) {
            return false;
        }
        
        if (mode == MaskMode.AUTO) {
            return element.getChildCount() == 0;
        }
        
        return mode == MaskMode.ENABLE;
    }
    
    /**
     * Show or hide the design mode mask.
     */
    public void update() {
        boolean shouldShow = shouldShow();
        
        if (visible != shouldShow) {
            visible = shouldShow;
            Component target = element.getMaskTarget();
            
            if (visible) {
                target.addEventListener("onMask", this);
                maskEvent = new Event("onMask", target);
            } else {
                maskEvent = null;
                target.removeEventListener("onMask", this);
                ZKUtil.removeMask(target);
            }
        }
        
        if (maskEvent != null && element.isActivated()) {
            Events.postEvent(-9999, maskEvent);
        }
    }
    
    @Override
    public void onEvent(Event event) throws Exception {
        Component target = element.getMaskTarget();
        Menupopup contextMenu = UIElementZKBase.getDesignContextMenu(target);
        String displayName = element.getDisplayName();
        ZKUtil.addMask(target, displayName, contextMenu, displayName);
    }
}
