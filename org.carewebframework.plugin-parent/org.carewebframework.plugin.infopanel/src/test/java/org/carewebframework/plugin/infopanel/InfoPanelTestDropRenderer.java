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
package org.carewebframework.plugin.infopanel;

import org.carewebframework.ui.zk.IDropRenderer;

import org.zkoss.zk.ui.Component;
import org.zkoss.zul.Label;
import org.zkoss.zul.Listitem;

/**
 * This is the drop renderer for the test component. It expects a list item whose value is set to a
 * DroppedItem object.
 */
public class InfoPanelTestDropRenderer implements IDropRenderer {
    
    private boolean enabled = true;
    
    /**
     * Extract the associated dropped item from the dropped UI component.
     * 
     * @param comp The component (a list item) that was dropped.
     * @return The associated drop item.
     */
    private DroppedItem getDroppedItem(Component comp) {
        return (DroppedItem) ((Listitem) comp).getValue();
    }
    
    /**
     * Called by the info panel to rendered the dropped UI component.
     * 
     * @param droppedItem The dropped UI component (a list item in this case).
     * @return The top level component of the rendering.
     */
    @Override
    public Component renderDroppedItem(Component droppedItem) {
        DroppedItem item = getDroppedItem(droppedItem);
        return new Label(item.getItemDetail());
    }
    
    /**
     * Returns the display text for the dropped item.
     * 
     * @param droppedItem The dropped UI component (a list item in this case).
     * @return The text to display in association with the rendered item.
     */
    @Override
    public String getDisplayText(Component droppedItem) {
        return getDroppedItem(droppedItem).getItemName();
    }
    
    /**
     * Returns the enable status of the drop renderer.
     * 
     * @return The enable status.
     */
    @Override
    public boolean isEnabled() {
        return enabled;
    }
    
    /**
     * Sets the enable status of the drop renderer.
     * 
     * @param enabled The new enable status.
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
