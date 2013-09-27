/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.shell.designer;

import org.carewebframework.api.spring.SpringUtil;
import org.carewebframework.shell.layout.UIElementBase;
import org.carewebframework.shell.property.PropertyInfo;

/**
 * Editor for enumerable types (enums and iterables). The editor expects one of two named config
 * parameters: class or bean. A class may be an enum or an iterable. A bean is the id of a bean that
 * implements an iterable.
 */
public class PropertyEditorEnum extends PropertyEditorList {
    
    /**
     * Initialize the list, based on the configuration data which can specify an enumeration class,
     * an iterable class, or the id of an iterable bean.
     */
    @Override
    protected void init(UIElementBase target, PropertyInfo propInfo, PropertyGrid propGrid) {
        super.init(target, propInfo, propGrid);
        
        try {
            String className = propInfo.getConfigValue("class");
            Iterable<?> iter = null;
            
            if (className != null) {
                Class<?> clazz = Class.forName(className);
                
                if (clazz.isEnum()) {
                    for (Object value : clazz.getEnumConstants()) {
                        Enum<?> enm = (Enum<?>) value;
                        appendItem(enm.toString(), enm.name());
                    }
                    
                    return;
                }
                
                if (Iterable.class.isAssignableFrom(clazz)) {
                    iter = (Iterable<?>) clazz.newInstance();
                } else {
                    throw new RuntimeException("Not an enumerable type: " + className);
                }
            }
            
            if (iter == null) {
                String beanId = propInfo.getConfigValue("bean");
                
                if (beanId == null) {
                    throw new RuntimeException("Missing config parameter.");
                }
                
                iter = SpringUtil.getAppContext().getBean(beanId, Iterable.class);
            }
            
            for (Object value : iter) {
                appendItem(value.toString());
            }
            
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Error processing enumerable type.", e);
        }
    }
}
