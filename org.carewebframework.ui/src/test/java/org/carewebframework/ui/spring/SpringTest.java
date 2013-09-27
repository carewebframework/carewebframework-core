/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.ui.spring;

import static org.junit.Assert.assertEquals;

import org.carewebframework.ui.test.CommonTest;

import org.zkoss.util.resource.Labels;

import org.junit.Test;

public class SpringTest extends CommonTest {
    
    @Test
    public void test() {
        TestBean bean = desktopContext.getBean(TestBean.class);
        //Test ZK LabelPropertySource and DomainPropertySource
        assertEquals("Label injection failed.", Labels.getLabel("cwf.ui.test.label"), bean.getLabel());
        assertEquals("Property injection failed.", "Default property1", bean.getProperty1());
        assertEquals("Property override failed.", "Overridden property2", bean.getProperty2());
    }
    
}
