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
package org.carewebframework.hibernate.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.carewebframework.api.domain.IUser;
import org.carewebframework.api.security.SecurityDomainRegistry;
import org.carewebframework.api.test.CommonTest;
import org.junit.Test;

public class TestSecurity extends CommonTest {
    
    @Test
    public void testService() throws Exception {
        String dir = System.getProperty("java.io.tmpdir") + "cwf";
        System.out.println("Test database is at: " + dir);
        SecurityDomainDAO sdao = appContext.getBean(SecurityDomainDAO.class);
        setupDomains(sdao);
        sdao.init();
        assertEquals(3, SecurityDomainRegistry.getInstance().getAll().size());
        UserDAO udao = appContext.getBean(UserDAO.class);
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
