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
package org.carewebframework.plugin.messagetesting;

import org.carewebframework.api.messaging.Message;
import org.fujion.component.Cell;
import org.fujion.component.Grid;
import org.fujion.component.Label;
import org.fujion.component.Row;
import org.fujion.event.DblclickEvent;
import org.fujion.model.IComponentRenderer;

public class ReceivedMessageRenderer implements IComponentRenderer<Row, Message> {
    
    private final Grid grid;
    
    public ReceivedMessageRenderer(Grid grid) {
        this.grid = grid;
    }
    
    @Override
    public Row render(Message message) {
        Row row = new Row();
        createCell(row, message.getCreated());
        createCell(row, message.getMetadata("cwf.pub.channel"));
        createCell(row, message.getType());
        createCell(row, message.getId());
        Cell cell = createCell(row, message.getPayload());
        cell.setHint(cell.getChild(Label.class).getLabel());
        row.addEventForward(DblclickEvent.TYPE, grid, null);
        return row;
    }
    
    public Cell createCell(Row row, Object value) {
        Cell cell = new Cell(value == null ? null : value.toString());
        row.addChild(cell);
        return cell;
    }
}
