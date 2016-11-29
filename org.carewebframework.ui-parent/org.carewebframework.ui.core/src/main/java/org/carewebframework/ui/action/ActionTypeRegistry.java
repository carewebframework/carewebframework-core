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
package org.carewebframework.ui.action;

import org.carewebframework.api.spring.BeanRegistry;

/**
 * Registry of supported action types.
 */
public class ActionTypeRegistry extends BeanRegistry<String, IActionType> {
    
    private static final ActionTypeRegistry instance = new ActionTypeRegistry();
    
    public static ActionTypeRegistry getInstance() {
        return instance;
    }
    
    /**
     * Returns the action type given a script.
     * 
     * @param script The action script.
     * @return The action type, or an exception if a matching action was not found.
     */
    public static IActionType getType(String script) {
        for (IActionType actionType : instance) {
            if (actionType.matches(script)) {
                return actionType;
            }
        }
        
        throw new IllegalArgumentException("Script type was not recognized: " + script);
    }
    
    private ActionTypeRegistry() {
        super(IActionType.class);
    }
    
    @Override
    protected String getKey(IActionType item) {
        return item.getName();
    }
}
