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

import static org.junit.Assert.assertEquals;

import org.carewebframework.shell.plugins.PluginContainer;
import org.carewebframework.ui.FrameworkController;
import org.carewebframework.ui.test.CommonTest;
import org.carewebframework.ui.zk.ZKUtil;

import org.zkoss.zk.ui.Component;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listheader;

import org.junit.Test;

public class TestForms extends CommonTest {
    
    @Test
    public void testForm() throws Exception {
        Component root = ZKUtil.loadZulPage("~./org/carewebframework/ui/sharedforms/testForm.zul", null);
        TestController controller = (TestController) FrameworkController.getController(root);
        PluginContainer dummy = new PluginContainer();
        controller.onLoad(dummy);
        controller.loadList();
        assertEquals(10, controller.itemList.size());
        Listbox listbox = (Listbox) root.getFellow("listbox");
        assertEquals(10, listbox.getItemCount());
        assertEquals("Item #2.3", ((Listcell) listbox.getItemAtIndex(1).getChildren().get(2)).getLabel());
        assertEquals("Test Title", controller.getCaption());
        assertEquals("Header3", ((Listheader) listbox.getListhead().getLastChild()).getLabel());
        assertEquals("50:1:false;0:33%;1:33%;2:33%", controller.getLayout());
        controller.setLayout("75:2:true;0:20%;1:30%;2:50%");
        assertEquals("75:2:true;0:20%;1:30%;2:50%", controller.getLayout());
    }
}
