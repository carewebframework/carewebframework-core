/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.h2.security;

public class H2Constants {
    
    protected static final String TABLE_USER = "CWF_USER";
    
    protected static final String TABLE_DOMAIN = "CWF_DOMAIN";
    
    protected static final String SQL_GET_DOMAINS = "SELECT NAME, ID, ATTRIBUTES FROM " + TABLE_DOMAIN;
    
    protected static final String SQL_GET_USER = "SELECT * FROM " + TABLE_USER
            + " WHERE (DOMAIN_ID=?1 OR DOMAIN_ID='*') AND USERNAME=?2 AND PASSWORD=?3";
            
    protected static final String SQL_TEST = "SELECT ID FROM " + TABLE_USER + " LIMIT 1";
    
    protected static final String SQL_CHANGE_PASSWORD = "UPDATE " + TABLE_USER + " SET PASSWORD=?1 "
            + "WHERE ID=?2 AND (DOMAIN_ID=?3 OR DOMAIN_ID='*')";
            
    /**
     * Static class.
     */
    private H2Constants() {
    }
}
