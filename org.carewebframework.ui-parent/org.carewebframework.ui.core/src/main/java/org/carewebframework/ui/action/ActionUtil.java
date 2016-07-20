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

import java.util.Comparator;

import org.carewebframework.common.StrUtil;

/**
 * Static utility class.
 */
public class ActionUtil {
    
    /**
     * Comparator for sorting actions alphabetically by display text.
     */
    public static final Comparator<IAction> comparator = new Comparator<IAction>() {
        
        @Override
        public int compare(IAction a1, IAction a2) {
            return a1.toString().compareToIgnoreCase(a2.toString());
        }
        
    };
    
    /**
     * Creates an action object from fields.
     * 
     * @param label Action's label name. May be a label reference (prefixed with an '@' character)
     *            or the label itself.
     * @param script Action's script.
     * @return An action object.
     */
    public static IAction createAction(final String label, final String script) {
        return new IAction() {
            
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
                return false;
            }
            
            @Override
            public String toString() {
                return label == null ? super.toString() : StrUtil.formatMessage(label);
            }
        };
    }
    
    /**
     * Enforce singleton.
     */
    private ActionUtil() {
        super();
    }
    
}
