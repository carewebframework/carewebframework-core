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
package org.carewebframework.ui.command;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

public class CommandTest {

    private static final String[] VALID_SHORTCUTS = { "$a", "@f1", "up", "^a" };

    private static final String[] INVALID_SHORTCUTS = { "#xyz", "123", "$", "#$", "@" };

    @Test
    public void testShortcutValidation() {
        testShortcutValidation(VALID_SHORTCUTS, true);
        testShortcutValidation(INVALID_SHORTCUTS, false);
    }

    private void testShortcutValidation(String[] shortcuts, boolean areValid) {
        for (String shortcut : shortcuts) {
            assertTrue(CommandUtil.validateShortcut(shortcut) != null == areValid);
        }
    }

    @Test
    public void testShortcutParsing() {
        testShortcutParsing(VALID_SHORTCUTS, true);
        testShortcutParsing(INVALID_SHORTCUTS, false);
    }

    private void testShortcutParsing(String[] shortcuts, boolean areValid) {
        Set<String> sc1 = new HashSet<>();
        sc1.addAll(Arrays.asList(shortcuts));
        String concat_sc1 = CommandUtil.concatShortcuts(sc1);
        Set<String> sc2 = CommandUtil.parseShortcuts(concat_sc1, null);
        assertTrue(sc1.size() == sc2.size() == areValid);
        assertTrue(sc2.isEmpty() != areValid);
    }
}
