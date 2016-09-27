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
import org.carewebframework.ui.action.ActionRegistry;
import org.carewebframework.ui.action.ActionRegistry.ActionScope;
import org.carewebframework.ui.action.IAction;
import org.carewebframework.ui.zk.ZKUtil;
import org.carewebframework.web.component.BaseComponent;

/**
 * Creates a default UI based on all detected plugins.
 */
public class TestHarnessController extends FrameworkController {
    
    private static final Comparator<PluginDefinition> pluginComparator = new Comparator<PluginDefinition>() {
        
        @Override
        public int compare(PluginDefinition def1, PluginDefinition def2) {
            return def1.getName().compareToIgnoreCase(def2.getName());
        }
        
    };
    
    private static final Comparator<IAction> actionComparator = new Comparator<IAction>() {
        
        @Override
        public int compare(IAction act1, IAction act2) {
            return act1.toString().compareToIgnoreCase(act2.toString());
        }
        
    };
    
    private CareWebShellEx shell;
    
    @Override
    public void afterInitialized(BaseComponent comp) {
        super.afterInitialized(comp);
        shell = (CareWebShellEx) comp;
        
        if (shell.getLayout() == null) {
            shell.setLayout(ZKUtil.getResourcePath(TestHarnessController.class) + "testharness-layout.xml");
        }
        
        List<PluginDefinition> plugins = new ArrayList<>();
        
        for (PluginDefinition plugin : PluginRegistry.getInstance()) {
            if (!StringUtils.isEmpty(plugin.getUrl()) && shell.getLoadedPlugin(plugin.getId()) == null) {
                plugins.add(plugin);
            }
        }
        
        Collections.sort(plugins, pluginComparator);
        
        for (PluginDefinition plugin : plugins) {
            shell.register("Test Harness\\" + plugin.getName(), plugin);
        }
        
        List<IAction> actions = new ArrayList<IAction>(ActionRegistry.getRegisteredActions(ActionScope.BOTH));
        Collections.sort(actions, actionComparator);
        
        for (IAction action : actions) {
            shell.registerMenu("Actions\\" + action, action.getScript()).setHint(action.getScript());
        }
        
        shell.start();
    }
    
}
