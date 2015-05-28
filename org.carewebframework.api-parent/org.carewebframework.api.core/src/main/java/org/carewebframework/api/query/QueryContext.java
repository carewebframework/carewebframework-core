/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.api.query;

import java.util.HashMap;
import java.util.Map;

public class QueryContext implements IQueryContext {
    
    private boolean changed = true;
    
    private final Map<String, Object> params = new HashMap<>();
    
    protected void dirty() {
        changed = true;
    }
    
    public void clear() {
        params.clear();
        changed = true;
    }
    
    @Override
    public boolean hasChanged() {
        return changed;
    }
    
    @Override
    public void reset() {
        changed = false;
    }
    
    @Override
    public boolean setParam(String name, Object newValue) {
        Object oldValue = params.get(name);
        boolean change = false;
        
        if (oldValue == null || newValue == null) {
            change = oldValue != newValue;
        } else {
            change = !oldValue.equals(newValue);
        }
        
        if (change) {
            params.put(name, newValue);
        }
        
        return change;
    }
    
    @Override
    public Object getParam(String name) {
        return params.get(name);
    }
    
}
