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
package org.carewebframework.ui.wonderbar;

import java.util.ArrayList;
import java.util.List;

import org.carewebframework.common.StrUtil;
import org.carewebframework.web.ancillary.ComponentException;
import org.carewebframework.web.annotation.Component.PropertyGetter;
import org.carewebframework.web.annotation.Component.PropertySetter;
import org.carewebframework.web.component.BaseComponent;
import org.carewebframework.web.component.BaseInputboxComponent;
import org.carewebframework.web.event.EventUtil;

/**
 * This is the main ZK component for the wonder bar. It supports both server-side searching for
 * searching through large lists of items and client/browser-side searching for smaller lists of
 * items.
 * 
 * @param <T> Type returned by search provider.
 */
public class Wonderbar<T> extends BaseInputboxComponent<String> {
    
    /**
     * Controls behavior of client-side searches.
     */
    public enum MatchMode {
        ANY_ORDER, // May match any any order
        SAME_ORDER, // Must match in same order, not necessarily contiguous
        ADJACENT, // Must match in same contiguous order, not necessarily at start
        FROM_START // Must match in same order at the start
    };
    
    private final String MESSAGE_TOO_MANY = StrUtil.getLabel("cwf.wonderbar.items.truncated",
        "<< More than {0} items were found for the current input.  Please enter more characters. >>");
    
    private boolean changeOnOKOnly;
    
    private WonderbarDefaults defaultItems;
    
    private WonderbarItems items;
    
    private boolean isClient;
    
    private int maxSearchResults;
    
    private int minSearchCharacters = 3;
    
    private IWonderbarSearchProvider<T> searchProvider;
    
    private IWonderbarItemRenderer<T> renderer;
    
    private boolean selectFirstItem = true;
    
    private boolean openOnFocus;
    
    private WonderbarItem selectedItem;
    
    private final WonderbarGroup truncItem;
    
    private int clientThreshold = 100;
    
    private MatchMode clientMatchMode = MatchMode.ANY_ORDER;
    
    public Wonderbar() {
        super();
        truncItem = new WonderbarGroup();
        truncItem.addClass("cwf-wonderbar-item-more");
        setMaxSearchResults(100);
    }
    
    /**
     * Processes the onWonderbarSelect and onWonderbarSearch events.
     * 
     * @Override public void service(AuRequest request, boolean everError) { String cmd =
     *           request.getCommand(); if (cmd.equals(WonderbarSelectEvent.ON_WONDERBAR_SELECT)) {
     *           WonderbarSelectEvent event = WonderbarSelectEvent.getSelectEvent(request); if
     *           (selectedItem != event.getSelectedItem()) { selectedItem = event.getSelectedItem();
     *           Events.postEvent(event); } } else if
     *           (cmd.equals(WonderbarSearchEvent.ON_WONDERBAR_SEARCH)) { WonderbarSearchEvent event
     *           = WonderbarSearchEvent.getSearchEvent(request); Events.postEvent(event); } else {
     *           super.service(request, everError); } }
     */
    
    /**
     * Updates defaultItems and items when a child is added.
     */
    @Override
    public void addChild(BaseComponent child, BaseComponent refChild) {
        super.addChild(child, refChild);
        
        if (child instanceof WonderbarDefaults) {
            defaultItems = (WonderbarDefaults) child;
        } else if (child instanceof WonderbarItems) {
            items = (WonderbarItems) child;
        }
    }
    
    /**
     * Updates defaultItems and items when a child is removed.
     */
    @Override
    public void beforeRemoveChild(BaseComponent child) {
        super.beforeRemoveChild(child);
        
        if (defaultItems == child) {
            defaultItems = null;
        } else if (items == child) {
            items = null;
        }
    }
    
    @Override
    protected String _toValue(String value) {
        return value == null ? "" : value;
    }
    
    @Override
    protected String _toString(String value) {
        return value == null ? "" : value;
    }
    
    /**
     * Clear the current selection.
     */
    @Override
    public void clear() {
        super.clear();
        initItems(null, false, false);
    }
    
