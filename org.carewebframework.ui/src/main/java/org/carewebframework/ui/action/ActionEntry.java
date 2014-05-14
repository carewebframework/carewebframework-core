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
     * @param id
     * @param action
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
