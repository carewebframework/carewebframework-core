package org.carewebframework.ui.manifest;

import org.carewebframework.web.component.BaseComponent;
import org.carewebframework.web.component.Row;
import org.carewebframework.web.component.Table;
import org.carewebframework.web.event.ClickEvent;
import org.carewebframework.web.event.Event;
import org.carewebframework.web.event.EventUtil;
import org.carewebframework.web.event.IEventListener;

public class ManifestItemRenderer extends BaseRenderer<ManifestItem> {
    
    private final IEventListener listener = new IEventListener() {
        
        @Override
        public void onEvent(Event event) {
            BaseComponent target = event.getCurrentTarget();
            Event newEvent = new Event("showManifest", target.getAncestor(Table.class), target.getData());
            EventUtil.send(newEvent);
        }
        
    };
    
    @Override
    public Row render(ManifestItem manifestItem) {
        Row row = new Row();
        row.setData(manifestItem);
        addCell(row, manifestItem.implModule);
        addCell(row, manifestItem.implVersion);
        addCell(row, manifestItem.implVendor);
        row.registerEventListener(ClickEvent.TYPE, listener);
        return row;
    }
    
    @Override
    public void init(Table table) {
        table.getRows().getModelAndView(ManifestItem.class).setRenderer(this);
        addColumn(table, "Module", "40%", "@implModule").setSortColumn(true);
        addColumn(table, "Version", "20%", "@implVersion");
        addColumn(table, "Author", "40%", "@implVendor");
    }
}
