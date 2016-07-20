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

import java.util.Date;

public class DateRange {
    
    private static final String DELIM = "|";
    
    private static final String REGEX_DELIM = "\\" + DELIM;
    
    private String label = "";
    
    private String rawStartDate;
    
    private String rawEndDate;
    
    private Date startDate;
    
    private Date endDate;
    
    private boolean dflt;
    
    /**
     * Create a date range item from its string representation.
     * 
     * @param data Format is label|start date|end date|default Only label is required.
     */
    public DateRange(String data) {
        String pcs[] = data.split(REGEX_DELIM);
        label = pcs[0];
        setStartDate(fromArray(pcs, 1));
        setEndDate(fromArray(pcs, 2));
        setDefault(fromArray(pcs, 3));
        checkDates();
    }
    
    /**
     * Create a date range from individual components.
     * 
     * @param label Displayable text. If null, a default label is created.
     * @param startDate The start date.
     * @param endDate The end date.
     */
    public DateRange(String label, Date startDate, Date endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.rawStartDate = DateUtil.formatDate(startDate);
        this.rawEndDate = DateUtil.formatDate(endDate);
        checkDates();
        this.label = label != null ? label : (rawStartDate + " to " + rawEndDate);
    }
    
    /**
     * Create a date range from individual components, using the default label.
     * 
     * @param startDate The start date.
     * @param endDate The end date.
     */
    public DateRange(Date startDate, Date endDate) {
        this(null, startDate, endDate);
    }
    
    /**
     * Copy constructor.
     * 
     * @param dateRange Date range to copy.
     */
    public DateRange(DateRange dateRange) {
        this.dflt = dateRange.dflt;
        this.label = dateRange.label;
        this.endDate = DateUtil.cloneDate(dateRange.endDate);
        this.rawEndDate = dateRange.rawEndDate;
        this.startDate = DateUtil.cloneDate(dateRange.startDate);
        this.rawStartDate = dateRange.rawStartDate;
    }
    
    /**
     * Checks for equality based on start and end dates.
     * 
     * @return True if start and end dates are identical.
     */
    @Override
    public boolean equals(Object value) {
        if (!(value instanceof DateRange)) {
            return false;
        }
        
        if (value == this) {
            return true;
        }
        
        DateRange range = (DateRange) value;
        
        if (range.startDate == startDate && range.endDate == endDate) {
            return true;
        }
        
        if (range.startDate == null || range.endDate == null) {
            return false;
        }
        
        return range.startDate.equals(startDate) && range.endDate.equals(endDate);
    }
    
    /**
     * Returns the serialized version of the object.
     * 
     * @return Serialized object.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        addComponent(sb, label);
        addComponent(sb, rawStartDate);
        addComponent(sb, rawEndDate);
        sb.append(dflt ? "1" : "");
        return sb.toString();
    }
    
    /**
     * Adds a component to the serialized output.
     * 
     * @param sb String builder for the serialized output.
     * @param comp The component being added.
     */
    private void addComponent(StringBuilder sb, String comp) {
        sb.append(comp == null ? "" : comp);
        sb.append(DELIM);
    }
    
    /**
     * Swaps start and end dates if not in correct sequence.
     */
    private void checkDates() {
        if (startDate == null || endDate == null) {
            return;
        }
        
        if (startDate.after(endDate)) {
            Date date = startDate;
            String rawDate = rawStartDate;
            startDate = endDate;
            rawStartDate = rawEndDate;
            endDate = date;
            rawEndDate = rawDate;
        }
    }
    
    /**
     * Returns an element from a string array, or an empty string if the element does not exist.
     * 
     * @param array The string array.
     * @param index Index of element sought.
     * @return The requested element, or an empty string if the index is out of range.
     */
    private String fromArray(String[] array, int index) {
        return index >= array.length || index < 0 ? "" : array[index];
    }
    
    /**
     * Sets the start date.
     * 
     * @param startDate The start date.
     */
    private void setStartDate(String startDate) {
        this.rawStartDate = startDate;
        this.startDate = DateUtil.parseDate(startDate);
    }
    
    /**
     * Sets the end date.
     * 
     * @param endDate The end date.
     */
    private void setEndDate(String endDate) {
        this.rawEndDate = endDate;
        this.endDate = DateUtil.parseDate(endDate);
    }
    
    /**
     * Sets the default flag.
     * 
     * @param value Boolean string expression.
     */
    private void setDefault(String value) {
        this.dflt = StrUtil.toBoolean(value);
    }
    
    /**
     * Returns the display label.
     * 
     * @return The display label.
     */
    public String getLabel() {
        return label;
    }
    
    /**
     * Returns the start date.
     * 
     * @return The start date.
     */
    public Date getStartDate() {
        return startDate;
    }
    
    /**
     * Returns the end date.
     * 
     * @return The end date.
     */
    public Date getEndDate() {
        return endDate;
    }
    
    /**
     * Returns the raw start date.
     * 
     * @return The raw start date.
     */
    public String getRawStartDate() {
        return rawStartDate;
    }
    
    /**
     * Returns the raw end date.
     * 
     * @return The raw end date.
     */
    public String getRawEndDate() {
        return rawEndDate;
    }
    
    /**
     * Returns the default flag.
     * 
     * @return The default flag.
     */
    public boolean isDefault() {
        return dflt;
    }
    
    /**
     * Returns true if the reference date is within this range. The start date is included within
     * the interval, the end date is not.
     * 
     * @param refDate A reference date.
     * @return True if the reference date is within this range.
     */
    public boolean inRange(Date refDate) {
        return inRange(refDate, true, false);
    }
    
    /**
     * Returns true if the reference date is within this range.
     * 
     * @param refDate A reference date.
     * @param inclusiveStart If true, the start date is included in the range.
     * @param inclusiveEnd If true, the end date is included in the range.
     * @return True if the reference date is within this range.
     */
    public boolean inRange(Date refDate, boolean inclusiveStart, boolean inclusiveEnd) {
        if (refDate == null) {
            return false;
        }
        
        int cmp = startDate == null ? 1 : refDate.compareTo(startDate);
        
        if (cmp < 0 || (!inclusiveStart && cmp == 0)) {
            return false;
        }
        
        cmp = endDate == null ? -1 : refDate.compareTo(endDate);
        
        if (cmp > 0 || (!inclusiveEnd && cmp == 0)) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Returns true if the reference range is wholly contained within this range. The start date of
     * this range is included within the interval, the end date is not.
     * 
     * @param refRange A reference date range.
     * @return True if the reference date ranged is wholly contained within this range.
     */
    public boolean inRange(DateRange refRange) {
        return inRange(refRange, true, false);
    }
    
    /**
     * Returns true if the reference range is wholly contained within this range.
     * 
     * @param refRange A reference date range.
     * @param inclusiveStart If true, the start date is included in the range.
     * @param inclusiveEnd If true, the end date is included in the range.
     * @return True if the reference date ranged is wholly contained within this range.
     */
    public boolean inRange(DateRange refRange, boolean inclusiveStart, boolean inclusiveEnd) {
        return inRange(refRange.startDate, inclusiveStart, inclusiveEnd)
                && inRange(refRange.endDate, inclusiveStart, inclusiveEnd);
    }
    
}
