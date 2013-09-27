/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.ui.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;

import org.carewebframework.common.StrUtil;

import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;

import org.junit.AfterClass;
import org.junit.BeforeClass;

/**
 * Base class for tests using mock environment.
 */
public class CommonTest {
    
    public static Class<? extends MockEnvironment> mockEnvironmentClass = MockEnvironment.class;
    
    public static String[] configLocations;
    
    public static MockEnvironment mockEnvironment;
    
    public static ApplicationContext rootContext;
    
    public static ApplicationContext desktopContext;
    
    @BeforeClass
    public static void beforeClass$CommonTest() throws Exception {
        if (mockEnvironment == null) {
            System.out.println("Initializing mock environment...");
            mockEnvironment = mockEnvironmentClass.newInstance();
            mockEnvironment.init(configLocations);
            rootContext = mockEnvironment.getRootContext();
            desktopContext = mockEnvironment.getDesktopContext();
        }
    }
    
    @AfterClass
    public static void afterClass$CommonTest() {
        if (mockEnvironment != null) {
            System.out.println("Destroying mock environment...");
            mockEnvironment.close();
            mockEnvironment = null;
            rootContext = null;
            desktopContext = null;
        }
    }
    
    /**
     * Reads text from the specified resource on the classpath.
     * 
     * @param resourceName Name of the resource.
     * @return Text read from the resource.
     * @throws IOException
     */
    public static String getTextFromResource(String resourceName) throws IOException {
        Resource resource = desktopContext.getResource("classpath:" + resourceName);
        InputStream is = resource.getInputStream();
        Writer writer = new StringWriter();
        char[] buffer = new char[1024];
        
        try {
            Reader reader = new BufferedReader(new InputStreamReader(is, StrUtil.CHARSET));
            int n;
            while ((n = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, n);
            }
        } finally {
            is.close();
        }
        return writer.toString();
    }
}
