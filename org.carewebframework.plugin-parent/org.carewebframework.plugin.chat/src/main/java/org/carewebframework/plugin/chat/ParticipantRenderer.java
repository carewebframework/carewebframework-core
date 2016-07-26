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
package org.carewebframework.plugin.chat;

import java.util.Collection;

import org.carewebframework.api.messaging.IPublisherInfo;
import org.carewebframework.ui.zk.AbstractListitemRenderer;
import org.carewebframework.web.component.Listitem;

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
            item.addClass("chat-participant-active");
            item.setVisible(!hideExclusions);
        }
        
        if (participant.equals(self)) {
            item.addClass("chat-participant-self");
        }
    }
    
    public boolean getHideExclusions() {
        return hideExclusions;
    }
    
    public void setHideExclusions(boolean hideExclusions) {
        this.hideExclusions = hideExclusions;
    }
    
};
