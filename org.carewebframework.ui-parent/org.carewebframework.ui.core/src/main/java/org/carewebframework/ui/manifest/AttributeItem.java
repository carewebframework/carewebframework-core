package org.carewebframework.ui.manifest;

import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;

/**
 * Model object for a single manifest attribute.
 */
public class AttributeItem implements IMatchable<AttributeItem> {
    
    public final String name;
    
    public final String value;
    
    public AttributeItem(Entry<Object, Object> entry) {
        name = entry.getKey().toString();
        value = entry.getValue().toString();
    }
    
    @Override
    public int compareTo(AttributeItem o) {
        return name.compareToIgnoreCase(o.name);
    }
    
    @Override
    public boolean matches(String filter) {
        return StringUtils.containsIgnoreCase(name, filter) || StringUtils.containsIgnoreCase(value, filter);
    }
    
}
