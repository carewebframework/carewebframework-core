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

import org.carewebframework.shell.elements.ElementPlugin;
import org.carewebframework.ui.test.MockUITest;
import org.carewebframework.web.ancillary.ILabeled;
import org.carewebframework.web.component.BaseComponent;
import org.carewebframework.web.component.Column;
import org.carewebframework.web.component.Grid;
import org.carewebframework.web.component.Rows;
import org.carewebframework.web.page.PageUtil;
import org.junit.Test;

public class TestForms extends MockUITest {
    
    @Test
    public void testForm() throws Exception {
        BaseComponent root = PageUtil.createPage("web/org/carewebframework/ui/sharedforms/listviewForm.cwf", null).get(0);
        TestController controller = new TestController();
        root.wireController(controller);
        ElementPlugin dummy = new ElementPlugin();
        controller.onLoad(dummy);
        controller.requestData();
        assertEquals(10, controller.model.size());
        Grid grid = (Grid) root.findByName("grid");
        Rows rows = grid.getRows();
        assertEquals(10, rows.getChildCount());
        assertEquals("Item #2.3", ((ILabeled) rows.getChildAt(1).getChildAt(2)).getLabel());
        assertEquals("Test Title", controller.getCaption());
        assertEquals("Header3", ((Column) grid.getColumns().getLastChild()).getLabel());
        assertEquals(":1:false;0:33%;1:33%;2:33%", controller.getLayout());
        controller.setLayout(":2:true;0:20%;1:30%;2:50%");
        assertEquals(":2:true;0:20%;1:30%;2:50%", controller.getLayout());
    }
}
