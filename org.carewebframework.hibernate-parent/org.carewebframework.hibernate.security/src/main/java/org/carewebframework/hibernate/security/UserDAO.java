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

import org.carewebframework.api.domain.IUser;
import org.carewebframework.hibernate.core.AbstractDAO;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

/**
 * DAO for User class.
 */
public class UserDAO extends AbstractDAO<User> {
    
    
    private static final String HQL_AUTHENTICATE = "FROM org.carewebframework.hibernate.security.User "
            + "WHERE LOWER(username)=:username AND password=:password AND (domain=:domain OR domain='*')";
    
    public UserDAO(SessionFactory sessionFactory) {
        super(sessionFactory);
    }
    
    public IUser authenticate(String username, String password, SecurityDomain domain) {
        Session session = getSession();
        Transaction tx = session.beginTransaction();
        User user = null;
        
        try {
            Query query = getSession().createQuery(HQL_AUTHENTICATE);
            query.setString("password", password);
            query.setString("username", username.toLowerCase());
            query.setString("domain", domain.getLogicalId());
            user = (User) query.uniqueResult();
            
            if (user != null) {
                user.setLoginDomain(domain);
            }
            
            tx.commit();
            return user;
        } catch (Exception e) {
            tx.rollback();
            throw e;
        }
    }
    
}
