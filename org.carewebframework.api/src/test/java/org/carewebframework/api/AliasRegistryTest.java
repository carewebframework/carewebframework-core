/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class AliasRegistryTest {
    
    @Test
    public void test() {
        AliasTypeRegistry reg = AliasTypeRegistry.getInstance();
        reg.get("AUTHORITY").registerAlias("auth1", "auth.alias1");
        reg.get("AUTHORITY").registerAlias("auth2", "auth.alias2");
        AliasTypeRegistry.registerAlias("AUTHORITY", "auth3", "auth.alias3");
        reg.get("AUTHORITY").registerAlias("authx*", "auth.aliasx*");
        reg.get("AUTHORITY").registerAlias("authy.*.abc.*", "authz.*.xyz.*");
        reg.get("AUTHORITY").registerAlias("authy.?.def.*", "authz.?.xyz.*");
        
        reg.get("PROPERTY").registerAlias("prop1", "prop.alias1");
        reg.get("PROPERTY").registerAlias("prop2", "prop.alias2");
        AliasTypeRegistry.registerAlias("PROPERTY", "prop3", "prop.alias3");
        reg.get("PROPERTY").registerAlias("propx*", "prop.aliasx*");
        reg.get("PROPERTY").registerAlias("propy.*.abc.*", "propz.*.xyz.*");
        reg.get("PROPERTY").registerAlias("propy.?.def.*", "propz.?.xyz.*");
        
        assertEquals("auth.alias1", reg.get("AUTHORITY").get("auth1"));
        assertEquals("auth.alias2", reg.get("AUTHORITY").get("auth2"));
        assertEquals("auth.alias3", reg.get("AUTHORITY").get("auth3"));
        assertEquals("auth.aliasx.test", reg.get("AUTHORITY").get("authx.test"));
        assertEquals("authz.123.xyz.456", reg.get("AUTHORITY").get("authy.123.abc.456"));
        assertEquals("authz.9.xyz.789", reg.get("AUTHORITY").get("authy.9.def.789"));
        assertNull(reg.get("AUTHORITY").get("authz.5.ghi.987"));
        
        assertEquals("prop.alias1", reg.get("PROPERTY").get("prop1"));
        assertEquals("prop.alias2", reg.get("PROPERTY").get("prop2"));
        assertEquals("prop.alias3", reg.get("PROPERTY").get("prop3"));
        assertEquals("prop.aliasx.test", reg.get("PROPERTY").get("propx.test"));
        assertEquals("propz.123.xyz.456", reg.get("PROPERTY").get("propy.123.abc.456"));
        assertEquals("propz.9.xyz.789", reg.get("PROPERTY").get("propy.9.def.789"));
        assertNull(reg.get("PROPERTY").get("prop.test.property"));
        
        reg.get("AUTHORITY").registerAlias("auth1", "auth.new.alias1");
    }
    
}
