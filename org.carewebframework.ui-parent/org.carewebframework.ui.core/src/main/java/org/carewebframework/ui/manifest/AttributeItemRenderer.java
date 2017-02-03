package org.carewebframework.ui.manifest;

import org.carewebframework.web.component.Row;
import org.carewebframework.web.component.Grid;

public class AttributeItemRenderer extends BaseRenderer<AttributeItem> {
    
    @Override
    public Row render(AttributeItem attributeItem) {
        Row row = new Row();
        row.setData(attributeItem);
        addCell(row, attributeItem.name);
        addContent(row, attributeItem.value);
        return row;
    }
    
    @Override
    public void init(Grid grid) {
        grid.getRows().getModelAndView(AttributeItem.class).setRenderer(this);
        addColumn(grid, "Attribute", "30%", "@name");
        addColumn(grid, "Value", "70%", "@value");
    }
    
}
