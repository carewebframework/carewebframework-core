/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.ui.wonderbar;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.zkoss.json.JSONAware;
import org.zkoss.text.MessageFormats;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.au.AuRequest;
import org.zkoss.zk.au.out.AuInvoke;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.UiException;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.sys.ContentRenderer;
import org.zkoss.zul.impl.InputElement;

/**
 * This is the main ZK component for the wonder bar. It supports both server-side searching for
 * searching through large lists of items and client/browser-side searching for smaller lists of
 * items.
 * 
 * @param <T> Type returned by search provider.
 */
public class Wonderbar<T> extends InputElement {
    
    /**
     * Controls behavior of client-side searches.
     */
    public enum MatchMode implements JSONAware {
        ANY_ORDER, // May match any any order
        SAME_ORDER, // Must match in same order, not necessarily contiguous
        ADJACENT, // Must match in same contiguous order, not necessarily at start
        FROM_START; // Must match in same order at the start
        
        @Override
        public String toJSONString() {
            return Integer.toString(ordinal());
        }
    };
    
    private static final long serialVersionUID = 1L;
    
    private final String MESSAGE_TOO_MANY = Labels.getLabel("cwf.wonderbar.items.truncated",
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
    
    static {
        addClientEvent(Wonderbar.class, WonderbarSelectEvent.ON_WONDERBAR_SELECT, CE_IMPORTANT | CE_NON_DEFERRABLE);
        addClientEvent(Wonderbar.class, WonderbarSearchEvent.ON_WONDERBAR_SEARCH, CE_IMPORTANT | CE_NON_DEFERRABLE);
        addClientEvent(Wonderbar.class, Events.ON_FOCUS, CE_DUPLICATE_IGNORE);
        addClientEvent(Wonderbar.class, Events.ON_BLUR, CE_DUPLICATE_IGNORE);
        addClientEvent(Wonderbar.class, Events.ON_ERROR, CE_DUPLICATE_IGNORE | CE_IMPORTANT);
    }
    
    public Wonderbar() {
        super();
        truncItem = new WonderbarGroup();
        truncItem.setZclass("cwf-wonderbar-item-more");
        setMaxSearchResults(100);
    }
    
    @Override
    public void renderProperties(ContentRenderer renderer) throws IOException {
        super.renderProperties(renderer);
        renderer.render("_minLength", minSearchCharacters);
        renderer.render("_maxResults", maxSearchResults);
        renderer.render("_openOnFocus", openOnFocus);
        renderer.render("_skipTab", changeOnOKOnly);
        renderer.render("_selectFirst", selectFirstItem);
        renderer.render("_clientMode", isClient);
        renderer.render("_matchMode", clientMatchMode);
        renderer.render("_truncItem", truncItem);
    }
    
    @Override
    public String getZclass() {
        return _zclass == null ? "cwf-wonderbar" : _zclass;
    }
    
    /**
     * Processes the onWonderbarSelect and onWonderbarSearch events.
     */
    @Override
    public void service(AuRequest request, boolean everError) {
        String cmd = request.getCommand();
        
        if (cmd.equals(WonderbarSelectEvent.ON_WONDERBAR_SELECT)) {
            WonderbarSelectEvent event = WonderbarSelectEvent.getSelectEvent(request);
            
            if (selectedItem != event.getSelectedItem()) {
                selectedItem = event.getSelectedItem();
                Events.postEvent(event);
            }
        } else if (cmd.equals(WonderbarSearchEvent.ON_WONDERBAR_SEARCH)) {
            WonderbarSearchEvent event = WonderbarSearchEvent.getSearchEvent(request);
            Events.postEvent(event);
        } else {
            super.service(request, everError);
        }
    }
    
    /**
     * Validates attempt to add a child.
     */
    @Override
    public void beforeChildAdded(Component child, Component insertBefore) {
        super.beforeChildAdded(child, insertBefore);
        
        if (child instanceof WonderbarDefaults) {
            if (defaultItems != null && defaultItems != child) {
                throw new UiException("Default items already specified.");
            }
        } else if (child instanceof WonderbarItems) {
            if (items != null && items != child) {
                throw new UiException("Items already specified.");
            }
        } else {
            throw new UiException("Unsupported child for Wonderbar: " + child);
        }
    }
    
    /**
     * Updates defaultItems and items when a child is added.
     */
    @Override
    public boolean insertBefore(Component child, Component refChild) {
        if (child instanceof WonderbarDefaults) {
            if (super.insertBefore(child, refChild)) {
                defaultItems = (WonderbarDefaults) child;
                return true;
            }
        } else if (child instanceof WonderbarItems) {
            if (super.insertBefore(child, refChild)) {
                items = (WonderbarItems) child;
                return true;
            }
        } else {
            return super.insertBefore(child, refChild);
        }
        
        return false;
    }
    
    /**
     * Updates defaultItems and items when a child is removed.
     */
    @Override
    public void beforeChildRemoved(Component child) {
        super.beforeChildRemoved(child);
        
        if (defaultItems == child) {
            defaultItems = null;
        } else if (items == child) {
            items = null;
        }
    }
    
    @Override
    protected Object coerceFromString(String value) throws WrongValueException {
        return value == null ? "" : value;
    }
    
    @Override
    protected String coerceToString(Object value) {
        return value == null ? "" : value.toString();
    }
    
    /**
     * Clear the current selection.
     */
    public void clear() {
        setText("");
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
            setText(term);
            invoke("search", term);
            focus();
        } else {
            final IWonderbarServerSearchProvider<T> provider = (IWonderbarServerSearchProvider<T>) getSearchProvider();
            
            if (provider != null) {
                List<T> hits = new ArrayList<T>();
                boolean tooMany = !provider.getSearchResults(term, maxSearchResults, hits);
                initItems(hits, tooMany, false);
                invoke("_serverResponse", term);
            }
        }
    }
    
