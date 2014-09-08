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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.SelectEvent;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Column;
import org.zkoss.zul.Columns;
import org.zkoss.zul.Detail;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Group;
import org.zkoss.zul.Hbox;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.Rows;

/**
 * This is still a work in progress. The idea is to create a grid that supports selection of grid
 * rows much as a listbox with the checkbox styling would. Since listbox has anomalies with item
 * selection, this could be an alternative. There are still issues to be worked out such as
 * maintaining selection state when the grid is re-rendered.
 */
public class SelectionGrid extends Grid {
    
    private static final long serialVersionUID = 1L;
    
    private static final String UPDATING_ATTR = "_updating";
    
    private static final String DISABLED_ATTR = "_disabled";
    
    private final Set<Row> selectedRows = new HashSet<Row>();
    
    private Checkbox chkHeader;
    
    private boolean disabled;
    
    private boolean initialized;
    
    private int checkColumnIndex = -1;
    
    private boolean selectOnClick;
    
    /**
     * Filter for selecting rows.
     */
    private class Filter {
        
        Boolean selected;
        
        Boolean disabled;
        
        public Filter(Boolean selected, Boolean disabled) {
            this.selected = selected;
            this.disabled = disabled;
        }
        
        public boolean matches(Row row) {
            if (getCheckbox(row) == null) {
                return false;
            }
            
            if (selected != null && selected != isSelected(row)) {
                return false;
            }
            
            if (disabled != null && disabled != isDisabled(row)) {
                return false;
            }
            
            return true;
        }
    }
    
    public void onCreate() {
        init();
    }
    
    /**
     * Unselect all rows in the grid.
     */
    public void clearSelection() {
        selectAll(false);
    }
    
    /**
     * Select all rows in the grid.
     */
    public void selectAll() {
        selectAll(true);
    }
    
    /**
     * Set the selection state for all rows in the grid.
     * 
     * @param selected Selection state for the rows.
     */
    public void selectAll(boolean selected) {
        setSelected(getRows(new Filter(!selected, null), false), selected);
    }
    
    /**
     * Select all rows in the grid on the current page.
     */
    public void selectAllPage() {
        selectAllPage(true);
    }
    
    /**
     * Set the selection state of all rows on the current page.
     * 
     * @param selected Selection state for the rows.
     */
    public void selectAllPage(boolean selected) {
        setSelected(getPageRows(new Filter(!selected, null), false), selected);
    }
    
    /**
     * Set the selection state for the row at the given index.
     * 
     * @param index Index of row to change.
     * @param selected Selection state for the row.
     */
    public void setSelected(int index, boolean selected) {
        setSelected(getRowAtIndex(index), selected);
    }
    
    /**
     * Set the selection state for the given row.
     * 
     * @param row Row to change.
     * @param selected Selection state for the row.
     */
    public void setSelected(Row row, boolean selected) {
        setSelected(row, selected, true);
    }
    
    /**
     * Set the selection state for the given row, and optionally fire an onSelect event.
     * 
     * @param row Row whose selection state is being changed.
     * @param selected Selection state for the row. If the same as the current selection state, no
     *            action is taken.
     * @param fireEvent If true and the selection state has changed, fire an onSelect event.
     * @return True if the selection state changed.
     */
    public boolean setSelected(Row row, boolean selected, boolean fireEvent) {
        Checkbox cb;
        
        if (selected != isSelected(row) && (cb = getCheckbox(row)) != null && !cb.isDisabled()) {
            cb.setChecked(selected);
            updateState(row, fireEvent);
            return true;
        }
        
        return false;
    }
    
    /**
     * Sets the selection state for multiple rows. Fires a single onSelect event with a null target
     * if the selection state has changed for any of the rows.
     * 
     * @param rows List of rows to change.
     * @param selected Selection state for the rows.
     */
    public void setSelected(List<Row> rows, boolean selected) {
        boolean hasChanged = false;
        
        for (Row row : rows) {
            hasChanged |= setSelected(row, selected, false);
        }
        
        if (hasChanged) {
            fireSelectEvent(null);
        }
    }
    
