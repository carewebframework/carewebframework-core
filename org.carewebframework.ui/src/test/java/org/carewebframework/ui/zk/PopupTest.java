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

public class PopupTest extends Window {
    
    private static final long serialVersionUID = 1L;
    
    public void onTest() throws Exception {
        PopupDialog popup = new PopupDialog(null, "Test Popup");
        setSizable(false);
        ZKUtil.loadZulPage(ZKUtil.getResourcePath(PopupTest.class) + "testPopup2.zul", popup);
        popup.addForward("onTest", this, null);
        popup.show();
    }
}
