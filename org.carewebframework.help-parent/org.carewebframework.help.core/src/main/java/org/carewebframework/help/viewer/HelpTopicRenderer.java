package org.carewebframework.help.viewer;

import org.carewebframework.help.HelpTopic;
import org.fujion.component.Listitem;
import org.fujion.model.IComponentRenderer;

public class HelpTopicRenderer implements IComponentRenderer<Listitem, HelpTopic> {
    
    @Override
    public Listitem render(HelpTopic topic) {
        Listitem item = new Listitem(topic.getLabel());
        item.setData(topic);
        return item;
    }
}
