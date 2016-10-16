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

import java.util.Date;
import java.util.List;

import org.carewebframework.api.property.PropertyUtil;
import org.carewebframework.common.DateRange;
import org.carewebframework.web.component.BaseComponent;
import org.carewebframework.web.component.Listbox;
import org.carewebframework.web.component.Listitem;

/**
 * Generic component for choosing date ranges.
 */
public class DateRangeChooser extends Listbox {
    
    private static final long serialVersionUID = 1L;
    
    public static final String ON_SELECT_RANGE = "onSelectRange";
    
    private final Listitem customItem;
    
    private Listitem lastSelectedItem;
    
    public DateRangeChooser() {
        super();
        customItem = new Listitem();
        customItem.setLabel("Custom...");
        addChild(customItem);
        setAllowCustom(false);
        loadChoices(null);
    }
    
    /**
     * Load choices from the named property
     * 
     * @param propertyName Name of the property containing the choices. Must be in the acceptable
     *            format. If the property name is null, loads a default set of choices.
     */
    public void loadChoices(String propertyName) {
        clear();
        
        if (propertyName == null || propertyName.isEmpty()) {
            addChoice("All Dates", false);
            addChoice("Today|T|T", false);
            addChoice("Last Week|T|T-7", false);
            addChoice("Last Month|T|T-30|1", false);
            addChoice("Last Year|T|T-365", false);
            addChoice("Last Two Years|T|T-730", false);
        } else {
            for (String value : PropertyUtil.getValues(propertyName, null)) {
                addChoice(value, false);
            }
        }
        
        checkSelection(true);
    }
    
    /**
     * Adds a date range to the choice list.
     * 
     * @param range Date range item
     * @param isCustom If true, range is a custom item. In this case, if another matching custom
     *            item exists, it will not be added.
     * @return List item that was added (or found if duplicate custom item).
     */
    public Listitem addChoice(DateRange range, boolean isCustom) {
        Listitem item;
        
        if (isCustom) {
            item = findMatchingItem(range);
            
            if (item != null) {
                return item;
            }
        }
        
        item = new Listitem();
        item.setLabel(range.getLabel());
        item.setData(range);
        
        if (isCustom) {
            addChild(item);
        } else {
            insertChild(item, customItem);
        }
        
        if (range.isDefault()) {
            setSelectedItem(item);
        }
        
        return item;
    }
    
    /**
     * Adds a date range item to the choice list from its string representation (see DateRange class
     * for format).
     * 
     * @param range String representation of date range.
     * @param isCustom If true, range is a custom item. In this case, if another matching custom
     *            item exists, it will not be added.
     * @return List item that was added (or found if duplicate custom item).
     */
    public Listitem addChoice(String range, boolean isCustom) {
        return addChoice(new DateRange(range), isCustom);
    }
    
    /**
     * Removes all items (except for "custom") from the item list.
     */
    public void clear() {
        List<Listitem> items = getChildren(Listitem.class);
        
        for (int i = items.size() - 1; i >= 0; i--) {
            if (items.get(i) != customItem) {
                items.remove(i);
            }
        }
    }
    
    /**
     * Searches for a Listitem that has a date range equivalent to the specified range.
     * 
     * @param range The date range to locate.
     * @return A Listitem containing the date range, or null if not found.
     */
    public Listitem findMatchingItem(DateRange range) {
        for (BaseComponent item : getChildren()) {
            if (range.equals(item.getData())) {
                return (Listitem) item;
            }
        }
        
        return null;
    }
    
    /**
     * Searches for a Listitem that has a label that matches the specified value. The search is case
     * insensitive.
     * 
     * @param label Label text to find.
     * @return A Listitem with a matching label., or null if not found.
     */
    public Listitem findMatchingItem(String label) {
        for (BaseComponent item : getChildren()) {
            if (label.equalsIgnoreCase(item.getLabel())) {
                return item;
            }
        }
        
        return null;
    }
    
    /**
     * Need to update visual appearance of selection when it is changed.
     * 
     * @see org.zkoss.zul.Listbox#setSelectedItem(org.zkoss.zul.Listitem)
     */
    @Override
    public void setSelectedItem(Listitem item) {
        super.setSelectedItem(item);
        updateSelection();
    }
    
    /**
     * If set to true, the user may select "Custom..." from the choice list and enter a custom date
     * range. If set to false, this choice is hidden and any existing custom entries are removed
     * from the list.
     * 
     * @param allowCustom Determines whether or not custom entries are allowed.
     */
    public void setAllowCustom(boolean allowCustom) {
        customItem.setVisible(allowCustom);
        
        if (!allowCustom) {
            Component sibling;
            
            while ((sibling = customItem.getNextSibling()) != null) {
                removeChild(sibling);
            }
        }
    }
    
    /**
     * Returns true if custom ranges are allowed.
     * 
     * @return True if custom ranges are allowed.
     */
    public boolean isAllowCustom() {
        return customItem.isVisible();
    }
    
    /**
     * Returns the date range item that is currently selected.
     * 
     * @return Selected date range item, or null if none selected.
     */
    public DateRange getSelectedRange() {
        Listitem selected = getSelectedItem();
        return selected == null ? null : (DateRange) selected.getValue();
    }
    
    /**
     * Returns the selected start date. This may be null if there is no active selection or if the
     * selected date range has no start date.
     * 
     * @return Starting date of range, or null.
     */
    public Date getStartDate() {
        DateRange range = getSelectedRange();
        return range == null ? null : range.getStartDate();
    }
    
    /**
     * Returns the selected end date. This may be null if there is no active selection or if the
     * selected date range has no end date.
     * 
     * @return Ending date of range, or null.
     */
    public Date getEndDate() {
        DateRange range = getSelectedRange();
        return range == null ? null : range.getEndDate();
    }
    
    /**
     * Check the current selection. If nothing is selected, display a prompt message in gray text.
     * Also, remembers the last selection made.
     * 
     * @param suppressEvent If true, onSelectRange event is not fired.
     */
    private void checkSelection(boolean suppressEvent) {
        Listitem selectedItem = getSelectedItem();
        
        if (selectedItem == null) {
            selectedItem = lastSelectedItem;
            setSelectedItem(selectedItem);
        } else if (selectedItem != customItem && lastSelectedItem != selectedItem) {
            lastSelectedItem = selectedItem;
            
            if (!suppressEvent) {
                Events.sendEvent(new Event(ON_SELECT_RANGE, this));
            }
        }
        
        updateSelection();
    }
    
    /**
     * Updates the visual appearance of the current selection.
     */
    private void updateSelection() {
    }
    
    /**
     * onSelect event handler.
     * 
     * @param event The onSelect event.
     */
    public void onSelect(Event event) {
        /**
         * When the custom range item is selected, triggers the display of the date range dialog.
         */
        if (getSelectedItem() == customItem) {
            event.stopPropagation();
            DateRange range = DateRangeDialog.show(this);
            
            if (range == null) {
                setSelectedItem(lastSelectedItem);
            } else {
                setSelectedItem(addChoice(range, true));
            }
        }
        
        checkSelection(false);
    }
}
