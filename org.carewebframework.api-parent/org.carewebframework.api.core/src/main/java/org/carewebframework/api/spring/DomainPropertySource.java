/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.api.spring;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.carewebframework.api.property.IPropertyService;

import org.springframework.context.ApplicationContext;
import org.springframework.core.env.PropertySource;

/**
 * Allows domain properties (via the IPropertyService API) to be referenced in Spring configuration
 * files.
 */
public class DomainPropertySource extends PropertySource<Object> {
    
    private static final Log log = LogFactory.getLog(DomainPropertySource.class);
    
    private static final String PREFIX = "domain.";
    
    private static final int PREFIX_LEN = PREFIX.length();
    
    private IPropertyService propertyService;
    
    private final ApplicationContext appContext;
    
    public DomainPropertySource(ApplicationContext appContext) {
        super("Domain Properties");
        this.appContext = appContext;
    }
    
    /**
     * Returns a property value from the underlying data store.
     * 
     * @param name Property name prefixed with "domain.".
     */
    @Override
    public String getProperty(String name) {
        try {
            if (name.startsWith(PREFIX) && initPropertyService()) {
                return propertyService.getValue(name.substring(PREFIX_LEN), null);
            }
        } catch (Exception e) {
            log.error("Exception getting property.", e);
        }
        
        return null;
    }
    
    /**
     * Initializes the property service.
     * 
     * @return True if initialized.
     */
    private boolean initPropertyService() {
        if (propertyService == null && appContext.containsBean("propertyService")) {
            propertyService = appContext.getBean("propertyService", IPropertyService.class);
        }
        
        return propertyService != null;
    }
}
