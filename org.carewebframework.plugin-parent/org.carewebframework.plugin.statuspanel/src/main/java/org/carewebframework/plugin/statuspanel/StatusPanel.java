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
package org.carewebframework.plugin.statuspanel;

import org.carewebframework.api.event.EventUtil;
import org.carewebframework.api.event.IGenericEvent;
import org.carewebframework.common.StrUtil;
import org.carewebframework.ui.FrameworkController;
import org.carewebframework.web.component.BaseComponent;
import org.carewebframework.web.component.Label;
import org.carewebframework.web.component.Pane;

/**
 * Controller for status panel plugin.
 */
public class StatusPanel extends FrameworkController implements IGenericEvent<Object> {
    
    /**
     * Creates the default pane.
     */
    @Override
    public void afterInitialized(BaseComponent comp) {
        super.afterInitialized(comp);
        createLabel("default");
        getEventManager().subscribe(EventUtil.STATUS_EVENT, this);
    }
    
    /**
     * Handler for the STATUS event. The second level of the event name identifies the pane where
     * the status information (the event data) is to be displayed. For example, the event
     * STATUS.TIMING would display the status information in the pane whose associated label has an
     * id of "TIMING", creating one dynamically if necessary. If there is no second level event
     * name, the default pane is used.
     */
    @Override
    public void eventCallback(String eventName, Object eventData) {
        String pane = StrUtil.piece(eventName, ".", 2);
        Label lbl = getLabel(pane.isEmpty() ? "default" : pane);
        lbl.setLabel(eventData.toString());
        lbl.setHint(eventData.toString());
    }
    
    /**
     * Returns the label associated with the named pane, or creates a new one if necessary.
     * 
     * @param pane Name of the pane
     * @return The associated label.
     */
    private Label getLabel(String pane) {
        Label lbl = root.findByName(pane, Label.class);
        return lbl == null ? createLabel(pane) : lbl;
    }
    
    /**
     * Create a new status pane and associated label.
     * 
     * @param label Name of pane (becomes the name of the label).
     * @return The newly created label.
     */
    private Label createLabel(String label) {
        Pane pane = new Pane();
        root.addChild(pane);
        Label lbl = new Label();
        lbl.setName(label);
        pane.addChild(lbl);
        adjustPanes();
        return lbl;
    }
    
    private void adjustPanes() {
        boolean first = true;
        
        for (Pane pane : root.getChildren(Pane.class)) {
            pane.setFlex(first ? "1" : null);
            pane.setWidth(null);
            first = false;
        }
    }
}
