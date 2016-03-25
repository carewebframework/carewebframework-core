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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;

import org.carewebframework.hibernate.h2.H2DataSource.DBMode;

import org.junit.Test;

public class TestH2 {
    
    
    @Test
    public void test() throws Exception {
        String database = System.getProperty("java.io.tmpdir") + "cwf/database";
        Map<String, Object> params = new HashMap<>();
        
        params.put("url", "jdbc:h2:" + database);
        testDB(params, DBMode.EMBEDDED, null);
        
        params.put("url", "jdbc:h2:tcp://localhost/" + database);
        testDB(params, DBMode.LOCAL, null);
        testDB(params, DBMode.REMOTE, "Connection refused");
        
        params.put("url", "jdbc:h2:tcp://localhost:1234/" + database);
        testDB(params, DBMode.REMOTE, "Connection refused");
        
        params.put("url", "jdbc:h2:tcp://localhost/" + database);
        params.put("username", "username");
        params.put("password", "password");
        testDB(params, DBMode.LOCAL, "Wrong user name or password");
    }
    
    private void testDB(Map<String, Object> params, DBMode dbMode, String expectedException) throws IllegalAccessException,
                                                                                             InvocationTargetException {
        String error = null;
        H2DataSource h2 = new H2DataSource();
        BeanUtils.populate(h2, params);
        h2.setMode(dbMode.toString());
        try (H2DataSource ds = h2.init();
                Connection connection = ds.getConnection();
                PreparedStatement ps = connection.prepareStatement("SELECT X FROM SYSTEM_RANGE(1, 9);");
                ResultSet rs = ps.executeQuery();) {
            int i = 0;
            
            while (rs.next()) {
                i++;
                assertEquals(i, rs.getInt("X"));
            }
            
            assertEquals(i, 9);
            assertEquals(dbMode, ds.getMode());
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
