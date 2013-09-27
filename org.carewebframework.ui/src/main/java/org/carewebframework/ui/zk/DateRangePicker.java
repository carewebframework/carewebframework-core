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

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.carewebframework.api.property.PropertyUtil;
import org.carewebframework.common.DateRange;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.SelectEvent;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;

/**
 * Generic component for choosing date ranges.
 */
public class DateRangePicker extends Combobox {
    
    private static final long serialVersionUID = 1L;
    
    public static final String ON_SELECT_RANGE = "onSelectRange";
    
    private static final String[] DEFAULT_CHOICES = { "All Dates", "Today|T|T", "Last Week|T|T-7", "Last Month|T|T-30|1",
            "Last Year|T|T-365", "Last Two Years|T|T-730" };
    
    private final Comboitem customItem;
    
    private Comboitem lastSelectedItem;
    
    private String prompt = "Select a date range...";
    
    public DateRangePicker() {
        super();
        setReadonly(true);
        setTooltiptext(prompt);
        customItem = new Comboitem();
        customItem.setLabel("Custom...");
        appendChild(customItem);
        setAllowCustom(false);
        loadChoices(DEFAULT_CHOICES);
    }
    
    /**
     * Load choices from the named property
     * 
     * @param propertyName Name of the property containing the choices. Must be in the acceptable
     *            format. If the property name is null, loads a default set of choices.
     */
    public void loadChoices(String propertyName) {
        if (propertyName == null || propertyName.isEmpty()) {
            loadChoices(DEFAULT_CHOICES);
        } else {
            loadChoices(PropertyUtil.getValues(propertyName, null));
        }
    }
    
    /**
     * Load choices from a string array.
     * 
     * @param choices A string array containing choices.
     */
    public void loadChoices(String... choices) {
        loadChoices(Arrays.asList(choices));
    }
    
    /**
     * Load choices from a list.
     * 
     * @param choices A list of choices.
     */
    public void loadChoices(Iterable<String> choices) {
        clear();
        
        if (choices != null) {
            for (String choice : choices) {
                addChoice(choice, false);
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
     * @return combo box item that was added (or found if duplicate custom item).
     */
    public Comboitem addChoice(DateRange range, boolean isCustom) {
        Comboitem item;
        
        if (isCustom) {
            item = findMatchingItem(range);
            
            if (item != null) {
                return item;
            }
        }
        
        item = new Comboitem();
        item.setLabel(range.getLabel());
        item.setValue(range);
        
        if (isCustom) {
            appendChild(item);
        } else {
            insertBefore(item, customItem);
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
     * @return combo box item that was added (or found if duplicate custom item).
     */
    public Comboitem addChoice(String range, boolean isCustom) {
        return addChoice(new DateRange(range), isCustom);
    }
    
    /**
     * Removes all items (except for "custom") from the item list.
     */
    public void clear() {
        List<Comboitem> items = getItems();
        
        for (int i = items.size() - 1; i >= 0; i--) {
            if (items.get(i) != customItem) {
                items.remove(i);
            }
        }
    }
    
    /**
     * Searches for a comboitem that has a date range equivalent to the specified range.
     * 
     * @param range The date range to locate.
     * @return A comboitem containing the date range, or null if not found.
     */
    public Comboitem findMatchingItem(DateRange range) {
        for (Comboitem item : getItems()) {
            if (range.equals(item.getValue())) {
                return item;
            }
        }
        
        return null;
    }
    
    /**
     * Searches for a comboitem that has a label that matches the specified value. The search is
     * case insensitive.
     * 
     * @param label Label text to find.
     * @return A comboitem with a matching label., or null if not found.
     */
    public Comboitem findMatchingItem(String label) {
        for (Comboitem item : getItems()) {
            if (label.equalsIgnoreCase(item.getLabel())) {
                return item;
            }
        }
        
        return null;
    }
    
    /**
     * Need to update visual appearance of selection when it is changed.
     * 
     * @see org.zkoss.zul.Combobox#setSelectedItem(org.zkoss.zul.Comboitem)
     */
    @Override
    public void setSelectedItem(Comboitem item) {
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
     * Returns the prompt to display when there is no selection.
     * 
     * @return The prompt
     */
    public String getPrompt() {
        return prompt;
    }
    
    /**
     * Sets the prompt to display when there is no selection.
     * 
     * @param prompt The prompt to set
     */
    public void setPrompt(String prompt) {
        this.prompt = prompt;
        setTooltiptext(prompt);
    }
    
    /**
     * Returns the date range item that is currently selected.
     * 
     * @return Selected date range item, or null if none selected.
     */
    public DateRange getSelectedRange() {
        Comboitem selected = getSelectedItem();
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
        Comboitem selectedItem = getSelectedItem();
        
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
        Comboitem selectedItem = getSelectedItem();
        
        if (selectedItem == null) {
            setText(prompt);
            setStyle("color:gray");
        } else {
            setStyle("color:inherit");
        }
        
        setFocus(false);
    }
    
    /**
     * onSelect event handler.
     * 
     * @param event
     */
    public void onSelect(Event event) {
        // When the custom range item is selected, triggers the display of the date range dialog.
        @SuppressWarnings("rawtypes")
        SelectEvent selEvent = (SelectEvent) event;
        
        if (selEvent.getReference() == customItem) {
            selEvent.stopPropagation();
            DateRange range = DateRangeDialog.show(this);
            setSelectedItem(range == null ? lastSelectedItem : addChoice(range, true));
            
        }
        
        checkSelection(false);
    }
    
    /**
     * If the last selected item has been removed, then remove the reference.
     */
    @Override
    public void onChildRemoved(Component child) {
        super.onChildRemoved(child);
        
        if (child == lastSelectedItem) {
            lastSelectedItem = null;
        }
    }
}
