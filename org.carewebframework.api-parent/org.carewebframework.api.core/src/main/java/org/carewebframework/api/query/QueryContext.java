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
package org.carewebframework.api.query;

import java.util.HashMap;
import java.util.Map;

/**
 * A simple implementation of a query context, suitable for most applications.
 */
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
