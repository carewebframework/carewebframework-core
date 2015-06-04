/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.api.query;

import java.util.Date;

import org.carewebframework.common.DateRange;
import org.carewebframework.common.DateUtil;

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
