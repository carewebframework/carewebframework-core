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
package org.carewebframework.api.security;

import org.carewebframework.common.AbstractRegistry;

import org.springframework.util.StringUtils;

/**
 * Tracks all security domains.
 */
public class SecurityDomainRegistry extends AbstractRegistry<String, ISecurityDomain> {
    
    private static SecurityDomainRegistry instance = new SecurityDomainRegistry();
    
    public static SecurityDomainRegistry getInstance() {
        return instance;
    }
    
    public static ISecurityDomain getSecurityDomain(String name) {
        return StringUtils.isEmpty(name) ? null : instance.get(name);
    }
    
    public static void registerSecurityDomain(ISecurityDomain securityDomain) {
        instance.register(securityDomain);
    }
    
    private SecurityDomainRegistry() {
        super();
    }
    
    @Override
    protected String getKey(ISecurityDomain item) {
        return item.getLogicalId();
    }
    
}
