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

import org.apache.commons.lang.StringUtils;
import org.carewebframework.api.property.PropertyUtil;
import org.carewebframework.common.StrUtil;
import org.carewebframework.shell.layout.LayoutUtil;
import org.carewebframework.shell.layout.UIElementBase;
import org.carewebframework.shell.layout.UIElementLayout;
import org.carewebframework.shell.layout.UIElementPlugin;
import org.carewebframework.shell.plugins.PluginDefinition;
import org.carewebframework.shell.plugins.PluginRegistry;
import org.carewebframework.ui.zk.TreeUtil;
import org.carewebframework.web.ancillary.IAutoWired;
import org.carewebframework.web.ancillary.IResponseCallback;
import org.carewebframework.web.annotation.EventHandler;
import org.carewebframework.web.annotation.WiredComponent;
import org.carewebframework.web.component.BaseComponent;
import org.carewebframework.web.component.BaseUIComponent;
import org.carewebframework.web.component.Button;
import org.carewebframework.web.component.Span;
import org.carewebframework.web.component.Treenode;
import org.carewebframework.web.component.Treeview;
import org.carewebframework.web.component.Window;
import org.carewebframework.web.event.ClickEvent;
import org.carewebframework.web.event.DblclickEvent;
import org.carewebframework.web.event.EventUtil;
import org.carewebframework.web.event.IEventListener;
import org.carewebframework.web.page.PageUtil;

/**
 * Dialog for adding new component to UI.
 */
public class AddComponent implements IAutoWired {
    
    private static final String ON_FAVORITE = "favorite";
    
    private UIElementBase parentElement;
    
    private UIElementBase childElement;
    
    private PluginDefinition definition;
    
    private Window window;
    
    private boolean createChild;
    
    private List<String> favorites;
    
    private boolean favoritesChanged;
    
    private final String favoritesCategory = StrUtil.getLabel("cwf.shell.plugin.category.favorite");
    
    private final String favoritesAddHint = StrUtil.getLabel("cwf.shell.designer.add.component.favorite.add.hint");
    
    private final String favoritesRemoveHint = StrUtil.getLabel("cwf.shell.designer.add.component.favorite.remove.hint");
    
    private final String noDescriptionHint = StrUtil.getLabel("cwf.shell.designer.add.component.description.missing.hint");
    
    @WiredComponent
    private Treenode tnFavorites;
    
    @WiredComponent
    private Treeview tree;
    
    @WiredComponent
    private Button btnOK;
    
    /**
     * Handles click on item not under favorites category.
     */
    private final IEventListener favoriteListener1 = (event) -> {
        Treenode node = (Treenode) event.getTarget();
        String path = (String) node.getAttribute("path");
        boolean isFavorite = !(Boolean) node.getAttribute("favorite");
        Treenode other = (Treenode) node.getAttribute("other");
        favoritesChanged = true;
        
        if (isFavorite) {
            favorites.add(path);
            other = addTreenode((PluginDefinition) node.getData(), node);
            node.setAttribute("other", other);
        } else {
            favorites.remove(path);
            other.detach();
        }
        
        setFavoriteStatus(node, isFavorite);
    };
    
    /**
     * Handles click on item under favorites category.
     */
    private final IEventListener favoriteListener2 = (event) -> {
        Treenode node = (Treenode) event.getTarget();
        Treenode other = (Treenode) node.getAttribute("other");
        EventUtil.send(ON_FAVORITE, other, null);
    };
    
    /**
     * Display the add component dialog, presenting a list of candidate plugins that may serve as
     * children to the specified parent element.
     * 
     * @param parentElement Element to serve as parent to the newly created child element.
     * @param callback Callback to return the newly created child element.
     */
    public static void newChild(UIElementBase parentElement, IResponseCallback<UIElementBase> callback) {
        show(parentElement, true, (event) -> {
            if (callback != null) {
                callback.onComplete(event.getTarget().getAttribute("childElement", UIElementBase.class));
            }
        });
    }
    
    /**
     * Display the add component dialog, presenting a list of candidate plugins that may serve as
     * children to the specified parent element.
     * 
     * @param parentElement Element to serve as parent to the newly created child element.
     * @param callback Callback to return the plugin definition.
     */
    public static void getDefinition(UIElementBase parentElement, IResponseCallback<PluginDefinition> callback) {
        show(parentElement, false, (event) -> {
            if (callback != null) {
                callback.onComplete(event.getTarget().getAttribute("pluginDefinition", PluginDefinition.class));
            }
        });
    }
    
    /**
     * Display the add component dialog, presenting a list of candidate plugins that may serve as
     * children to the specified parent element.
     * 
     * @param parentElement Element to serve as parent to the newly created child element.
     * @param createChild If true, the selected element will be created.
     * @param callback The close event handler.
     */
    private static void show(UIElementBase parentElement, boolean createChild, IEventListener callback) {
        Map<String, Object> args = new HashMap<>();
        args.put("parentElement", parentElement);
        args.put("createChild", createChild);
        Window dlg = (Window) PageUtil.createPage(DesignConstants.RESOURCE_PREFIX + "addComponent.cwf", null, args).get(0);
        dlg.modal(callback);
    }
    
