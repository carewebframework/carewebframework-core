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
package org.carewebframework.api.query;

import java.util.Date;

import org.fujion.common.DateRange;
import org.fujion.common.DateUtil;

public class DateQueryFilter<T> extends AbstractQueryFilter<T> {
    
    public interface IDateTypeExtractor<T> {
        
        Date getDateByType(T result, DateType dateType);
    }
    
    /**
     * Type of date to be considered in a query.
     */
    public enum DateType {
        MEASURED, // When the entity was measured
        UPDATED, // When the entity was last updated
        CREATED; // When the entity was first created
    }
    
    private DateType dateType = DateType.MEASURED;
    
    private DateRange dateRange;
    
    private final IDateTypeExtractor<T> dateTypeExtractor;
    
    public DateQueryFilter(IDateTypeExtractor<T> dateTypeExtractor) {
        this.dateTypeExtractor = dateTypeExtractor;
    }
    
    /**
     * Filter result based on selected date range.
     */
    @Override
    public boolean include(T result) {
        return getDateRange().inRange(DateUtil.stripTime(dateTypeExtractor.getDateByType(result, dateType)), true, true);
    }
    
    @Override
    public boolean updateContext(IQueryContext context) {
        context.setParam("dateType", dateType);
        DateRange oldDateRange = (DateRange) context.getParam("dateRange");
        
        if (dateRange == null || oldDateRange == null || !oldDateRange.inRange(dateRange)) {
            context.setParam("dateRange", dateRange);
        }
        
        return context.hasChanged();
    }
    
    public DateType getDateType() {
        return dateType;
    }
    
    public void setDateType(DateType dateType) {
        if (this.dateType != dateType) {
            this.dateType = dateType;
            notifyListeners();
        }
    }
    
    public DateRange getDateRange() {
        return dateRange;
    }
    
    public void setDateRange(DateRange dateRange) {
        if (this.dateRange != dateRange) {
            this.dateRange = dateRange == null ? null : new DateRange(dateRange);
            notifyListeners();
        }
    }
    
}
