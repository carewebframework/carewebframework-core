/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.testharness;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import org.carewebframework.shell.CareWebShellEx;
import org.carewebframework.shell.plugins.PluginDefinition;
import org.carewebframework.shell.plugins.PluginRegistry;
import org.carewebframework.ui.FrameworkController;
import org.carewebframework.ui.zk.ZKUtil;

import org.zkoss.zk.ui.Component;

/**
 * Creates a default UI based on all detected plugins.
 */
public class TestHarnessController extends FrameworkController {
    
    private static final long serialVersionUID = 1L;
    
    private CareWebShellEx shell;
    
    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        shell = (CareWebShellEx) comp;
        
        if (shell.getLayout() == null) {
            shell.setLayout(ZKUtil.getResourcePath(TestHarnessController.class) + "testharness-layout.xml");
        }
        
        List<PluginDefinition> plugins = new ArrayList<PluginDefinition>();
        
        for (PluginDefinition plugin : PluginRegistry.getInstance()) {
            if (!StringUtils.isEmpty(plugin.getUrl()) && shell.getLoadedPlugin(plugin.getId()) == null) {
                plugins.add(plugin);
            }
        }
        
        Collections.sort(plugins, new Comparator<PluginDefinition>() {
            
            @Override
            public int compare(PluginDefinition def1, PluginDefinition def2) {
                return def1.getName().compareToIgnoreCase(def2.getName());
            }
            
        });
        
        for (PluginDefinition plugin : plugins) {
            shell.register("Test Harness\\" + plugin.getName(), plugin);
        }
        
        shell.start();
    }
    
}
