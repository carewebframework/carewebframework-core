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
import org.carewebframework.web.component.Cell;
import org.carewebframework.web.component.Row;
import org.carewebframework.web.model.IComponentRenderer;

/**
 * Renderer for participant list.
 */
public class ParticipantRenderer implements IComponentRenderer<Row, IPublisherInfo> {
    
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
    public Row render(IPublisherInfo participant) {
        Row row = new Row();
        row.setData(participant);
        
        createCell(row, participant.getUserName());
        
        if (exclusions != null && exclusions.contains(participant)) {
            row.setDisabled(true);
            row.addClass("chat-participant-active");
            row.setVisible(!hideExclusions);
        }
        
        if (participant.equals(self)) {
            row.addClass("chat-participant-self");
        }
        
        return row;
    }
    
    private void createCell(Row row, String userName) {
        row.addChild(new Cell(userName));
    }
    
    public boolean getHideExclusions() {
        return hideExclusions;
    }
    
    public void setHideExclusions(boolean hideExclusions) {
        this.hideExclusions = hideExclusions;
    }
    
};
