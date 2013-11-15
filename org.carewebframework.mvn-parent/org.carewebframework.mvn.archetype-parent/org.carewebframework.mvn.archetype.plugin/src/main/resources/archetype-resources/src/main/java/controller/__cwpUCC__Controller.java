#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package ${package}.controller;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.carewebframework.shell.plugins.PluginContainer;
import org.carewebframework.shell.plugins.PluginController;

import org.zkoss.zk.ui.Component;
import org.zkoss.zul.Label;

/**
 * This is a sample controller that subclasses the PluginController class which provides some
 * convenience methods for accessing framework services and can automatically register the
 * controller with the framework so that it may receive context change events (if controller is-an
 * instanceof a supported context-related interface).  This controller illustrates use of the
 * IPluginEvent interface to receive notification when the plugin is activated and inactivated
 * within the CareWeb Framework. 
 *
 */
public class ${cwpUCC}Controller extends PluginController {

    private static final long serialVersionUID = 1L;
    
    private static final Log log = LogFactory.getLog(${cwpUCC}Controller.class);

    private Label lblExample; // This value will be injected automatically by the parent class

    /**
     * @see org.carewebframework.ui.FrameworkController${symbol_pound}doAfterCompose(org.zkoss.zk.ui.Component)
     */
    @Override
    public void doAfterCompose(final Component comp) throws Exception {
        super.doAfterCompose(comp);
        log.trace("Controller composed");
        lblExample.setValue(new Date().toString());
    }
    
    /**
     * @see org.carewebframework.shell.plugins.IPluginEvent${symbol_pound}onLoad(org.carewebframework.shell.plugins.PluginContainer)
     */
    @Override
    public void onLoad(final PluginContainer container) {
        super.onLoad(container);
    }
    
    /**
     * @see org.carewebframework.shell.plugins.IPluginEvent${symbol_pound}onUnload()
     */
    @Override
    public void onUnload() {
        super.onUnload();
    }
    
    /**
     * @see org.carewebframework.shell.plugins.IPluginEvent${symbol_pound}onActivate()
     */
    @Override
    public void onActivate() {
        super.onActivate();
    }
    
    /**
     * @see org.carewebframework.shell.plugins.IPluginEvent${symbol_pound}onInactivate()
     */
    @Override
    public void onInactivate() {
        super.onInactivate();
    }

}
