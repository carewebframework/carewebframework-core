/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.jdbc.property;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.io.FileUtils;

import org.carewebframework.api.security.ISecurityService;
import org.carewebframework.api.security.mock.MockSecurityService;
import org.carewebframework.jdbc.h2.H2DataSource;
import org.carewebframework.jdbc.property.JDBCPropertyService;

import org.junit.Test;

public class TestJDBC {
    
    
    @SuppressWarnings("resource")
    @Test
    public void test() throws Exception {
        String dir = System.getProperty("java.io.tmpdir") + "/cwf";
        FileUtils.deleteDirectory(new File(dir));
        Map<String, String> params = new HashMap<>();
        params.put("database", dir + "/database");
        DataSource datasource = new H2DataSource(params).init();
        ISecurityService securityService = new MockSecurityService();
        JDBCPropertyService service = new JDBCPropertyService(datasource, securityService).init(true);
        test1(service, null);
        test1(service, "instance1");
        test1(service, "instance2");
        test2(service, "instance1");
        test2(service, "instance2");
        test3(service, "prop2", true, 2);
        test3(service, "prop2", false, 2);
        test3(service, "prop1", true, 0);
        test4(service, 8);
        service.destroy();
    }
    
    private void test1(JDBCPropertyService service, String instanceName) {
        service.saveValue("prop1", instanceName, false, "local1");
        service.saveValue("prop1", instanceName, true, "global1");
        service.saveValue("prop1", instanceName, false, null);
        assertEquals("global1", service.getValue("prop1", instanceName));
        service.saveValue("prop1", instanceName, false, "local1");
        assertEquals("local1", service.getValue("prop1", instanceName));
        service.saveValue("prop1", instanceName, true, null);
        assertEquals("local1", service.getValue("prop1", instanceName));
        service.saveValue("prop1", instanceName, false, null);
        assertNull(service.getValue("prop1", instanceName));
        service.saveValue("prop2", instanceName, false, "local2");
        service.saveValue("prop2", instanceName, true, "global2");
    }
    
    private void test2(JDBCPropertyService service, String instanceName) {
        List<String> local = initList("local");
        service.saveValues("multi1", instanceName, false, local);
        List<String> global = initList("global");
        service.saveValues("multi1", instanceName, true, global);
        assertEquals(local, service.getValues("multi1", instanceName));
        service.saveValues("multi1", instanceName, false, null);
        assertEquals(global, service.getValues("multi1", instanceName));
    }
    
    private List<String> initList(String value) {
        List<String> list = new ArrayList<>();
        
        for (int i = 1; i < 10; i++) {
            list.add(value + i);
        }
        
        return list;
    }
    
    private void test3(JDBCPropertyService service, String propertyName, boolean asGlobal, int count) {
        List<String> instances = service.getInstances(propertyName, asGlobal);
        assertEquals(count, instances.size());
    }
    
    private void test4(JDBCPropertyService service, int rowCount) throws SQLException {
        try (PreparedStatement ps = service.getPreparedStatement("SELECT COUNT(*) FROM %s");
                ResultSet rs = ps.executeQuery();) {
            rs.next();
            assertEquals(rowCount, rs.getInt(1));
        }
    }
}
