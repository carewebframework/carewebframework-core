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
import org.carewebframework.ui.zk.DropUtil;
import org.carewebframework.ui.zk.IDropRenderer;
import org.carewebframework.ui.zk.ZKUtil;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.DropEvent;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Panel;

/**
 * Container for receiving components rendered by drop renderer.
 */
public class DropContainer extends Panel implements IActionTarget {
    
    private static final long serialVersionUID = 1L;
    
    private static final String SCLASS = "cwf-infopanel-container";
    
    private static final String TEMPLATE = "~./org/carewebframework/plugin/infopanel/dropContainer.zul";
    
    private List<ActionListener> actionListeners;
    
    /**
     * Renders a droppedItem in a container.
     * 
     * @param dropRoot The root component that will host the container.
     * @param droppedItem The item being dropped.
     * @return The container hosting the rendered item. If the droppedItem was previously rendered,
     *         its hosting container is returned after moving it in front of its siblings. If the
     *         droppedItem is successfully rendered, its newly created container is returned. If the
     *         droppedItem cannot be rendered, null will be returned.
     */
    public static DropContainer render(Component dropRoot, Component droppedItem) {
        IDropRenderer dropRenderer = DropUtil.getDropRenderer(droppedItem);
        
        if (dropRenderer == null || !dropRenderer.isEnabled()) {
            return null;
        }
        
        Component renderedItem = dropRenderer.renderDroppedItem(droppedItem);
        DropContainer dropContainer = null;
        
        if (renderedItem != null) {
            String title = dropRenderer.getDisplayText(droppedItem);
            dropContainer = ZKUtil.findAncestor(renderedItem, DropContainer.class);
            
            if (dropContainer != null) {
                dropContainer.setTitle(title);
                dropContainer.moveToTop(dropRoot);
            } else {
                dropContainer = DropContainer.create(dropRoot, renderedItem, title,
                    InfoPanelService.getActionListeners(droppedItem));
            }
        }
        
        return dropContainer;
    }
    
    /**
     * Creates a new container for the contents to be rendered by the drop provider.
     * 
     * @param dropRoot The root component that will host the container.
     * @param cmpt Component rendered by the drop renderer.
     * @param title Title to be associated with the container.
     * @param actionListeners Listeners to be bound to the drop container (optional).
     */
    private static DropContainer create(Component dropRoot, Component cmpt, String title,
                                        List<ActionListener> actionListeners) {
        DropContainer dc = (DropContainer) ZKUtil.loadZulPage(TEMPLATE, null);
        dc.actionListeners = actionListeners;
        dc.setTitle(title);
        dc.setDroppable(SCLASS);
        dc.setDraggable(SCLASS);
        dc.getPanelchildren().appendChild(cmpt);
        dc.moveToTop(dropRoot);
        ActionListener.bindActionListeners(dc, actionListeners);
        return dc;
    }
    
    /**
     * Perform the specified action on the drop container.
     * 
     * @param action An action.
     */
    @Override
    public void doAction(Action action) {
        switch (action) {
            case REMOVE:
                onClose();
                break;
            
            case HIDE:
                setVisible(false);
                break;
            
            case SHOW:
                setVisible(true);
                break;
            
            case COLLAPSE:
                setOpen(false);
                break;
            
            case EXPAND:
                setOpen(true);
                break;
            
            case TOP:
                moveToTop();
                break;
        }
    }
    
    /**
     * Moves an existing entry to the beginning of the stream.
     */
    public void moveToTop() {
        moveToTop(getParent());
    }
    
    /**
     * Moves this entry to the beginning of the stream.
     * 
     * @param dropRoot Parent
     */
    public void moveToTop(Component dropRoot) {
        if (dropRoot != null) {
            dropRoot.insertBefore(this, dropRoot.getFirstChild());
            Clients.scrollIntoView(this);
        }
    }
    
    /**
     * Supports dragging drop container to a new position in the stream.
     * 
     * @param event The drop event.
     */
    public void onDrop(DropEvent event) {
        Component dragged = event.getDragged();
        
        if (dragged instanceof DropContainer) {
            getParent().insertBefore(dragged, this);
        }
    }
    
    @Override
    public void onClose() {
        ActionListener.unbindActionListeners(this, actionListeners);
        super.onClose();
    }
}
