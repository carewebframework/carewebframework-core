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
package org.carewebframework.plugin.sessiontracker;

import java.util.Date;

import org.carewebframework.web.client.Session;
import org.carewebframework.web.component.Cell;
import org.carewebframework.web.component.Row;
import org.carewebframework.web.model.IComponentRenderer;

/**
 * RowRenderer to define rows within the Session/Desktop Tracking Grid
 */
public class MainRowRenderer implements IComponentRenderer<Row, Session> {
    
    @Override
    public Row render(Session session) {
        Row row = new Row();
        String sessionId = session.getId();
        Date creationTime = new Date(session.getCreationTime());
        Date lastAccessedTime = new Date(session.getLastActivity());
        String clientAddress = session.getSocket().getRemoteAddress().toString();
        
        createCell(row, sessionId);
        createCell(row, clientAddress);
        createCell(row, creationTime);
        createCell(row, lastAccessedTime);
        return row;
    }
    
    private void createCell(Row row, Object data) {
        Cell cell = new Cell();
        cell.setLabel(data == null ? null : data.toString());
        row.addChild(cell);
    }
}
