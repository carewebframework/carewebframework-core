package org.carewebframework.help;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.util.List;
import java.util.Locale;

import org.apache.commons.io.IOUtils;

import org.carewebframework.common.MiscUtil;
import org.carewebframework.common.StrUtil;

import org.junit.Test;

public class HelpTest {
    
    private static final HelpModuleRegistry registry = HelpModuleRegistry.getInstance();
    
    @Test
    public void testRegistry() {
        registry.clear();
        HelpModule moduleDef = createHelpModule("helpModuleDefault.xml");
        HelpModule moduleEn = createHelpModule("helpModuleEn.xml");
        HelpModule moduleFr = createHelpModule("helpModuleFr.xml");
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
    
    private HelpModule createHelpModule(String file) {
        try (InputStream is = HelpTest.class.getResourceAsStream("/" + file);) {
            List<String> xml = IOUtils.readLines(is);
            HelpModule module = HelpXmlParser.fromXml(StrUtil.fromList(xml));
            registry.register(module);
            return module;
        } catch (Exception e) {
            throw MiscUtil.toUnchecked(e);
        }
    }
}
