/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.ui.zk;

import org.apache.commons.lang.StringUtils;

import org.carewebframework.common.StrUtil;

import org.zkoss.zk.ui.Component;
import org.zkoss.zul.Cell;
import org.zkoss.zul.Column;
import org.zkoss.zul.Columns;
import org.zkoss.zul.Detail;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Group;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.Rows;

/**
 * Base row renderer.
 * 
 * @param <T> Data type of row-associated object.
 * @param <G> Data type of group-associated object.
 */
public abstract class AbstractRowRenderer<T, G> extends AbstractRenderer implements RowRenderer<T> {
    
    private static final String ATTR_DETAIL = AbstractRowRenderer.class.getName() + ".detail";
    
    private static final String ATTR_DETAIL_EXPAND = ATTR_DETAIL + ".expand";
    
    private static final String ATTR_GROUP_EXPAND = AbstractRowRenderer.class.getName() + ".group.expand";
    
    /**
     * Associates the detail view with the specified row. This allows better performance when
     * changing the expand detail state by avoiding iterating over the component tree to find the
     * detail component.
     * 
     * @param row Row with which to associate the detail.
     * @param detail The detail.
     */
    private static void associateDetail(Row row, Detail detail) {
        row.setAttribute(ATTR_DETAIL, detail);
    }
    
    /**
     * Returns the detail associated with the row.
     * 
     * @param row The row whose associated detail is sought.
     * @return The associated detail, or null if none.
     */
    private static Detail getDetail(Row row) {
        return row.hasAttribute(ATTR_DETAIL) ? (Detail) row.getAttribute(ATTR_DETAIL) : row.getDetailChild();
    }
    
    /**
     * Updates the detail open state for all detail views in the grid.
     * 
     * @param grid The grid.
     * @param detailOpen The open state for detail views. If null, the open state for detail views
     *            is unaffected.
     * @param groupOpen The open state for groups. If null, the open state for groups is unaffected.
     */
    public static void expandAll(Grid grid, Boolean detailOpen, Boolean groupOpen) {
        Rows rows = grid.getRows();
        
        if (rows != null) {
            for (Row row : rows.<Row> getChildren()) {
                if (groupOpen != null && row instanceof Group) {
                    ((Group) row).setOpen(groupOpen);
                }
                
                if (detailOpen != null) {
                    Detail detail = getDetail(row);
                    
                    if (detail != null) {
                        detail.setOpen(detailOpen);
                    }
                }
            }
        }
    }
    
    /**
     * Returns the default detail expansion state for the grid.
     * 
     * @param grid The grid.
     * @return The default detail expansion state.
     */
    public static boolean getExpandDetail(Grid grid) {
        Boolean expandDetail = (Boolean) grid.getAttribute(ATTR_DETAIL_EXPAND);
        return expandDetail != null && expandDetail;
    }
    
    /**
     * Sets the default detail expansion state for the grid and for any existing detail views within
     * the grid.
     * 
     * @param grid The grid.
     * @param value The default detail expansion state.
     */
    public static void setExpandDetail(Grid grid, boolean value) {
        boolean oldValue = getExpandDetail(grid);
        
        if (oldValue != value) {
            grid.setAttribute(ATTR_DETAIL_EXPAND, value);
            expandAll(grid, value, null);
        }
    }
    
    /**
     * Returns the default group expansion state for the grid.
     * 
     * @param grid The grid.
     * @return The default group expansion state.
     */
    public static boolean getExpandGroup(Grid grid) {
        Boolean expandGroup = (Boolean) grid.getAttribute(ATTR_GROUP_EXPAND);
        return expandGroup == null || expandGroup;
    }
    
    /**
     * Sets the default group expansion state for all groups within the grid.
     * 
     * @param grid The grid.
     * @param value The default group expansion state.
     */
    public static void setExpandGroup(Grid grid, boolean value) {
        boolean oldValue = getExpandGroup(grid);
        
        if (oldValue != value) {
            grid.setAttribute(ATTR_GROUP_EXPAND, value);
            expandAll(grid, null, value);
        }
    }
    
    /**
     * No args Constructor
     */
    public AbstractRowRenderer() {
        super();
    }
    
    /**
     * @param rowStyle Style to be applied to each rendered row.
     * @param cellStyle Style to be applied to each cell.
     */
    public AbstractRowRenderer(String rowStyle, String cellStyle) {
        super(rowStyle, cellStyle);
    }
    
    /**
     * Row rendering logic.
     * 
     * @param row Row being rendered.
     * @param object The data object associated with the row.
     * @return Parent component for detail. If null, no detail will be created.
     */
    protected abstract Component renderRow(Row row, T object);
    
    /**
     * Groups rendering logic.
     * 
     * @param group Group being rendered.
     * @param object The data object associated with the group.
     */
    protected void renderGroup(Group group, G object) {
        group.setLabel(object.toString());
    }
    
    /**
     * Detail rendering logic.
     * 
     * @param detail The detail being rendered.
     * @param object The data object associated with the row.
     */
    protected void renderDetail(Detail detail, T object) {
    }
    
    /**
     * @see org.zkoss.zul.RowRenderer#render(org.zkoss.zul.Row, java.lang.Object, int)
     */
    @SuppressWarnings("unchecked")
    @Override
    public final void render(Row row, Object object, int index) throws Exception {
        row.setValue(object);
        
        if (row instanceof Group) {
            Group group = (Group) row;
            group.setOpen(getExpandGroup(row.getGrid()));
            renderGroup(group, (G) object);
            return;
        }
        
        row.setStyle(compStyle);
        row.setValign("middle");
        Component detailParent = renderRow(row, (T) object);
        
        if (detailParent != null) {
            Detail detail = createDetail(row, detailParent);
            renderDetail(detail, (T) object);
            
            if (detail.getFirstChild() == null) {
                detail.setVisible(false);
            }
        } else {
            associateDetail(row, null);
        }
    }
    
