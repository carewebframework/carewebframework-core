/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.api.test;

import org.carewebframework.api.AppFramework;
import org.carewebframework.api.context.ContextManager;
import org.carewebframework.api.event.EventManager;
import org.carewebframework.api.spring.FrameworkAppContext;

import org.junit.AfterClass;
import org.junit.BeforeClass;

public class CommonTest {
    
    
    public static FrameworkAppContext appContext;
    
    public static AppFramework appFramework;
    
    public static EventManager eventManager;
    
    public static ContextManager contextManager;
    
    @BeforeClass
    public static void beforeClass$CommonTest() {
        if (appContext == null) {
            System.out.println("Initializing test IOC container...");
            try {
                appContext = new FrameworkAppContext(true);
                appContext.refresh();
            } catch (Exception e) {
                appContext = null;
                System.out.println(e.getMessage());
                throw e;
            }
            appFramework = appContext.getBean("appFramework", AppFramework.class);
            eventManager = appContext.getBean("eventManager", EventManager.class);
            contextManager = appContext.getBean("contextManager", ContextManager.class);
        }
    }
    
    @AfterClass
    public static void afterClass$CommonTest() {
        if (appContext != null) {
            System.out.println("Closing test IOC container...");
            try {
                appContext.close();
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
            appContext = null;
            appFramework = null;
            eventManager = null;
            contextManager = null;
        }
    }
}
