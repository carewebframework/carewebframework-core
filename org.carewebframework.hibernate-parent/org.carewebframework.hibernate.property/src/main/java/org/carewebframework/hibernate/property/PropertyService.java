/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.hibernate.property;

import java.util.List;

import org.carewebframework.api.domain.IUser;
import org.carewebframework.api.property.IPropertyService;
import org.carewebframework.api.security.ISecurityService;
import org.carewebframework.common.StrUtil;

/**
 * H2-based implementation of a property service.
 */
public class PropertyService implements IPropertyService {
    
    
    private final PropertyDAO propertyDAO;
    
    private final ISecurityService securityService;
    
    public PropertyService(PropertyDAO propertyDAO, ISecurityService securityService) {
        this.propertyDAO = propertyDAO;
        this.securityService = securityService;
    }
    
    public PropertyService init() {
        return this;
    }
    
    public void destroy() {
    }
    
    private IUser getUser(boolean asGlobal) {
        return asGlobal ? null : securityService.getAuthenticatedUser();
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
