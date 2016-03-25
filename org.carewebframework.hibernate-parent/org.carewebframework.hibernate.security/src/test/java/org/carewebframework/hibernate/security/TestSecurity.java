/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.hibernate.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.carewebframework.api.domain.IUser;
import org.carewebframework.api.security.SecurityDomainRegistry;
import org.carewebframework.ui.test.CommonTest;

import org.junit.Test;

public class TestSecurity extends CommonTest {
    
    
    @Test
    public void testService() throws Exception {
        String dir = System.getProperty("java.io.tmpdir") + "cwf";
        System.out.println("Test database is at: " + dir);
        SecurityDomainDAO sdao = rootContext.getBean(SecurityDomainDAO.class);
        setupDomains(sdao);
        sdao.init();
        assertEquals(3, SecurityDomainRegistry.getInstance().getAll().size());
        UserDAO udao = rootContext.getBean(UserDAO.class);
        setupUsers(udao);
        SecurityDomain domain = getSecurityDomain("1");
        IUser user = domain.authenticate("DOCTOR123", "DOCTOR321$");
        assertNotNull(user);
        assertEquals("1", user.getLogicalId());
        user = domain.authenticate("DOCTOR123", "DOCTOR321$XXX");
        assertNull(user);
        user = domain.authenticate("USER123", "USER321$");
        assertNotNull(user);
        assertEquals("2", user.getLogicalId());
    }
    
    private void setupDomains(SecurityDomainDAO sc) {
        SecurityDomain domain = new SecurityDomain("1", "General Medicine Clinic", null);
        sc.saveOrUpdate(domain);
        domain = new SecurityDomain("2", "Emergency Room", null);
        sc.saveOrUpdate(domain);
        domain = new SecurityDomain("3", "Test Hospital", "default=true");
        sc.saveOrUpdate(domain);
    }
    
    private void setupUsers(UserDAO udao) {
        SecurityDomain domain = getSecurityDomain("1");
        User user = new User("1", "Doctor, Test", "DOCTOR123", "DOCTOR321$", domain, "PRIV_PATIENT_SELECT");
        udao.saveOrUpdate(user);
        user = new User("2", "User, Test", "USER123", "USER321$", null,
                "PRIV_DEBUG,PRIV_CAREWEB_DESIGNER,PRIV_PATIENT_SELECT");
        udao.saveOrUpdate(user);
    }
    
    private SecurityDomain getSecurityDomain(String id) {
        return (SecurityDomain) SecurityDomainRegistry.getInstance().get(id);
    }
}
