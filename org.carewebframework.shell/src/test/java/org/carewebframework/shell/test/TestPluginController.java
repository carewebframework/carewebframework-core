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
package org.carewebframework.shell.test;

import org.carewebframework.shell.elements.ElementPlugin;
import org.carewebframework.shell.plugins.IPluginController;
import org.carewebframework.shell.plugins.PluginController;
import org.carewebframework.web.annotation.EventHandler;
import org.carewebframework.web.annotation.WiredComponent;
import org.carewebframework.web.component.Button;
import org.carewebframework.web.component.Menu;

public class TestPluginController extends PluginController implements IPluginController {
    
    private int activateCount;
    
    private int inactivateCount;
    
    private int loadCount;
    
    private int unloadCount;
    
    private int clickButtonCount;
    
    private int clickMenuCount;
    
    private String prop1;
    
    private int prop2;
    
    private boolean prop3;
    
    @WiredComponent
    public Button btnTest;
    
    @WiredComponent
    public Menu mnuTest;
    
    @Override
    public void onActivate() {
        super.onActivate();
        activateCount++;
    }
    
    @Override
    public void onInactivate() {
        super.onInactivate();
        inactivateCount++;
    }
    
    @Override
    public void onLoad(ElementPlugin plugin) {
        super.onLoad(plugin);
        plugin.registerProperties(this, "prop1", "prop2", "prop3");
        loadCount++;
    }
    
    @Override
    public void onUnload() {
        super.onUnload();
        unloadCount++;
    }
    
    @EventHandler(value = "click", target = "btnTest")
    public void onClick$btnTest() {
        clickButtonCount++;
    }
    
    @EventHandler(value = "click", target = "mnuTest")
    public void onClick$mnuTest() {
        clickMenuCount++;
    }
    
    public int getActivateCount() {
        return activateCount;
    }
    
    public int getInactivateCount() {
        return inactivateCount;
    }
    
    public int getLoadCount() {
        return loadCount;
    }
    
    public int getUnloadCount() {
        return unloadCount;
    }
    
    public int getClickButtonCount() {
        return clickButtonCount;
    }
    
    public int getClickMenuCount() {
        return clickMenuCount;
    }
    
    public String getProp1() {
        return prop1;
    }
    
    public void setProp1(String prop1) {
        this.prop1 = prop1;
    }
    
    public int getProp2() {
        return prop2;
    }
    
    public void setProp2(int prop2) {
        this.prop2 = prop2;
    }
    
    public boolean isProp3() {
        return prop3;
    }
    
    public void setProp3(boolean prop3) {
        this.prop3 = prop3;
    }
}
