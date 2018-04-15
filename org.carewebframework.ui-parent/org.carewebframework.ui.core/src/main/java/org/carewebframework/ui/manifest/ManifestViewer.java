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
import org.carewebframework.ui.dialog.PopupDialog;
import org.carewebframework.ui.util.CWFUtil;
import org.fujion.ancillary.IAutoWired;
import org.fujion.annotation.EventHandler;
import org.fujion.annotation.WiredComponent;
import org.fujion.component.BaseComponent;
import org.fujion.component.BaseUIComponent;
import org.fujion.component.Column;
import org.fujion.component.Grid;
import org.fujion.component.Label;
import org.fujion.component.Row;
import org.fujion.component.Textbox;
import org.fujion.component.Window;
import org.fujion.event.ChangeEvent;
import org.fujion.event.Event;
import org.fujion.model.IListModel;
import org.fujion.model.IModelAndView;
import org.fujion.model.ListModel;

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
    private Grid grid;

    @WiredComponent
    private Label caption;

    @WiredComponent
    private Textbox txtSearch;

    @WiredComponent
    private BaseUIComponent printRoot;

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
        PopupDialog.show(CWFUtil.getResourcePath(ManifestViewer.class) + "manifestViewer.fsp", args, true, false, true,
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

        renderer.init(grid);
        filterChanged(null);
        grid.getColumns().getChild(Column.class).sort();
    }

    /**
     * Show a detail view of the selected manifest.
     *
     * @param event The triggering event.
     */
    @EventHandler(value = "showManifest", target = "@grid")
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
    private void onClick$btnClose() {
        root.close();
    }

    @EventHandler(value = "click", target = "btnPrint")
    private void onClick$btnPrint() {
        printRoot.print();
    }

    @EventHandler(value = "change", target = "@grid")
    private void onChange$grid() {
        txtSearch.focus();
    }

    public void filterChanged(String filter) {
        IModelAndView<Row, IMatchable> modelAndView = grid.getRows().getModelAndView(IMatchable.class);
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
