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
package org.carewebframework.plugin.infopanel.controller;

import java.util.List;

import org.carewebframework.plugin.infopanel.model.IActionTarget;
import org.carewebframework.plugin.infopanel.model.IInfoPanel.Action;
import org.carewebframework.plugin.infopanel.service.InfoPanelService;
import org.fujion.ancillary.INamespace;
import org.fujion.component.BaseComponent;
import org.fujion.component.Row;
import org.fujion.event.EventUtil;

/**
 * Container for receiving components rendered by drop renderer.
 */
public class AlertContainer extends Row implements INamespace, IActionTarget {
    
    private final List<ActionListener> actionListeners;
    
    /**
     * Renders an alert in its container.
     * 
     * @param parent Parent component that will host the container.
     * @param child Child component that is the root component of the alert.
     * @return The created container.
     */
    public static AlertContainer render(BaseComponent parent, BaseComponent child) {
        AlertContainer container = new AlertContainer(child);
        parent.addChild(container, 0);
        return container;
    }
    
    /**
     * Creates a new container for the alert.
     * 
     * @param child The child component.
     */
    private AlertContainer(BaseComponent child) {
        super();
        addChild(child);
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
        BaseComponent parent = getParent();
        
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
                parent.addChild(this, 0);
                break;
        }
        
        if (parent != null) {
            EventUtil.post(MainController.ALERT_ACTION_EVENT, parent, action);
        }
    }
    
}
