/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.h2.property;

import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.carewebframework.api.domain.IUser;
import org.carewebframework.api.property.IPropertyService;
import org.carewebframework.api.security.ISecurityService;
import org.carewebframework.common.MiscUtil;
import org.carewebframework.common.StrUtil;
import org.carewebframework.h2.core.H2Database;

/**
 * H2-based implementation of a property service.
 */
public class H2PropertyService implements IPropertyService {
    
    private static final String PROPERTIES_TABLE = "CWF_PROPERTY";
    
    private static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS %1$s ("
            + "NAME VARCHAR(255) NOT NULL, USER VARCHAR(255) DEFAULT '', INSTANCE VARCHAR(255) DEFAULT '', VALUE CLOB)";
            
    private static final String CREATE_INDEX1 = "CREATE INDEX IF NOT EXISTS INSTANCE_INDEX ON %1$s (INSTANCE)";
    
    private static final String CREATE_INDEX2 = "CREATE PRIMARY KEY ON %1$s (NAME, USER, INSTANCE)";
    
    private static final String DROP_TABLE = "DROP TABLE IF EXISTS %s";
    
    private static final String SAVE_VALUE = "MERGE INTO %s (NAME, USER, INSTANCE, VALUE) "
            + "KEY(NAME, USER, INSTANCE) VALUES(?1, ?2, ?3, ?4)";
            
    private static final String GET_VALUE = "SELECT VALUE FROM %s WHERE NAME=?1 AND INSTANCE=?2 AND (USER='' OR USER=?3) "
            + "ORDER BY USER DESC";
            
    private static final String DELETE_VALUE = "DELETE FROM %s WHERE NAME=?1 AND USER=?2 AND INSTANCE=?3";
    
    private static final String GET_INSTANCES = "SELECT DISTINCT INSTANCE FROM %s WHERE NAME=?1 AND USER=?2";
    
    private Connection connection;
    
    private final H2Database database;
    
    private final ISecurityService securityService;
    
    public H2PropertyService(H2Database database, ISecurityService securityService) {
        this.database = database;
        this.securityService = securityService;
    }
    
    public H2PropertyService init() throws Exception {
        return init(false);
    }
    
    public H2PropertyService init(boolean reset) throws Exception {
        Class.forName("org.h2.Driver");
        connection = database.getConnection();
        
        if (reset) {
            executePreparedStatement(DROP_TABLE);
        }
        
        executePreparedStatement(CREATE_TABLE);
        executePreparedStatement(CREATE_INDEX1);
        
        try {
            executePreparedStatement(CREATE_INDEX2);
        } catch (Exception e) {}
        
        return this;
    }
    
    public void destroy() throws Exception {
        connection.close();
    }
    
    private String formatSQL(String sql) {
        return String.format(sql, PROPERTIES_TABLE);
    }
    
    protected PreparedStatement getPreparedStatement(String sql, Object... params) {
        try {
            PreparedStatement ps = connection.prepareStatement(formatSQL(sql));
            
            for (int i = 0; i < params.length; i++) {
                Object param = params[i];
                ps.setObject(i + 1, param == null ? "" : param);
            }
            
            return ps;
        } catch (SQLException e) {
            throw MiscUtil.toUnchecked(e);
        }
    }
    
    private void executePreparedStatement(String sql, Object... params) {
        try (PreparedStatement ps = getPreparedStatement(sql, params);) {
            ps.execute();
        } catch (Exception e) {
            throw MiscUtil.toUnchecked(e);
        }
    }
    
    private String getUserId(boolean asGlobal) {
        if (asGlobal) {
            return "";
        }
        
        IUser user = securityService.getAuthenticatedUser();
        return user == null ? "anonymous" : user.getLogicalId();
    }
    
    @Override
    public boolean isAvailable() {
        return true;
    }
    
    @Override
    public String getValue(String propertyName, String instanceName) {
        try (PreparedStatement ps = getPreparedStatement(GET_VALUE, propertyName, instanceName, getUserId(false));
                ResultSet rs = ps.executeQuery();) {
                
            if (rs.next()) {
                Clob clob = rs.getClob("VALUE");
                return clob.getSubString(1, (int) clob.length());
            }
        } catch (SQLException e) {
            throw MiscUtil.toUnchecked(e);
        }
        
        return null;
    }
    
    @Override
    public List<String> getValues(String propertyName, String instanceName) {
        List<String> results = null;
        String result = getValue(propertyName, instanceName);
        
        if (result != null && !result.isEmpty()) {
            results = StrUtil.toList(result);
        }
        
        return results;
    }
    
    @Override
    public void saveValue(String propertyName, String instanceName, boolean asGlobal, String value) {
        if (value == null) {
            deleteValue(propertyName, instanceName, getUserId(asGlobal));
        } else {
            try {
                Clob clob = connection.createClob();
                clob.setString(1, value == null ? "" : value);
                executePreparedStatement(SAVE_VALUE, propertyName, getUserId(asGlobal), instanceName, clob);
            } catch (SQLException e) {
                throw MiscUtil.toUnchecked(e);
            }
        }
    }
    
    @Override
    public void saveValues(String propertyName, String instanceName, boolean asGlobal, List<String> values) {
        saveValue(propertyName, instanceName, asGlobal, values == null ? null : StrUtil.fromList(values));
    }
    
    private void deleteValue(String propertyName, String instanceName, String user) {
        executePreparedStatement(DELETE_VALUE, propertyName, user, instanceName);
    }
    
    @Override
    public List<String> getInstances(String propertyName, boolean asGlobal) {
        try (PreparedStatement ps = getPreparedStatement(GET_INSTANCES, propertyName, getUserId(asGlobal));
                ResultSet rs = ps.executeQuery();) {
                
            List<String> results = new ArrayList<>();
            
            while (rs.next()) {
                String instance = rs.getString("INSTANCE");
                
                if (!instance.isEmpty()) {
                    results.add(instance);
                }
            }
            
            return results;
        } catch (Exception e) {
            throw MiscUtil.toUnchecked(e);
        }
    }
    
}
