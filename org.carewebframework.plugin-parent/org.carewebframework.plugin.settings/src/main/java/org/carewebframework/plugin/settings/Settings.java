/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.plugin.settings;

import org.carewebframework.shell.layout.UIElementBase;
import org.carewebframework.shell.property.IPropertyAccessor;
import org.carewebframework.shell.property.PropertyInfo;

/**
 * This is a simple wrapper class that allows the property editor to access settings through the
 * settings provider.
 */
public class Settings extends UIElementBase implements IPropertyAccessor {
    
    private final ISettingsProvider provider;
    
    public Settings(String group, ISettingsProvider provider) {
        super();
        this.provider = provider;
        setDefinition(provider.fetch(group));
    }
    
    @Override
    public Object getPropertyValue(PropertyInfo propInfo) throws Exception {
        return provider.getPropertyValue(propInfo);
    }
    
    @Override
    public void setPropertyValue(PropertyInfo propInfo, Object value) throws Exception {
        provider.setPropertyValue(propInfo, value);
    }
    
}
