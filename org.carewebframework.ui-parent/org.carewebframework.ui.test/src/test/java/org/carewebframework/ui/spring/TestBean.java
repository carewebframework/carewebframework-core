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
