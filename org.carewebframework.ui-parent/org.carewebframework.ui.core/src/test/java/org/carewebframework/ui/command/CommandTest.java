/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.ui.command;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

public class CommandTest {
    
    private static final String[] VALID_SHORTCUTS = { "@#f1", "#up", "^a" };
    
    private static final String[] INVALID_SHORTCUTS = { "$a", "#xyz", "123", "$", "#$", "@" };
    
    @Test
    public void testShortcutValidation() {
        testShortcutValidation(VALID_SHORTCUTS, true);
        testShortcutValidation(INVALID_SHORTCUTS, false);
    }
    
    private void testShortcutValidation(String[] shortcuts, boolean areValid) {
        for (String shortcut : shortcuts) {
            assertTrue(CommandUtil.validateShortcut(shortcut) == areValid);
        }
    }
    
    @Test
    public void testShortcutParsing() {
        testShortcutParsing(VALID_SHORTCUTS, true);
        testShortcutParsing(INVALID_SHORTCUTS, false);
    }
    
    private void testShortcutParsing(String[] shortcuts, boolean areValid) {
        Set<String> sc1 = new HashSet<String>();
        sc1.addAll(Arrays.asList(shortcuts));
        String concat_sc1 = CommandUtil.concatShortcuts(sc1);
        Set<String> sc2 = CommandUtil.parseShortcuts(concat_sc1, null);
        assertTrue(sc1.equals(sc2) == areValid);
        assertTrue(sc2.isEmpty() != areValid);
    }
}
