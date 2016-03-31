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
        authenticate(domain, "DOCTOR123", "DOCTOR321$", "1");
        authenticate(domain, "DOCTOR123", "DOCTOR321$XXX", null);
        authenticate(domain, "USER123", "USER321$", "2");
    }
    
    private void setupDomains(SecurityDomainDAO sc) {
        SecurityDomain domain = new SecurityDomain("1", "General Medicine Clinic", null);
        sc.saveOrUpdate(domain);
        domain = new SecurityDomain("2", "Emergency Room", null);
        sc.saveOrUpdate(domain);
        domain = new SecurityDomain("3", "Test Hospital", "default=true");
        sc.saveOrUpdate(domain);
        domain = new SecurityDomain("*", "All Domains", null);
        sc.saveOrUpdate(domain);
    }
    
    private void setupUsers(UserDAO udao) {
        SecurityDomain domain = getSecurityDomain("1");
        User user = new User("1", "Doctor, Test", "DOCTOR123", "DOCTOR321$", domain, "PRIV_PATIENT_SELECT");
        udao.saveOrUpdate(user);
        user = new User("2", "User, Test", "USER123", "USER321$", domain,
                "PRIV_DEBUG,PRIV_CAREWEB_DESIGNER,PRIV_PATIENT_SELECT");
        udao.saveOrUpdate(user);
    }
    
    private void authenticate(SecurityDomain domain, String username, String password, String expectedId) {
        try {
            IUser user = domain.authenticate(username, password);
            assertEquals(expectedId, user.getLogicalId());
        } catch (Exception e) {
            assertNull(expectedId);
        }
    }
    
    private SecurityDomain getSecurityDomain(String id) {
        return (SecurityDomain) SecurityDomainRegistry.getInstance().get(id);
    }
}
