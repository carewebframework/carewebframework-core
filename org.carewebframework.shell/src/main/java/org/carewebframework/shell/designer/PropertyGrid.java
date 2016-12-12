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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.carewebframework.common.StrUtil;
import org.carewebframework.shell.layout.UIElementBase;
import org.carewebframework.shell.layout.UIException;
import org.carewebframework.shell.plugins.PluginDefinition;
import org.carewebframework.shell.property.PropertyInfo;
import org.carewebframework.shell.property.PropertyType;
import org.carewebframework.ui.core.CWFUtil;
import org.carewebframework.web.ancillary.IAutoWired;
import org.carewebframework.web.ancillary.INamespace;
import org.carewebframework.web.annotation.EventHandler;
import org.carewebframework.web.annotation.WiredComponent;
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
import org.carewebframework.web.event.ChangeEvent;
import org.carewebframework.web.event.ClickEvent;
import org.carewebframework.web.event.Event;
import org.carewebframework.web.event.EventUtil;
import org.carewebframework.web.model.IComponentRenderer;
import org.carewebframework.web.model.IListModel;
import org.carewebframework.web.model.IModelAndView;
import org.carewebframework.web.model.ListModel;
import org.carewebframework.web.page.PageUtil;

/**
 * Dialog for managing property values of UI elements within the designer. Each editable property of
 * the target UI element is presented as a row in the grid with its associated property editor.
 */
public class PropertyGrid implements IAutoWired {
    
    private static final Log log = LogFactory.getLog(PropertyGrid.class);
    
    private static final String EDITOR_ATTR = "@editor";
    
    private static class RowEx extends Row implements INamespace {};
    
    @SuppressWarnings("rawtypes")
    private final IComponentRenderer<Row, PropertyEditorBase> rowRenderer = new IComponentRenderer<Row, PropertyEditorBase>() {
        
        @Override
        public Row render(PropertyEditorBase editor) {
            Row row = new RowEx();
            row.setData(editor);
            BaseComponent cmpt = editor.getEditor();
            PropertyInfo propInfo = editor.getPropInfo();
            Cell cell = new Cell();
            row.addChild(cell);
            cell.addEventForward(ClickEvent.TYPE, window, ChangeEvent.TYPE);
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
            return row;
        }
        
    };
    
    @WiredComponent
    private Table gridProperties;
    
    @WiredComponent
    private Label lblPropertyInfo;
    
    @WiredComponent
    private Column colProperty;
    
    @WiredComponent
    private Button btnOK;
    
    @WiredComponent
    private Button btnCancel;
    
    @WiredComponent
    private Button btnApply;
    
    @WiredComponent
    private Button btnRestore;
    
    @WiredComponent
    private BaseUIComponent toolbar;
    
    @SuppressWarnings("rawtypes")
    private final IListModel<PropertyEditorBase> model = new ListModel<>();
    
    private UIElementBase target;
    
    private Row selectedRow;
    
    private boolean pendingChanges;
    
    private boolean propertiesModified;
    
    private boolean embedded;
    
    private Window window;
    
    /**
     * Creates a property grid for the given target UI element.
     * 
     * @param target UI element whose properties are to be edited.
     * @param parent Parent component for property grid (may be null).
     * @return Newly created PropertyGrid instance.
     */
    public static PropertyGrid create(UIElementBase target, BaseComponent parent) {
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
        Map<String, Object> args = new HashMap<>();
        args.put("target", target);
        args.put("embedded", embedded);
        Window window = (Window) PageUtil.createPage(DesignConstants.RESOURCE_PREFIX + "propertyGrid.cwf", parent, args)
                .get(0);
        
        if (parent == null) {
            window.modal(null);
        }
        
        return window.getAttribute("controller", PropertyGrid.class);
    }
    
    /**
     * Initializes the property grid.
     * 
     * @param target UI element whose properties are to be edited.
     * @param parent Parent component for property grid (may be null).
     * @param embedded If true, the property grid is embedded within another component.
     */
    @Override
    public void afterInitialized(BaseComponent comp) {
        window = (Window) comp;
        @SuppressWarnings("rawtypes")
        IModelAndView<Row, PropertyEditorBase> mv = gridProperties.getRows().getModelAndView(PropertyEditorBase.class);
        mv.setRenderer(rowRenderer);
        mv.setModel(model);
        comp.setAttribute("controller", this);
        this.embedded = comp.getAttribute("embedded", false);
        setTarget(comp.getAttribute("target", UIElementBase.class));
        
        if (window.getParent() != null) {
            window.setClosable(false);
            window.setWidth("100%");
            window.setHeight("100%");
            window.setSizable(false);
            window.addClass("cwf-propertygrid-embedded");
            toolbar.setVisible(embedded);
        }
        
        btnOK.setVisible(!embedded);
        btnCancel.setVisible(!embedded);
    }
    
