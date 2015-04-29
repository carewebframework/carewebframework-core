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

/**
 * Mock servlet context for unit testing.
 */
public class MockServletContext extends org.springframework.mock.web.MockServletContext {
    
    public MockServletContext() {
        super();
    }
    
    @Override
    protected String getResourceLocation(String path) {
        if (path.startsWith("/~./")) {
            path = "/web" + path.substring(3);
        }
        
        return super.getResourceLocation(path);
    }
}
