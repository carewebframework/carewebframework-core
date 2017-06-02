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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.carewebframework.common.StrUtil;
import org.carewebframework.shell.CareWebShell;
import org.carewebframework.shell.elements.ElementBase;
import org.carewebframework.shell.elements.ElementDesktop;
import org.carewebframework.shell.elements.ElementMenubar;
import org.carewebframework.shell.elements.ElementPlugin;
import org.carewebframework.shell.elements.ElementPlugin.PluginContainer;
import org.carewebframework.shell.elements.ElementTreeView;
import org.carewebframework.shell.layout.UILayout;
import org.carewebframework.shell.plugins.PluginDefinition;
import org.carewebframework.shell.property.PropertyInfo;
import org.carewebframework.ui.controller.FrameworkController;
import org.carewebframework.web.event.ClickEvent;
import org.carewebframework.web.event.EventUtil;
import org.carewebframework.web.test.MockTest;
import org.junit.Test;

public class LayoutParserTest {

    private final UILayout layout = new UILayout();

    @Test
    public void ParserTest() throws Exception {
        String xml = MockTest.getTextFromResource("LayoutParserTest1.xml");
        layout.loadFromText(xml);
        assertFalse(layout.isEmpty());
        testNode(1, "_menubar");
        testNode(0, "_toolbar");
        testNode(0, "splitterview");
        testNode(1, "splitterpane");
        testProperty("size", "90");
        testProperty("relative", "true");
        testNode(1, "tabview");
        testProperty("orientation", "top");
        testNode(1, "tabpane");
        testNode(1, "treeview");
        testProperty("open", "true");
        testNode(1, "treepane");
        testProperty("path", "Pane 1");
        testNode(0, "treepane");
        testProperty("path", "Pane 2");
        PluginDefinition def = PluginDefinition.getDefinition("treeview");
        assertNotNull(def);
        assertEquals(def.getDescription(), StrUtil.getLabel("cwf.shell.plugin.treeview.description"));
        ElementBase ele = def.createElement(null, null);
        assertTrue(ele instanceof ElementTreeView);
        CareWebShell shell = new CareWebShell();
        shell.setParent(MockTest.mockEnvironment.getSession().getPage());
        ElementDesktop root = shell.getUIDesktop();
        ElementBase element = layout.deserialize(root);
        assertTrue(element instanceof ElementMenubar);
        assertTrue(element.hasAncestor(root));
        ElementPlugin plugin1 = shell.getLoadedPlugin("testplugin1");
        assertNotNull(plugin1);
        PluginContainer container1 = (PluginContainer) plugin1.getOuterComponent();
        TestPluginController controller = (TestPluginController) FrameworkController
                .getController(container1.getFirstChild());
        assertNotNull(controller);
        assertEquals(plugin1, controller.getPlugin());
        testPlugin(controller, 1, 1, 0, 0);
        root.activate(false);
        testPlugin(controller, 1, 1, 1, 0);
        root.activate(true);
        MockTest.mockEnvironment.flushEvents();
        testPlugin(controller, 1, 2, 1, 0);
        testProperty(plugin1, "prop1", "value1");
        testProperty(plugin1, "prop2", 123);
        testProperty(plugin1, "prop3", true);
        // Test auto-wire
        assertNotNull(controller.btnTest);
        assertNotNull(controller.mnuTest);
        EventUtil.send(ClickEvent.TYPE, controller.btnTest, null);
        EventUtil.send(ClickEvent.TYPE, controller.mnuTest, null);
        assertEquals(1, controller.getClickButtonCount());
        assertEquals(1, controller.getClickMenuCount());
        root.removeChildren();
        testPlugin(controller, 1, 2, 1, 1);
    }

    private void testProperty(ElementPlugin plugin, String propertyName, Object expectedValue) throws Exception {
        PluginDefinition def = plugin.getDefinition();
        PropertyInfo propInfo = null;

        for (PropertyInfo pi : def.getProperties()) {
            if (pi.getId().equals(propertyName)) {
                propInfo = pi;
                break;
            }
        }

        assertNotNull("Property not found: " + propertyName, propInfo);
        assertEquals(expectedValue, plugin.getPropertyValue(propInfo));
    }

    private void testPlugin(TestPluginController controller, int loadCount, int activateCount, int inactivateCount,
                            int unloadCount) {
        MockTest.mockEnvironment.flushEvents();
        assertEquals(loadCount, controller.getLoadCount());
        assertEquals(activateCount, controller.getActivateCount());
        assertEquals(inactivateCount, controller.getInactivateCount());
        assertEquals(unloadCount, controller.getUnloadCount());
    }

    private void testNode(int dir, String name) {
        switch (dir) {
            case 1:
                layout.moveDown();
                break;

            case -1:
                layout.moveUp();
                break;

            case 0:
                layout.moveNext();
                break;
        }

        assertEquals(layout.getObjectName(), name);
    }

    private void testProperty(String key, String value) {
        assertEquals(layout.getProperty(key), value);
    }
}
