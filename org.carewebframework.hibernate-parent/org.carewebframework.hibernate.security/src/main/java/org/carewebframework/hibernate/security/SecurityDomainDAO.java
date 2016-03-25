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
                SecurityDomainRegistry.registerSecurityDomain(domain);
            }
            
            tx.commit();
        } catch (Exception e) {
            tx.rollback();
            throw e;
        }
    }
}
