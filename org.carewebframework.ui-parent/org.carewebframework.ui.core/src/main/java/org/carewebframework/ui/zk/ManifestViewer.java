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
import org.carewebframework.web.component.Label;
import org.carewebframework.web.component.Listbox;
import org.carewebframework.web.component.Listitem;
import org.carewebframework.web.component.Span;
import org.carewebframework.web.component.Textbox;
import org.carewebframework.web.event.DblclickEvent;
import org.carewebframework.web.event.EventUtil;
import org.carewebframework.web.event.InputEvent;
import org.carewebframework.web.model.IComponentRenderer;
import org.carewebframework.web.model.ListModel;
import org.carewebframework.web.model.ModelAndView;

/**
 * Displays a dialog showing all known manifests or details about a single manifest.
 */
public class ManifestViewer extends FrameworkController {
    
    private static final long serialVersionUID = 1L;
    
    private interface Matchable<T> extends Comparable<T> {
        
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
     * A single attribute item from a manifest.
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
     * @param <T> Class of rendered object.
     */
    private static abstract class BaseRenderer<T> implements IComponentRenderer<Listitem, T> {
        
        public abstract ModelAndView<Listitem, T> init(Listbox list);
        
        /**
         * Adds a cell with the specified content to the list item.
         * 
         * @param item List item.
         * @param label Content for cell. Auto-detects type of content.
         * @return Newly created cell.
         */
        public Span addContent(Listitem item, String label) {
            Span cell = new Span();
            cell.addChild(ZKUtil.getTextComponent(label));
            item.addChild(cell);
            return cell;
        }
        
        /**
         * Adds a cell to the list item.
         * 
         * @param item List item.
         * @param label Label text for cell.
         * @return Newly created cell.
         */
        public Label addLabel(Listitem item, String label) {
            Label cell = new Label(label);
            item.addChild(cell);
            return cell;
        }
        
        /**
         * Adds a header to a list box.
         * 
         * @param list List box.
         * @param label Label for header.
         * @param width Width for header.
         * @param orderBy The field to sort by.
         * @return Newly created header.
         */
        public Listheader addHeader(final Listbox list, String label, String width, String orderBy) {
            Listheader header = new Listheader();
            list.getListhead().appendChild(header);
            header.setLabel(label);
            header.setWidth(width);
            orderBy = "auto(upper(" + orderBy + "))";
            header.setSort(orderBy);
            
            if (header.getColumnIndex() == 0) {
                header.setSortDirection("ascending");
            }
            
            header.addEventListener(Events.ON_SORT, new EventListener<SortEvent>() {
                
                @Override
                public void onEvent(SortEvent event) throws Exception {
                    EventUtil.post("afterSort", list, event);
                }
                
            });
            return header;
        }
        
    }
    
    /**
     * Renderer for a single manifest.
     */
    private static final BaseRenderer<ManifestItem> manifestItemRenderer = new BaseRenderer<ManifestItem>() {
        
        @Override
        public Listitem render(ManifestItem manifestItem) {
            Listitem item = new Listitem();
            item.setData(manifestItem);
            addLabel(item, manifestItem.implModule);
            addLabel(item, manifestItem.implVersion);
            addLabel(item, manifestItem.implVendor);
            return item;
        }
        
        @Override
        public ModelAndView<Listitem, ManifestItem> init(Listbox list) {
            ModelAndView<Listitem, ManifestItem> modelAndView = new ModelAndView<>(list, null, this);
            list.registerEventForward(DblclickEvent.TYPE, list, "onShowManifest");
            addHeader(list, "Module", "40%", "implModule");
            addHeader(list, "Version", "20%", "implVersion");
            addHeader(list, "Author", "40%", "implVendor");
        }
        
    };
    
    /**
     * Renderer for a single manifest attribute.
     */
    private static final BaseRenderer<AttributeItem> attributeItemRenderer = new BaseRenderer<AttributeItem>() {
        
        @Override
        public Listitem render(AttributeItem attributeItem) {
            Listitem item = new Listitem();
            item.setData(attributeItem);
            addLabel(item, attributeItem.name);
            addContent(item, attributeItem.value);
            return item;
        }
        
        @Override
        public void init(Listbox list, ModelAndView<Listitem, ?> modelAndView) {
            modelAndView.setRenderer(this);
            addHeader(list, "Attribute", "30%", "name");
            addHeader(list, "Value", "70%", "value");
        }
        
    };
    
    private ModelAndView<Listitem, Matchable<?>> modelAndView;
    
    private Listbox list;
    
    private Caption caption;
    
    private Textbox txtSearch;
    
    private SortEvent sortEvent;
    
    private final List<Matchable<?>> items = new ArrayList<>();
    
    private final ListModel<Matchable<?>> model = new ListModel<>();
    
    /**
     * Display a summary dialog of all known manifests.
     */
    public static void execute() {
        execute(null);
    }
    
    /**
     * Display a detail dialog for a single manifest entry.
     * 
     * @param manifestItem The item to display. If null, all manifests are displayed.
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
        modelAndView = new ModelAndView<>(list);
        ManifestItem manifestItem = (ManifestItem) comp.getAttribute("manifestItem");
        BaseRenderer<?> renderer;
        
        if (manifestItem != null) {
            renderer = attributeItemRenderer;
            caption.setLabel(manifestItem.implModule);
            
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
        
        renderer.init(list);
        int rows = items.size();
        list.setRows(rows > 15 ? 15 : rows);
        filterChanged(null);
    }
    
    /**
     * Show a detail view of the selected manifest.
     */
    public void onShowManifest$list() {
        Listitem item = list.getSelectedItem();
        ManifestItem manifestItem = item == null ? null : (ManifestItem) item.getData();
        
        if (manifestItem != null) {
            execute(manifestItem);
        }
    }
    
    /**
     * Force rendering of all list items after sorting (so printing works correctly).
     * 
     * @param event The sort event.
     */
    public void onAfterSort$list(SortEvent event) {
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
    
    public void onSelect$list() {
        txtSearch.focus();
    }
    
    public void filterChanged(String filter) {
        modelAndView.setModel(null);
        model.clear();
        
        if (StringUtils.isEmpty(filter)) {
            model.addAll(items);
        } else {
            for (Matchable<?> item : items) {
                if (item.matches(filter)) {
                    model.add(item);
                }
            }
        }
        
        if (sortEvent == null) {
            model.sort(true);
        }
        
        modelAndView.setModel(model);
        
        if (sortEvent != null) {
            Listheader lh = (Listheader) sortEvent.getTarget();
            lh.sort(sortEvent.isAscending(), true);
        }
    }
    
}
