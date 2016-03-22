/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.jdbc.security;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.sql.DataSource;

import org.carewebframework.api.security.SecurityDomainRegistry;

/**
 * Loader for H2-based security domains.
 */
public class JDBCSecurityDomainLoader {
    
    
    public JDBCSecurityDomainLoader(DataSource datasource) throws Exception {
        try (Connection connection = datasource.getConnection();
                PreparedStatement ps = connection.prepareStatement(JDBCConstants.SQL_GET_DOMAINS);
                ResultSet rs = ps.executeQuery();) {
            while (rs.next()) {
                String name = rs.getString("NAME");
                String id = rs.getString("ID");
                String attrs = rs.getString("ATTRIBUTES");
                JDBCSecurityDomain securityDomain = new JDBCSecurityDomain(datasource, id, name, attrs);
                SecurityDomainRegistry.registerSecurityDomain(securityDomain);
            }
        }
    }
}
