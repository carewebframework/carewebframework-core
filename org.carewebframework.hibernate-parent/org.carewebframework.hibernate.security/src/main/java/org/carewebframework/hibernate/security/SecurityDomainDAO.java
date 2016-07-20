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

import java.util.List;

import org.carewebframework.api.security.SecurityDomainRegistry;
import org.carewebframework.hibernate.core.AbstractDAO;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

/**
 * Loader for Hibernate-based security domains.
 */
public class SecurityDomainDAO extends AbstractDAO<SecurityDomain> {
    
    
    public SecurityDomainDAO(SessionFactory sessionFactory) {
        super(sessionFactory);
    }
    
    public void init() {
        Session session = getSession();
        Transaction tx = session.beginTransaction();
        
        try {
            Criteria criteria = session.createCriteria(SecurityDomain.class);
            @SuppressWarnings("unchecked")
            List<SecurityDomain> domains = criteria.list();
            
            for (SecurityDomain domain : domains) {
                if (!"*".equals(domain.getLogicalId())) {
                    SecurityDomainRegistry.registerSecurityDomain(domain);
                }
            }
            
            tx.commit();
        } catch (Exception e) {
            tx.rollback();
            throw e;
        }
    }
}
