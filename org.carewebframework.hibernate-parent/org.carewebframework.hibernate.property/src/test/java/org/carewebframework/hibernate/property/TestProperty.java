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
package org.carewebframework.hibernate.property;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.List;

import org.carewebframework.api.test.CommonTest;

import org.junit.Test;

public class TestProperty extends CommonTest {
    
    
    @Test
    public void testService() throws Exception {
        String dir = System.getProperty("java.io.tmpdir") + "cwf";
        System.out.println("Test database is at: " + dir);
        PropertyService service = appContext.getBean(PropertyService.class);
        test1(service, null);
        test1(service, "instance1");
        test1(service, "instance2");
        test2(service, "instance1");
        test2(service, "instance2");
        test3(service, "prop2", true, 2);
        test3(service, "prop2", false, 2);
        test3(service, "prop1", true, 0);
        service.destroy();
    }
    
    private void test1(PropertyService service, String instanceName) {
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
    
    private void test2(PropertyService service, String instanceName) {
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
    
    private void test3(PropertyService service, String propertyName, boolean asGlobal, int count) {
        List<String> instances = service.getInstances(propertyName, asGlobal);
        assertEquals(count, instances.size());
    }
    
}
