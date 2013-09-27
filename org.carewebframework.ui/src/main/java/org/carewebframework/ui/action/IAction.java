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
 * Interface implemented by actions that may be associated with certain UI elements (e.g.,
 * UIElementButton).
 */
public interface IAction {
    
    /**
     * Returns the label associated with the action. This may be a label reference prefixed by an
     * '@' character, or the label text itself.
     * 
     * @return Associated label.
     */
    String getLabel();
    
    /**
     * Returns the action script. See {@link ActionType#getType(String)} for supported formats.
     * 
     * @return The action script.
     */
    String getScript();
    
    /**
     * Returns true if the action is disabled.
     * 
     * @return Disabled status of action.
     */
    boolean isDisabled();
    
    /**
     * Returns the display text for the action.
     * 
     * @return The display text.
     */
    @Override
    String toString();
}
