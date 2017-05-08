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
package org.carewebframework.ui.test;

import org.carewebframework.web.test.MockConfig;
import org.carewebframework.web.test.MockTest;
import org.junit.BeforeClass;

public class MockUITest extends MockTest {
    
    private static final String[] ROOT_CONFIG_LOCATIONS = { "classpath:/META-INF/cwf-dispatcher-servlet.xml",
            "classpath*:**/META-INF/*-spring.xml" };

    private static final String[] ROOT_PROFILES = { "root", "root-test" };

    private static final String[] CHILD_CONFIG_LOCATIONS = { "classpath*:**/META-INF/*-spring.xml" };

    private static final String[] CHILD_PROFILES = { "child", "child-test" };

    @BeforeClass
    public static void beforeClass() throws Exception {
        MockTest.rootConfig = new MockConfig(ROOT_CONFIG_LOCATIONS, ROOT_PROFILES);
        MockTest.childConfig = new MockConfig(CHILD_CONFIG_LOCATIONS, CHILD_PROFILES);
        MockTest.mockEnvironmentClass = MockUIEnvironment.class;
        MockTest.beforeClass();
    }
    
}
