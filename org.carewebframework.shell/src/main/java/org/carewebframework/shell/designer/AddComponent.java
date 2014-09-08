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

import java.util.List;

import org.apache.commons.lang.StringUtils;

import org.carewebframework.api.property.PropertyUtil;
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
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Button;
import org.zkoss.zul.Image;
import org.zkoss.zul.Tree;
import org.zkoss.zul.Treeitem;
import org.zkoss.zul.Window;

/**
 * Dialog for adding new component to UI.
 */
public class AddComponent extends Window {
    
    private static final long serialVersionUID = 1L;
    
    private static final String ON_FAVORITE = "onFavorite";
    
    private UIElementBase parentElement;
    
    private UIElementBase childElement;
    
    private PluginDefinition definition;
    
    private Tree tree;
    
    private Button btnOK;
    
    private boolean createChild;
    
    private Treeitem itmFavorites;
    
    private List<String> favorites;
    
    private boolean favoritesChanged;
    
    private final String favoritesCategory = Labels.getLabel("cwf.shell.plugin.category.favorite");
    
    private final String favoritesAddHint = Labels.getLabel("cwf.shell.designer.add.component.favorite.add.hint");
    
    private final String favoritesRemoveHint = Labels.getLabel("cwf.shell.designer.add.component.favorite.remove.hint");
    
    private final String noDescriptionHint = Labels.getLabel("cwf.shell.designer.add.component.description.missing.hint");
    
    /**
     * Handles click on item not under favorites category.
     */
    private final EventListener<Event> favoriteListener1 = new EventListener<Event>() {
        
        @Override
        public void onEvent(Event event) throws Exception {
            Treeitem item = (Treeitem) event.getTarget();
            String path = (String) item.getAttribute("path");
            boolean isFavorite = !(Boolean) item.getAttribute("favorite");
            Treeitem other = (Treeitem) item.getAttribute("other");
            favoritesChanged = true;
            
            if (isFavorite) {
                favorites.add(path);
                other = addTreeitem((PluginDefinition) item.getValue(), item);
                item.setAttribute("other", other);
            } else {
                favorites.remove(path);
                other.detach();
            }
            
            setFavoriteStatus(item, isFavorite);
        }
    };
    
    /**
     * Handles click on item under favorites category.
     */
    private final EventListener<Event> favoriteListener2 = new EventListener<Event>() {
        
        @Override
        public void onEvent(Event event) throws Exception {
            Treeitem item = (Treeitem) event.getTarget();
            Treeitem other = (Treeitem) item.getAttribute("other");
            Events.sendEvent(ON_FAVORITE, other, null);
        }
        
    };
    
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
        loadFavorites();
        
        for (PluginDefinition def : PluginRegistry.getInstance()) {
            if (def.isInternal()) {
                continue;
            }
            
            Class<? extends UIElementBase> clazz = def.getClazz();
            
            if (!parentElement.canAcceptChild(clazz) || !UIElementBase.canAcceptParent(clazz, parentElement.getClass())) {
                continue;
            }
            
            Treeitem item = addTreeitem(def, null);
            
            if (item == null) {
                continue;
            }
            
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
        ZKUtil.moveChild(itmFavorites, 0);
        
        if (useDefault && defaultItem != null) {
            defaultItem.setSelected(true);
            onSelect$tree();
        }
        
        setTitle(StrUtil.formatMessage("@cwf.shell.designer.add.component.title", parentElement.getDefinition().getName()));
    }
    
    private void loadFavorites() {
        try {
            favorites = PropertyUtil.getValues(DesignConstants.DESIGN_FAVORITES_PROPERTY);
        } catch (Exception e) {
            favorites = null;
        }
    }
    
    private void addLayouts(boolean shared) {
        for (String layout : LayoutUtil.getLayouts(shared)) {
            UIElementLayout ele = new UIElementLayout(layout, shared);
            addTreeitem(ele.getDefinition(), null);
        }
    }
    
    private Treeitem addTreeitem(PluginDefinition def, Treeitem other) {
        String category = other != null ? favoritesCategory : def.getCategory();
        
        if (StringUtils.isEmpty(category)) {
            if (UIElementPlugin.class.isAssignableFrom(def.getClazz())) {
                category = Labels.getLabel("cwf.shell.plugin.category._default");
            } else {
                return null;
            }
        }
        
        String path = category + (category.endsWith("\\") ? "" : "\\") + def.getName();
        boolean isFavorite = other != null || (favorites != null && favorites.contains(path));
        boolean disabled = def.isDisabled() || def.isForbidden();
        Treeitem item = TreeUtil.findNode(tree, path, true);
        item.setValue(def);
        item.setTooltiptext(StringUtils.defaultString(def.getDescription(), noDescriptionHint));
        
        if (disabled) {
            item.setDisabled(true);
        } else {
            item.getTreerow().addForward(Events.ON_DOUBLE_CLICK, btnOK, Events.ON_CLICK);
        }
        
        if (favorites != null) {
            Image image = new Image();
            Component cell = item.getTreerow().getFirstChild();
            cell.insertBefore(image, cell.getFirstChild());
            image.addForward(Events.ON_CLICK, item, ON_FAVORITE);
            item.addEventListener(ON_FAVORITE, other == null ? favoriteListener1 : favoriteListener2);
            
            if (isFavorite && other == null) {
                other = addTreeitem(def, item);
            }
            
            item.setAttribute("other", other);
            item.setAttribute("path", path);
            item.setAttribute("image", image);
            setFavoriteStatus(item, isFavorite);
        }
        return item;
    }
    
    /**
     * Updates the tree item according to the favorite status.
     * 
     * @param item Tree item to update.
     * @param isFavorite If true, the item is a favorite.
     * @return The original image.
     */
    private Image setFavoriteStatus(Treeitem item, boolean isFavorite) {
        Image image = (Image) item.getAttribute("image");
        image.setSrc(isFavorite ? DesignConstants.DESIGN_FAVORITES_ACTIVE : DesignConstants.DESIGN_FAVORITES_INACTIVE);
        image.setTooltiptext(isFavorite ? favoritesRemoveHint : favoritesAddHint);
        item.setAttribute("favorite", isFavorite);
        itmFavorites.setVisible(isFavorite || itmFavorites.getTreechildren().getFirstChild() != null);
        return image;
    }
    
    /**
     * Returns currently selected plugin definition, or null if none selected.
     * 
     * @return Definition of the currently selected plugin.
     */
    private PluginDefinition selectedPluginDefinition() {
        return (PluginDefinition) (tree.getSelectedItem() == null ? null : tree.getSelectedItem().getValue());
    }
    
    /**
     * Close dialog without further action.
     */
    public void onClick$btnCancel() {
        onClose();
    }
    
    /**
     * Create new element based on selected plugin definition, add it to the parent element and
     * close the dialog.
     * 
     * @throws Exception Unspecified exception.
     */
    public void onClick$btnOK() throws Exception {
        definition = selectedPluginDefinition();
        
        if (definition != null) {
            childElement = createChild ? definition.createElement(parentElement, null) : null;
            
            if (childElement != null) {
                childElement.bringToFront();
            }
            
            onClose();
        }
    }
    
    /**
     * Close the dialog, saving any changes to favorites.
     */
    @Override
    public void onClose() {
        super.onClose();
        
        if (favoritesChanged) {
            PropertyUtil.saveValues(DesignConstants.DESIGN_FAVORITES_PROPERTY, null, false, favorites);
        }
    }
    
    /**
     * Update buttons based on current selection.
     */
    public void onSelect$tree() {
        btnOK.setDisabled(selectedPluginDefinition() == null);
    }
}
