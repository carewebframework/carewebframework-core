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
package org.carewebframework.api.spring;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.AbstractRefreshableConfigApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

public class AppContextInitializer implements ApplicationContextInitializer<AbstractRefreshableConfigApplicationContext> {

    private final boolean testConfig;

    public AppContextInitializer() {
        this(false);
    }

    public AppContextInitializer(boolean testConfig) {
        this.testConfig = testConfig;
    }

    @Override
    public void initialize(AbstractRefreshableConfigApplicationContext ctx) {
        ctx.setAllowBeanDefinitionOverriding(false);
        ConfigurableEnvironment env = ctx.getEnvironment();
        Set<String> aps = new LinkedHashSet<>();
        Collections.addAll(aps, env.getActiveProfiles());
        Collections.addAll(aps, testConfig ? Constants.PROFILES_TEST : Constants.PROFILES_PROD);
        env.getPropertySources().addLast(new LabelPropertySource());
        env.getPropertySources().addLast(new DomainPropertySource(ctx));
        env.setDefaultProfiles(Constants.PROFILE_ROOT_DEFAULT);
        ctx.setConfigLocations(Constants.DEFAULT_LOCATIONS);
        env.setActiveProfiles(aps.toArray(new String[aps.size()]));
    }

}
