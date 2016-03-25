/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.hibernate.property;

import java.util.Collections;
import java.util.List;

import org.carewebframework.api.domain.IUser;
import org.carewebframework.hibernate.core.AbstractDAO;
import org.carewebframework.hibernate.property.Property.PropertyId;

import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

public class PropertyDAO extends AbstractDAO<Property> {
    
    
    private static final String GET_INSTANCES = "SELECT DISTINCT INSTANCE FROM CWF_PROPERTY WHERE NAME=:name AND USER=:user AND INSTANCE<>''";
    
    public PropertyDAO(SessionFactory sessionFactory) {
        super(sessionFactory);
    }
    
    public Property get(String propertyName, String instanceName, IUser user) {
        PropertyId id = new PropertyId(propertyName, instanceName, user == null ? null : user.getLogicalId());
        return get(Property.class, id);
    }
    
    public List<String> getInstances(String propertyName, IUser user) {
        Session session = getSession();
        Transaction tx = session.beginTransaction();
        
        try {
            SQLQuery query = session.createSQLQuery(GET_INSTANCES);
            query.setString("name", propertyName).setString("user", user == null ? "" : user.getLogicalId());
            @SuppressWarnings("unchecked")
            List<String> result = query.list();
            Collections.sort(result, String.CASE_INSENSITIVE_ORDER);
            tx.commit();
            return result;
        } catch (Exception e) {
            tx.rollback();
            throw e;
        }
    }
    
}