    /**
     * Updates the state for the given row.
     * 
     * @param row Row to update.
     * @param fireEvent If true, an onSelect event is fired.
     */
    private void updateState(Row row, boolean fireEvent) {
        Checkbox cb = getCheckbox(row);
        
        if (cb == null) {
            return;
        }
        
        boolean selected = cb.isChecked();
        
        if (selected) {
            selectedRows.add(row);
        } else {
            selectedRows.remove(row);
        }
        
        if (row instanceof Group) {
            updateMembersFromGroup((Group) row);
        } else {
            updateGroupFromMembers(row.getGroup());
        }
        
        chkHeader.setChecked(selected ? isPageSelected() : false);
        chkHeader.setDisabled(isPageDisabled());
        
        if (fireEvent) {
            fireSelectEvent(row);
        }
    }
    
    /**
     * Updates group member selection and disabled states based on the state of the group.
     * 
     * @param group The group.
     */
    private void updateMembersFromGroup(Group group) {
        if (group != null && !isUpdating(group)) {
            try {
                setUpdating(group, true);
                boolean isSelected = isSelected(group);
                boolean isDisabled = isDisabled(group);
                
                for (Object item : group.getItems()) {
                    setSelected((Row) item, isSelected, false);
                    setDisabled((Row) item, isDisabled);
                }
            } finally {
                setUpdating(group, false);
            }
        }
    }
    
    /**
     * Update the selection and disabled states for the group based on the state of its members.
     * 
     * @param group The group.
     */
    private void updateGroupFromMembers(Group group) {
        if (group != null && !isUpdating(group)) {
            try {
                setUpdating(group, true);
                boolean isSelected = true;
                boolean isDisabled = true;
                
                for (Object item : group.getItems()) {
                    if (testRow((Row) item, false)) {
                        isSelected = false;
                    }
                    
                    isDisabled &= isDisabled((Row) item);
                    
                    if (!isDisabled && !isSelected) {
                        break;
                    }
                }
                
                setSelected(group, isSelected, false);
                setDisabled(group, isDisabled);
            } finally {
                setUpdating(group, false);
            }
        }
    }
    
    /**
     * Returns true if a component is being updated.
     * 
     * @param cmpt The component to test.
     * @return True if component is being updated.
     */
    private boolean isUpdating(Component cmpt) {
        return cmpt.getAttribute(UPDATING_ATTR) != null;
    }
    
    /**
     * Sets the update state of the specified component.
     * 
     * @param cmpt The target component.
     * @param updating The updating state.
     */
    private void setUpdating(Component cmpt, boolean updating) {
        if (updating) {
            cmpt.setAttribute(UPDATING_ATTR, true);
        } else {
            cmpt.removeAttribute(UPDATING_ATTR);
        }
    }
    
    /**
     * Fires an onSelect event.to the grid.
     * 
     * @param row The selected row.
     */
    private void fireSelectEvent(Row row) {
        @SuppressWarnings({ "rawtypes", "unchecked" })
        SelectEvent event = new SelectEvent(Events.ON_SELECT, row, null);
        Events.sendEvent(this, event);
    }
    
    /**
     * Sets the row renderer for the grid. Throws a run time exception if the renderer is not an
     * instance of SelectionGridRenderer.
     */
    @Override
    public void setRowRenderer(RowRenderer<?> renderer) {
        if (renderer != null && !(renderer instanceof SelectionGridRenderer)) {
            throw new RuntimeException("Renderer must be of type SelectionGridRenderer.");
        }
        
        super.setRowRenderer(renderer);
    }
    
    /**
     * Adds a checkbox to the specified row in the column designated by checkColumnIndex. If the row
     * already has a checkbox, this call is ignored.
     * 
     * @param row Row to receive the checkbox.
     */
    public void addCheckbox(Row row) {
        init();
        
        if (checkColumnIndex > -1 && getCheckbox(row) == null) {
            fillRow(row, checkColumnIndex + 1);
            Component cell = row.getChildren().get(checkColumnIndex);
            
            if (!(cell instanceof Hbox) && !(cell instanceof Detail)) {
                Component child = cell;
                cell = new Hbox();
                row.insertBefore(cell, child);
                child.setParent(cell);
            }
            
            Checkbox cb = new Checkbox();
            cb.addForward(Events.ON_CHECK, this, null);
            cell.insertBefore(cb, cell.getFirstChild());
            row.setAttribute("_cb", cb);
            cb.setAttribute("_row", row);
            
            /*
             * If selectOnClick is enabled, forward row click events to the grid.  To avoid problem of expanding/collapsing detail
             * views affecting the selection of the row, we add a JavaScript action to suppress click events on the detail elements.
             */
            if (selectOnClick) {
                row.setWidgetListener("onClick", "cwf.selectiongrid_onclick(event);");
                row.addForward(Events.ON_CLICK, this, "onRowClick");
                row.addForward(Events.ON_DOUBLE_CLICK, this, "onRowDoubleClick");
            }
        }
    }
    
