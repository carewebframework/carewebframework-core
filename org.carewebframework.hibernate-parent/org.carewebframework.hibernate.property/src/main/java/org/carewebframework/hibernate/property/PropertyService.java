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
package org.carewebframework.hibernate.property;

import java.util.List;

import org.carewebframework.api.domain.IUser;
import org.carewebframework.api.property.IPropertyService;
import org.carewebframework.api.security.ISecurityService;
import org.carewebframework.api.security.SecurityUtil;
import org.carewebframework.common.StrUtil;

/**
 * H2-based implementation of a property service.
 */
public class PropertyService implements IPropertyService {

    private final PropertyDAO propertyDAO;
    
    public PropertyService(PropertyDAO propertyDAO) {
        this.propertyDAO = propertyDAO;
    }
    
    public PropertyService init() {
        return this;
    }
    
    public void destroy() {
    }
    
    private IUser getUser(boolean asGlobal) {
        ISecurityService securityService = SecurityUtil.getSecurityService();
        return asGlobal || securityService == null ? null : securityService.getAuthenticatedUser();
    }
    
    @Override
    public boolean isAvailable() {
        return true;
    }
    
    @Override
    public String getValue(String propertyName, String instanceName) {
        Property property = propertyDAO.get(propertyName, instanceName, getUser(false));
        property = property != null ? property : propertyDAO.get(propertyName, instanceName, null);
        return property == null ? null : property.getValue();
    }
    
    @Override
    public List<String> getValues(String propertyName, String instanceName) {
        List<String> results = null;
        String result = getValue(propertyName, instanceName);
        
        if (result != null && !result.isEmpty()) {
            results = StrUtil.toList(result);
        }
        
        return results;
    }
    
    @Override
    public void saveValue(String propertyName, String instanceName, boolean asGlobal, String value) {
        Property property = new Property(propertyName, value, instanceName, getUser(asGlobal));
        
        if (value == null) {
            propertyDAO.delete(property);
        } else {
            propertyDAO.saveOrUpdate(property);
        }
    }
    
    @Override
    public void saveValues(String propertyName, String instanceName, boolean asGlobal, List<String> values) {
        saveValue(propertyName, instanceName, asGlobal, values == null ? null : StrUtil.fromList(values));
    }
    
    @Override
    public List<String> getInstances(String propertyName, boolean asGlobal) {
        return propertyDAO.getInstances(propertyName, getUser(asGlobal));
    }
    
}
