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
package org.carewebframework.api.context;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.fujion.common.DateUtil;

/**
 * Encapsulates a set of context items. Internally, these are stored in a hash map with a separate
 * index to allow case-insensitive lookup.
 */
public class ContextItems {
    
    private final Map<String, String> items = new HashMap<>();

    private final Map<String, String> index = new HashMap<>();

    /**
     * Serializes the context item set to a string.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        for (String key : items.keySet()) {
            sb.append(key).append('=').append(items.get(key)).append('\n');
        }

        return sb.toString();
    }

    /**
     * Performs a case-insensitive lookup of the item name in the index.
     *
     * @param itemName Item name
     * @param autoAdd If true and item name not in index, add it.
     * @return Item name as stored internally. If not already stored, returns the item name as it
     *         was specified in itemName.
     */
    private String lookupItemName(String itemName, boolean autoAdd) {
        String indexedName = index.get(itemName.toLowerCase());

        if (indexedName == null && autoAdd) {
            index.put(itemName.toLowerCase(), itemName);
        }

        return indexedName == null ? itemName : indexedName;
    }

    /**
     * Performs a case-insensitive lookup of the item name + suffix in the index.
     *
     * @param itemName Item name
     * @param suffix Item suffix
     * @param autoAdd If true and item name not in index, add it.
     * @return Item name with suffix as stored internally
     */
    private String lookupItemName(String itemName, String suffix, boolean autoAdd) {
        return lookupItemName(itemName + "." + suffix, autoAdd);
    }

    /**
     * Clear all context items and the index.
     */
    public void clear() {
        items.clear();
        index.clear();
    }

    /**
     * Remove all context items for the specified subject.
     *
     * @param subject Prefix whose items are to be removed.
     */
    public void removeSubject(String subject) {
        String prefix = normalizePrefix(subject);

        for (String suffix : getSuffixes(prefix).keySet()) {
            setItem(prefix + suffix, null);
        }
    }

    /**
     * Returns a set of all item names.
     *
     * @return Set of all item names in the context.
     */
    public Set<String> getItemNames() {
        return items.keySet();
    }

    /**
     * Returns a map consisting of all suffixes of context items that match the specified prefix.
     *
     * @param prefix Item name less any suffix.
     * @return Map of all suffixes whose prefix matches the specified value. The value of each map
     *         entry is the value of the original context item.
     */
    public Map<String, String> getSuffixes(String prefix) {
        return getSuffixes(prefix, false);
    }

    /**
     * Returns a map consisting of suffixes of context items that match the specified prefix.
     *
     * @param prefix Item name less any suffix.
     * @param firstOnly If true, only the first match is returned. Otherwise, all matches are
     *            returned.
     * @return Map of suffixes whose prefix matches the specified value. The value of each map entry
     *         is the value of the original context item.
     */
    private Map<String, String> getSuffixes(String prefix, Boolean firstOnly) {
        HashMap<String, String> matches = new HashMap<>();
        prefix = normalizePrefix(prefix);
        int i = prefix.length();

        for (String itemName : index.keySet()) {
            if (itemName.startsWith(prefix)) {
                String suffix = lookupItemName(itemName, false).substring(i);
                matches.put(suffix, getItem(itemName));

                if (firstOnly) {
                    break;
                }
            }
        }

        return matches;
    }

    /**
     * Returns true if any context item belonging to the specified subject exists.
     *
     * @param subject The subject of interest.
     * @return True if the subject was found.
     */
    public boolean containsSubject(String subject) {
        return !getSuffixes(subject, true).isEmpty();
    }

    /**
     * Normalizes a prefix by appending a "." if necessary and converting to lower case.
     *
     * @param prefix Prefix to normalize.
     * @return Normalized prefix.
     */
    private String normalizePrefix(String prefix) {
        return (prefix.endsWith(".") ? prefix : prefix + ".").toLowerCase();
    }

