/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.hibernate.core;

import java.io.Serializable;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

/**
 * Abstract base implementation for a DAO object.
 * 
 * @param <T> The data class.
 */
public class AbstractDAO<T> {
    
    
    public enum Operation {
        PERSIST, DELETE, SAVE, UPDATE, SAVEORUPDATE, LOAD
    };
    
    private final SessionFactory sessionFactory;
    
    public AbstractDAO(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }
    
    protected Session getSession() {
        return sessionFactory.getCurrentSession();
    }
    
    public void persist(T entity) {
        doOperation(Operation.PERSIST, entity);
    }
    
    public void save(T entity) {
        doOperation(Operation.SAVE, entity);
    }
    
    public void update(T entity) {
        doOperation(Operation.UPDATE, entity);
    }
    
    public void saveOrUpdate(T entity) {
        doOperation(Operation.SAVEORUPDATE, entity);
    }
    
    public void delete(T entity) {
        doOperation(Operation.DELETE, entity);
    }
    
    @SuppressWarnings("unchecked")
    public T get(Class<T> clazz, Serializable id) {
        Session session = getSession();
        Transaction tx = session.beginTransaction();
        T result = null;
        
        try {
            result = (T) getSession().get(clazz, id);
            tx.commit();
            return result;
        } catch (Exception e) {
            tx.rollback();
            throw e;
        }
    }
    
    private void doOperation(Operation operation, T entity) {
        Session session = getSession();
        Transaction tx = session.beginTransaction();
        
        try {
            switch (operation) {
                case PERSIST:
                    session.persist(entity);
                    break;
                
                case DELETE:
                    session.delete(entity);
                    break;
                
                case UPDATE:
                    session.update(entity);
                    break;
                
                case SAVE:
                    session.save(entity);
                    break;
                
                case SAVEORUPDATE:
                    session.saveOrUpdate(entity);
                    break;
            }
            
            tx.commit();
            
        } catch (Exception e) {
            tx.rollback();
            throw e;
        }
    }
    
}
