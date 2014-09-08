/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.shell.designer;

import java.util.Comparator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.carewebframework.common.StrUtil;
import org.carewebframework.shell.layout.UIElementBase;
import org.carewebframework.shell.layout.UIException;
import org.carewebframework.shell.plugins.PluginDefinition;
import org.carewebframework.shell.property.PropertyInfo;
import org.carewebframework.shell.property.PropertyType;
import org.carewebframework.ui.zk.PopupDialog;
import org.carewebframework.ui.zk.ZKUtil;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.IdSpace;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.metainfo.PageDefinition;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Button;
import org.zkoss.zul.Caption;
import org.zkoss.zul.Cell;
import org.zkoss.zul.Column;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Label;
import org.zkoss.zul.Row;
import org.zkoss.zul.Rows;
import org.zkoss.zul.Toolbar;
import org.zkoss.zul.Window;

/**
 * Dialog for managing property values of UI elements within the designer. Each editable property of
 * the target UI element is presented as a row in the grid with its associated property editor.
 */
public class PropertyGrid extends Window {
    
    private static final Log log = LogFactory.getLog(PropertyGrid.class);
    
    private static final long serialVersionUID = 1L;
    
    private static final String EDITOR_ATTR = "@editor";
    
    private static final String LABEL_ATTR = "@label";
    
    /**
     * Used to sort the properties column alphabetically. We store the property name in an attribute
     * on the corresponding row to make it simpler.
     */
    private static class PropertySorter implements Comparator<Row> {
        
        private final boolean ascending;
        
        private PropertySorter(boolean ascending) {
            this.ascending = ascending;
        }
        
        @Override
        public int compare(Row r1, Row r2) {
            String label1 = (String) r1.getAttribute(LABEL_ATTR);
            String label2 = (String) r2.getAttribute(LABEL_ATTR);
            int cmp = label1.compareToIgnoreCase(label2);
            return ascending ? cmp : -cmp;
        }
        
    }
    
    @SuppressWarnings("serial")
    private static class RowEx extends Row implements IdSpace {};
    
    private static final PropertySorter propSortAscending = new PropertySorter(true);
    
    private static final PropertySorter propSortDescending = new PropertySorter(false);
    
    private Grid gridProperties;
    
    private UIElementBase target;
    
    private Label lblPropertyInfo;
    
    private Column colProperty;
    
    private Caption capPropertyName;
    
    private Button btnOK;
    
    private Button btnCancel;
    
    private Button btnApply;
    
    private Button btnRestore;
    
    private Toolbar toolbar;
    
    private Row selectedRow;
    
    private boolean pendingChanges;
    
    private boolean propertiesModified;
    
    private boolean embedded;
    
    /**
     * Creates a property grid for the given target UI element.
     * 
     * @param target UI element whose properties are to be edited.
     * @param parent Parent component for property grid (may be null).
     * @return Newly created PropertyGrid instance.
     * @throws Exception Unspecified exception.
     */
    public static PropertyGrid create(UIElementBase target, Component parent) throws Exception {
        return create(target, parent, false);
    }
    
    /**
     * Creates a property grid for the given target UI element.
     * 
     * @param target UI element whose properties are to be edited.
     * @param parent Parent component for property grid (may be null).
     * @param embedded If true, the property grid is embedded within another component.
     * @return Newly created PropertyGrid instance.
     * @throws Exception Unspecified exception.
     */
    public static PropertyGrid create(UIElementBase target, Component parent, boolean embedded) throws Exception {
        PageDefinition def = ZKUtil.loadCachedPageDefinition(DesignConstants.RESOURCE_PREFIX + "PropertyGrid.zul");
        PropertyGrid propertyGrid = (PropertyGrid) PopupDialog.popup(def, null, false, true, false);
        propertyGrid.init(target, parent, embedded);
        
        if (parent == null) {
            propertyGrid.doModal();
        }
        
        return propertyGrid;
    }
    
