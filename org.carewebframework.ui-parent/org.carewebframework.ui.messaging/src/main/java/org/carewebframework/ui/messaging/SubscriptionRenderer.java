package org.carewebframework.ui.messaging;

import org.carewebframework.ui.zk.AbstractListitemRenderer;
import org.zkoss.zul.Listitem;

public class SubscriptionRenderer extends AbstractListitemRenderer<String, Object> {
    
    @Override
    protected void renderItem(Listitem item, String channel) {
        item.setLabel(channel);
        item.setSelectable(true);
        item.setSelected(true);
    }
    
}
