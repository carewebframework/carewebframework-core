package org.carewebframework.plugin.messaging;

import org.carewebframework.api.messaging.Message;
import org.carewebframework.ui.zk.AbstractListitemRenderer;
import org.carewebframework.ui.zk.ZKUtil;
import org.zkoss.zul.Label;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;

public class ReceivedMessageRenderer extends AbstractListitemRenderer<Message, Object> {
    
    @Override
    protected void renderItem(Listitem item, Message message) {
        createCell(item, message.getCreated());
        createCell(item, message.getType());
        createCell(item, message.getId());
        Listcell cell = createCell(item, message.getPayload());
        cell.setTooltiptext(ZKUtil.findChild(cell, Label.class).getValue());
        item.addForward("onDoubleClick", item.getListbox(), null);
    }
    
}