    /**
     * Creates a cell containing a label with the specified parameters.
     * 
     * @param parent Component that will be the parent of the cell.
     * @param value Value to be used as label text.
     * @return The newly created cell.
     */
    public Cell createCell(Component parent, Object value) {
        return createCell(parent, value, null);
    }
    
    /**
     * Creates a cell containing a label with the specified parameters.
     * 
     * @param parent Component that will be the parent of the cell.
     * @param value Value to be used as label text.
     * @param prefix Value to be used as a prefix for the label text.
     * @return The newly created cell.
     */
    public Cell createCell(Component parent, Object value, String prefix) {
        return createCell(parent, value, prefix, null);
    }
    
    /**
     * Creates a cell containing a label with the specified parameters.
     * 
     * @param parent Component that will be the parent of the cell.
     * @param value Value to be used as label text.
     * @param prefix Value to be used as a prefix for the label text.
     * @param style Style to be applied to the label.
     * @return The newly created cell.
     */
    public Cell createCell(Component parent, Object value, String prefix, String style) {
        return createCell(parent, value, prefix, style, null);
    }
    
    /**
     * Creates a cell containing a label with the specified parameters.
     * 
     * @param parent Component that will be the parent of the cell.
     * @param value Value to be used as label text.
     * @param prefix Value to be used as a prefix for the label text.
     * @param style Style to be applied to the label.
     * @param width Width of the cell.
     * @return The newly created cell.
     */
    public Cell createCell(Component parent, Object value, String prefix, String style, String width) {
        return createCell(parent, value, prefix, style, width, Cell.class);
    }
    
    /**
     * If a value to be added is empty or null, rather than creating a new cell with no content,
     * will increase the span count of the preceding cell by one.
     * 
     * @param parent Parent for a newly created cell.
     * @param cell The preceding cell, or null to force new cell creation.
     * @param value The content for the new cell.
     * @return If the previous cell was re-used, this is returned. Otherwise, returns a new cell.
     */
    public Cell createOrMergeCell(Component parent, Cell cell, Object value) {
        return createOrMergeCell(parent, cell, value, null);
    }
    
    /**
     * If a value to be added is empty or null, rather than creating a new cell with no content,
     * will increase the span count of the preceding cell by one.
     * 
     * @param parent Parent for a newly created cell.
     * @param cell The preceding cell, or null to force new cell creation.
     * @param value The content for the new cell.
     * @param prefix Text prefix for content.
     * @return If the previous cell was re-used, this is returned. Otherwise, returns a new cell.
     */
    public Cell createOrMergeCell(Component parent, Cell cell, Object value, String prefix) {
        return createOrMergeCell(parent, cell, value, prefix, null);
    }
    
    /**
     * If a value to be added is empty or null, rather than creating a new cell with no content,
     * will increase the span count of the preceding cell by one.
     * 
     * @param parent Parent for a newly created cell.
     * @param cell The preceding cell, or null to force new cell creation.
     * @param value The content for the new cell.
     * @param prefix Text prefix for content.
     * @param style Optional style for label.
     * @return If the previous cell was re-used, this is returned. Otherwise, returns a new cell.
     */
    public Cell createOrMergeCell(Component parent, Cell cell, Object value, String prefix, String style) {
        value = value instanceof String ? StringUtils.trimToNull(createLabelText(value, prefix)) : value;
        
        if (cell == null || value != null) {
            cell = createCell(parent, value, null, style, "100%");
            cell.setColspan(1);
        } else {
            cell.setColspan(cell.getColspan() + 1);
        }
        
        return cell;
    }
    
    /**
     * Creates a grid for detail view.
     * 
     * @param parent The detail parent.
     * @param colWidths Array of column widths.
     * @return The detail grid.
     */
    public Grid createDetailGrid(Component parent, String[] colWidths) {
        return createDetailGrid(parent, colWidths, null);
    }
    
    /**
     * Creates a grid for detail view.
     * 
     * @param parent The detail parent.
     * @param colWidths Array of column widths.
     * @param colLabels Array of column labels (may be null).
     * @return The detail grid.
     */
    public Grid createDetailGrid(Component parent, String[] colWidths, String[] colLabels) {
        Grid detailGrid = new Grid();
        detailGrid.setOddRowSclass("none");
        detailGrid.setWidth("100%");
        detailGrid.setParent(parent);
        Columns detailColumns = new Columns();
        detailColumns.setSizable(true);
        detailColumns.setParent(detailGrid);
        int cols = Math.max(colWidths == null ? 0 : colWidths.length, colLabels == null ? 0 : colLabels.length);
        
        for (int i = 0; i < cols; i++) {
            String colLabel = colLabels == null || i >= colLabels.length ? " " : StrUtil.formatMessage(colLabels[i]);
            Column col = new Column(colLabel);
            String colWidth = colWidths == null || i >= colWidths.length ? null : colWidths[i];
            col.setWidth(colWidth);
            col.setParent(detailColumns);
        }
        
        detailGrid.appendChild(new Rows());
        return detailGrid;
    }
    
    private Detail createDetail(Row row, Component parent) {
        Detail detail = new Detail();
        detail.setParent(parent);
        associateDetail(row, detail);
        detail.setOpen(getExpandDetail(row.getGrid()));
        return detail;
    }
}
