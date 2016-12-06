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
import org.carewebframework.web.component.Cell;
import org.carewebframework.web.component.Label;
import org.carewebframework.web.component.Listitem;
import org.carewebframework.web.event.DblclickEvent;
import org.carewebframework.web.model.IComponentRenderer;

public class ReceivedMessageRenderer implements IComponentRenderer<Listitem, Message> {
    
    @Override
    public Listitem render(Message message) {
        Listitem item = new Listitem();
        createCell(item, message.getCreated());
        createCell(item, message.getMetadata("cwf.pub.channel"));
        createCell(item, message.getType());
        createCell(item, message.getId());
        Cell cell = createCell(item, message.getPayload());
        cell.setHint(cell.getChild(Label.class).getLabel());
        item.addEventForward(DblclickEvent.TYPE, item.getListbox(), null);
        return item;
    }
    
    public Cell createCell(Listitem item, Object value) {
        Cell cell = new Cell(value == null ? null : value.toString());
        item.addChild(cell);
        return cell;
    }
}
