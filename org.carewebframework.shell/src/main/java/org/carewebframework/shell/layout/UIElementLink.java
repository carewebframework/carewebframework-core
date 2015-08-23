/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.shell.layout;

import org.carewebframework.ui.themes.ThemeUtil;

import org.zkoss.zul.A;

/**
 * Simple hyperlink stock object.
 */
public class UIElementLink extends UIElementButton {
    
    static {
        registerAllowedParentClass(UIElementLink.class, UIElementBase.class);
    }
    
    public UIElementLink() {
        super(new A(), ThemeUtil.ButtonSize.DEFAULT, ThemeUtil.ButtonStyle.LINK);
    }
    
}