    /**
     * Ensures that no cell in the given row is empty by adding an hbox for any empty cell.
     * 
     * @param row The target row.
     */
    public void fillRow(Row row) {
        fillRow(row, forceColumns().getChildren().size());
    }
    
    /**
     * Fills all empty cells for the given row up to the specified column count.
     * 
     * @param row Row to fill.
     * @param colCount Number of columns to fill.
     */
    public void fillRow(Row row, int colCount) {
        for (int i = row.getChildren().size(); i < colCount; i++) {
            row.appendChild(new Hbox());
        }
    }
    
    /**
     * Returns all rows in the current page that match the specified criteria. If the grid is not in
     * paging mode, all rows are considered.
     * 
     * @param filter Filter containing match criteria.
     * @param testing If true, only care if one matching row exists.
     * @return List of rows matching the selection state.
     */
    private List<Row> getPageRows(Filter filter, boolean testing) {
        if (!isPagingActive()) {
            return getRows(filter, testing);
        }
        
        int start = getPageSize() * getActivePage();
        int end = Math.min(getRowCount(), start + getPageCount());
        return getRows(getAllRows().subList(start, end), filter, testing);
    }
    
    /**
     * Returns a list of rows that match the specified criteria.
     * 
     * @param filter Filter containing match criteria.
     * @param testing If true, only care if one matching row exists.
     * @return List of rows matching the selection state.
     */
    private List<Row> getRows(Filter filter, boolean testing) {
        return getRows(getAllRows(), filter, testing);
    }
    
    /**
     * Returns a list of rows from the input list that matches the desired criteria.
     * 
     * @param rows List of rows to search.
     * @param filter Filter containing match criteria.
     * @param testing If true, only care if one matching row exists.
     * @return List of rows matching the selection state.
     */
    private List<Row> getRows(List<Row> rows, Filter filter, boolean testing) {
        List<Row> list = new ArrayList<Row>();
        
        for (Row row : rows) {
            if (filter.matches(row)) {
                list.add(row);
                
                if (testing) {
                    break;
                }
            }
        }
        
        return list;
    }
    
    /**
     * Tests the selection state of a specified row.
     * 
     * @param row Row to test.
     * @param selected Desired selection state.
     * @return True if the row's selection state matched the desired selection state. If the row is
     *         not selectable (i.e., has no associated checkbox), this always returns false.
     */
    private boolean testRow(Row row, boolean selected) {
        return getCheckbox(row) != null && selected == isSelected(row);
    }
    
    /**
     * Adds a row to the end of the grid.
     * 
     * @return Newly created row.
     */
    public Row addRow() {
        return addRow(-1);
    }
    
    /**
     * Inserts a row at the specified row index.
     * 
     * @param index Row index for new row. If < 0, appends the row to the end of the grid.
     * @return Newly created row.
     */
    public Row addRow(int index) {
        return addRow(new Row(), index);
    }
    
    /**
     * Inserts the specified row at the specified row index.
     * 
     * @param row Row to insert. If there is no checkbox associated with the row, one is added.
     * @param index Row index for the row. If < 0, appends the row to the end of the grid.
     * @return The same as the row passed in.
     */
    public Row addRow(Row row, int index) {
        return addRow(row, index < 0 ? null : getRowAtIndex(index));
    }
    
    /**
     * Inserts the specified row before the reference row.
     * 
     * @param row Row to insert. If there is no checkbox associated with the row, one is added.
     * @param refRow The reference row.
     * @return The same as the row passed in.
     */
    public Row addRow(Row row, Row refRow) {
        if (getCheckbox(row) == null) {
            addCheckbox(row);
        }
        
        forceRows().insertBefore(row, refRow);
        updateState(row, true);
        return row;
    }
    
