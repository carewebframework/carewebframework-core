#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
/*
 * #%L
 * carewebframework
 * %%
 * Copyright (C) 2008 - 2016 Regenstrief Institute, Inc.
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related
 * Additional Disclaimer of Warranty and Limitation of Liability available at
 *
 *      http://www.carewebframework.org/licensing/disclaimer.
 *
 * #L%
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
 * This is a sample controller that extends the PluginController class which provides some
 * convenience methods for accessing framework services and automatically registers the
 * controller with the framework so that it may receive context change events (if the controller
 * implements a supported context-related interface).  This controller illustrates the use of
 * the IPluginEvent interface to receive plugin lifecycle notifications.. 
 *
 */
public class ${cwpUCC}Controller extends PluginController {

    private static final long serialVersionUID = 1L;
    
    private static final Log log = LogFactory.getLog(${cwpUCC}Controller.class);

    private Label lblDate; // This value will be injected automatically by the parent class

    /**
     * @see org.carewebframework.ui.FrameworkController${symbol_pound}doAfterCompose(org.zkoss.zk.ui.Component)
     */
    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        log.trace("Controller composed");
    }
    
    /**
     * @see org.carewebframework.shell.plugins.IPluginEvent${symbol_pound}onLoad(org.carewebframework.shell.plugins.PluginContainer)
     */
    @Override
    public void onLoad(PluginContainer container) {
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
        lblDate.setValue(new Date().toString());
    }
    
    /**
     * @see org.carewebframework.shell.plugins.IPluginEvent${symbol_pound}onInactivate()
     */
    @Override
    public void onInactivate() {
        super.onInactivate();
    }

}
