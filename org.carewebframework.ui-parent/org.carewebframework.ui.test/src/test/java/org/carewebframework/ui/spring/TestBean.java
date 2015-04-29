/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.ui.spring;

/**
 * Use to test label and property resolution within IOC.
 */
public class TestBean {
    
    // This should be injected by a label placeholder.
    private String label;
    
    // This should be injected by a local property placeholder.
    private String property1;
    
    // This should be injected by an overridden local property placeholder.
    private String property2;
    
    // This should be injected by a domain property placeholder.
    private String property3;
    
    public String getLabel() {
        return label;
    }
    
    public void setLabel(String label) {
        this.label = label;
    }
    
    public String getProperty1() {
        return property1;
    }
    
    public void setProperty1(String property) {
        this.property1 = property;
    }
    
    public String getProperty2() {
        return property2;
    }
    
    public void setProperty2(String property2) {
        this.property2 = property2;
    }
    
    public String getProperty3() {
        return property3;
    }
    
    public void setProperty3(String property3) {
        this.property3 = property3;
    }
    
}
