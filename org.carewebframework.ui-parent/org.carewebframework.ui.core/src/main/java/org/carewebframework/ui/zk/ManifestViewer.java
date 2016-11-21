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
package org.carewebframework.ui.zk;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.apache.commons.lang.StringUtils;
import org.carewebframework.api.ManifestIterator;
import org.carewebframework.common.StrUtil;
import org.carewebframework.ui.FrameworkController;
import org.carewebframework.web.component.BaseComponent;
import org.carewebframework.web.component.Column;
import org.carewebframework.web.component.Label;
import org.carewebframework.web.component.Row;
import org.carewebframework.web.component.Span;
import org.carewebframework.web.component.Table;
import org.carewebframework.web.component.Textbox;
import org.carewebframework.web.event.DblclickEvent;
import org.carewebframework.web.event.Event;
import org.carewebframework.web.event.InputEvent;
import org.carewebframework.web.model.IComponentRenderer;
import org.carewebframework.web.model.IListModel;
import org.carewebframework.web.model.IModelAndView;
import org.carewebframework.web.model.ListModel;
import org.carewebframework.web.model.ModelAndView;
import org.carewebframework.web.model.Sorting.SortOrder;

/**
 * Displays a dialog showing all known manifests or details about a single manifest.
 */
public class ManifestViewer extends FrameworkController {
    
    private interface Matchable<M> extends Comparable<M> {
        
        boolean matches(String filter);
    }
    
    /**
     * List model object. Extracts attributes of interest from a manifest.
     */
    public static class ManifestItem implements Matchable<ManifestItem> {
        
        public final Manifest manifest;
        
        public final String implVersion;
        
        public final String implVendor;
        
        public final String implModule;
        
        /**
         * Wrapper for a single manifest.
         * 
         * @param manifest A manifest.
         */
        public ManifestItem(Manifest manifest) {
            this.manifest = manifest;
            implVersion = get("Implementation-Version", "Bundle-Version");
            implVendor = get("Implementation-Vendor", "Bundle-Vendor");
            implModule = get("Bundle-Name", "Implementation-Title", "Implementation-URL");
        }
        
        private String get(String... names) {
            String result = null;
            Attributes attributes = manifest.getMainAttributes();
            
            for (String name : names) {
                result = attributes.getValue(name);
                result = result == null ? "" : StrUtil.stripQuotes(result.trim());
                
                if (!result.isEmpty()) {
                    break;
                }
            }
            
            return result;
        }
        
        public boolean isEmpty() {
            return implModule == null || implModule.isEmpty();
        }
        
        @Override
        public int compareTo(ManifestItem o) {
            int result = compare(implModule, o.implModule);
            result = result == 0 ? compare(implVendor, o.implVendor) : result;
            result = result == 0 ? compare(implVersion, o.implVersion) : result;
            return result;
        }
        
        @Override
        public boolean equals(Object o) {
            return o instanceof ManifestItem && compareTo((ManifestItem) o) == 0;
        }
        
        private int compare(String s1, String s2) {
            return s1 == s2 ? 0 : s1 == null ? -1 : s2 == null ? 1 : s1.compareToIgnoreCase(s2);
        }
        
        @Override
        public boolean matches(String filter) {
            return StringUtils.containsIgnoreCase(implModule, filter) || StringUtils.containsIgnoreCase(implVendor, filter)
                    || StringUtils.containsIgnoreCase(implVersion, filter);
        }
    }
    
    /**
     * A single attribute row from a manifest.
     */
    public static class AttributeItem implements Matchable<AttributeItem> {
        
        public final String name;
        
        public final String value;
        
        public AttributeItem(Entry<Object, Object> entry) {
            name = entry.getKey().toString();
            value = entry.getValue().toString();
        }
        
        @Override
        public int compareTo(AttributeItem o) {
            return name.compareToIgnoreCase(o.name);
        }
        
        @Override
        public boolean matches(String filter) {
            return StringUtils.containsIgnoreCase(name, filter) || StringUtils.containsIgnoreCase(value, filter);
        }
        
    }
    
    /**
     * Base renderer.
     * 
     * @param <M> Class of rendered object.
     */
    private static abstract class BaseRenderer<M> implements IComponentRenderer<Row, M> {
        
        public abstract void init(Table table);
        
        /**
         * Adds a cell with the specified content to the table row.
         * 
         * @param row List row.
         * @param label Content for cell. Auto-detects type of content.
         * @return Newly created cell.
         */
        public Span addContent(Row row, String label) {
            Span cell = new Span();
            cell.addChild(ZKUtil.getTextComponent(label));
            row.addChild(cell);
            return cell;
        }
        
        /**
         * Adds a cell to the table row.
         * 
         * @param row List row.
         * @param label Label text for cell.
         * @return Newly created cell.
         */
        public Label addLabel(Row row, String label) {
            Label cell = new Label(label);
            row.addChild(cell);
            return cell;
        }
        
