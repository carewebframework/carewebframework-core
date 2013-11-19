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

import org.apache.commons.lang.StringUtils;

import org.carewebframework.common.StrUtil;
import org.carewebframework.shell.layout.LayoutUtil;
import org.carewebframework.shell.layout.UIElementBase;
import org.carewebframework.shell.layout.UIElementLayout;
import org.carewebframework.shell.layout.UIElementPlugin;
import org.carewebframework.shell.plugins.PluginDefinition;
import org.carewebframework.shell.plugins.PluginRegistry;
import org.carewebframework.ui.zk.PopupDialog;
import org.carewebframework.ui.zk.TreeUtil;
import org.carewebframework.ui.zk.ZKUtil;

import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Button;
import org.zkoss.zul.Tree;
import org.zkoss.zul.Treeitem;
import org.zkoss.zul.Window;

/**
 * Dialog for adding new component to UI.
 */
public class AddComponent extends Window {
    
    private static final long serialVersionUID = 1L;
    
    private UIElementBase parentElement;
    
    private UIElementBase childElement;
    
    private PluginDefinition definition;
    
    private Tree tree;
    
    private Button btnOK;
    
    private boolean createChild;
    
    /**
     * Display the add component dialog, presenting a list of candidate plugins that may serve as
     * children to the specified parent element.
     * 
     * @param parentElement Element to serve as parent to the newly created child element.
     * @return The newly created child element, or null if the dialog was cancelled.
     */
    public static UIElementBase newChild(UIElementBase parentElement) {
        return execute(parentElement, true).childElement;
    }
    
    /**
     * Display the add component dialog, presenting a list of candidate plugins that may serve as
     * children to the specified parent element.
     * 
     * @param parentElement Element to serve as parent to the newly created child element.
     * @return The selected plugin definition, or null if the dialog was cancelled.
     */
    public static PluginDefinition getDefinition(UIElementBase parentElement) {
        return execute(parentElement, false).definition;
    }
    
    /**
     * Display the add component dialog, presenting a list of candidate plugins that may serve as
     * children to the specified parent element.
     * 
     * @param parentElement Element to serve as parent to the newly created child element.
     * @param createChild If true, the selected element will be created.
     * @return The dialog instance that was created.
     */
    private static AddComponent execute(UIElementBase parentElement, boolean createChild) {
        AddComponent dlg = null;
        
        try {
            dlg = (AddComponent) PopupDialog.popup(
                ZKUtil.loadCachedPageDefinition(DesignConstants.RESOURCE_PREFIX + "AddComponent.zul"), null, true, true,
                false);
            dlg.init(parentElement, createChild);
            dlg.doModal();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        
        return dlg;
    }
    
    /**
     * Initialize the tree view based with list of plugins that may serve as children to the parent
     * element.
     * 
     * @param parentElement Element to serve as parent to the newly created child element.
     * @param createChild If true, the selected element will be created.
     */
    private void init(UIElementBase parentElement, boolean createChild) {
        this.parentElement = parentElement;
        this.createChild = createChild;
        ZKUtil.wireController(this);
        Treeitem defaultItem = null;
        boolean useDefault = true;
        
        for (PluginDefinition def : PluginRegistry.getInstance()) {
            if (def.isInternal()) {
                continue;
            }
            
            Class<? extends UIElementBase> clazz = def.getClazz();
            
            if (!parentElement.canAcceptChild(clazz) || !UIElementBase.canAcceptParent(clazz, parentElement.getClass())) {
                continue;
            }
            
            Treeitem item = addTreeitem(def);
            
            if (item == null) {
                continue;
            }
            
            item.setDisabled(def.isDisabled() || def.isForbidden());
            
            if (!item.isDisabled()) {
                if (defaultItem == null) {
                    defaultItem = item;
                } else {
                    useDefault = false;
                }
            }
        }
        
        if (parentElement.canAcceptChild(UIElementLayout.class)) {
            addLayouts(true);
            addLayouts(false);
        }
        
        TreeUtil.sort(tree);
        
        if (useDefault && defaultItem != null) {
            defaultItem.setSelected(true);
            onSelect$tree();
        }
        
        setTitle(StrUtil.formatMessage("@cwf.shell.designer.add.component.title", parentElement.getDefinition().getName()));
    }
    
    private void addLayouts(boolean shared) {
        for (String layout : LayoutUtil.getLayouts(shared)) {
            UIElementLayout ele = new UIElementLayout(layout, shared);
            addTreeitem(ele.getDefinition());
        }
    }
    
    private Treeitem addTreeitem(PluginDefinition def) {
        String category = def.getCategory();
        
        if (StringUtils.isEmpty(category)) {
            if (UIElementPlugin.class.isAssignableFrom(def.getClazz())) {
                category = Labels.getLabel("cwf.shell.plugin.category._default");
            } else {
                return null;
            }
        }
        
        Treeitem item = TreeUtil.findNode(tree, category + (category.endsWith("\\") ? "" : "\\") + def.getName(), true);
        item.setValue(def);
        item.setTooltiptext(def.getDescription());
        item.getFirstChild().addForward(Events.ON_DOUBLE_CLICK, btnOK, Events.ON_CLICK);
        return item;
    }
    
    /**
     * Returns currently selected plugin definition, or null if none selected.
     * 
     * @return
     */
    private PluginDefinition selectedPluginDefinition() {
        return (PluginDefinition) (tree.getSelectedItem() == null ? null : tree.getSelectedItem().getValue());
    }
    
    /**
     * Close dialog without further action.
     */
    public void onClick$btnCancel() {
        detach();
    }
    
    /**
     * Create new element based on selected plugin definition, add it to the parent element and
     * close the dialog.
     * 
     * @throws Exception
     */
    public void onClick$btnOK() throws Exception {
        definition = selectedPluginDefinition();
        
        if (definition != null) {
            childElement = createChild ? definition.createElement(parentElement, null) : null;
            
            if (childElement != null) {
                childElement.bringToFront();
            }
            
            detach();
        }
    }
    
    /**
     * Update buttons based on current selection.
     */
    public void onSelect$tree() {
        btnOK.setDisabled(selectedPluginDefinition() == null);
    }
}
