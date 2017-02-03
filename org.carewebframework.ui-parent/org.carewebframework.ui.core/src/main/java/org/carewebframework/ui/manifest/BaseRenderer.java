package org.carewebframework.ui.manifest;

import org.carewebframework.ui.util.CWFUtil;
import org.carewebframework.web.component.Cell;
import org.carewebframework.web.component.Column;
import org.carewebframework.web.component.Row;
import org.carewebframework.web.component.Span;
import org.carewebframework.web.component.Grid;
import org.carewebframework.web.model.IComponentRenderer;
import org.carewebframework.web.model.Sorting.SortOrder;

/**
 * Base renderer.
 * 
 * @param <M> Class of rendered object.
 */
abstract class BaseRenderer<M> implements IComponentRenderer<Row, M> {
    
    public abstract void init(Grid grid);
    
    /**
     * Adds a cell with the specified content to the grid row.
     * 
     * @param row List row.
     * @param label Content for cell. Auto-detects type of content.
     * @return Newly created cell.
     */
    public Span addContent(Row row, String label) {
        Span cell = new Span();
        cell.addChild(CWFUtil.getTextComponent(label));
        row.addChild(cell);
        return cell;
    }
    
    /**
     * Adds a cell to the grid row.
     * 
     * @param row List row.
     * @param label Label text for cell.
     * @return Newly created cell.
     */
    public Cell addCell(Row row, String label) {
        Cell cell = new Cell(label);
        row.addChild(cell);
        return cell;
    }
    
    /**
     * Adds a column to a grid.
     * 
     * @param grid Grid.
     * @param label Label for column.
     * @param width Width for column.
     * @param sortBy Field for sorting.
     * @return Newly created column.
     */
    public Column addColumn(Grid grid, String label, String width, String sortBy) {
        Column column = new Column();
        grid.getColumns().addChild(column);
        column.setLabel(label);
        column.setWidth(width);
        column.setSortComparator(sortBy);
        column.setSortOrder(SortOrder.ASCENDING);
        return column;
    }
    
    public int compareStr(String s1, String s2) {
        return s1 == s2 ? 0 : s1 == null ? -1 : s2 == null ? 1 : s1.compareToIgnoreCase(s2);
    }
    
}
