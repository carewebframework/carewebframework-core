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
package org.carewebframework.ui.spring;

import static org.junit.Assert.assertEquals;

import org.carewebframework.common.StrUtil;
import org.carewebframework.ui.test.CommonTest;
import org.junit.Test;

public class SpringTest extends CommonTest {
    
    @Test
    public void test() {
        TestBean bean = pageContext.getBean(TestBean.class);
        //Test ZK LabelPropertySource and DomainPropertySource
        assertEquals("Label injection failed.", StrUtil.getLabel("cwf.ui.test.label"), bean.getLabel());
        assertEquals("Property injection failed.", "Default property1", bean.getProperty1());
        assertEquals("Property override failed.", "Overridden property2", bean.getProperty2());
        assertEquals("Domain property injection failed.", "Default property3", bean.getProperty3());
    }
    
}
