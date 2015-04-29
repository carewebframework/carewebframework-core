/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.api;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.carewebframework.api.security.SecurityUtil;

import org.junit.Test;

public class SecurityUtilTest {
    
    private static final Log log = LogFactory.getLog(SecurityUtilTest.class);
    
    private static String[] CONSTRAINTS = { "1,ABCDEFGHIJKLMNOPQURSTUVWXYZ", "1,abcdefghijklmnopqurstuvwxyz",
            "1,!@#$%^&*_-+" };
    
    @Test
    public void testGenerateRandomPassword() {
        for (int i = 0; i < 10; i++) {
            log.info(SecurityUtil.generateRandomPassword(6, 12, CONSTRAINTS));
        }
    }
    
}
