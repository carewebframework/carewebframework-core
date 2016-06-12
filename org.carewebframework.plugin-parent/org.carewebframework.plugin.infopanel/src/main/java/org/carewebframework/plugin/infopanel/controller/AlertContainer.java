/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.plugin.infopanel.controller;

import java.util.List;

import org.carewebframework.plugin.infopanel.model.IActionTarget;
import org.carewebframework.plugin.infopanel.model.IInfoPanel.Action;
import org.carewebframework.plugin.infopanel.service.InfoPanelService;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.IdSpace;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Row;

/**
 * Container for receiving components rendered by drop renderer.
 */
public class AlertContainer extends Row implements IdSpace, IActionTarget {
    
    private static final long serialVersionUID = 1L;
    
    private final List<ActionListener> actionListeners;
    
    /**
     * Renders an alert in its container.
     * 
     * @param parent Parent component that will host the container.
     * @param child Child component that is the root component of the alert.
     * @return The created container.
     */
    public static AlertContainer render(Component parent, Component child) {
        AlertContainer container = new AlertContainer(child);
        parent.insertBefore(container, parent.getFirstChild());
        return container;
    }
    
    /**
     * Creates a new container for the alert.
     * 
     * @param child The child component.
     */
    private AlertContainer(Component child) {
        super();
        appendChild(child);
        actionListeners = InfoPanelService.getActionListeners(child);
        ActionListener.bindActionListeners(this, actionListeners);
    }
    
    /**
     * Perform the specified action on the drop container. Also, notifies the container's parent
     * that an action has occurred.
     * 
     * @param action An action.
     */
    @Override
    public void doAction(Action action) {
        Component parent = getParent();
        
        switch (action) {
            case REMOVE:
                ActionListener.unbindActionListeners(this, actionListeners);
                detach();
                break;
            
            case HIDE:
            case COLLAPSE:
                setVisible(false);
                break;
            
            case SHOW:
            case EXPAND:
                setVisible(true);
                break;
            
            case TOP:
                parent.insertBefore(this, parent.getFirstChild());
                break;
        }
        
        if (parent != null) {
            Events.postEvent(MainController.ALERT_ACTION_EVENT, parent, action);
        }
    }
    
}
