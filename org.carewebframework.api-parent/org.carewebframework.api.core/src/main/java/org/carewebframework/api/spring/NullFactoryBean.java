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

import org.springframework.beans.factory.FactoryBean;

/**
 * Allows the instantiation of a bean reference with a value of null. This is useful where the
 * implementation of a bean may be optional.
 */
public class NullFactoryBean implements FactoryBean<Void> {
    
    @Override
    public Void getObject() throws Exception {
        return null;
    }
    
    @Override
    public Class<? extends Void> getObjectType() {
        return null;
    }
    
    @Override
    public boolean isSingleton() {
        return true;
    }
}