    public Window getWindow() {
        return window;
    }
    
    public void capture(String eventType, BaseComponent target) {
        target.addEventForward(eventType, gridProperties, "updated");
    }
    
    /**
     * Sets the target UI element for the property grid. Iterates throw the target's property
     * definitions and presents a row for each editable property.
     * 
     * @param target UI element whose properties are to be edited.
     */
    public void setTarget(UIElementBase target) {
        this.target = target;
        gridProperties.getRows().destroyChildren();
        
        if (target == null) {
            ((BaseUIComponent) window.getFirstChild()).setVisible(false);
            window.setTitle(StrUtil.formatMessage("@cwf.shell.designer.property.grid.noselection"));
            disableButtons(true);
            return;
        }
        
        ((BaseUIComponent) window.getFirstChild()).setVisible(true);
        window.setTitle(StrUtil.formatMessage("@cwf.shell.designer.property.grid.title", target.getDisplayName()));
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
    protected PropertyEditorBase<?> addPropertyEditor(PropertyInfo propInfo, boolean append) {
        PropertyEditorBase<?> editor = null;
        
        try {
            PropertyType type = propInfo.getPropertyType();
            
            if (type == null) {
                throw new UIException("Unknown property type: " + propInfo.getType());
            }
            
            Class<? extends PropertyEditorBase<?>> editorClass = type.getEditorClass();
            
            if (editorClass != null && propInfo.isEditable()) {
                editor = editorClass.newInstance();
                editor.init(target, propInfo, this);
            }
        } catch (Exception e) {
            log.error("Error creating editor for property '" + propInfo.getName() + "'.", e);
        }
        
        if (editor != null) {
            model.add(append ? model.size() : 0, editor);
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
                PropertyEditorBase<?> editor = (PropertyEditorBase<?>) row.getAttribute(EDITOR_ATTR);
                
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
    protected PropertyEditorBase<?> findEditor(String propName) {
        for (Object child : gridProperties.getRows().getChildren()) {
            if (child instanceof Row) {
                Row row = (Row) child;
                PropertyEditorBase<?> editor = (PropertyEditorBase<?>) row.getAttribute(EDITOR_ATTR);
                
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
    @EventHandler(value = "click", target = "@btnCancel")
    private void onClick$btnCancel() {
        if (embedded) {
            commitChanges(false);
        } else {
            window.close();
        }
    }
    
    /**
     * Clicking the apply button commits any pending edits.
     */
    @EventHandler(value = "click", target = "@btnApply")
    private void onClick$btnApply() {
        commitChanges(true);
    }
    
    /**
     * Clicking the restore button cancels any pending edits.
     */
    @EventHandler(value = "click", target = "@btnRestore")
    private void onClick$btnRestore() {
        commitChanges(false);
    }
    
    /**
     * Clicking the OK button commits any pending edits and closes the dialog.
     */
    @EventHandler(value = "click", target = "@btnOK")
    private void onClick$btnOK() {
        if (commitChanges(true)) {
            window.close();
        }
    }
    
    /**
     * Displays the description information for a property.
     * 
     * @param propertyName Property name.
     * @param propertyDescription Property description.
     */
    private void setPropertyDescription(String propertyName, String propertyDescription) {
        //capPropertyName.setLabel(StrUtil.formatMessage(propertyName));
        lblPropertyInfo.setLabel(StrUtil.formatMessage(propertyDescription));
    }
    
    /**
     * Handles the selection of a row in the property grid.
     * 
     * @param event Row selection event.
     */
    @EventHandler(value = "change", target = "rowProperties")
    private void onChange(ChangeEvent event) {
        if (event.getValue(Boolean.class)) {
            Rows rows = gridProperties.getRows();
            selectedRow = rows.getSelectedCount() == 0 ? null : rows.getSelected().get(0);
            PropertyEditorBase<?> editor = selectedRow == null ? null
                    : (PropertyEditorBase<?>) (selectedRow.getAttribute(EDITOR_ATTR));
            PropertyInfo propInfo = editor == null ? null : editor.getPropInfo();
            setPropertyDescription(
                propInfo == null ? "@cwf.shell.designer.property.grid.propdx.some.caption" : propInfo.getName(),
                propInfo == null ? " " : propInfo.getDescription());
            
            if (editor != null) {
                editor.setFocus();
            }
        }
    }
    
    /**
     * Handles change events from property editors.
     * 
     * @param event Change event.
     */
    @EventHandler(value = { "updated" }, target = "gridProperties")
    private void onChange(Event event) {
        changed((BaseUIComponent) event.getTarget());
    }
    
    public void changed(BaseUIComponent comp) {
        comp.setBalloon(null);
        disableButtons(false);
    }
}
