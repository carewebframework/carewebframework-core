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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.carewebframework.web.component.Listbox;
import org.carewebframework.web.component.Listitem;

/**
 * Useful combo and list box functions.
 */
public class ListUtil {
    
    /**
     * Used for restoring list items to original order.
     */
    private static final Comparator<Listitem> listItemComparator = new Comparator<Listitem>() {
        
        @Override
        public int compare(Listitem i1, Listitem i2) {
            return i1.indexOf() - i2.indexOf();
        }
        
    };
    
    /**
     * Finds an item with a matching label. Searches are case-insensitive.
     * 
     * @param cbo Combo box to search
     * @param label Label value being sought.
     * @return The index of the matching item, or -1 if no match found.
     */
    public static int findComboboxItem(Combobox cbo, String label) {
        return findComboboxItem(cbo, label.toLowerCase(), true);
    }
    
    /**
     * Finds and selects an item with a matching label. Searches are case-insensitive.
     * 
     * @param cbo Combo box to search
     * @param label Label value being sought.
     * @return The index of the matching item, or -1 if no match found.
     */
    public static int selectComboboxItem(Combobox cbo, String label) {
        int i = findComboboxItem(cbo, label);
        cbo.setSelectedIndex(i);
        return i;
    }
    
    /**
     * Finds an item whose associated value matches the specified object.
     * 
     * @param cbo Combo box to search
     * @param data Object being sought.
     * @return The index of the matching item, or -1 if no match found.
     */
    public static int findComboboxData(Combobox cbo, Object data) {
        return findComboboxItem(cbo, data, false);
    }
    
    /**
     * Finds and selects an item whose associated value matches the specified object.
     * 
     * @param cbo Combo box to search
     * @param data Object being sought.
     * @return The index of the matching item, or -1 if no match found.
     */
    public static int selectComboboxData(Combobox cbo, Object data) {
        int i = findComboboxData(cbo, data);
        cbo.setSelectedIndex(i);
        return i;
    }
    
    /**
     * Finds the matching object in the item list of the combo box.
     * 
     * @param cbo Combo box to search
     * @param object Object being sought.
     * @param useLabel If true, each item's label is used for the comparison. Otherwise, the item's
     *            associated value is used. Label comparisons are case-insensitive.
     * @return The index of the matching item, or -1 if no match found.
     */
    public static int findComboboxItem(Combobox cbo, Object object, boolean useLabel) {
        if (object != null) {
            for (int i = 0; i < cbo.getItemCount(); i++) {
                Comboitem item = cbo.getItemAtIndex(i);
                Object value = useLabel ? item.getLabel().toLowerCase() : item.getValue();
                
                if (object == value || object.equals(value)) {
                    return i;
                }
            }
        }
        
        return -1;
    }
    
    /**
     * Finds an item with a matching label. Searches are case-insensitive.
     * 
     * @param lb List box to search
     * @param label Label value being sought.
     * @return The index of the matching item, or -1 if no match found.
     */
    public static int findListboxItem(Listbox lb, String label) {
        return findListboxItem(lb, label.toLowerCase(), true);
    }
    
    /**
     * Finds and selects an item with a matching label. Searches are case-insensitive.
     * 
     * @param lb List box to search
     * @param label Label value being sought.
     * @return The index of the matching item, or -1 if no match found.
     */
    public static int selectListboxItem(Listbox lb, String label) {
        int i = findListboxItem(lb, label);
        lb.setSelected(i);
        return i;
    }
    
    /**
     * Finds an item whose associated value matches the specified object.
     * 
     * @param lb List box to search
     * @param data Object being sought.
     * @return The index of the matching item, or -1 if no match found.
     */
    public static int findListboxData(Listbox lb, Object data) {
        return findListboxItem(lb, data, false);
    }
    
    /**
     * Finds and selects an item whose associated value matches the specified object.
     * 
     * @param lb List box to search
     * @param data Object being sought.
     * @return The index of the matching item, or -1 if no match found.
     */
    public static int selectListboxData(Listbox lb, Object data) {
        int i = findListboxData(lb, data);
        lb.setSelected(i);
        return i;
    }
    
    /**
     * Finds the matching object in the item list of the list box.
     * 
     * @param lb List box to search
     * @param object Object being sought.
     * @param useLabel If true, each item's label is used for the comparison. Otherwise, the item's
     *            associated value is used. Label comparisons are case-insensitive.
     * @return The index of the matching item, or -1 if no match found.
     */
    public static int findListboxItem(Listbox lb, Object object, boolean useLabel) {
        if (object != null) {
            for (int i = 0; i < lb.getItemCount(); i++) {
                Listitem item = lb.getItemAtIndex(i);
                lb.renderItem(item);
                Object value = useLabel ? item.getLabel().toLowerCase() : item.getValue();
                
                if (object == value || object.equals(value)) {
                    return i;
                }
            }
        }
        
        return -1;
    }
    
    /**
     * Returns a list of all items that are selected. Use this instead of the list box's
     * getSelectedItems method if you want to preserve the original ordering of the list items.
     * 
     * @param lb List box.
     * @return List of selected items; never null;
     */
    public static List<Listitem> getSelectedItems(Listbox lb) {
        if (lb.getSelectedCount() == lb.getItemCount()) {
            return lb.getItems();
        }
        
        List<Listitem> items = new ArrayList<>(lb.getSelectedItems());
        Collections.sort(items, listItemComparator);
        return items;
    }
    
    /**
     * Enforces static class.
     */
    private ListUtil() {
    }
}