        /**
         * Adds a column to a table.
         * 
         * @param table Table.
         * @param label Label for column.
         * @param width Width for column.
         * @param orderBy The field to sort by.
         * @return Newly created column.
         */
        public Column addColumn(Table table, String label, String width, String orderBy) {
            Column column = new Column();
            table.getColumns().addChild(column);
            column.setLabel(label);
            column.setWidth(width);
            orderBy = "auto(upper(" + orderBy + "))";
            column.setSort(orderBy);
            
            if (column.indexOf() == 0) {
                column.setSortOrder(SortOrder.ASCENDING);
            }
            
            return column;
        }
        
    }
    
    /**
     * Renderer for a single manifest.
     */
    private static final BaseRenderer<ManifestItem> manifestItemRenderer = new BaseRenderer<ManifestItem>() {
        
        @Override
        public Row render(ManifestItem manifestItem) {
            Row row = new Row();
            row.setData(manifestItem);
            addLabel(row, manifestItem.implModule);
            addLabel(row, manifestItem.implVersion);
            addLabel(row, manifestItem.implVendor);
            return row;
        }
        
        @Override
        public void init(Table table) {
            table.getRows().getModelAndView(ManifestItem.class).setRenderer(this);
            table.registerEventForward(DblclickEvent.TYPE, table, "onShowManifest");
            addColumn(table, "Module", "40%", "implModule");
            addColumn(table, "Version", "20%", "implVersion");
            addColumn(table, "Author", "40%", "implVendor");
        }
        
    };
    
    /**
     * Renderer for a single manifest attribute.
     */
    private static final BaseRenderer<AttributeItem> attributeItemRenderer = new BaseRenderer<AttributeItem>() {
        
        @Override
        public Row render(AttributeItem attributeItem) {
            Row row = new Row();
            row.setData(attributeItem);
            addLabel(row, attributeItem.name);
            addContent(row, attributeItem.value);
            return row;
        }
        
        @Override
        public void init(Table table) {
            //modelAndView.setRenderer(this);
            addColumn(table, "Attribute", "30%", "name");
            addColumn(table, "Value", "70%", "value");
        }
        
    };
    
    private ModelAndView<Row, Matchable<?>> modelAndViewx;
    
    private Table table;
    
    private Label caption;
    
    private Textbox txtSearch;
    
    private Event sortEvent;
    
    private final List<Matchable<?>> rows = new ArrayList<>();
    
    private final IListModel<Matchable> model = new ListModel<>();
    
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
        Map<Object, Object> args = new HashMap<>();
        args.put("manifestItem", manifestItem);
        PopupDialog.popup(Constants.RESOURCE_PREFIX + "manifestViewer.cwf", args, true, false, true);
    }
    
    /**
     * Display the contents of a single manifest or all discovered manifests.
     */
    @Override
    public void afterInitialized(BaseComponent comp) {
        super.afterInitialized(comp);
        ManifestItem manifestItem = (ManifestItem) comp.getAttribute("manifestItem");
        BaseRenderer<?> renderer;
        
        if (manifestItem != null) {
            renderer = attributeItemRenderer;
            caption.setLabel(manifestItem.implModule);
            
            for (Entry<Object, Object> entry : manifestItem.manifest.getMainAttributes().entrySet()) {
                rows.add(new AttributeItem(entry));
            }
        } else {
            renderer = manifestItemRenderer;
            
            for (Manifest mnfst : ManifestIterator.getInstance()) {
                ManifestItem anItem = new ManifestItem(mnfst);
                
                if (!anItem.isEmpty() && !rows.contains(anItem)) {
                    rows.add(anItem);
                }
            }
        }
        
        renderer.init(table);
        filterChanged(null);
    }
    
    /**
     * Show a detail view of the selected manifest.
     */
    public void onShowManifest$table() {
        Row row = table.getRows().getSelectedCount() == 0 ? null : table.getRows().getSelected().get(0);
        ManifestItem manifestItem = row == null ? null : (ManifestItem) row.getData();
        
        if (manifestItem != null) {
            execute(manifestItem);
        }
    }
    
    /**
     * Force rendering of all table rows after sorting (so printing works correctly).
     * 
     * @param event The sort event.
     */
    public void onAfterSort$table(Event event) {
        sortEvent = event;
    }
    
    /**
     * Search for user-specified text.
     * 
     * @param event The input event.
     */
    public void onChanging$txtSearch(InputEvent event) {
        filterChanged(event.getValue());
        
    }
    
    public void onSelect$table() {
        txtSearch.focus();
    }
    
    public void filterChanged(String filter) {
        IModelAndView<Row, Matchable> modelAndView = table.getRows().getModelAndView(Matchable.class);
        modelAndView.setModel(null);
        model.clear();
        
        if (StringUtils.isEmpty(filter)) {
            model.addAll(rows);
        } else {
            for (Matchable<?> row : rows) {
                if (row.matches(filter)) {
                    model.add(row);
                }
            }
        }
        
        modelAndView.setModel(model);
    }
    
}
