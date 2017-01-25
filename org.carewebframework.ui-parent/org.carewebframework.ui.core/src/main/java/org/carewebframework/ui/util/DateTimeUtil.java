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
package org.carewebframework.ui.util;

import java.util.Calendar;
import java.util.Date;

import org.carewebframework.common.DateUtil;
import org.carewebframework.web.component.Datebox;
import org.carewebframework.web.component.Timebox;

/**
 * Utility functions related to datebox and timebox components.
 */
public class DateTimeUtil {
    
    /**
     * Returns a date/time from the UI. This is combined from two UI input elements, one for date
     * and one for time.
     * 
     * @param datebox The date box.
     * @param timebox The time box.
     * @return The combined date/time.
     */
    public static Date getTime(Datebox datebox, Timebox timebox) {
        if (timebox.getValue() == null) {
            return DateUtil.stripTime(datebox.getValue());
        }
        
        Calendar date = Calendar.getInstance();
        Calendar time = Calendar.getInstance();
        date.setTime(datebox.getValue());
        time.setTime(timebox.getValue());
        time.set(date.get(Calendar.YEAR), date.get(Calendar.MONTH), date.get(Calendar.DAY_OF_MONTH));
        return time.getTime();
    }
    
    /**
     * Sets the UI to reflect the specified time.
     * 
     * @param datebox The date box.
     * @param timebox The time box.
     * @param value Time value to set.
     */
    public static void setTime(Datebox datebox, Timebox timebox, Date value) {
        value = value == null ? new Date() : value;
        datebox.setValue(DateUtil.stripTime(value));
        timebox.setValue(value);
    }
    
    /**
     * Enforce static class.
     */
    private DateTimeUtil() {
    }
}
