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

import java.io.StringReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.Properties;

import org.carewebframework.api.domain.IUser;
import org.carewebframework.api.security.ISecurityDomain;
import org.carewebframework.common.MiscUtil;
import org.carewebframework.h2.core.H2Database;

import org.springframework.security.authentication.BadCredentialsException;

/**
 * H2 user for testing.
 */
public class H2SecurityDomain implements ISecurityDomain {
    
    private static final long serialVersionUID = 1L;
    
    private final String logicalId;
    
    private final String name;
    
    private final Properties attributes = new Properties();
    
    private final H2Database database;
    
    public H2SecurityDomain(H2Database database, String logicalId, String name, String attrs) {
        this.database = database;
        this.logicalId = logicalId;
        this.name = name;
        
        if (attrs != null) {
            try (StringReader reader = new StringReader(attrs.replace(';', '\n'));) {
                attributes.load(reader);
            } catch (Exception e) {
            
            }
        }
        
    }
    
    @Override
    public String toString() {
        return name;
    }
    
    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public String getLogicalId() {
        return logicalId;
    }
    
    @Override
    public String getAttribute(String name) {
        return attributes.getProperty(name);
    }
    
    @Override
    public IUser authenticate(String username, String password) {
        try (Connection connection = database.getConnection();
                PreparedStatement ps = connection.prepareStatement(H2Constants.SQL_GET_USER);) {
                
            ps.setString(1, logicalId);
            ps.setString(2, username.toUpperCase());
            ps.setString(3, password);
            
            try (ResultSet rs = ps.executeQuery();) {
                if (!rs.next()) {
                    throw new BadCredentialsException("Invalid credentials");
                }
                
                String logicalId = rs.getString("ID");
                String fullName = rs.getString("NAME");
                String authorities = rs.getString("AUTHORITIES");
                return new H2User(logicalId, fullName, username, password, this, authorities);
            }
        } catch (Exception e) {
            throw MiscUtil.toUnchecked(e);
        }
    }
    
    @Override
    public H2SecurityDomain getNativeSecurityDomain() {
        return this;
    }
    
    @Override
    public List<String> getGrantedAuthorities(IUser user) {
        return ((H2User) user).getGrantedAuthorities();
    }
    
}
