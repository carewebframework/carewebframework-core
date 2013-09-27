/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.ui.zk;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.carewebframework.common.DateUtil;

import org.zkoss.zul.Datebox;
import org.zkoss.zul.Timebox;

/**
 * Utility functions related to datebox and timebox components.
 */
public class ZKDateUtil {
    
    /**
     * Returns a date/time from the UI. This is combined from two UI input elements, one for date
     * and one for time.
     * 
     * @param datebox
     * @param timebox
     * @return The combined date/time.
     */
    public static Date getTime(Datebox datebox, Timebox timebox) {
        if (timebox.getValue() == null) {
            return DateUtil.stripTime(datebox.getValue());
        }
        
        TimeZone timezone = DateUtil.getLocalTimeZone();
        Calendar date = Calendar.getInstance(timezone);
        Calendar time = Calendar.getInstance(timezone);
        date.setTime(datebox.getValue());
        time.setTime(timebox.getValue());
        time.set(date.get(Calendar.YEAR), date.get(Calendar.MONTH), date.get(Calendar.DAY_OF_MONTH));
        return DateUtil.changeTimeZone(time.getTime(), timezone, TimeZone.getDefault());
    }
    
    /**
     * Sets the UI to reflect the specified time.
     * 
     * @param datebox
     * @param timebox
     * @param value Time value to set.
     */
    public static void setTime(Datebox datebox, Timebox timebox, Date value) {
        TimeZone timezone = DateUtil.getLocalTimeZone();
        value = value == null ? new Date() : DateUtil.changeTimeZone(value, TimeZone.getDefault(), timezone);
        datebox.setValue(DateUtil.stripTime(value));
        timebox.setValue(value);
    }
    
    /**
     * Enforce static class.
     */
    private ZKDateUtil() {
    }
}
