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
import static org.junit.Assert.assertTrue;

import org.carewebframework.shell.plugins.PluginDefinition;
import org.carewebframework.shell.plugins.PluginResourceButton;
import org.carewebframework.shell.plugins.PluginResourceHelp;
import org.carewebframework.shell.plugins.PluginResourcePropertyGroup;
import org.carewebframework.shell.plugins.PluginXmlParser;
import org.carewebframework.ui.test.MockUITest;
import org.carewebframework.web.test.MockTest;
import org.junit.Test;

public class PluginDefinitionParserTest extends MockUITest {
    
    @Test
    public void parserTest() throws Exception {
        String xml = MockTest.getTextFromResource("pluginDefinition.xml");
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
