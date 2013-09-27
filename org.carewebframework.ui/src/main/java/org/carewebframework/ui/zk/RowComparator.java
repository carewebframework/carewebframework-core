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

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.util.StringUtils;

import org.zkoss.zk.ui.Component;
import org.zkoss.zul.Column;
import org.zkoss.zul.Listheader;

/**
 * Serves as a generic comparator for list and grid displays.
 */
public class RowComparator implements Comparator<Object>, Serializable {
    
    private static final Log log = LogFactory.getLog(RowComparator.class);
    
    private static final long serialVersionUID = 1L;
    
    private final boolean _asc;
    
    private final String _beanProperty;
    
    /**
     * Automatically wires column or list headers to generic comparators. This is done by deriving
     * the getter method for each header element from the element's id. This is done by prepending
     * "get" to the header id and using that method name as the getter when comparing values across
     * rows under that header. If no id is specified for a header, no comparator is generated for
     * that header. ZK-generated id's are excluded.
     * 
     * @param headers List of headers (of type Column or Listheader).
     */
    public static void autowireColumnComparators(List<Component> headers) {
        for (Object obj : headers) {
            if (obj instanceof Column) {
                Column col = (Column) obj;
                String getter = getterMethod(col);
                
                if (getter != null) {
                    col.setSortAscending(new RowComparator(true, getter));
                    col.setSortDescending(new RowComparator(false, getter));
                }
            } else if (obj instanceof Listheader) {
                Listheader hdr = (Listheader) obj;
                String getter = getterMethod(hdr);
                
                if (getter != null) {
                    hdr.setSortAscending(new RowComparator(true, getter));
                    hdr.setSortDescending(new RowComparator(false, getter));
                }
            }
        }
    }
    
    /**
     * Constructs a row comparator.
     * 
     * @param asc If true, an ascending comparator is created. If false, a descending comparator is
     *            created.
     * @param beanProperty This is the name of the getter method that will be used to retrieve a
     *            value from the underlying model object for comparison.
     */
    public RowComparator(boolean asc, String beanProperty) {
        _asc = asc;
        _beanProperty = beanProperty;
    }
    
    /**
     * Performs a comparison between two objects. If the objects implement the Comparable interface,
     * that method is used. Otherwise, the objects are converted to their string representations and
     * that method is used. Null values are handled.
     */
    @SuppressWarnings("unchecked")
    @Override
    public int compare(Object o1, Object o2) {
        Object v1 = getValue(o1), v2 = getValue(o2);
        int result;
        
        if (v1 == null && v2 == null) {
            result = 0;
        } else if (v2 == null) {
            result = -1;
        } else if (v1 == null) {
            result = 1;
        } else if (v1 instanceof Comparable && !(v1 instanceof String)) {
            result = ((Comparable<Object>) v1).compareTo(v2);
        } else {
            result = v1.toString().compareToIgnoreCase(v2.toString());
        }
        
        return _asc ? result : -result;
    }
    
    /**
     * Gets a value from the model object using the getter method specified in _beanProperty.
     * 
     * @param o The model object.
     * @return Value returned by the getter method.
     */
    private Object getValue(Object o) {
        try {
            Object[] params = null;
            Method method = o.getClass().getMethod(_beanProperty, (Class<?>[]) params);
            return method.invoke(o, params);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * Derives the name of the getter method from the component id, following "JavaBean"
     * conventions. Supports traditional property "getters" as well as boolean getter methods (i.e.
     * isActive(), hasChildren()). If the id is mapped to a boolean getter, then your id should
     * begin with 'is' or 'has'.
     * 
     * @param component Component used to derive getter method.
     * @return Null if the component has no id or has a ZK-generated id. Otherwise, if the id is
     *         prefixed with a standard getter prefix ("get", "has", "is"), it is assumed to be the
     *         name of the getter method. Lacking such a prefix, a prefix of "get" is prepended to
     *         the case-adjusted id to obtain the getter method.
     */
    private static String getterMethod(Component component) {
        String id = component.getId();
        
        if (id == null || id.isEmpty() || id.startsWith("z")) {
            return null;
        }
        
        String lc = id.toLowerCase();
        
        if (lc.startsWith("is") || lc.startsWith("has") || lc.startsWith("get")) {
            return StringUtils.uncapitalize(id);//i.e. isActive, hasChildren
        }
        return "get".concat(StringUtils.capitalize(id));//i.e. getOrderStatus, getTitle
    }
    
}
