#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package ${package};

import static org.junit.Assert.assertEquals;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.carewebframework.ui.test.CommonTest;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for ${cwpName}
 */
public class ${cwpUCC}Test extends CommonTest {
    
    private static final Log log = LogFactory.getLog(${cwpUCC}Test.class);
    
    private static final String HELLO_WORLD = "Hello World";
    
    private String testBean;
    
    /**
     * Unit Test initialization
     */
    @Before
    public final void init() {
        log.info("Initializing Test Class");
        testBean = (String) desktopContext.getBean("testBean");
    }
    
    /**
     * Performs unit test A
     */
    @Test
    public void performUnitTestA() {
        log.info("Performing Unit Test A");
        assertEquals(HELLO_WORLD, testBean);
    }
    
}
