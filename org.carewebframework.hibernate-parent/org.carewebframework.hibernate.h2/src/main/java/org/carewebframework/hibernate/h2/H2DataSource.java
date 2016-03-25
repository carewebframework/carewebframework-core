/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.hibernate.h2;

import java.sql.SQLException;

import org.apache.commons.dbcp2.BasicDataSource;

import org.springframework.util.StringUtils;

import org.h2.tools.Server;

/**
 * H2-based data source that also handles starting the database in the appropriate mode.
 */
public class H2DataSource extends BasicDataSource {
    
    
    public enum DBMode {
        EMBEDDED, // H2 embedded mode
        REMOTE, // H2 remote server
        LOCAL // H2 local server
    };
    
    private Server server;
    
    private DBMode dbMode = DBMode.EMBEDDED;
    
    public H2DataSource() {
    }
    
    /**
     * If running H2 in local mode, starts the server.
     * 
     * @return this (for chaining)
     * @throws Exception Unspecified exception
     */
    public H2DataSource init() throws Exception {
        if (dbMode == DBMode.LOCAL) {
            String port = getPort();
            
            if (port.isEmpty()) {
                server = Server.createTcpServer();
            } else {
                server = Server.createTcpServer("-tcpPort", port);
            }
            
            server.start();
        }
        
        return this;
    }
    
    /**
     * Extract the TCP port from the connection URL.
     * 
     * @return The TCP port, or empty string if none.
     */
    private String getPort() {
        String url = getUrl();
        int i = url.indexOf("://") + 3;
        int j = url.indexOf("/", i);
        String s = i == 2 || j == -1 ? "" : url.substring(i, j);
        i = s.indexOf(":");
        return i == -1 ? "" : s.substring(i + 1);
    }
    
    public void destroy() throws Exception {
        close();
    }
    
    public DBMode getMode() {
        return dbMode;
    }
    
    public void setMode(String value) {
        dbMode = StringUtils.isEmpty(value) ? DBMode.EMBEDDED : DBMode.valueOf(value.toUpperCase());
    }
    
    @Override
    public void close() throws SQLException {
        super.close();
        
        if (server != null) {
            server.stop();
        }
    }
    
}
