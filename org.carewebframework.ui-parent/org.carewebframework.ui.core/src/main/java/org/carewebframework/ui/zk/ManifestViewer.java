/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
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

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.InputEvent;
import org.zkoss.zk.ui.event.SortEvent;
import org.zkoss.zul.Caption;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listheader;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.ListitemRenderer;
import org.zkoss.zul.Textbox;

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
    private static abstract class BaseRenderer<T> implements ListitemRenderer<T> {
        
        public abstract void init(Listbox list);
        
        /**
         * Adds a cell with the specified content to the list item.
         * 
         * @param item List item.
         * @param label Content for cell. Auto-detects type of content.
         * @return Newly created cell.
         */
        public Listcell addContent(Listitem item, String label) {
            Listcell cell = new Listcell();
            cell.appendChild(ZKUtil.getTextComponent(label));
            item.appendChild(cell);
            return cell;
        }
        
        /**
         * Adds a cell to the list item.
         * 
         * @param item List item.
         * @param label Label text for cell.
         * @return Newly created cell.
         */
        public Listcell addLabel(Listitem item, String label) {
            Listcell cell = new Listcell(label);
            item.appendChild(cell);
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
                    Events.postEvent("onAfterSort", list, event);
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
        public void render(Listitem item, ManifestItem manifestItem, int index) throws Exception {
            item.setValue(manifestItem);
            addLabel(item, manifestItem.implModule);
            addLabel(item, manifestItem.implVersion);
            addLabel(item, manifestItem.implVendor);
        }
        
        @Override
        public void init(Listbox list) {
            list.setItemRenderer(this);
            list.addForward(Events.ON_DOUBLE_CLICK, list, "onShowManifest");
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
        public void render(Listitem item, AttributeItem attributeItem, int index) throws Exception {
            item.setValue(attributeItem);
            addLabel(item, attributeItem.name);
            addContent(item, attributeItem.value);
        }
        
        @Override
        public void init(Listbox list) {
            list.setItemRenderer(this);
            addHeader(list, "Attribute", "30%", "name");
            addHeader(list, "Value", "70%", "value");
        }
        
    };
    
    private Listbox list;
    
    private Caption caption;
    
    private Textbox txtSearch;
    
    private SortEvent sortEvent;
    
    private final List<Matchable<?>> items = new ArrayList<>();
    
    private final ListModelList<Matchable<?>> model = new ListModelList<>();
    
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
        PopupDialog.popup(Constants.RESOURCE_PREFIX + "manifestViewer.zul", args, true, false, true);
    }
    
    /**
     * Display the contents of a single manifest or all discovered manifests.
     */
    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        ManifestItem manifestItem = (ManifestItem) arg.get("manifestItem");
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
        ManifestItem manifestItem = item == null ? null : (ManifestItem) item.getValue();
        
        if (manifestItem != null) {
            execute(manifestItem);
        }
    }
    
    /**
     * Force rendering of all list items after sorting (so printing works correctly).
     * 
     * @param event The sort event.
     */
    public void onAfterSort$list(Event event) {
        sortEvent = (SortEvent) ZKUtil.getEventOrigin(event).getData();
        list.renderAll();
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
        list.setModel((ListModelList<?>) null);
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
            model.sort(null, true);
        }
        
        list.setModel(model);
        
        if (sortEvent != null) {
            Listheader lh = (Listheader) sortEvent.getTarget();
            lh.sort(sortEvent.isAscending(), true);
        }
        
        list.renderAll();
    }
    
}
