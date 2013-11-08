/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.ui.sharedforms;

import org.carewebframework.shell.plugins.PluginContainer;
import org.carewebframework.shell.plugins.PluginController;

/**
 * Controller for base shared form.
 */
public class BaseForm extends PluginController {
    
    private static final long serialVersionUID = 1L;
    
    @Override
    public void onLoad(PluginContainer container) {
        super.onLoad(container);
        init();
    }
    
    /**
     * Override to perform initializations after form is instantiated.
     */
    protected void init() {
        
    }
}
