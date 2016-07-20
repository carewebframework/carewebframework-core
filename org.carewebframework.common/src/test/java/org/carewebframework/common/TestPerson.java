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
    
    private long id;
    
    public TestPerson() {
        id = 1234567890;
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
        return name.equals(obj2.name) && dob.equals(obj2.dob) && id == obj2.id;
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
    
    public long getId() {
        return id;
    }
    
    public void setId(long id) {
        this.id = id;
    }
    
}
