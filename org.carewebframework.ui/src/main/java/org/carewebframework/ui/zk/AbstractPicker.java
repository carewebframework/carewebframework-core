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
import java.util.List;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Bandbox;
import org.zkoss.zul.Bandpopup;
import org.zkoss.zul.Div;
import org.zkoss.zul.Hlayout;
import org.zkoss.zul.Panel;
import org.zkoss.zul.Panelchildren;

/**
 * Base class for grid-based item pickers.
 * 
 * @param <T> Type of item.
 */
public abstract class AbstractPicker<T extends Component> extends Bandbox {
    
    private static final long serialVersionUID = 1L;
    
    public static final String ON_SELECT_ITEM = "onSelectItem";
    
    protected static final String NO_CHOICE_URL = "~./org/carewebframework/ui/zk/no-choice.png";
    
    private T selectedItem;
    
    private boolean autoAdd;
    
    private int itemsPerRow;
    
    protected final Panel panel;
    
    private final T itemNoChoice;
    
    private final List<T> items = new ArrayList<T>();
    
    /**
     * Click event listener for each item. Causes item to be selected when it is clicked.
     */
    private final EventListener<Event> clickListener = new EventListener<Event>() {
        
        @SuppressWarnings("unchecked")
        @Override
        public void onEvent(Event event) throws Exception {
            doSelectItem((T) event.getTarget(), true);
        }
        
    };
    
    /**
     * Creates the item picker instance and all required child components.
     * 
     * @param sclass Sclass to be applied to band popup component (may be null).
     * @param itemNoChoice Item representing no choice (may be null).
     */
    public AbstractPicker(String sclass, T itemNoChoice) {
        super();
        this.itemNoChoice = itemNoChoice;
        setReadonly(true);
        Bandpopup bp = new Bandpopup();
        appendChild(bp);
        panel = new Panel();
        ZKUtil.updateSclass(panel, "cwf-picker", false);
        bp.appendChild(panel);
        Panelchildren pc = new Panelchildren();
        panel.appendChild(pc);
        setItemsPerRow(20);
        
        if (itemNoChoice != null) {
            itemNoChoice.addEventListener(Events.ON_CLICK, clickListener);
            doSelectItem(itemNoChoice, false);
        }
    }
    
    /**
     * Removes all items from model.
     */
    public void clear() {
        items.clear();
        render();
    }
    
    /**
     * Adds an item.
     * 
     * @param item Item to add.
     */
    public void addItem(T item) {
        _addItem(item);
        render();
    }
    
    /**
     * Adds multiple items.
     * 
     * @param items Items to add.
     */
    public void addItems(List<T> items) {
        for (T item : items) {
            _addItem(item);
        }
        
        render();
    }
    
    /**
     * Adds an item to the model. Prepares the item before adding. If the model is empty and a no
     * choice item exists, the item will be added to the list. Note that this does not trigger a
     * re-rendering.
     * 
     * @param item Item to add.
     */
    protected void _addItem(T item) {
        if (itemNoChoice != null && items.isEmpty()) {
            items.add(itemNoChoice);
        }
        
        items.add(prepItem(item));
    }
    
    /**
     * Updates the model. The model entry consists of the index of the first item to appear on each
     * row.
     */
    protected void render() {
        Panelchildren pc = panel.getPanelchildren();
        ZKUtil.detachChildren(pc);
        
        for (int i = 0; i < items.size(); i += itemsPerRow) {
            int max = Math.min(i + itemsPerRow, items.size());
            Hlayout row = new Hlayout();
            pc.appendChild(row);
            
            for (int j = i; j < max; j++) {
                Div div = new Div();
                div.setSclass("cwf-picker-cell");
                row.appendChild(div);
                div.appendChild(items.get(j));
            }
        }
        
        Clients.resize(this);
    }
    
    /**
     * Selects an item.
     * 
     * @param item Item to be selected
     * @param fireEvent If false, suppress firing of onSelectItem event.
     */
    protected void doSelectItem(T item, boolean fireEvent) {
        close();
        
        if (item != selectedItem) {
            selectedItem = item == itemNoChoice ? null : item;
            setText(selectedItem == null ? "none" : getItemText(item));
            
            if (fireEvent) {
                Events.postEvent(new Event(ON_SELECT_ITEM, this, selectedItem));
            }
        }
    }
    
    /**
     * This is the text to appear in the bandbox when an item is selected.
     * 
     * @param item The item.
     * @return Text to display.
     */
    protected abstract String getItemText(T item);
    
    /**
     * Prepares an item before adding to the model. In this minimal implementation, adds the click
     * handler to the item. Override to provide any additional preparation to the item.
     * 
     * @param item Item being prepared.
     * @return The prepared item.
     */
    protected T prepItem(T item) {
        item.addEventListener(Events.ON_CLICK, clickListener);
        return item;
    }
    
    /**
     * Override to perform alternate comparison.
     * 
     * @param item1 First item to compare.
     * @param item2 Second item to compare.
     * @return Result of comparison.
     */
    protected boolean itemsAreEqual(T item1, T item2) {
        return item1 == item2 || item1.equals(item2);
    }
    
    /**
     * Searches for the specified item, returning its index in the model.
     * 
     * @param item Item to find.
     * @param add If true and item not found, it will be added.
     * @return The index of the item if found; -1 if not.
     */
    public int findItem(T item, boolean add) {
        for (int i = 0; i < items.size(); i++) {
            if (itemsAreEqual(item, items.get(i))) {
                return i;
            }
        }
        
        if (add) {
            addItem(item);
            return items.size() - 1;
        }
        
        return -1;
    }
    
    /**
     * Sets the autoAdd setting.
     * 
     * @param autoAdd If true, an item is automatically added if it does not already exist when the
     *            setSelectedItem call is made.
     */
    public void setAutoAdd(boolean autoAdd) {
        this.autoAdd = autoAdd;
    }
    
    /**
     * Returns the autoAdd setting.
     * 
     * @return The autoAdd setting.
     */
    public boolean isAutoAdd() {
        return autoAdd;
    }
    
    /**
     * Returns list of items in model.
     * 
     * @return Items in model.
     */
    public List<T> getItems() {
        return items;
    }
    
    /**
     * Returns the value of the currently selected item, or null if none is selected.
     * 
     * @return Currently selected item.
     */
    public T getSelectedItem() {
        return selectedItem;
    }
    
    /**
     * Set the selected item to the specified value.
     * 
     * @param item Item to set as current.
     */
    public void setSelectedItem(T item) {
        if (item != null) {
            int i = findItem(item, autoAdd);
            
            if (i == -1) {
                throw new RuntimeException("Item is not registered to this picker.");
            }
            item = items.get(i);
        }
        
        doSelectItem(item, false);
    }
    
    /**
     * Returns the number of items to display per row.
     * 
     * @return Number of items per row.
     */
    public int getItemsPerRow() {
        return itemsPerRow;
    }
    
    /**
     * Sets the number of items to display per row. Must be greater than zero.
     * 
     * @param itemsPerRow Number of items per row.
     */
    public void setItemsPerRow(int itemsPerRow) {
        itemsPerRow = itemsPerRow < 1 ? 1 : itemsPerRow;
        
        if (this.itemsPerRow != itemsPerRow) {
            this.itemsPerRow = itemsPerRow;
            render();
        }
    }
    
}
