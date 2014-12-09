/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.ui.zk;

import org.zkoss.zul.Window;

/**
 * Subclasses Window to correct minimize behavior
 */
public class WindowEx extends Window {
    
    private static final long serialVersionUID = 1L;
    
    @Override
    public void setMinimized(boolean minimized) {
        boolean visible = isVisible();
        super.setMinimized(minimized);
        setVisible(visible);
    }
}