    /**
     * Ensures that getRows() will not return null.
     * 
     * @return Value of getRows().
     */
    private Rows forceRows() {
        if (getRows() == null) {
            appendChild(new Rows());
        }
        
        return getRows();
    }
    
    /**
     * Ensures that getColumns() will not return null.
     * 
     * @return Value of getColumns().
     */
    private Columns forceColumns() {
        if (getColumns() == null) {
            appendChild(new Columns());
        }
        
        return getColumns();
    }
    
    /**
     * Removes the row at the specified index.
     * 
     * @param index Index of row to remove.
     */
    public void removeRow(int index) {
        removeRow(getRowAtIndex(index));
    }
    
    /**
     * Removes the specified row.
     * 
     * @param row Row to remove.
     */
    public void removeRow(Row row) {
        if (getRows() != null && getRows().removeChild(row)) {
            Checkbox cb = getCheckbox(row);
            
            if (cb != null) {
                cb.setChecked(false);
                updateState(row, true);
            }
        }
    }
    
    /**
     * Returns the count of selected rows.
     * 
     * @return Count of selected rows.
     */
    public int getSelectedCount() {
        return selectedRows.size();
    }
    
    /**
     * Sets the select-on-click behavior. If true, clicking on a row is the same as clicking on its
     * checkbox. If false, one must click the checkbox to select the row.
     * 
     * @param selectOnClick The select-on-click setting.
     */
    public void setSelectOnClick(boolean selectOnClick) {
        this.selectOnClick = selectOnClick;
    }
    
    /**
     * Returns the select-on-click setting. If true, clicking on a row is the same as clicking on
     * its checkbox. If false, one must click the checkbox to select the row.
     * 
     * @return Select-on-click setting.
     */
    public boolean isSelectOnClick() {
        return selectOnClick;
    }
    
    /**
     * Returns a list of rows.
     * 
     * @return List of rows. May be null.
     */
    @SuppressWarnings("unchecked")
    public List<Row> getAllRows() {
        return getRows() == null ? null : (List<Row>) (List<?>) getRows().getChildren();
    }
    
    /**
     * Adds a row to the current selection.
     * 
     * @param row Row to add.
     */
    public void addRowToSelection(Row row) {
        setSelected(row, true);
    }
    
    /**
     * Removes a row from the current selection.
     * 
     * @param row Row to remove.
     */
    public void removeRowFromSelection(Row row) {
        setSelected(row, false);
    }
    
    /**
     * Returns the row located at the specified index.
     * 
     * @param index Index of row.
     * @return Row at the specified index.
     */
    public Row getRowAtIndex(int index) {
        return (Row) getRows().getChildren().get(index);
    }
    
    /**
     * Returns the number of rows.
     * 
     * @return Number of rows.
     */
    public int getRowCount() {
        return getRows() == null ? 0 : getRows().getChildren().size();
    }
    
    /**
     * Returns the selection state of the row at the specified index.
     * 
     * @param index Row index.
     * @return Selection state of the row.
     */
    public boolean isSelected(int index) {
        return isSelected(getRowAtIndex(index));
    }
    
    /**
     * Returns the selection state of the given row.
     * 
     * @param row Row whose selection state is sought.
     * @return Selection state of the given row.
     */
    public boolean isSelected(Row row) {
        return selectedRows.contains(row);
    }
    
    /**
     * Returns the checkbox associated with the row.
     * 
     * @param row Row whose checkbox is to be returned.
     * @return Checkbox associated with the row. May be null.
     */
    private Checkbox getCheckbox(Row row) {
        return (Checkbox) row.getAttribute("_cb");
    }
    
    /**
     * Returns the row associated with the checkbox.
     * 
     * @param cb Checkbox whose associated row is to be returned.
     * @return Row associated with the checkbox.
     */
    private Row getRow(Checkbox cb) {
        return (Row) cb.getAttribute("_row");
    }
    
    /**
     * Returns true if all items on current page are selected.
     * 
     * @return True if all items on page selected.
     */
    private boolean isPageSelected() {
        return getPageRows(new Filter(false, null), true).size() == 0;
    }
    
    /**
     * Returns true if all items on current page are disabled.
     * 
     * @return True if all items on page disabled.
     */
    private boolean isPageDisabled() {
        return getPageRows(new Filter(null, false), true).size() == 0;
    }
    