    /**
     * Retrieves a context item by name.
     *
     * @param itemName Item name
     * @return Item value
     */
    public String getItem(String itemName) {
        return items.get(lookupItemName(itemName, false));
    }

    /**
     * Retrieves a context item qualified by a suffix.
     *
     * @param itemName Item name
     * @param suffix Item suffix
     * @return Item value
     */
    public String getItem(String itemName, String suffix) {
        return items.get(lookupItemName(itemName, suffix, false));
    }

    /**
     * Returns an object of the specified class. The class must have an associated context
     * serializer registered.
     *
     * @param <T> The item's class.
     * @param itemName Item name
     * @param clazz Class of item to be returned.
     * @return Deserialized item of specified class.
     * @throws ContextException If no context serializer found.
     */
    @SuppressWarnings("unchecked")
    public <T> T getItem(String itemName, Class<T> clazz) throws ContextException {
        String item = getItem(itemName);

        if (item == null || item.isEmpty()) {
            return null;
        }

        ISerializer<?> contextSerializer = ContextSerializerRegistry.getInstance().get(clazz);

        if (contextSerializer == null) {
            throw new ContextException("No serializer found for type " + clazz.getName());
        }

        return (T) contextSerializer.deserialize(item);
    }

    /**
     * Sets a context item value.
     *
     * @param itemName Item name
     * @param value Item value
     */
    public void setItem(String itemName, String value) {
        itemName = lookupItemName(itemName, value != null);

        if (value == null) {
            items.remove(itemName);
            index.remove(itemName.toLowerCase());
        } else {
            items.put(itemName, value);
        }
    }

    /**
     * Sets a context item value.
     *
     * @param itemName Item name.
     * @param value The value to set. The value's class must have an associated context serializer
     *            registered for it.
     */
    public void setItem(String itemName, Object value) {
        if (value == null) {
            setItem(itemName, (String) null);
        } else {
            @SuppressWarnings("unchecked")
            ISerializer<Object> contextSerializer = (ISerializer<Object>) ContextSerializerRegistry.getInstance()
                    .get(value.getClass());

            if (contextSerializer == null) {
                throw new ContextException("No serializer found for type " + value.getClass().getName());
            }

            setItem(itemName, contextSerializer.serialize(value));
        }
    }

    /**
     * Sets a context item value, qualified with the specified suffix.
     *
     * @param itemName Item name
     * @param value Item value
     * @param suffix Item suffix
     */
    public void setItem(String itemName, String value, String suffix) {
        itemName = lookupItemName(itemName, suffix, value != null);
        items.put(itemName, value);
    }

    /**
     * Saves a date item object as a context item.
     *
     * @param itemName Item name
     * @param date Date value
     */
    public void setDate(String itemName, Date date) {
        if (date == null) {
            setItem(itemName, null);
        } else {
            setItem(itemName, DateUtil.toHL7(date));
        }
    }

    /**
     * Returns a date item associated with the specified item name.
     *
     * @param itemName Item name
     * @return Date value
     */
    public Date getDate(String itemName) {
        try {
            return DateUtil.parseDate(getItem(itemName));
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Adds context items from a serialized string.
     *
     * @param values Serialized context items to add.
     * @throws Exception Unspecified exception.
     */
    public void addItems(String values) throws Exception {
        for (String line : values.split("[\\r\\n]")) {
            String[] pcs = line.split("\\=", 2);

            if (pcs.length == 2) {
                setItem(pcs[0], pcs[1]);
            }
        }
    }

    /**
     * Adds context items to this set.
     *
     * @param contextItems Context items to add.
     */
    public void addItems(ContextItems contextItems) {
        addItems(contextItems.items);
    }

    /**
     * Adds property values to the context item list.
     *
     * @param values Values to add.
     */
    private void addItems(Map<String, String> values) {
        for (String itemName : values.keySet()) {
            setItem(itemName, values.get(itemName));
        }
    }
}
