/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.shell.layout;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.carewebframework.common.StrUtil;
import org.carewebframework.shell.CareWebShell;
import org.carewebframework.shell.plugins.PluginContainer;
import org.carewebframework.shell.plugins.PluginDefinition;
import org.carewebframework.shell.plugins.TestPluginController;
import org.carewebframework.shell.property.PropertyInfo;
import org.carewebframework.ui.FrameworkController;
import org.carewebframework.ui.test.CommonTest;

import org.zkoss.zk.ui.event.Events;

import org.junit.Test;

public class LayoutParserTest extends CommonTest {
    
    private final UILayout layout = new UILayout();
    
    @Test
    public void ParserTest() throws Exception {
        String xml = CommonTest.getTextFromResource("LayoutParserTest1.xml");
        layout.loadFromText(xml);
        assertFalse(layout.isEmpty());
        testNode(1, "_menubar");
        testNode(0, "_toolbar");
        testNode(0, "splitterview");
        testNode(1, "splitterpane");
        testProperty("size", "90");
        testProperty("relative", "true");
        testNode(1, "tabview");
        testProperty("orientation", "horizontal");
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
        UIElementBase ele = def.createElement(null, null);
        assertTrue(ele instanceof UIElementTreeView);
        CareWebShell shell = new CareWebShell();
        shell.afterCompose();
        UIElementDesktop root = shell.getUIDesktop();
        UIElementBase element = layout.deserialize(root);
        assertTrue(element instanceof UIElementMenubar);
        assertTrue(element.hasAncestor(root));
        PluginContainer container1 = shell.getLoadedPlugin("testplugin1");
        assertNotNull(container1);
        TestPluginController controller = (TestPluginController) FrameworkController.getController(container1
                .getFirstChild());
        assertNotNull(controller);
        assertEquals(container1, controller.getContainer());
        testPlugin(controller, 1, 1, 0, 0);
        root.activate(false);
        testPlugin(controller, 1, 1, 1, 0);
        root.activate(true);
        testPlugin(controller, 1, 2, 1, 0);
        testProperty(container1, "prop1", "value1");
        testProperty(container1, "prop2", 123);
        testProperty(container1, "prop3", true);
        root.removeChildren();
        testPlugin(controller, 1, 2, 1, 1);
        // Test auto-wire
        assertNotNull(controller.btnTest);
        assertNotNull(controller.mnuTest);
        Events.sendEvent(Events.ON_CLICK, controller.btnTest, null);
        Events.sendEvent(Events.ON_CLICK, controller.mnuTest, null);
        assertEquals(1, controller.getClickButtonCount());
        assertEquals(1, controller.getClickMenuCount());
        assertEquals(controller.btnTest, container1.getAttribute("btnTest"));
        assertEquals(controller.mnuTest, container1.getAttribute("mnuTest"));
    }
    
    private void testProperty(PluginContainer container, String propertyName, Object expectedValue) throws Exception {
        PluginDefinition def = container.getPluginDefinition();
        PropertyInfo propInfo = null;
        
        for (PropertyInfo pi : def.getProperties()) {
            if (pi.getId().equals(propertyName)) {
                propInfo = pi;
                break;
            }
        }
        
        assertNotNull("Property not found: " + propertyName, propInfo);
        assertEquals(expectedValue, container.getPropertyValue(propInfo));
    }
    
    private void testPlugin(TestPluginController controller, int loadCount, int activateCount, int inactivateCount,
                            int unloadCount) {
        mockEnvironment.flushEvents();
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
