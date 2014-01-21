/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.shell.plugins;

import org.zkoss.zul.Button;
import org.zkoss.zul.Menu;

public class TestPluginController extends PluginController {
    
    private static final long serialVersionUID = 1L;
    
    private int activateCount;
    
    private int inactivateCount;
    
    private int loadCount;
    
    private int unloadCount;
    
    private int clickButtonCount;
    
    private int clickMenuCount;
    
    private String prop1;
    
    private int prop2;
    
    private boolean prop3;
    
    public Button btnTest;
    
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
    public void onLoad(PluginContainer container) {
        super.onLoad(container);
        container.registerProperties(this, "prop1", "prop2", "prop3");
        loadCount++;
    }
    
    @Override
    public void onUnload() {
        super.onUnload();
        unloadCount++;
    }
    
    public void onClick$btnTest() {
        clickButtonCount++;
    }
    
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
