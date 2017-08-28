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

import org.fujion.annotation.Component.PropertyGetter;
import org.fujion.annotation.Component.PropertySetter;

/**
 * A selectable wonder bar search item.
 */
public class WonderbarItem extends WonderbarAbstractItem {
    
    private int choiceNumber; // this would be the value that the user keys
    
    private String searchTerm;
    
    private String uniqueKey;
    
    private int uniquePriority;
    
    private String category;
    
    public WonderbarItem() {
    }
    
    public WonderbarItem(String label) {
        super(label);
    }
    
    public WonderbarItem(String label, String value) {
        super(label, value);
    }
    
    public WonderbarItem(String label, String value, Object data) {
        super(label, value, data);
    }
    
    /**
     * Returns the choice number for this item. When the user enters a positive integer value that
     * matches the choice number, that item is highlighted in the wonder bar menu.
     * 
     * @return The choice number.
     */
    @PropertyGetter("choiceNumber")
    public int getChoiceNumber() {
        return choiceNumber;
    }
    
    /**
     * Sets the choice number for this item. When the user enters a positive integer value that
     * matches the choice number, that item is highlighted in the wonder bar menu.
     * 
     * @param choiceNumber The choice number. Non-positive values suppress this function.
     */
    @PropertySetter("choiceNumber")
    public void setChoiceNumber(int choiceNumber) {
        if (choiceNumber != this.choiceNumber) {
            sync("choiceNumber", this.choiceNumber = choiceNumber);
        }
    }
    
    /**
     * Returns the term to use for comparison during searches. This can be used when the label
     * itself is not suitable for searching (e.g., it contains special formatting for display
     * purposes).
     * 
     * @return The search term.
     */
    @PropertyGetter("searchTerm")
    public String getSearchTerm() {
        return searchTerm;
    }
    
    /**
     * Sets the term to use for comparison during searches. This can be used when the label itself
     * is not suitable for searching (e.g., it contains special formatting for display purposes).
     * 
     * @param searchTerm The search term.
     */
    @PropertySetter("searchTerm")
    public void setSearchTerm(String searchTerm) {
        if (!areEqual(searchTerm, this.searchTerm)) {
            sync("searchTerm", this.searchTerm = searchTerm);
        }
    }
    
    /**
     * Unique key identifying the item. This will be used to filter out items that have the same
     * unique key from displaying in the list.
     * 
     * @return The unique key.
     */
    @PropertyGetter("uniqueKey")
    public String getUniqueKey() {
        return uniqueKey;
    }
    
    /**
     * Sets the unique key identifying the item. This will be used to filter out items that have the
     * same unique key from displaying in the list.
     * 
     * @param uniqueKey The unique key.
     */
    @PropertySetter("uniqueKey")
    public void setUniqueKey(String uniqueKey) {
        if (!areEqual(uniqueKey, this.uniqueKey)) {
            sync("uniqueKey", this.uniqueKey = uniqueKey);
        }
    }
    
    /**
     * Priority used to determine which items with the same unique key to keep in the search list.
     * The matching item with the greatest priority will be kept.
     * 
     * @return The unique priority.
     */
    @PropertyGetter("uniquePriority")
    public int getUniquePriority() {
        return uniquePriority;
    }
    
    /**
     * Sets the priority used to determine which items with the same unique key to keep in the
     * search list. The matching item with the greatest priority will be kept.
     * 
     * @param uniquePriority The unique priority.
     */
    @PropertySetter("uniquePriority")
    public void setUniquePriority(int uniquePriority) {
        if (uniquePriority != this.uniquePriority) {
            sync("uniquePriority", this.uniquePriority = uniquePriority);
        }
    }
    
    /**
     * Returns the category to which this item belongs. In default rendering, the category appears
     * to the right of the item in the wonder bar menu and is not re-rendered for consecutive items
     * with the same category.
     * 
     * @return The category.
     */
    @PropertyGetter("category")
    public String getCategory() {
        return category;
    }
    
    /**
     * Sets the category to which this item belongs. In default rendering, the category appears to
     * the right of the item in the wonder bar menu and is not re-rendered for consecutive items
     * with the same category.
     * 
     * @param category The category.
     */
    @PropertySetter("category")
    public void setCategory(String category) {
        if (!areEqual(category, this.category)) {
            sync("category", this.category = category);
        }
    }
    
    @Override
    public String getLabel() {
        return super.getLabel();
    }
    
    @Override
    public void setLabel(String label) {
        super.setLabel(label);
    }
    
    @Override
    public String getValue() {
        return super.getValue();
    }
    
    @Override
    public void setValue(String value) {
        super.setValue(value);
    }
}
