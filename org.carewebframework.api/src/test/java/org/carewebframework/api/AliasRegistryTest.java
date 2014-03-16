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

import org.carewebframework.api.AliasRegistry.AliasType;

import org.junit.Test;

public class AliasRegistryTest {
    
    @Test
    public void test() {
        AliasRegistry reg = AliasRegistry.getInstance();
        reg.registerAlias(AliasType.AUTHORITY, "auth1", "auth.alias1");
        reg.registerAlias(AliasType.AUTHORITY, "auth2", "auth.alias2");
        reg.registerAlias("AUTHORITY.auth3", "auth.alias3");
        reg.registerAlias(AliasType.AUTHORITY, "authx*", "auth.aliasx*");
        reg.registerAlias(AliasType.AUTHORITY, "authy.*.abc.*", "authz.*.xyz.*");
        reg.registerAlias(AliasType.AUTHORITY, "authy.?.def.*", "authz.?.xyz.*");
        
        reg.registerAlias(AliasType.PROPERTY, "prop1", "prop.alias1");
        reg.registerAlias(AliasType.PROPERTY, "prop2", "prop.alias2");
        reg.registerAlias("PROPERTY.prop3", "prop.alias3");
        reg.registerAlias(AliasType.PROPERTY, "propx*", "prop.aliasx*");
        reg.registerAlias(AliasType.PROPERTY, "propy.*.abc.*", "propz.*.xyz.*");
        reg.registerAlias(AliasType.PROPERTY, "propy.?.def.*", "propz.?.xyz.*");
        
        assertEquals("auth.alias1", reg.get(AliasType.AUTHORITY, "auth1"));
        assertEquals("auth.alias2", reg.get(AliasType.AUTHORITY, "auth2"));
        assertEquals("auth.alias3", reg.get(AliasType.AUTHORITY, "auth3"));
        assertEquals("auth.aliasx.test", reg.get(AliasType.AUTHORITY, "authx.test"));
        assertEquals("authz.123.xyz.456", reg.get(AliasType.AUTHORITY, "authy.123.abc.456"));
        assertEquals("authz.9.xyz.789", reg.get(AliasType.AUTHORITY, "authy.9.def.789"));
        
        assertEquals("prop.alias1", reg.get(AliasType.PROPERTY, "prop1"));
        assertEquals("prop.alias2", reg.get(AliasType.PROPERTY, "prop2"));
        assertEquals("prop.alias3", reg.get(AliasType.PROPERTY, "prop3"));
        assertEquals("prop.aliasx.test", reg.get(AliasType.PROPERTY, "propx.test"));
        assertEquals("propz.123.xyz.456", reg.get(AliasType.PROPERTY, "propy.123.abc.456"));
        assertEquals("propz.9.xyz.789", reg.get(AliasType.PROPERTY, "propy.9.def.789"));
    }
    
}
