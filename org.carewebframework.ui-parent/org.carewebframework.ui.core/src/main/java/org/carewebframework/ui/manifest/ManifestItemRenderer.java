package org.carewebframework.ui.manifest;

import org.fujion.component.BaseComponent;
import org.fujion.component.Row;
import org.fujion.component.Grid;
import org.fujion.event.ClickEvent;
import org.fujion.event.Event;
import org.fujion.event.EventUtil;
import org.fujion.event.IEventListener;

public class ManifestItemRenderer extends BaseRenderer<ManifestItem> {
    
    private final IEventListener listener = new IEventListener() {
        
        @Override
        public void onEvent(Event event) {
            BaseComponent target = event.getCurrentTarget();
            Event newEvent = new Event("showManifest", target.getAncestor(Grid.class), target.getData());
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
        row.addEventListener(ClickEvent.TYPE, listener);
        return row;
    }
    
    @Override
    public void init(Grid grid) {
        grid.getRows().getModelAndView(ManifestItem.class).setRenderer(this);
        addColumn(grid, "Module", "40%", "@implModule").setSortColumn(true);
        addColumn(grid, "Version", "20%", "@implVersion");
        addColumn(grid, "Author", "40%", "@implVendor");
    }
}