    /**
     * Initializes the property grid.
     * 
     * @param target UI element whose properties are to be edited.
     * @param parent Parent component for property grid (may be null).
     * @param embedded If true, the property grid is embedded within another component.
     */
    private void init(UIElementBase target, Component parent, boolean embedded) {
        this.embedded = embedded;
        ZKUtil.wireController(this);
        setTarget(target);
        colProperty.setSortAscending(propSortAscending);
        colProperty.setSortDescending(propSortDescending);
        
        if (parent != null) {
            setWidth("100%");
            setHeight("100%");
            setBorder("none");
            setContentStyle("border: solid 1px gray");
            setSizable(false);
            toolbar.setVisible(embedded);
            setParent(parent);
        }
        
        btnOK.setVisible(!embedded);
        btnCancel.setVisible(!embedded);
    }
    
    /**
     * Sets the target UI element for the property grid. Iterates throw the target's property
     * definitions and presents a row for each editable property.
     * 
     * @param target UI element whose properties are to be edited.
     */
    public void setTarget(UIElementBase target) {
        this.target = target;
        ZKUtil.detachChildren(gridProperties.getRows());
        
        if (target == null) {
            setVisible(false);
            disableButtons(true);
            return;
        }
        
        setVisible(true);
        setTitle(StrUtil.formatMessage("@cwf.shell.designer.property.grid.title", target.getDisplayName()));
        PluginDefinition def = target.getDefinition();
        List<PropertyInfo> props = def.getProperties();
        
        if (props != null && props.size() > 0) {
            for (PropertyInfo prop : props) {
                addPropertyEditor(prop, true);
            }
            
            gridProperties.setVisible(true);
            setPropertyDescription("@cwf.shell.designer.property.grid.propdx.some.caption",
                "@cwf.shell.designer.property.grid.propdx.some.message");
        } else {
            gridProperties.setVisible(false);
            setPropertyDescription("@cwf.shell.designer.property.grid.propdx.none.caption",
                "@cwf.shell.designer.property.grid.propdx.none.message");
        }
        
        disableButtons(true);
    }
    
    /**
     * Adds a property editor to the grid for the specified property definition.
     * 
     * @param propInfo Property definition information.
     * @param append If true, the property editor is appended to the end of the grid. Otherwise, it
     *            is inserted at the beginning.
     * @return The newly added property editor.
     */
    protected PropertyEditorBase addPropertyEditor(PropertyInfo propInfo, boolean append) {
        PropertyEditorBase editor = null;
        
        try {
            PropertyType type = propInfo.getTypeInfo();
            
            if (type == null) {
                throw new UIException("Unknown property type: " + propInfo.getType());
            }
            
            Class<? extends PropertyEditorBase> editorClass = type.getEditorClass();
            
            if (editorClass != null && propInfo.isEditable()) {
                editor = editorClass.newInstance();
                editor.init(target, propInfo, this);
            }
        } catch (Exception e) {
            log.error("Error creating editor for property '" + propInfo.getName() + "'.", e);
        }
        
        if (editor != null) {
            Component cmpt = editor.getComponent();
            Row row = new RowEx();
            row.setAttribute(LABEL_ATTR, propInfo.getName());
            Rows rows = gridProperties.getRows();
            
            if (append) {
                rows.appendChild(row);
            } else {
                rows.insertBefore(row, rows.getFirstChild());
            }
            
            Cell cell = new Cell();
            row.appendChild(cell);
            cell.addForward(Events.ON_CLICK, this, Events.ON_SELECT);
            Label lbl = new Label(propInfo.getName());
            cell.appendChild(lbl);
            row.setAttribute(EDITOR_ATTR, editor);
            
            try {
                editor.setValue(propInfo.getPropertyValue(target));
            } catch (Exception e) {
                lbl = new Label(ZKUtil.formatExceptionForDisplay(e));
                lbl.setTooltiptext(lbl.getValue());
                cmpt = lbl;
            }
            
            row.appendChild(cmpt);
        }
        
        return editor;
    }
    
    /**
     * Commit or revert all pending changes to the target object.
     * 
     * @param commit True to commit. False to revert.
     * @return True if all operations completed successfully.
     */
    protected boolean commitChanges(boolean commit) {
        boolean result = true;
        
        for (Object child : gridProperties.getRows().getChildren()) {
            if (child instanceof Row) {
                Row row = (Row) child;
                PropertyEditorBase editor = (PropertyEditorBase) row.getAttribute(EDITOR_ATTR);
                
                if (editor != null && editor.hasChanged()) {
                    if (commit) {
                        result &= editor.commit();
                    } else {
                        result &= editor.revert();
                    }
                }
            }
        }
        
        disableButtons(result);
        
        if (commit) {
            Events.postEvent(new LayoutChangedEvent(null, target));
        }
        
        return result;
    }
    
