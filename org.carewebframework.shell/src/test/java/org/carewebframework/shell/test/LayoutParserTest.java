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

import org.apache.commons.beanutils.PropertyUtils;
import org.fujion.common.StrUtil;
import org.carewebframework.shell.CareWebShell;
import org.carewebframework.shell.elements.ElementBase;
import org.carewebframework.shell.elements.ElementDesktop;
import org.carewebframework.shell.elements.ElementMenubar;
import org.carewebframework.shell.elements.ElementPlugin;
import org.carewebframework.shell.elements.ElementPlugin.PluginContainer;
import org.carewebframework.shell.elements.ElementSplitterPane;
import org.carewebframework.shell.elements.ElementSplitterView;
import org.carewebframework.shell.elements.ElementTabPane;
import org.carewebframework.shell.elements.ElementTabView;
import org.carewebframework.shell.elements.ElementToolbar;
import org.carewebframework.shell.elements.ElementTreePane;
import org.carewebframework.shell.elements.ElementTreeView;
import org.carewebframework.shell.elements.ElementTrigger;
import org.carewebframework.shell.elements.ElementUI;
import org.carewebframework.shell.layout.Layout;
import org.carewebframework.shell.layout.LayoutParser;
import org.carewebframework.shell.plugins.PluginDefinition;
import org.carewebframework.shell.property.PropertyInfo;
import org.carewebframework.shell.triggers.TriggerConditionActivate;
import org.carewebframework.ui.controller.FrameworkController;
import org.carewebframework.ui.test.MockUITest;
import org.fujion.event.ClickEvent;
import org.fujion.event.EventUtil;
import org.fujion.test.MockTest;
import org.junit.Test;

public class LayoutParserTest extends MockUITest {

    private CareWebShell shell;
    
    private ElementUI element;
    
    @Test
    public void parserTest() throws Exception {
        shell = new CareWebShell();
        shell.setParent(getMockEnvironment().getSession().getPage());
        parserTestFile("layout-v3.xml", false);
        parserTestFile("layout-v4.xml", true);
    }

    private void parserTestFile(String file, boolean hasTrigger) throws Exception {
        Layout layout = parserTestXML(getTextFromResource(file), hasTrigger);
        parserTestXML(layout.toString(), hasTrigger);
    }

    private Layout parserTestXML(String xml, boolean hasTrigger) throws Exception {
        Layout layout = LayoutParser.parseText(xml);
        parserTestLayout(layout, hasTrigger);
        return layout;
    }

    private void parserTestLayout(Layout layout, boolean hasTrigger) throws Exception {
        assertFalse(layout.isEmpty());
        assertEquals(layout.getName(), "test");
        PluginDefinition def = PluginDefinition.getDefinition("treeview");
        assertNotNull(def);
        assertEquals(def.getDescription(), StrUtil.getLabel("cwf.shell.plugin.treeview.description"));
        ElementBase ele = def.createElement(null, null, false);
        assertTrue(ele instanceof ElementTreeView);
        ElementDesktop root = shell.getDesktop();
        root.removeChildren();
        layout.materialize(root);
        element = root;
        testNode(1, ElementMenubar.class);
        testNode(0, ElementToolbar.class);
        testNode(0, ElementSplitterView.class);
        testNode(1, ElementSplitterPane.class);
        testProperty("size", 90);
        testProperty("relative", true);
        testNode(1, ElementTabView.class);
        testProperty("orientation", "top");
        testNode(1, ElementTabPane.class);
        testNode(1, ElementTreeView.class);
        testProperty("open", true);
        testNode(1, ElementTreePane.class);
        testProperty("label", "Pane 1");
        testNode(0, ElementTreePane.class);
        testProperty("label", "Pane 2");
        ElementPlugin plugin1 = shell.getLoadedPlugin("testplugin1");
        assertNotNull(plugin1);
        assertEquals(hasTrigger ? 1 : 0, plugin1.getTriggers().size());
        assertEquals(hasTrigger ? "triggered" : null, plugin1.getHint());

        if (hasTrigger) {
            ElementTrigger trigger = plugin1.getTriggers().iterator().next();
            assertTrue(trigger.getAction() instanceof TestTriggerAction);
            assertTrue(trigger.getCondition() instanceof TriggerConditionActivate);
        }

        PluginContainer container1 = (PluginContainer) plugin1.getOuterComponent();
        TestPluginController controller = (TestPluginController) FrameworkController
                .getController(container1.getFirstChild());
        assertNotNull(controller);
        assertEquals(plugin1, controller.getPlugin());
        testPlugin(controller, 1, 1, 0, 0);
        root.activate(false);
        testPlugin(controller, 1, 1, 1, 0);
        root.activate(true);
        MockTest.getMockEnvironment().flushEvents();
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
        getMockEnvironment().flushEvents();
        assertEquals(loadCount, controller.getLoadCount());
        assertEquals(activateCount, controller.getActivateCount());
        assertEquals(inactivateCount, controller.getInactivateCount());
        assertEquals(unloadCount, controller.getUnloadCount());
    }

    private void testNode(int dir, Class<? extends ElementUI> clazz) {
        switch (dir) {
            case 1:
                element = (ElementUI) element.getFirstChild();
                break;

            case -1:
                element = element.getParent();
                break;

            case 0:
                element = element.getNextSibling(false);
                break;
        }

        assertTrue(clazz.isInstance(element));
    }

    private void testProperty(String key, Object value) throws Exception {
        Object prop = PropertyUtils.getProperty(element, key);
        assertEquals(prop, value);
    }
}
