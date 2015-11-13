package org.carewebframework.help;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Locale;

import org.junit.Test;

public class HelpTest {
    
    private static final HelpModuleRegistry registry = HelpModuleRegistry.getInstance();
    
    @Test
    public void testRegistry() {
        registry.clear();
        HelpModule moduleDef = createHelpModule("testModule", null);
        HelpModule moduleEn = createHelpModule("testModule", "en");
        HelpModule moduleFr = createHelpModule("testModule", "fr");
        Locale.setDefault(new Locale("en"));
        assertTrue(registry.get("testModule") == moduleEn);
        Locale.setDefault(new Locale("en", "CA"));
        assertTrue(registry.get("testModule") == moduleEn);
        Locale.setDefault(new Locale("fr"));
        assertTrue(registry.get("testModule") == moduleFr);
        Locale.setDefault(new Locale("de"));
        assertTrue(registry.get("testModule") == moduleDef);
        assertNull(registry.get("otherModule"));
        registry.clear();
    }
    
    private HelpModule createHelpModule(String id, String locale) {
        HelpModule module = new HelpModule();
        module.setId(id);
        module.setLocale(locale);
        registry.register(module);
        return module;
    }
}