    /**
     * Invokes a search. Supports both client- and server-side searching.
     * 
     * @param term Term to search for.
     */
    public void doSearch(String term) {
        doSearch(term, false);
    }
    
    /**
     * Invokes a search. Supports both client- and server-side searching.
     * 
     * @param term Term to search for.
     * @param fromClient If true, request came from client.
     */
    protected void doSearch(String term, boolean fromClient) {
        term = term == null ? "" : term.trim();
        
        if (!fromClient) {
            setValue(term);
            invoke("search", term);
            focus();
        } else {
            IWonderbarServerSearchProvider<T> provider = (IWonderbarServerSearchProvider<T>) getSearchProvider();
            
            if (provider != null) {
                List<T> hits = new ArrayList<>();
                boolean tooMany = !provider.getSearchResults(term, maxSearchResults, hits);
                initItems(hits, tooMany, false);
                invoke("_serverResponse", term);
            }
        }
    }
    
    /**
     * Handles the client request for a server-based search.
     * 
     * @param event The search event.
     */
    public void onWonderbarSearch(WonderbarSearchEvent event) {
        doSearch(event.getTerm(), true);
    }
    
    /**
     * Open the drop down.
     */
    public void open() {
        invoke("_open");
    }
    
    /**
     * Close the drop down.
     */
    public void close() {
        invoke("_close");
    }
    
    public WonderbarDefaults getDefaultItems() {
        return this.defaultItems;
    }
    
    /**
     * @return true if this wonderbar is currently running in client mode, false if server mode
     */
    public boolean isClientMode() {
        return this.isClient;
    }
    
    protected void setClientMode(boolean value) {
        if (!(value ? IWonderbarClientSearchProvider.class : IWonderbarServerSearchProvider.class)
                .isInstance(searchProvider)) {
            throw new IllegalStateException("Search provider not compatible with selected mode.");
        }
        
        this.isClient = value;
        sync("_clientMode", value);
        initItems(searchProvider.getDefaultItems(), false, true);
        initItems(value ? ((IWonderbarClientSearchProvider<T>) searchProvider).getAllItems() : null, false, false);
    }
    
    /**
     * Initializes the searchable item list.
     * 
     * @param list The list of searchable items. Replaces the existing list. A null value clears the
     *            list.
     * @param tooMany If true, search was truncated because of too many results.
     * @param defaults If true, initialize the default items.
     */
    private void initItems(List<T> list, boolean tooMany, boolean defaults) {
        WonderbarItems parent = defaults ? defaultItems : items;
        
        if (list != null) {
            if (parent != null) {
                parent.destroyChildren();
            } else {
                parent = defaults ? new WonderbarDefaults() : new WonderbarItems();
                addChild(parent);
            }
            
            for (T data : list) {
                WonderbarItem item = new WonderbarItem();
                parent.addChild(item);
                
                if (renderer != null) {
                    renderer.render(item, data, parent.getChildren().size() - 1);
                } else {
                    item.setLabel(data.toString());
                    item.setValue(data.toString());
                    item.setData(data);
                }
            }
            
            if (tooMany) {
                parent.addChild(truncItem);
            }
        } else if (parent != null) {
            parent.destroyChildren();
        }
        
        selectedItem = null;
    }
    
    /**
     * Returns true if item selection occurs only when the enter key is pressed, or false if item
     * selection also occurs when tabbing away from the component.
     * 
     * @return Change-on-OK setting.
     */
    @PropertyGetter("changeOnOKOnly")
    public boolean getChangeOnOKOnly() {
        return changeOnOKOnly;
    }
    
    /**
     * Sets item selection behavior. Set to true if item selection occurs only when the enter key is
     * pressed, or false if item selection also occurs when tabbing away from the component.
     * 
     * @param changeOnOKOnly Change-on-OK setting.
     */
    @PropertySetter("changeOnOKOnly")
    public void setChangeOnOKOnly(boolean changeOnOKOnly) {
        if (changeOnOKOnly != this.changeOnOKOnly) {
            sync("_skipTab", this.changeOnOKOnly = changeOnOKOnly);
        }
    }
    
