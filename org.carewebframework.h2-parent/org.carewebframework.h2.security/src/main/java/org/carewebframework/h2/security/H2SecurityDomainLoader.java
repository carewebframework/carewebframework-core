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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.carewebframework.api.security.SecurityDomainRegistry;
import org.carewebframework.h2.core.H2Database;

/**
 * Loader for H2-based security domains.
 */
public class H2SecurityDomainLoader {
    
    public H2SecurityDomainLoader(H2Database database) throws Exception {
        try (Connection connection = database.getConnection();
                PreparedStatement ps = connection.prepareStatement(H2Constants.SQL_GET_DOMAINS);
                ResultSet rs = ps.executeQuery();) {
            while (rs.next()) {
                String name = rs.getString("NAME");
                String id = rs.getString("ID");
                String attrs = rs.getString("ATTRIBUTES");
                H2SecurityDomain securityDomain = new H2SecurityDomain(database, id, name, attrs);
                SecurityDomainRegistry.registerSecurityDomain(securityDomain);
            }
        }
    }
}