    /**
     * Returns true if paging mode is active.
     * 
     * @return True if paging mode active.
     */
    private boolean isPagingActive() {
        return getPagingChild() != null;
    }
    
    /**
     * Returns true if the grid is disabled.
     * 
     * @return True if the grid is disabled.
     */
    public boolean isDisabled() {
        return disabled;
    }
    
    /**
     * Returns true if the specified row is disabled.
     * 
     * @param row The row to test.
     * @return True if the specified row is disabled.
     */
    public boolean isDisabled(Row row) {
        Checkbox cb;
        
        if ((cb = getCheckbox(row)) == null) {
            return false;
        }
        
        return cb.getAttribute(DISABLED_ATTR) != null;
    }
    
    /**
     * Sets the disabled state of the grid. Disabling the grid disables all checkboxes within the
     * grid.
     * 
     * @param value The disabled state.
     */
    public void setDisabled(boolean value) {
        if (value != disabled) {
            disabled = value;
            
            if (chkHeader != null) {
                chkHeader.setDisabled(disabled);
            }
            
            for (Row row : getAllRows()) {
                Checkbox chk = getCheckbox(row);
                
                if (chk != null && !isDisabled(row)) {
                    chk.setDisabled(disabled);
                }
            }
        }
    }
    
    /**
     * Sets the disabled state of the specified row.
     * 
     * @param row The row to test.
     * @param value The disabled state.
     */
    public void setDisabled(Row row, boolean value) {
        Checkbox cb = getCheckbox(row);
        
        if (cb != null) {
            cb.setDisabled(value || disabled);
            
            if (value) {
                cb.setAttribute(DISABLED_ATTR, true);
            } else {
                cb.removeAttribute(DISABLED_ATTR);
            }
            
            updateState(row, false);
        }
    }
    
    /**
     * Performs any special initializations prior to rendering. Currently locates the column that
     * will contain the checkboxes. This is determined by searching for the first column that has a
     * style class of cwf-checkColumn. If one is not found, assumes the first column. The
     * checkColumnIndex variable reflects the index of this column.
     */
    @SuppressWarnings("rawtypes")
    /*package*/void init() {
        if (!initialized) {
            initialized = true;
            List cols = forceColumns().getChildren();
            Column checkColumn = null;
            checkColumnIndex = -1;
            
            for (int i = 0; i < cols.size(); i++) {
                checkColumn = (Column) cols.get(i);
                
                if (checkColumn.getSclass() != null && checkColumn.getSclass().contains("cwf-checkColumn")) {
                    checkColumnIndex = i;
                    break;
                }
                
            }
            
            if (checkColumnIndex == -1 && cols.size() > 0) {
                checkColumn = (Column) cols.get(0);
                checkColumnIndex = 0;
            }
            
            if (checkColumn != null) {
                chkHeader = new Checkbox();
                checkColumn.insertBefore(chkHeader, checkColumn.getFirstChild());
                chkHeader.addForward(Events.ON_CHECK, this, null);
            }
        }
    }
    
    /**
     * onCheck events are generated by individual check boxes.
     * 
     * @param event The on check event.
     */
    public void onCheck(Event event) {
        event = ZKUtil.getEventOrigin(event);
        Checkbox chk = (Checkbox) event.getTarget();
        
        if (chk == chkHeader) {
            selectAllPage(chk.isChecked());
        } else if (!selectOnClick) {
            updateState(getRow(chk), true);
        }
    }
    
    /**
     * Process a row click event, toggling the row selection if selectOnClick is enabled.
     * 
     * @param event The row click event.
     */
    public void onRowClick(Event event) {
        if (selectOnClick) {
            event = ZKUtil.getEventOrigin(event);
            Row row = (Row) event.getTarget();
            setSelected(row, !isSelected(row));
        }
    }
    
    /**
     * Process a row double click event, selecting the target row.
     * 
     * @param event The row double click event.
     */
    public void onRowDoubleClick(Event event) {
        if (selectOnClick) {
            event = ZKUtil.getEventOrigin(event);
            Row row = (Row) event.getTarget();
            setSelected(row, true);
        }
    }
    
    /**
     * Remove all rows.
     */
    public void clear() {
        if (getRowCount() > 0) {
            getAllRows().clear();
        }
        
        selectedRows.clear();
        chkHeader.setChecked(false);
    }
}
