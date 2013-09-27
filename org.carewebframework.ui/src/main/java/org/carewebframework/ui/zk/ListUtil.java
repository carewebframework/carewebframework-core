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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listitem;

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
            return i1.getIndex() - i2.getIndex();
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
     * Finds the matching object in the item list of the combo box.
     * 
     * @param cbo Combo box to search
     * @param object Object being sought.
     * @param useLabel If true, each item's label is used for the comparison. Otherwise, the item's
     *            associated value is used. Label comparisons are case-insensitive.
     * @return The index of the matching item, or -1 if no match found.
     */
    public static int findComboboxItem(Combobox cbo, Object object, boolean useLabel) {
        for (int i = 0; i < cbo.getItemCount(); i++) {
            Comboitem item = cbo.getItemAtIndex(i);
            Object value = useLabel ? item.getLabel().toLowerCase() : item.getValue();
            
            if (object == value || object.equals(value)) {
                return i;
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
     * Finds the matching object in the item list of the list box.
     * 
     * @param lb List box to search
     * @param object Object being sought.
     * @param useLabel If true, each item's label is used for the comparison. Otherwise, the item's
     *            associated value is used. Label comparisons are case-insensitive.
     * @return The index of the matching item, or -1 if no match found.
     */
    public static int findListboxItem(Listbox lb, Object object, boolean useLabel) {
        for (int i = 0; i < lb.getItemCount(); i++) {
            Listitem item = lb.getItemAtIndex(i);
            lb.renderItem(item);
            Object value = useLabel ? item.getLabel().toLowerCase() : item.getValue();
            
            if (object == value || object.equals(value)) {
                return i;
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
        
        List<Listitem> items = new ArrayList<Listitem>(lb.getSelectedItems());
        Collections.sort(items, listItemComparator);
        return items;
    }
    
    /**
     * Enforces static class.
     */
    private ListUtil() {
    };
}
