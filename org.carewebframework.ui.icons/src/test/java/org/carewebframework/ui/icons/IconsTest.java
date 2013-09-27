/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.ui.icons;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.carewebframework.ui.test.CommonTest;

import org.junit.Test;

public class IconsTest extends CommonTest {
    
    @Test
    public void test() {
        IconLibraryRegistry reg = desktopContext.getBean("iconLibraryRegistry", IconLibraryRegistry.class);
        assertNotNull(reg);
        IIconLibrary lib = reg.get("silk");
        assertNotNull(lib);
        assertEquals("~./org/carewebframework/ui/icons/16x16/silk/help.png", lib.getIconUrl("help.png", "16x16"));
        assertEquals(1001, lib.getMatching("*", "16x16").size());
        assertEquals(0, lib.getMatching("*", "32x32").size());
        assertEquals(6, lib.getMatching("weather*", "16x16").size());
        assertEquals(6, lib.getMatching("weather*", "*").size());
        assertEquals(6, reg.getMatching("s?l*", "we*her*", "16x*").size());
        lib = reg.get("none");
        assertNull(lib);
    }
}
