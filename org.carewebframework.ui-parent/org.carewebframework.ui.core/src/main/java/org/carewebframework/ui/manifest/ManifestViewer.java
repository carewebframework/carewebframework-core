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
package org.carewebframework.ui.manifest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.jar.Manifest;

import org.apache.commons.lang.StringUtils;
import org.carewebframework.api.ManifestIterator;
import org.carewebframework.ui.core.CWFUtil;
import org.carewebframework.ui.dialog.PopupDialog;
import org.carewebframework.web.ancillary.IAutoWired;
import org.carewebframework.web.annotation.EventHandler;
import org.carewebframework.web.annotation.WiredComponent;
import org.carewebframework.web.component.BaseComponent;
import org.carewebframework.web.component.Column;
import org.carewebframework.web.component.Label;
import org.carewebframework.web.component.Row;
import org.carewebframework.web.component.Table;
import org.carewebframework.web.component.Textbox;
import org.carewebframework.web.component.Window;
import org.carewebframework.web.event.ChangeEvent;
import org.carewebframework.web.event.Event;
import org.carewebframework.web.model.IListModel;
import org.carewebframework.web.model.IModelAndView;
import org.carewebframework.web.model.ListModel;

/**
 * Displays a dialog showing all known manifests or details about a single manifest.
 */
@SuppressWarnings("rawtypes")
public class ManifestViewer implements IAutoWired {
    
    /**
     * Renderer for a single manifest.
     */
    private static final BaseRenderer<ManifestItem> manifestItemRenderer = new ManifestItemRenderer();
    
    /**
     * Renderer for a single manifest attribute.
     */
    private static final BaseRenderer<AttributeItem> attributeItemRenderer = new AttributeItemRenderer();
    
    private Window root;
    
    @WiredComponent
    private Table table;
    
    @WiredComponent
    private Label caption;
    
    @WiredComponent
    private Textbox txtSearch;
    
    private final List<IMatchable<?>> items = new ArrayList<>();
    
    private final IListModel<IMatchable> model = new ListModel<>();
    
    /**
     * Display a summary dialog of all known manifests.
     */
    public static void execute() {
        execute(null);
    }
    
    /**
     * Display a detail dialog for a single manifest entry.
     * 
     * @param manifestItem The row to display. If null, all manifests are displayed.
     */
    private static void execute(ManifestItem manifestItem) {
        Map<String, Object> args = new HashMap<>();
        args.put("manifestItem", manifestItem);
        PopupDialog.show(CWFUtil.getResourcePath(ManifestViewer.class) + "manifestViewer.cwf", args, true, false, true,
            null);
    }
    
    /**
     * Display the contents of a single manifest or all discovered manifests.
     */
    @Override
    public void afterInitialized(BaseComponent comp) {
        root = (Window) comp;
        ManifestItem manifestItem = (ManifestItem) comp.getAttribute("manifestItem");
        BaseRenderer<?> renderer;
        
        if (manifestItem != null) {
            renderer = attributeItemRenderer;
            root.setTitle(root.getTitle() + " - " + manifestItem.implModule);
            
            for (Entry<Object, Object> entry : manifestItem.manifest.getMainAttributes().entrySet()) {
                items.add(new AttributeItem(entry));
            }
        } else {
            renderer = manifestItemRenderer;
            
            for (Manifest mnfst : ManifestIterator.getInstance()) {
                ManifestItem anItem = new ManifestItem(mnfst);
                
                if (!anItem.isEmpty() && !items.contains(anItem)) {
                    items.add(anItem);
                }
            }
        }
        
        renderer.init(table);
        filterChanged(null);
        table.getColumns().getChild(Column.class).sort();
    }
    
    /**
     * Show a detail view of the selected manifest.
     * 
     * @param event The triggering event.
     */
    @EventHandler(value = "showManifest", target = "@table")
    public void onShowManifest(Event event) {
        ManifestItem manifestItem = (ManifestItem) event.getData();
        
        if (manifestItem != null) {
            execute(manifestItem);
        }
    }
    
    /**
     * Search for user-specified text.
     * 
     * @param event The input event.
     */
    @EventHandler(value = "change", target = "@txtSearch")
    private void onChange$txtSearch(ChangeEvent event) {
        filterChanged(event.getValue(String.class));
        
    }
    
    @EventHandler(value = "click", target = "btnClose")
    private void onClose$btnClose() {
        root.close();
    }
    
    @EventHandler(value = "change", target = "@table")
    private void onChange$table() {
        txtSearch.focus();
    }
    
    public void filterChanged(String filter) {
        IModelAndView<Row, IMatchable> modelAndView = table.getRows().getModelAndView(IMatchable.class);
        modelAndView.setModel(null);
        model.clear();
        
        if (StringUtils.isEmpty(filter)) {
            model.addAll(items);
        } else {
            for (IMatchable<?> row : items) {
                if (row.matches(filter)) {
                    model.add(row);
                }
            }
        }
        
        modelAndView.setModel(model);
    }
    
}
