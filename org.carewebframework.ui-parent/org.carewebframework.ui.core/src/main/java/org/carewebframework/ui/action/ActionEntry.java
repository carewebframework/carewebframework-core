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

/**
 * Class used for registered actions.
 */
public class ActionEntry implements IAction, Comparable<IAction> {
    
    private final IAction action;
    
    private final String id;
    
    /**
     * Creates an action object from fields.
     * 
     * @param id Unique id associated with action (may be null).
     * @param label Action's label name. May be a label reference (prefixed with an '@' character)
     *            or the label itself.
     * @param script Action's script.
     */
    public ActionEntry(String id, String label, String script) {
        this(id, ActionUtil.createAction(label, script));
    }
    
    /**
     * Wraps an action with an associated id.
     * 
     * @param id The action id.
     * @param action The action.
     */
    public ActionEntry(String id, IAction action) {
        this.id = id;
        this.action = action;
    }
    
    public String getId() {
        return id;
    }
    
    @Override
    public String getLabel() {
        return action.getLabel();
    }
    
    @Override
    public String getScript() {
        return action.getScript();
    }
    
    @Override
    public boolean isDisabled() {
        return action.isDisabled();
    }
    
    @Override
    public String toString() {
        return action.toString();
    }
    
    @Override
    public int compareTo(IAction action2) {
        return ActionUtil.comparator.compare(action, action2);
    }
    
}
