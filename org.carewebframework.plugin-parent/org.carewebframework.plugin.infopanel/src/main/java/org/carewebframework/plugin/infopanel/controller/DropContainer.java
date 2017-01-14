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
import org.carewebframework.web.annotation.EventHandler;
import org.carewebframework.web.component.BaseComponent;
import org.carewebframework.web.component.Window;
import org.carewebframework.web.dragdrop.DropUtil;
import org.carewebframework.web.dragdrop.IDropRenderer;
import org.carewebframework.web.event.DropEvent;
import org.carewebframework.web.page.PageUtil;

/**
 * Container for receiving components rendered by drop renderer.
 */
public class DropContainer extends Window implements IActionTarget {
    
    private static final String SCLASS = "cwf-infopanel-container";
    
    private static final String TEMPLATE = "web/org/carewebframework/plugin/infopanel/dropContainer.cwf";
    
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
    public static DropContainer render(BaseComponent dropRoot, BaseComponent droppedItem) {
        IDropRenderer dropRenderer = DropUtil.getDropRenderer(droppedItem);
        
        if (dropRenderer == null || !dropRenderer.isEnabled()) {
            return null;
        }
        
        BaseComponent renderedItem = dropRenderer.renderDroppedItem(droppedItem);
        DropContainer dropContainer = null;
        
        if (renderedItem != null) {
            String title = dropRenderer.getDisplayText(droppedItem);
            dropContainer = renderedItem.getAncestor(DropContainer.class);
            
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
     * @param cmpt BaseComponent rendered by the drop renderer.
     * @param title Title to be associated with the container.
     * @param actionListeners Listeners to be bound to the drop container (optional).
     */
    private static DropContainer create(BaseComponent dropRoot, BaseComponent cmpt, String title,
                                        List<ActionListener> actionListeners) {
        DropContainer dc = (DropContainer) PageUtil.createPage(TEMPLATE, null);
        dc.actionListeners = actionListeners;
        dc.setTitle(title);
        dc.setDropid(SCLASS);
        dc.setDragid(SCLASS);
        dc.addChild(cmpt);
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
                close();
                break;
            
            case HIDE:
                setVisible(false);
                break;
            
            case SHOW:
                setVisible(true);
                break;
            
            case COLLAPSE:
                setSize(Size.MINIMIZED);
                break;
            
            case EXPAND:
                setSize(Size.NORMAL);
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
    public void moveToTop(BaseComponent dropRoot) {
        if (dropRoot != null) {
            dropRoot.addChild(this, 0);
            this.scrollIntoView(false);
        }
    }
    
    /**
     * Supports dragging drop container to a new position in the stream.
     * 
     * @param event The drop event.
     */
    @EventHandler("drop")
    private void onDrop(DropEvent event) {
        BaseComponent dragged = event.getRelatedTarget();
        
        if (dragged instanceof DropContainer) {
            getParent().addChild(dragged, this);
        }
    }
    
    @Override
    public void destroy() {
        ActionListener.unbindActionListeners(this, actionListeners);
        super.destroy();
    }
}
