/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
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
