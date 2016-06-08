/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.plugin.chat;

import java.util.Collection;

import org.carewebframework.api.messaging.IPublisherInfo;
import org.carewebframework.ui.zk.AbstractListitemRenderer;

import org.zkoss.zul.Listitem;

/**
 * Renderer for participant list.
 */
public class ParticipantRenderer extends AbstractListitemRenderer<IPublisherInfo, Object> {
    
    private final Collection<IPublisherInfo> exclusions;
    
    private final IPublisherInfo self;
    
    private boolean hideExclusions;
    
    /**
     * Creates a participant renderer.
     * 
     * @param self The publisher info of the owner of the renderer.
     * @param exclusions Optional list of participants to be excluded from selection. Null for no
     *            exclusions.
     */
    public ParticipantRenderer(IPublisherInfo self, Collection<IPublisherInfo> exclusions) {
        this.self = self;
        this.exclusions = exclusions;
    }
    
    @Override
    protected void renderItem(Listitem item, IPublisherInfo participant) {
        if (item.getListbox().isCheckmark()) {
            createCell(item, null);
        }
        
        createCell(item, participant.getUserName());
        
        if (exclusions != null && exclusions.contains(participant)) {
            item.setSelectable(false);
            item.setDisabled(true);
            item.setSclass("chat-participant-active");
            item.setVisible(!hideExclusions);
        }
        
        if (participant.equals(self)) {
            item.setSclass("chat-participant-self");
        }
    }
    
    public boolean getHideExclusions() {
        return hideExclusions;
    }
    
    public void setHideExclusions(boolean hideExclusions) {
        this.hideExclusions = hideExclusions;
    }
    
};
