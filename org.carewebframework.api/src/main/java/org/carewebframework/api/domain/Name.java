/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.api.domain;

import java.io.Serializable;

/**
 * Person name.
 */
public class Name implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private String firstName = "";
    
    private String lastName = "";
    
    private String middleName = "";
    
    public Name() {
        
    }
    
    public Name(String value) {
        value = value == null ? "" : value.trim();
        String[] pcs = value.split("\\,", 2);
        lastName = pcs[0].trim();
        pcs = pcs.length == 1 ? null : pcs[1].trim().split("\\s", 2);
        firstName = pcs == null ? "" : pcs[0].trim();
        middleName = pcs == null ? "" : pcs.length == 1 ? "" : pcs[1].trim();
    }
    
    public String getFirstName() {
        return firstName;
    }
    
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    
    public String getLastName() {
        return lastName;
    }
    
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    
    public String getMiddleName() {
        return middleName;
    }
    
    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }
    
    public String getFullName() {
        return (lastName + ", " + firstName + " " + middleName).trim();
    }
    
    @Override
    public String toString() {
        return getFullName();
    }
    
}
