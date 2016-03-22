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

import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.sql.DataSource;

import org.apache.commons.io.IOUtils;

import org.carewebframework.common.StrUtil;
import org.carewebframework.security.spring.AbstractSecurityService;

/**
 * H2 security service for testing.
 */
public class JDBCSecurityService extends AbstractSecurityService {
    
    
    private final DataSource datasource;
    
    public JDBCSecurityService(DataSource datasource) throws Exception {
        this.datasource = datasource;
        initTables();
    }
    
    @Override
    public boolean validatePassword(String password) {
        return password.equals(getAuthenticatedUser().getPassword());
    }
    
    @Override
    public String changePassword(String oldPassword, String newPassword) {
        if (!validatePassword(oldPassword)) {
            return "Invalid username or password.";
        }
        
        JDBCUser user = (JDBCUser) getAuthenticatedUser();
        try (Connection connection = datasource.getConnection();
                PreparedStatement ps = connection.prepareStatement(JDBCConstants.SQL_CHANGE_PASSWORD);) {
            
            ps.setString(1, newPassword);
            ps.setString(2, user.getLogicalId());
            ps.setString(3, user.getSecurityDomain().getLogicalId());
            ps.execute();
            
            if (ps.getUpdateCount() != 1) {
                return "Error saving new password.";
            }
            
            user.setPassword(newPassword);
            return null;
        } catch (Exception e) {
            return e.getMessage();
        }
    }
    
    private void initTables() throws Exception {
        try (Connection connection = datasource.getConnection();) {
            
            try (PreparedStatement ps = connection.prepareStatement(JDBCConstants.SQL_TEST);
                    ResultSet rs = ps.executeQuery();) {
                if (rs.next()) {
                    return;
                }
            } catch (Exception e) {}
            
            String sql;
            
            try (InputStream is = getClass().getResourceAsStream("/MockSetup.sql");) {
                sql = StrUtil.fromList(IOUtils.readLines(is), " ");
            }
            
            try (PreparedStatement ps2 = connection.prepareStatement(sql);) {
                ps2.execute();
            }
        }
    }
}
