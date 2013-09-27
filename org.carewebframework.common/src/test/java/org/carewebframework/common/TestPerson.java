/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.common;

import java.util.Calendar;
import java.util.Date;

public class TestPerson {
    
    public static class Name {
        
        private String first;
        
        private String last;
        
        public Name() {
            
        }
        
        public String getFirst() {
            return first;
        }
        
        public void setFirst(String first) {
            this.first = first;
        }
        
        public String getLast() {
            return last;
        }
        
        public void setLast(String last) {
            this.last = last;
        }
        
        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Name)) {
                return false;
            }
            
            Name obj2 = (Name) obj;
            return first.equals(obj2.first) && last.equals(obj2.last);
        }
    }
    
    private Name name;
    
    private Date dob;
    
    public TestPerson() {
        name = new Name();
        name.first = "Test";
        name.last = "Person";
        Calendar cal = Calendar.getInstance();
        cal.set(1957, 7, 27);
        dob = cal.getTime();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof TestPerson)) {
            return false;
        }
        
        TestPerson obj2 = (TestPerson) obj;
        return name.equals(obj2.name) && dob.equals(obj2.dob);
    }
    
    public Name getName() {
        return name;
    }
    
    public void setName(Name name) {
        this.name = name;
    }
    
    public Date getDob() {
        return dob;
    }
    
    public void setDob(Date dob) {
        this.dob = dob;
    }
    
}