    /**
     * Handles the client request for a server-based search.
     * 
     * @param event
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
    
    /**
     * Invokes a function on the client.
     * 
     * @param fnc
     * @param args
     */
    private void invoke(String fnc, Object... args) {
        response(new AuInvoke(this, fnc, args));
    }
    
    @Override
    public String getWidgetClass() {
        return "wonderbar.ext.Wonderbar";
    }
    
    @Override
    protected boolean isChildable() {
        return true;
    }
    
    public WonderbarDefaults getDefaultItems() {
        return this.defaultItems;
    }
    
    @Override
    public void onPageAttached(Page newpage, Page oldpage) {
        super.onPageAttached(newpage, oldpage);
        truncItem.setPage(newpage);
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
        smartUpdate("_clientMode", value);
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
                parent.clear();
            } else {
                parent = defaults ? new WonderbarDefaults() : new WonderbarItems();
                appendChild(parent);
            }
            
            for (T data : list) {
                WonderbarItem item = new WonderbarItem();
                parent.appendChild(item);
                
                if (renderer != null) {
                    renderer.render(item, data, parent.getChildren().size() - 1);
                } else {
                    item.setLabel(data.toString());
                    item.setValue(data.toString());
                }
            }
            
            if (tooMany) {
                parent.appendChild(truncItem);
            }
        } else if (parent != null) {
            parent.clear();
        }
        
        selectedItem = null;
    }
    
    /**
     * Returns true if item selection occurs only when the enter key is pressed, or false if item
     * selection also occurs when tabbing away from the component.
     * 
     * @return
     */
    public boolean getChangeOnOKOnly() {
        return changeOnOKOnly;
    }
    
    /**
     * Sets item selection behavior. Set to true if item selection occurs only when the enter key is
     * pressed, or false if item selection also occurs when tabbing away from the component.
     * 
     * @param value
     */
    public void setChangeOnOKOnly(boolean value) {
        this.changeOnOKOnly = value;
        smartUpdate("_skipTab", value);
    }
    
    /**
     * Returns true if the wonder bar menu should appear when the component receives focus.
     * 
     * @return
     */
    public boolean getOpenOnFocus() {
        return openOnFocus;
    }
    
    /**
     * Set to true if the wonder bar menu should appear when the component receives focus.
     * 
     * @param value
     */
    public void setOpenOnFocus(boolean value) {
        this.openOnFocus = value;
        smartUpdate("_openOnFocus", value);
    }
    
    /**
     * Returns the maximum search results to be returned by the search provider.
     * 
     * @return The max number of search results to return.
     */
    public int getMaxSearchResults() {
        return this.maxSearchResults;
    }
    
    /**
     * Sets the maximum search results to be returned by the search provider.
     * 
     * @param value The max number of search results to return.
     */
    public void setMaxSearchResults(int value) {
        this.maxSearchResults = value;
        smartUpdate("_maxResults", value);
        truncItem.setLabel(MessageFormats.format(MESSAGE_TOO_MANY, new Object[] { value }));
    }
    
    /**
     * Returns the minimum number of characters that must be typed before search results are
     * displayed.
     * 
     * @return
     */
    public int getMinSearchCharacters() {
        return this.minSearchCharacters;
    }
    
    /**
     * Set the minimum number of characters that must be typed before search results are displayed
     * 
     * @param value
     */
    public void setMinSearchCharacters(int value) {
        this.minSearchCharacters = value;
        smartUpdate("_minLength", value);
    }
    
    /**
     * For search providers that support both client- and server-side searching, this parameter
     * determines which search strategy is employed based on the total number of searchable items.
     * When the number of searchable items exceeds this threshold, the server-based strategy is
     * employed.
     * 
     * @return
     */
    public int getClientThreshold() {
        return clientThreshold;
    }
    
    /**
     * For search providers that support both client- and server-side searching, this parameter
     * determines which search strategy is employed based on the total number of searchable items.
     * When the number of searchable items exceeds this threshold, the server-based strategy is
     * employed.
     * 
     * @param value The client search threshold.
     */
    public void setClientThreshold(int value) {
        if (this.clientThreshold != value) {
            this.clientThreshold = value;
            
            if (searchProvider != null) {
                init(false);
            }
        }
    }
    
    /**
     * Returns the match mode to be used for client-based searches.
     * 
     * @return
     */
    public MatchMode getClientMatchMode() {
        return clientMatchMode;
    }
    
    /**
     * Sets the match mode to be used by the client. Does not affect server-based searching.
     * 
     * @param clientMatchMode
     */
    public void setClientMatchMode(MatchMode clientMatchMode) {
        this.clientMatchMode = clientMatchMode;
        smartUpdate("_matchMode", clientMatchMode);
    }
    
    public String getValue() throws WrongValueException {
        return getText();
    }
    
    public void setValue(final String value) throws WrongValueException {
        setText(value);
    }
    
    /**
     * Returns the currently selected item.
     * 
     * @return
     */
    public WonderbarItem getSelectedItem() {
        return selectedItem;
    }
    
    /**
     * Returns the data associated with the currently selected item.
     * 
     * @return
     */
    public Object getSelectedData() {
        return selectedItem == null ? null : selectedItem.getData();
    }
    
    /**
     * Selects the specified item on the client and fires an onWonderbarSelect event.
     * 
     * @param selectedItem
     */
    public void setSelectedItem(WonderbarItem selectedItem) {
        setSelectedItem(selectedItem, true);
    }
    
    /**
     * Selects the specified item on the client and fires an onWonderbarSelect event.
     * 
     * @param selectedItem
     * @param fireEvent If true, an onWonderbarSelect event will be fired.
     */
    public void setSelectedItem(WonderbarItem selectedItem, boolean fireEvent) {
        if (selectedItem != this.selectedItem) {
            if (selectedItem != null && (selectedItem.getParent() == null || selectedItem.getParent().getParent() != this)) {
                throw new UiException("Item does not belong to this parent.");
            }
            
            this.selectedItem = selectedItem;
            invoke("_selectItem", selectedItem);
            
            if (fireEvent) {
                Events.postEvent(WonderbarSelectEvent.ON_WONDERBAR_SELECT, this, null);
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
            appendChild(new WonderbarItems());
        }
        
        this.items.appendChild(item);
        setSelectedItem(item, fireEvent);
    }
    
    /**
     * Returns the search provider.
     * 
     * @return
     */
    public IWonderbarSearchProvider<T> getSearchProvider() {
        return this.searchProvider;
    }
    
    /**
     * Set the search provider. This must be an instance of IWonderbarServerSearchProvider,
     * IWonderbarClientSearchProvider, or both.
     * 
     * @param searchProvider
     */
    public void setSearchProvider(final IWonderbarSearchProvider<T> searchProvider) {
        this.searchProvider = searchProvider;
        init(true);
    }
    
    /**
     * Renderer for searchable and default items.
     * 
     * @return
     */
    public IWonderbarItemRenderer<T> getItemRenderer() {
        return renderer;
    }
    
    /**
     * Sets the renderer for searchable and default items. If none is specified, a default renderer
     * will be used.
     * 
     * @param renderer
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
     * @return
     */
    public boolean isSelectFirstItem() {
        return selectFirstItem;
    }
    
    /**
     * Set to true if the first selectable item in the wonder bar menu should be selected by
     * default.
     * 
     * @param value
     */
    public void setSelectFirstItem(boolean value) {
        this.selectFirstItem = value;
        smartUpdate("_selectFirst", value);
    }
    
}
