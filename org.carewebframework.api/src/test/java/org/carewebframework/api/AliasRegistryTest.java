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

import org.carewebframework.api.AliasRegistry.AliasType;

import org.junit.Test;

public class AliasRegistryTest {
    
    @Test
    public void test() {
        AliasRegistry reg = AliasRegistry.getInstance();
        AliasType.AUTHORITY.registerAlias("auth1", "auth.alias1");
        AliasType.AUTHORITY.registerAlias("auth2", "auth.alias2");
        reg.registerAlias("AUTHORITY.auth3", "auth.alias3");
        AliasType.AUTHORITY.registerAlias("authx*", "auth.aliasx*");
        AliasType.AUTHORITY.registerAlias("authy.*.abc.*", "authz.*.xyz.*");
        AliasType.AUTHORITY.registerAlias("authy.?.def.*", "authz.?.xyz.*");
        
        AliasType.PROPERTY.registerAlias("prop1", "prop.alias1");
        AliasType.PROPERTY.registerAlias("prop2", "prop.alias2");
        reg.registerAlias("PROPERTY.prop3", "prop.alias3");
        AliasType.PROPERTY.registerAlias("propx*", "prop.aliasx*");
        AliasType.PROPERTY.registerAlias("propy.*.abc.*", "propz.*.xyz.*");
        AliasType.PROPERTY.registerAlias("propy.?.def.*", "propz.?.xyz.*");
        
        assertEquals("auth.alias1", AliasType.AUTHORITY.get("auth1"));
        assertEquals("auth.alias2", AliasType.AUTHORITY.get("auth2"));
        assertEquals("auth.alias3", AliasType.AUTHORITY.get("auth3"));
        assertEquals("auth.aliasx.test", AliasType.AUTHORITY.get("authx.test"));
        assertEquals("authz.123.xyz.456", AliasType.AUTHORITY.get("authy.123.abc.456"));
        assertEquals("authz.9.xyz.789", AliasType.AUTHORITY.get("authy.9.def.789"));
        assertNull(AliasType.AUTHORITY.get("authz.5.ghi.987"));
        
        assertEquals("prop.alias1", AliasType.PROPERTY.get("prop1"));
        assertEquals("prop.alias2", AliasType.PROPERTY.get("prop2"));
        assertEquals("prop.alias3", AliasType.PROPERTY.get("prop3"));
        assertEquals("prop.aliasx.test", AliasType.PROPERTY.get("propx.test"));
        assertEquals("propz.123.xyz.456", AliasType.PROPERTY.get("propy.123.abc.456"));
        assertEquals("propz.9.xyz.789", AliasType.PROPERTY.get("propy.9.def.789"));
        assertNull(AliasType.PROPERTY.get("prop.test.property"));
        
        AliasType.AUTHORITY.registerAlias("auth1", "auth.new.alias1");
    }
    
}