    /**
     * Returns true if the wonder bar menu should appear when the component receives focus.
     * 
     * @return The open-on-focus setting.
     */
    @PropertyGetter("openOnFocus")
    public boolean getOpenOnFocus() {
        return openOnFocus;
    }
    
    /**
     * Set to true if the wonder bar menu should appear when the component receives focus.
     * 
     * @param openOnFocus The open-on-focus setting.
     */
    @PropertySetter("openOnFocus")
    public void setOpenOnFocus(boolean openOnFocus) {
        if (openOnFocus != this.openOnFocus) {
            sync("_openOnFocus", this.openOnFocus = openOnFocus);
        }
    }
    
    /**
     * Returns the maximum search results to be returned by the search provider.
     * 
     * @return The max number of search results to return.
     */
    @PropertyGetter("maxSearchResults")
    public int getMaxSearchResults() {
        return this.maxSearchResults;
    }
    
    /**
     * Sets the maximum search results to be returned by the search provider.
     * 
     * @param maxSearchResults The max number of search results to return.
     */
    @PropertySetter("maxSearchResults")
    public void setMaxSearchResults(int maxSearchResults) {
        if (maxSearchResults != this.maxSearchResults) {
            sync("_maxResults", this.maxSearchResults = maxSearchResults);
            truncItem.setLabel(StrUtil.formatMessage(MESSAGE_TOO_MANY, maxSearchResults));
        }
    }
    
    /**
     * Returns the minimum number of characters that must be typed before search results are
     * displayed.
     * 
     * @return Minimum search characters.
     */
    @PropertyGetter("minSearchCharacters")
    public int getMinSearchCharacters() {
        return this.minSearchCharacters;
    }
    
    /**
     * Set the minimum number of characters that must be typed before search results are displayed
     * 
     * @param minSearchCharacters Minimum search characters.
     */
    @PropertySetter("minSearchCharacters")
    public void setMinSearchCharacters(int minSearchCharacters) {
        if (minSearchCharacters != this.minSearchCharacters) {
            sync("_minLength", this.minSearchCharacters = minSearchCharacters);
        }
    }
    
    /**
     * For search providers that support both client- and server-side searching, this parameter
     * determines which search strategy is employed based on the total number of searchable items.
     * When the number of searchable items exceeds this threshold, the server-based strategy is
     * employed.
     * 
     * @return The client search threshold.
     */
    @PropertyGetter("clientThreshold")
    public int getClientThreshold() {
        return clientThreshold;
    }
    
    /**
     * For search providers that support both client- and server-side searching, this parameter
     * determines which search strategy is employed based on the total number of searchable items.
     * When the number of searchable items exceeds this threshold, the server-based strategy is
     * employed.
     * 
     * @param clientThreshold The client search threshold.
     */
    @PropertySetter("clientThreshold")
    public void setClientThreshold(int clientThreshold) {
        if (this.clientThreshold != clientThreshold) {
            this.clientThreshold = clientThreshold;
            
            if (searchProvider != null) {
                init(false);
            }
        }
    }
    
    /**
     * Returns the match mode to be used for client-based searches.
     * 
     * @return The client match mode.
     */
    @PropertyGetter("clientMatchMode")
    public MatchMode getClientMatchMode() {
        return clientMatchMode;
    }
    
    /**
     * Sets the match mode to be used by the client. Does not affect server-based searching.
     * 
     * @param clientMatchMode The client match mode.
     */
    @PropertySetter("clientMatchMode")
    public void setClientMatchMode(MatchMode clientMatchMode) {
        if (clientMatchMode != this.clientMatchMode) {
            sync("_matchMode", this.clientMatchMode = clientMatchMode);
        }
    }
    
    /**
     * Returns the currently selected item.
     * 
     * @return The currently selected item.
     */
    public WonderbarItem getSelectedItem() {
        return selectedItem;
    }
    
    /**
     * Returns the data associated with the currently selected item.
     * 
     * @return Data of the currently selected item.
     */
    public Object getSelectedData() {
        return selectedItem == null ? null : selectedItem.getData();
    }
    
