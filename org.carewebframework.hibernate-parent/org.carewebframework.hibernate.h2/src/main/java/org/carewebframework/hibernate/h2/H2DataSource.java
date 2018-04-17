/*
 * #%L
 * carewebframework
 * %%
 * Copyright (C) 2008 - 2016 Regenstrief Institute, Inc.
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * This Source Code Form is also subject to the terms of the Health-Related
 * Additional Disclaimer of Warranty and Limitation of Liability available at
 *
 *      http://www.carewebframework.org/licensing/disclaimer.
 *
 * #L%
 */
package org.carewebframework.hibernate.h2;

import java.sql.SQLException;

import org.apache.commons.dbcp2.BasicDataSource;
import org.h2.tools.Server;
import org.springframework.util.StringUtils;

/**
 * H2-based data source that also handles starting the database in the appropriate mode.
 */
public class H2DataSource extends BasicDataSource {

    /**
     * Server modes.
     */
    public enum DBMode {
        EMBEDDED, // H2 embedded mode
        REMOTE, // H2 remote server
        LOCAL // H2 local server
    }

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