    /**
     * Returns the editor associated with the named property, or null if none found.
     * 
     * @param propName The property name.
     * @return The associated property editor (may be null).
     */
    protected PropertyEditorBase findEditor(String propName) {
        for (Object child : gridProperties.getRows().getChildren()) {
            if (child instanceof Row) {
                Row row = (Row) child;
                PropertyEditorBase editor = (PropertyEditorBase) row.getAttribute(EDITOR_ATTR);
                
                if (editor != null && editor.getPropInfo().getId().equals(propName)) {
                    return editor;
                }
            }
        }
        
        return null;
    }
    
    /**
     * Change button enable states to reflect whether or not pending changes exist.
     * 
     * @param disable The disable status.
     */
    private void disableButtons(boolean disable) {
        btnRestore.setDisabled(disable);
        btnApply.setDisabled(disable);
        pendingChanges = !disable;
        propertiesModified |= pendingChanges;
    }
    
    /**
     * Returns true if there are uncommitted edits.
     * 
     * @return True if there are uncommitted edits.
     */
    public boolean hasPendingChanges() {
        return pendingChanges;
    }
    
    public boolean getPropertiesModified() {
        return propertiesModified;
    }
    
    /**
     * Returns the target UI element.
     * 
     * @return The target UI element.
     */
    public UIElementBase getTarget() {
        return target;
    }
    
    /**
     * Clicking the cancel button cancels any pending edits and, if embedded mode is not active,
     * closes the dialog.
     */
    public void onClick$btnCancel() {
        if (embedded) {
            commitChanges(false);
        } else {
            onClose();
        }
    }
    
    /**
     * Clicking the apply button commits any pending edits.
     */
    public void onClick$btnApply() {
        commitChanges(true);
    }
    
    /**
     * Clicking the restore button cancels any pending edits.
     */
    public void onClick$btnRestore() {
        commitChanges(false);
    }
    
    /**
     * Clicking the OK button commits any pending edits and closes the dialog.
     */
    public void onClick$btnOK() {
        if (commitChanges(true)) {
            onClose();
        }
    }
    
    /**
     * Overrides the onClose method to prevent dialog closure when in embedded mode.
     */
    @Override
    public void onClose() {
        if (embedded) {
            setVisible(false);
        } else {
            super.onClose();
        }
    }
    
    /**
     * Displays the description information for a property.
     * 
     * @param propertyName Property name.
     * @param propertyDescription Property description.
     */
    private void setPropertyDescription(String propertyName, String propertyDescription) {
        capPropertyName.setLabel(StrUtil.formatMessage(propertyName));
        lblPropertyInfo.setValue(StrUtil.formatMessage(propertyDescription));
    }
    
    /**
     * Sets the selected row.
     * 
     * @param row The row to select.
     */
    private void setSelectedRow(Row row) {
        if (row == selectedRow) {
            return;
        }
        
        if (selectedRow != null) {
            selectedRow.setSclass(null);
        }
        
        selectedRow = row;
        PropertyEditorBase editor = selectedRow == null ? null
                : (PropertyEditorBase) (selectedRow.getAttribute(EDITOR_ATTR));
        PropertyInfo propInfo = editor == null ? null : editor.getPropInfo();
        setPropertyDescription(
            propInfo == null ? "@cwf.shell.designer.property.grid.propdx.some.caption" : propInfo.getName(),
            propInfo == null ? " " : propInfo.getDescription());
        
        if (selectedRow != null) {
            selectedRow.setSclass("cwf-propertygrid-selectedrow");
        }
        
        if (editor != null) {
            editor.setFocus();
        }
        
    }
    
    /**
     * Handles the selection of a row in the property grid.
     * 
     * @param event Row selection event.
     */
    public void onSelect(Event event) {
        event = ZKUtil.getEventOrigin(event);
        setSelectedRow(ZKUtil.findAncestor(event.getTarget(), Row.class));
    }
    
    /**
     * Handles change events from property editors.
     * 
     * @param event Change event.
     */
    public void onChange(Event event) {
        event = ZKUtil.getEventOrigin(event);
        Clients.clearWrongValue(event.getTarget());
        disableButtons(false);
    }
}
