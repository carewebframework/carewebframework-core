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
package org.carewebframework.ui.action;

import static org.junit.Assert.assertTrue;

import org.fujion.script.ScriptRegistry;
import org.fujion.script.groovy.GroovyScript;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class ActionTest {

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void testActionFormats() {
        loadActionTypes();
        loadGroovyScript();
        assertTrue(ActionTypeRegistry.getType("jscript: alert('hi');") instanceof ActionTypeJavascript);
        assertTrue(ActionTypeRegistry.getType("javascript: alert('hi');") instanceof ActionTypeJavascript);
        assertTrue(ActionTypeRegistry.getType("http://www.regenstrief.org") instanceof ActionTypeUrl);
        assertTrue(ActionTypeRegistry.getType("https://www.regenstrief.org") instanceof ActionTypeUrl);
        assertTrue(ActionTypeRegistry.getType("groovy: test") instanceof ActionTypeServerScript);
        exception.expect(IllegalArgumentException.class);
        ActionTypeRegistry.getType("unknown type");
    }
    
    private void loadGroovyScript() {
        ScriptRegistry.getInstance().register(new GroovyScript());
    }

    private void loadActionTypes() {
        loadActionType(new ActionTypeJavascript());
        loadActionType(new ActionTypeServerScript());
        loadActionType(new ActionTypeUrl());
    }
    
    private void loadActionType(IActionType<?> actionType) {
        ActionTypeRegistry.getInstance().register(actionType);
    }

}
