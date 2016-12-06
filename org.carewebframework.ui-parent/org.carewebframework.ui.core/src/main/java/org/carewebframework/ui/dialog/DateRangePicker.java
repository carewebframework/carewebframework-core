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
package org.carewebframework.ui.dialog;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.carewebframework.api.property.PropertyUtil;
import org.carewebframework.common.DateRange;
import org.carewebframework.web.annotation.EventHandler;
import org.carewebframework.web.component.BaseComponent;
import org.carewebframework.web.component.Combobox;
import org.carewebframework.web.component.Comboitem;
import org.carewebframework.web.event.Event;
import org.carewebframework.web.event.EventUtil;

/**
 * Generic component for choosing date ranges.
 */
public class DateRangePicker extends Combobox {
    
    public static final String ON_SELECT_RANGE = "selectRange";
    
    private static final String[] DEFAULT_CHOICES = { "All Dates", "Today|T|T", "Last Week|T|T-7", "Last Month|T|T-30|1",
            "Last Year|T|T-365", "Last Two Years|T|T-730" };
    
    private final Comboitem customItem;
    
    private Comboitem lastSelectedItem;
    
    private String prompt = "Select a date range...";
    
    public DateRangePicker() {
        super();
        setReadonly(true);
        setHint(prompt);
        customItem = new Comboitem();
        customItem.setLabel("Custom...");
        addChild(customItem);
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
        item.setData(range);
        addChild(item, isCustom ? null : customItem);
        
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
    @Override
    public void clear() {
        List<BaseComponent> items = getChildren();
        
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
        for (BaseComponent item : getChildren()) {
            if (range.equals(item.getData())) {
                return (Comboitem) item;
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
        for (BaseComponent child : getChildren()) {
            Comboitem item = (Comboitem) child;
            
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
            BaseComponent sibling;
            
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
        setHint(prompt);
    }
    
    /**
     * Returns the date range item that is currently selected.
     * 
     * @return Selected date range item, or null if none selected.
     */
    public DateRange getSelectedRange() {
        Comboitem selected = getSelectedItem();
        return selected == null ? null : (DateRange) selected.getData();
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
                EventUtil.send(new Event(ON_SELECT_RANGE, this));
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
            setValue(prompt);
            addStyle("color", "gray");
        } else {
            addStyle("color", "inherit");
        }
        
        setFocus(false);
    }
    
    /**
     * onSelect event handler.
     * 
     * @param event The onSelect event.
     */
    @EventHandler("select")
    private void onSelect(Event event) {
        // When the custom range item is selected, triggers the display of the date range dialog.
        if (event.getTarget() == customItem) {
            event.stopPropagation();
            DateRangeDialog.show((range) -> {
                setSelectedItem(range == null ? lastSelectedItem : addChoice(range, true));
                checkSelection(false);
            });
            
        } else {
            checkSelection(false);
        }
    }
    
    /**
     * If the last selected item has been removed, then remove the reference.
     */
    @Override
    public void afterRemoveChild(BaseComponent child) {
        super.afterRemoveChild(child);
        
        if (child == lastSelectedItem) {
            lastSelectedItem = null;
        }
    }
}
