/*
 * #%L
 * carewebframework
 * %%
 * Copyright (C) 2008 - 2016 Regenstrief Institute, Inc.
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related
 * Additional Disclaimer of Warranty and Limitation of Liability available at
 *
 *      http://www.carewebframework.org/licensing/disclaimer.
 *
 * #L%
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
import org.carewebframework.ui.core.CWFUtil;
import org.carewebframework.ui.dialog.DialogUtil;
import org.carewebframework.ui.zk.ZKUtil;
import org.carewebframework.web.ancillary.INamespace;
import org.carewebframework.web.component.BaseComponent;
import org.carewebframework.web.component.BaseUIComponent;
import org.carewebframework.web.component.Button;
import org.carewebframework.web.component.Cell;
import org.carewebframework.web.component.Column;
import org.carewebframework.web.component.Label;
import org.carewebframework.web.component.Row;
import org.carewebframework.web.component.Rows;
import org.carewebframework.web.component.Table;
import org.carewebframework.web.component.Window;
import org.carewebframework.web.event.ClickEvent;
import org.carewebframework.web.event.Event;
import org.carewebframework.web.event.EventUtil;
import org.carewebframework.web.event.SelectEvent;

/**
 * Dialog for managing property values of UI elements within the designer. Each editable property of
 * the target UI element is presented as a row in the grid with its associated property editor.
 */
public class PropertyGrid extends Window {
    
    private static final Log log = LogFactory.getLog(PropertyGrid.class);
    
    private static final String EDITOR_ATTR = "@editor";
    
    private static final String LABEL_ATTR = "@label";
    
    private static class RowEx extends Row implements INamespace {};
    
    /**
     * Used to sort the properties column alphabetically. We store the property name in an attribute
     * on the corresponding row to make it simpler.
     */
    private static final Comparator<Row> propSort = new Comparator<Row>() {
        
        @Override
        public int compare(Row r1, Row r2) {
            String label1 = (String) r1.getAttribute(LABEL_ATTR);
            String label2 = (String) r2.getAttribute(LABEL_ATTR);
            return label1.compareToIgnoreCase(label2);
        }
        
    };
    
    private Table gridProperties;
    
    private UIElementBase target;
    
    private Label lblPropertyInfo;
    
    private Column colProperty;
    
    private Label capPropertyName;
    
    private Button btnOK;
    
    private Button btnCancel;
    
    private Button btnApply;
    
    private Button btnRestore;
    
    private BaseUIComponent toolbar;
    
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
    public static PropertyGrid create(UIElementBase target, BaseComponent parent) throws Exception {
        return create(target, parent, false);
    }
    
    /**
     * Creates a property grid for the given target UI element.
     * 
     * @param target UI element whose properties are to be edited.
     * @param parent Parent component for property grid (may be null).
     * @param embedded If true, the property grid is embedded within another component.
     * @return Newly created PropertyGrid instance.
     */
    public static PropertyGrid create(UIElementBase target, BaseComponent parent, boolean embedded) {
        PropertyGrid propertyGrid = (PropertyGrid) DialogUtil.popup(DesignConstants.RESOURCE_PREFIX + "PropertyGrid.cwf",
            !embedded, true, false);
        propertyGrid.init(target, parent, embedded);
        
        if (parent == null) {
            propertyGrid.setMode(Mode.MODAL);
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
    private void init(UIElementBase target, BaseComponent parent, boolean embedded) {
        this.embedded = embedded;
        wireController(this);
        setTarget(target);
        colProperty.setSortComparator(propSort);
        
        if (parent != null) {
            setClosable(false);
            setWidth("100%");
            setHeight("100%");
            setSizable(false);
            addClass("panel-primary cwf-propertygrid-embedded");
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
            ((BaseUIComponent) getFirstChild()).setVisible(false);
            setTitle(StrUtil.formatMessage("@cwf.shell.designer.property.grid.noselection"));
            disableButtons(true);
            return;
        }
        
        ((BaseUIComponent) getFirstChild()).setVisible(true);
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
            PropertyType type = propInfo.getPropertyType();
            
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
            BaseComponent cmpt = editor.getComponent();
            Row row = new RowEx();
            row.setAttribute(LABEL_ATTR, propInfo.getName());
            Rows rows = gridProperties.getRows();
            rows.addChild(row, append ? null : rows.getFirstChild());
            Cell cell = new Cell();
            row.addChild(cell);
            cell.addEventForward(ClickEvent.TYPE, this, SelectEvent.TYPE);
            Label lbl = new Label(propInfo.getName());
            cell.addChild(lbl);
            row.setAttribute(EDITOR_ATTR, editor);
            
            try {
                editor.setValue(propInfo.getPropertyValue(target));
            } catch (Exception e) {
                lbl = new Label(CWFUtil.formatExceptionForDisplay(e));
                lbl.setHint(lbl.getLabel());
                cmpt = lbl;
            }
            
            row.addChild(cmpt);
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
            EventUtil.post(new LayoutChangedEvent(null, target));
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
            close();
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
            close();
        }
    }
    
    /**
     * Overrides the onClose method to prevent dialog closure when in embedded mode.
     */
    @Override
    public void close() {
        if (embedded) {
            setVisible(false);
        } else if (getMode() == Mode.MODAL) {
            super.close();
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
        lblPropertyInfo.setLabel(StrUtil.formatMessage(propertyDescription));
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
            selectedRow.removeClass("cwf-propertygrid-selectedrow");
        }
        
        selectedRow = row;
        PropertyEditorBase editor = selectedRow == null ? null
                : (PropertyEditorBase) (selectedRow.getAttribute(EDITOR_ATTR));
        PropertyInfo propInfo = editor == null ? null : editor.getPropInfo();
        setPropertyDescription(
            propInfo == null ? "@cwf.shell.designer.property.grid.propdx.some.caption" : propInfo.getName(),
            propInfo == null ? " " : propInfo.getDescription());
        
        if (selectedRow != null) {
            selectedRow.addClass("cwf-propertygrid-selectedrow");
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
        setSelectedRow(event.getTarget().getAncestor(Row.class));
    }
    
    /**
     * Handles change events from property editors.
     * 
     * @param event Change event.
     */
    public void onChange(Event event) {
        ((BaseUIComponent) event.getTarget()).setBalloon(null);
        disableButtons(false);
    }
}
