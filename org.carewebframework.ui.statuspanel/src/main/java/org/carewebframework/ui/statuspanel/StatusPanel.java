/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.ui.statuspanel;

import org.carewebframework.api.event.IGenericEvent;
import org.carewebframework.common.StrUtil;
import org.carewebframework.ui.FrameworkController;

import org.zkoss.zk.ui.Component;
import org.zkoss.zul.Cell;
import org.zkoss.zul.Label;
import org.zkoss.zul.Splitter;

/**
 * Controller for status panel plugin.
 */
public class StatusPanel extends FrameworkController implements IGenericEvent<Object> {
    
    private static final long serialVersionUID = 1L;
    
    private static final String STATUS_EVENT = "STATUS";
    
    private Component root;
    
    /**
     * Creates the default pane.
     */
    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        root = comp;
        createLabel("default");
        getEventManager().subscribe(STATUS_EVENT, this);
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
        lbl.setValue(eventData.toString());
        lbl.setTooltiptext(eventData.toString());
    }
    
    /**
     * Returns the label associated with the named pane, or creates a new one if necessary.
     * 
     * @param pane Name of the pane
     * @return The associated label.
     */
    private Label getLabel(String pane) {
        Label lbl = (Label) root.getFellowIfAny(pane);
        return lbl == null ? createLabel(pane) : lbl;
    }
    
    /**
     * Create a new status pane and associated label.
     * 
     * @param pane Name of pane (becomes the id of the label).
     * @return The newly created label.
     */
    private Label createLabel(String pane) {
        boolean first = root.getFirstChild() == null;
        
        if (!first) {
            root.appendChild(new Splitter());
        }
        
        Cell cell = new Cell();
        
        if (first) {
            cell.setHflex("1");
        } else {
            cell.setAlign("center");
        }
        
        cell.setVflex("1");
        cell.setSclass("cwf-header-cell");
        root.appendChild(cell);
        Label lbl = new Label();
        lbl.setId(pane);
        cell.appendChild(lbl);
        return lbl;
    }
}
