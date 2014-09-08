/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.ui.infopanel.controller;

import java.util.List;

import org.carewebframework.ui.infopanel.model.IActionTarget;
import org.carewebframework.ui.infopanel.model.IInfoPanel.Action;
import org.carewebframework.ui.infopanel.service.InfoPanelService;
import org.carewebframework.ui.zk.DropUtil;
import org.carewebframework.ui.zk.IDropRenderer;
import org.carewebframework.ui.zk.ZKUtil;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.IdSpace;
import org.zkoss.zk.ui.event.DropEvent;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Panel;
import org.zkoss.zul.Panelchildren;

/**
 * Container for receiving components rendered by drop renderer.
 */
public class DropContainer extends Panel implements IdSpace, IActionTarget {
    
    private static final long serialVersionUID = 1L;
    
    private static final String SCLASS = "cwf-infopanel-container";
    
    private final List<ActionListener> actionListeners;
    
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
                dropContainer = new DropContainer(dropRoot, renderedItem, title,
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
    private DropContainer(Component dropRoot, Component cmpt, String title, List<ActionListener> actionListeners) {
        super();
        this.actionListeners = actionListeners;
        setWidth("100%");
        setTitle(title);
        setBorder("none");
        setCollapsible(true);
        setClosable(true);
        setSclass(SCLASS);
        setDroppable(SCLASS);
        setDraggable(SCLASS);
        Panelchildren pc = new Panelchildren();
        appendChild(pc);
        pc.appendChild(cmpt);
        moveToTop(dropRoot);
        ActionListener.bindActionListeners(this, actionListeners);
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
