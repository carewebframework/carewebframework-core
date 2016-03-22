/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.jdbc.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

import org.carewebframework.jdbc.h2.H2DataSource;
import org.carewebframework.jdbc.h2.H2DataSource.DBMode;

import org.junit.Test;

public class TestH2 {
    
    @Test
    public void test() throws Exception {
        String database = System.getProperty("java.io.tmpdir") + "/cwf/database";
        Map<String, String> params = new HashMap<>();
        params.put("database", database);
        testDB(params, DBMode.EMBEDDED, null);
        params.put("port", "default");
        testDB(params, DBMode.LOCAL, null);
        params.put("address", "localhost");
        testDB(params, DBMode.REMOTE, "NumberFormatException");
        params.put("port", "1234");
        testDB(params, DBMode.REMOTE, "Connection refused");
        params.remove("address");
        testDB(params, DBMode.LOCAL, null);
        params.put("user", "user");
        params.put("password", "password");
        testDB(params, DBMode.LOCAL, "Wrong user name or password");
    }
    
    private void testDB(Map<String, String> params, DBMode dbMode, String expectedException) {
        String error = null;
        
        try (H2DataSource db = new H2DataSource(params).init();
                Connection connection = db.getConnection();
                PreparedStatement ps = connection.prepareStatement("SELECT X FROM SYSTEM_RANGE(1, 9);");
                ResultSet rs = ps.executeQuery();) {
            int i = 0;
            
            while (rs.next()) {
                i++;
                assertEquals(i, rs.getInt("X"));
            }
            
            assertEquals(i, 9);
            assertEquals(dbMode, db.getMode());
        } catch (AssertionError e) {
            throw e;
        } catch (Exception e) {
            error = e.getMessage();
        }
        
        if (expectedException == null) {
            assertNull(error);
        } else {
            assertNotNull(error);
            assertTrue(error, error.contains(expectedException));
        }
    }
}
