package org.carewebframework.plugin.messagetesting;

import org.carewebframework.api.messaging.IMessageProducer;
import org.carewebframework.ui.zk.AbstractListitemRenderer;
import org.zkoss.zul.Listitem;

public class MessageProviderRenderer extends AbstractListitemRenderer<IMessageProducer, Object> {
    
    @Override
    protected void renderItem(Listitem item, IMessageProducer producer) {
        String name = producer.getClass().getName();
        item.setLabel(name);
        item.setSelectable(true);
        item.setSelected(true);
    }
    
}
