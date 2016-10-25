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
package org.carewebframework.ui.sharedforms;

import static org.junit.Assert.assertEquals;

import org.carewebframework.shell.plugins.PluginContainer;
import org.carewebframework.ui.FrameworkController;
import org.carewebframework.ui.test.CommonTest;
import org.carewebframework.web.component.BaseComponent;
import org.carewebframework.web.component.Label;
import org.carewebframework.web.component.Listbox;
import org.carewebframework.web.page.PageUtil;
import org.junit.Test;

public class TestForms extends CommonTest {
    
    @Test
    public void testForm() throws Exception {
        BaseComponent root = PageUtil.createPage("~./org/carewebframework/ui/sharedforms/testForm.cwf", null).get(0);
        TestController controller = (TestController) FrameworkController.getController(root);
        PluginContainer dummy = new PluginContainer();
        controller.onLoad(dummy);
        controller.requestData();
        assertEquals(10, controller.model.size());
        Listbox listbox = (Listbox) root.findByName("listbox");
        assertEquals(10, listbox.getChildCount());
        assertEquals("Item #2.3", ((Label) listbox.getChildAt(1).getChildren().get(2).getFirstChild()).getLabel());
        assertEquals("Test Title", controller.getCaption());
        //assertEquals("Header3", ((Listheader) listbox.getListhead().getLastChild()).getLabel());
        assertEquals("50:1:false;0:33%;1:33%;2:33%", controller.getLayout());
        controller.setLayout("75:2:true;0:20%;1:30%;2:50%");
        assertEquals("75:2:true;0:20%;1:30%;2:50%", controller.getLayout());
    }
}