    /**
     * Selects the specified item on the client and fires an onWonderbarSelect event.
     * 
     * @param selectedItem The item to be selected.
     */
    public void setSelectedItem(WonderbarItem selectedItem) {
        setSelectedItem(selectedItem, true);
    }
    
    /**
     * Selects the specified item on the client and fires an onWonderbarSelect event.
     * 
     * @param selectedItem The item to be selected.
     * @param fireEvent If true, an onWonderbarSelect event will be fired.
     */
    public void setSelectedItem(WonderbarItem selectedItem, boolean fireEvent) {
        if (selectedItem != this.selectedItem) {
            if (selectedItem != null && (selectedItem.getParent() == null || selectedItem.getParent().getParent() != this)) {
                throw new ComponentException("Item does not belong to this parent.");
            }
            
            this.selectedItem = selectedItem;
            invoke("_selectItem", selectedItem);
            
            if (fireEvent) {
                EventUtil.post(WonderbarSelectEvent.TYPE, this, null);
            }
        }
    }
    
    /**
     * Creates a wonderbar item and sets it as the current selection.
     * 
     * @param label Label for the item.
     * @param data Data for the item.
     */
    public void setSelectedItem(String label, Object data) {
        setSelectedItem(label, data, true);
    }
    
    /**
     * Creates a wonderbar item and sets it as the current selection.
     * 
     * @param label Label for the item.
     * @param data Data for the item.
     * @param fireEvent If true, an onWonderbarSelect event will be fired.
     */
    public void setSelectedItem(String label, Object data, boolean fireEvent) {
        WonderbarItem item = new WonderbarItem(label, null, data);
        
        if (this.items == null) {
            addChild(new WonderbarItems());
        }
        
        this.items.addChild(item);
        setSelectedItem(item, fireEvent);
    }
    
    /**
     * Returns the search provider.
     * 
     * @return The search provider.
     */
    public IWonderbarSearchProvider<T> getSearchProvider() {
        return this.searchProvider;
    }
    
    /**
     * Set the search provider. This must be an instance of IWonderbarServerSearchProvider,
     * IWonderbarClientSearchProvider, or both.
     * 
     * @param searchProvider The search provider.
     */
    public void setSearchProvider(IWonderbarSearchProvider<T> searchProvider) {
        this.searchProvider = searchProvider;
        init(true);
    }
    
    /**
     * Renderer for searchable and default items.
     * 
     * @return The item renderer.
     */
    public IWonderbarItemRenderer<T> getItemRenderer() {
        return renderer;
    }
    
    /**
     * Sets the renderer for searchable and default items. If none is specified, a default renderer
     * will be used.
     * 
     * @param renderer The item renderer.
     */
    public void setItemRenderer(IWonderbarItemRenderer<T> renderer) {
        this.renderer = renderer;
    }
    
    /**
     * Initializes the component.
     * 
     * @param force If true, forces initialization even if mode does not change.
     */
    private void init(boolean force) {
        boolean isclient = searchProvider instanceof IWonderbarClientSearchProvider;
        boolean isserver = searchProvider instanceof IWonderbarServerSearchProvider;
        
        if (isclient && isserver) {
            isclient = ((IWonderbarClientSearchProvider<T>) searchProvider).getAllItems().size() <= getClientThreshold();
        }
        
        if (force || isclient != isClient) {
            setClientMode(isclient);
        }
    }
    
    /**
     * Returns true if the first selectable item in the wonder bar menu should be selected by
     * default.
     * 
     * @return The select first item setting.
     */
    @PropertyGetter("selectFirstItem")
    public boolean isSelectFirstItem() {
        return selectFirstItem;
    }
    
    /**
     * Set to true if the first selectable item in the wonder bar menu should be selected by
     * default.
     * 
     * @param selectFirstItem The select first item setting.
     */
    @PropertySetter("selectFirstItem")
    public void setSelectFirstItem(boolean selectFirstItem) {
        if (selectFirstItem != this.selectFirstItem) {
            sync("_selectFirst", this.selectFirstItem = selectFirstItem);
        }
    }
    
}
