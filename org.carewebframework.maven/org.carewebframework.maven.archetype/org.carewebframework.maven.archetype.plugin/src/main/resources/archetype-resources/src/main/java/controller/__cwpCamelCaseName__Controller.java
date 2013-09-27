#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
/**
 * The contents of this file are subject to the Regenstrief Public License
 * Version 1.0 (the "License"); you may not use this file except in compliance with the License.
 * Please contact Regenstrief Institute if you would like to obtain a copy of the license.
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) Regenstrief Institute.  All Rights Reserved.
 */
package ${package}.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.carewebframework.shell.plugins.PluginContainer;
import org.carewebframework.shell.plugins.PluginController;

import org.zkoss.zk.ui.Component;
import org.zkoss.zul.Label;

/**
 * This is a sample controller that subclasses the FrameworkController class which provides some
 * convenience methods for accessing framework services and can automatically register the
 * controller with the framework so that it may receive context change events (if controller is-an
 * instanceof a supported context-related interface). This particular controller illustrates
 * registering a patient context listener so that it may receive notification of patient context
 * changes. This controller also illustrates registration of a member as a CareWeb Framework plugin
 * event listener so that it may receive notification when the plugin is activated and inactivated
 * within the CareWeb Framework. This is useful if one wants to defer certain expensive operations
 * until the component is actually visible.
 *
 */
public class ${cwpCamelCaseName}Controller extends PluginController {

    private static final long serialVersionUID = 1L;
    
    private static final Log log = LogFactory.getLog(${cwpCamelCaseName}Controller.class);

    //private Label lblExample; // This value will be injected automatically by the parent class

    /**
     * @see org.carewebframework.ui.FrameworkController${symbol_pound}doAfterCompose(org.zkoss.zk.ui.Component)
     */
    @Override
    public void doAfterCompose(final Component comp) throws Exception {
        super.doAfterCompose(comp);
        log.trace("Controller composed");
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
