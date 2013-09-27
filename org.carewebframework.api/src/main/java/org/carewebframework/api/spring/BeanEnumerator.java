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

import java.util.Arrays;
import java.util.Iterator;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Allows enumerating of all managed beans given a class/interface type.
 */
public class BeanEnumerator implements ApplicationContextAware, Iterable<String> {
    
    private ApplicationContext applicationContext;
    
    private final Class<?> clazz;
    
    /**
     * Create an instance of the BeanEnumerator for the specified class.
     * 
     * @param clazz All beans of this class will be enumerated.
     */
    public BeanEnumerator(Class<?> clazz) {
        this.clazz = clazz;
    }
    
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
    
    @Override
    public Iterator<String> iterator() {
        return Arrays.asList(applicationContext.getBeanNamesForType(clazz, false, false)).iterator();
    }
}