    /**
     * Initialize the tree view based with list of plugins that may serve as children to the parent
     * element.
     * 
     * @param parentElement Element to serve as parent to the newly created child element.
     * @param createChild If true, the selected element will be created.
     */
    @Override
    public void afterInitialized(BaseComponent comp) {
        window = (Window) comp;
        this.parentElement = comp.getAttribute("parentElement", UIElementBase.class);
        this.createChild = comp.getAttribute("createChild", false);
        Treenode defaultItem = null;
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
            
            Treenode item = addTreenode(def, null);
            
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
        tnFavorites.setIndex(0);
        
        if (useDefault && defaultItem != null) {
            defaultItem.setSelected(true);
            onChange$tree();
        }
        
        window.setTitle(
            StrUtil.formatMessage("@cwf.shell.designer.add.component.title", parentElement.getDefinition().getName()));
        window.setOnCanClose(() -> {
            if (favoritesChanged) {
                PropertyUtil.saveValues(DesignConstants.DESIGN_FAVORITES_PROPERTY, null, false, favorites);
            }
            
            return true;
        });
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
            addTreenode(ele.getDefinition(), null);
        }
    }
    
    private Treenode addTreenode(PluginDefinition def, Treenode other) {
        String category = other != null ? favoritesCategory : def.getCategory();
        
        if (StringUtils.isEmpty(category)) {
            if (UIElementPlugin.class.isAssignableFrom(def.getClazz())) {
                category = StrUtil.getLabel("cwf.shell.plugin.category.default");
            } else {
                return null;
            }
        }
        
        String path = category + (category.endsWith("\\") ? "" : "\\") + def.getName();
        boolean isFavorite = other != null || (favorites != null && favorites.contains(path));
        boolean disabled = def.isDisabled() || def.isForbidden();
        Treenode node = TreeUtil.findNode(tree, path, true);
        node.setData(def);
        node.setHint(StringUtils.defaultString(def.getDescription(), noDescriptionHint));
        
        if (disabled) {
            node.setDisabled(true);
        } else {
            node.addEventForward(DblclickEvent.TYPE, btnOK, ClickEvent.TYPE);
        }
        
        if (favorites != null) {
            Span image = new Span();
            image.addClass("glyphicon");
            image.addStyle("float", "left");
            BaseComponent cell = node.getFirstChild();
            cell.addChild(image, cell.getFirstChild());
            image.addEventForward(ClickEvent.TYPE, node, ON_FAVORITE);
            node.addEventListener(ON_FAVORITE, other == null ? favoriteListener1 : favoriteListener2);
            
            if (isFavorite && other == null) {
                other = addTreenode(def, node);
            }
            
            node.setAttribute("other", other);
            node.setAttribute("path", path);
            node.setAttribute("image", image);
            setFavoriteStatus(node, isFavorite);
        }
        return node;
    }
    
    /**
     * Updates the tree item according to the favorite status.
     * 
     * @param item Tree item to update.
     * @param isFavorite If true, the item is a favorite.
     * @return The original image.
     */
    private BaseUIComponent setFavoriteStatus(Treenode item, boolean isFavorite) {
        BaseUIComponent image = (BaseUIComponent) item.getAttribute("image");
        image.addClass(isFavorite ? "glyphicon-star text-primary" : "glyphicon-star-empty text-muted");
        image.setHint(isFavorite ? favoritesRemoveHint : favoritesAddHint);
        item.setAttribute("favorite", isFavorite);
        tnFavorites.setVisible(isFavorite || tnFavorites.getFirstChild() != null);
        return image;
    }
    
    /**
     * Returns currently selected plugin definition, or null if none selected.
     * 
     * @return Definition of the currently selected plugin.
     */
    private PluginDefinition selectedPluginDefinition() {
        return (PluginDefinition) (tree.getSelectedNode() == null ? null : tree.getSelectedNode().getData());
    }
    
    /**
     * Close dialog without further action.
     */
    @EventHandler(value = "click", target = "btnCancel")
    private void onClick$btnCancel() {
        window.close();
    }
    
    /**
     * Create new element based on selected plugin definition, add it to the parent element and
     * close the dialog.
     */
    @EventHandler(value = "click", target = "@btnOK")
    private void onClick$btnOK() {
        definition = selectedPluginDefinition();
        
        if (definition != null) {
            childElement = createChild ? definition.createElement(parentElement, null) : null;
            
            if (childElement != null) {
                childElement.bringToFront();
            }
            
            window.setAttribute("pluginDefinition", definition);
            window.setAttribute("childElement", childElement);
            window.close();
        }
    }
    
    /**
     * Update buttons based on current selection.
     */
    @EventHandler(value = "change", target = "@tree")
    private void onChange$tree() {
        btnOK.setDisabled(selectedPluginDefinition() == null);
    }
}
