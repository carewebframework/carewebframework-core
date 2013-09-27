/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.help;

/**
 * Interface to be implemented by a help view provider.
 */
public interface IHelpView {
    
    /**
     * Returns the topic tree associated with the view, if any.
     * 
     * @return The root node of the associated topic tree, or null if none.
     */
    HelpTopicNode getTopicTree();
    
    /**
     * The display name of the view.
     * 
     * @return View display name.
     */
    String getName();
    
    /**
     * The type of this view.
     * 
     * @return A view type.
     */
    HelpViewType getViewType();
    
}
