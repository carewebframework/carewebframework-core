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

import org.carewebframework.common.StrUtil;
import org.springframework.util.Assert;

/**
 * Class used for registered actions.
 */
public class Action implements IAction, Comparable<IAction> {
    
    private final String id;
    
    private final String label;
    
    private final String script;
    
    @SuppressWarnings("rawtypes")
    private IActionType type;
    
    private Object target;
    
    private boolean disabled;
    
    /**
     * Creates an action object.
     *
     * @param id Unique id associated with action.
     * @param label Action's label name. May be a label reference (prefixed with an '@' character)
     *            or the label itself.
     * @param script Action's script.
     */
    public Action(String id, String label, String script) {
        Assert.notNull(id, "Action id must not be null");
        this.id = id;
        this.label = label;
        Assert.notNull(script, "Script must not be null");
        this.script = script;
    }
    
    @Override
    public String getId() {
        return id;
    }
    
    @Override
    public String getLabel() {
        return label;
    }
    
    @Override
    public String getScript() {
        return script;
    }
    
    @Override
    public boolean isDisabled() {
        return disabled;
    }
    
    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public void execute() {
        init();
        type.execute(target);
    }
    
    @Override
    public String toString() {
        return label == null ? super.toString() : StrUtil.formatMessage(label);
    }
    
    @Override
    public int compareTo(IAction action2) {
        return ActionUtil.comparator.compare(this, action2);
    }
    
    private void init() {
        if (type == null) {
            type = ActionTypeRegistry.getType(script);
            target = type.parse(script);
        }
    }

}
