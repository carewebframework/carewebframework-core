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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.carewebframework.ui.test.CommonTest;

import org.junit.Test;

public class PluginDefinitionParserTest extends CommonTest {
    
    @Test
    public void ParserTest() throws Exception {
        String xml = getTextFromResource("PluginDefinitionParserTest1.xml");
        PluginDefinition def = PluginXmlParser.fromXml(xml);
        assertEquals("Test1", def.getName());
        assertEquals("plugin-test", def.getId());
        assertEquals(def.getResources().size(), 4);
        assertTrue(def.getResources().get(0) instanceof PluginResourceButton);
        assertTrue(def.getResources().get(1) instanceof PluginResourceButton);
        assertTrue(def.getResources().get(2) instanceof PluginResourceHelp);
        assertTrue(def.getResources().get(3) instanceof PluginResourcePropertyGroup);
    }
    
}
