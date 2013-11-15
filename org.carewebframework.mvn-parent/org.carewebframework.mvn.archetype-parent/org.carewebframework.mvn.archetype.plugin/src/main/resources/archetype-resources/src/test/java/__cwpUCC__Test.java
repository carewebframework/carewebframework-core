#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
/**
 * The contents of this file are subject to the Regenstrief Public License
 * Version 1.0 (the "License"); you may not use this file except in compliance with the License.
 * Please contact Regenstrief Institute if you would like to obtain a copy of the license.
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) Regenstrief Institute.  All Rights Reserved.
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
