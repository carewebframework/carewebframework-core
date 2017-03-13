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

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.servlet.ServletContext;

import org.apache.commons.lang.ArrayUtils;
import org.carewebframework.api.spring.Constants;
import org.carewebframework.api.spring.DomainPropertySource;
import org.carewebframework.api.spring.LabelPropertySource;
import org.carewebframework.web.client.ExecutionContext;
import org.carewebframework.web.component.Page;
import org.carewebframework.web.spring.ClasspathMessageSource;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.web.context.support.XmlWebApplicationContext;

public class AppContextInitializer implements ApplicationContextInitializer<XmlWebApplicationContext> {

    public static final String[] DEFAULT_LOCATIONS = { "classpath*:/META-INF/*-spring.xml" };

    private final Page page;

    private final boolean testConfig;

    public AppContextInitializer() {
        this(null, false);
    }

    public AppContextInitializer(Page page) {
        this(page, false);
    }

    public AppContextInitializer(Page page, boolean testConfig) {
        this.page = page;
        this.testConfig = testConfig;
    }

    @Override
    public void initialize(XmlWebApplicationContext ctx) {
        ctx.setAllowBeanDefinitionOverriding(true);
        ConfigurableEnvironment env = ctx.getEnvironment();
        Set<String> aps = new LinkedHashSet<>();
        Collections.addAll(aps, env.getActiveProfiles());

        if (page != null) {
            page.setAttribute(AppContextFinder.APP_CONTEXT_ATTRIB, ctx);
            ServletContext sc = ExecutionContext.getSession().getServletContext();
            ctx.setDisplayName("Child XmlWebApplicationContext " + page);
            ctx.setParent(AppContextFinder.rootContext);
            ctx.setServletContext(sc);
            // Set up profiles (remove root profiles merged from parent)
            aps.removeAll(Arrays.asList(Constants.PROFILES_ROOT));
            Collections.addAll(aps, testConfig ? Constants.PROFILES_CHILD_TEST : Constants.PROFILES_CHILD_PROD);
            env.setDefaultProfiles(Constants.PROFILE_CHILD_DEFAULT);
            ctx.setConfigLocations(DEFAULT_LOCATIONS);
        } else {
            AppContextFinder.rootContext = ctx;
            Collections.addAll(aps, testConfig ? Constants.PROFILES_ROOT_TEST : Constants.PROFILES_ROOT_PROD);
            env.getPropertySources().addFirst(new LabelPropertySource());
            env.getPropertySources().addLast(new DomainPropertySource(ctx));
            env.setDefaultProfiles(Constants.PROFILE_ROOT_DEFAULT);
            ctx.setConfigLocations((String[]) ArrayUtils.addAll(Constants.DEFAULT_LOCATIONS, ctx.getConfigLocations()));
            ClasspathMessageSource.getInstance().setResourceLoader(ctx);
        }

        env.setActiveProfiles(aps.toArray(new String[aps.size()]));
    }

}
