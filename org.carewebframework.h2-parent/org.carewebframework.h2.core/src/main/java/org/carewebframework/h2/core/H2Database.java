/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.h2.core;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;

import org.apache.commons.lang.math.NumberUtils;

import org.carewebframework.common.MiscUtil;

import org.h2.tools.Server;

/**
 * H2-based implementation of a property service.
 */
public class H2Database implements AutoCloseable {
    
    public enum DBMode {
        EMBEDDED, // H2 embedded mode
        REMOTE, // H2 remote server
        LOCAL // H2 local server
    };
    
    private final Map<String, String> params;
    
    private Server server;
    
    private String connectionString;
    
    private DBMode dbMode;
    
    public H2Database(Map<String, String> params) throws Exception {
        this.params = params;
    }
    
    /**
     * Returns a database connection.
     * 
     * @return A database connection.
     */
    public Connection getConnection() {
        try {
            return DriverManager.getConnection(connectionString);
        } catch (SQLException e) {
            throw MiscUtil.toUnchecked(e);
        }
    }
    
    /**
     * Initialize the database server.
     * 
     * @return this (for chaining)
     * @throws Exception Unspecified exception
     */
    public H2Database init() throws Exception {
        Class.forName("org.h2.Driver");
        StringBuilder sb = new StringBuilder("jdbc:h2:");
        
        String address = getParam("address");
        
        if (!address.isEmpty()) {
            dbMode = DBMode.REMOTE;
            sb.append("tcp://").append(address);
        }
        
        String port = getParam("port");
        
        if (dbMode != null || !port.isEmpty()) {
            if (dbMode == null) {
                port = port.toLowerCase();
                dbMode = DBMode.LOCAL;
                sb.append("tcp://localhost");
            } else if (port.isEmpty()) {
                throw new IllegalArgumentException("Remote server port must be specified.");
            }
            
            sb.append(":").append(port);
        }
        
        if (dbMode == null) {
            dbMode = DBMode.EMBEDDED;
        } else {
            sb.append("/");
        }
        
        String database = getParam("database");
        
        if (database.isEmpty()) {
            throw new IllegalArgumentException("No H2 database was specified.");
        }
        
        sb.append(database);
        appendConfigByName(sb, "user");
        appendConfigByName(sb, "password");
        appendConfigByValue(sb, getParam("extra"));
        connectionString = sb.toString();
        
        if (dbMode == DBMode.LOCAL) {
            if ("default".equals(port)) {
                server = Server.createTcpServer();
                port = Integer.toString(server.getPort());
                connectionString = connectionString.replaceFirst("\\Qdefault\\E", port);
            } else {
                if (NumberUtils.toInt(port) <= 0) {
                    throw new IllegalArgumentException("Port value is invalid: " + port);
                }
                
                server = Server.createTcpServer("-tcpPort", port);
            }
            
            server.start();
        }
        
        return this;
    }
    
    public void destroy() throws Exception {
        close();
    }
    
    public DBMode getMode() {
        return dbMode;
    }
    
    /**
     * Returns a configuration parameter.
     * 
     * @param name Name of parameter.
     * @return The configuration parameter value (never null).
     */
    private String getParam(String name) {
        String param = params.get(name);
        return param == null ? "" : param.trim();
    }
    
    private void appendConfigByName(StringBuilder sb, String configName) {
        String configValue = getParam(configName);
        
        if (!configValue.isEmpty()) {
            appendConfigByValue(sb, configName.toUpperCase() + "=" + configValue);
        }
    }
    
    private void appendConfigByValue(StringBuilder sb, String configValue) {
        if (!configValue.isEmpty()) {
            if (!configValue.startsWith(";")) {
                sb.append(";");
            }
            
            sb.append(configValue);
        }
    }
    
    @Override
    public void close() {
        if (server != null) {
            server.stop();
        }
    }
    
}
