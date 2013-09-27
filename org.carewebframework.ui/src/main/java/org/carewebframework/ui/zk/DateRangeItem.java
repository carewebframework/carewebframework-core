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

import java.util.Date;

import org.carewebframework.common.DateRange;

/**
 * @deprecated Use org.carewebframework.common.DateRange
 */
@Deprecated
public final class DateRangeItem extends org.carewebframework.common.DateRange {
    
    public DateRangeItem(String data) {
        super(data);
    }
    
    public DateRangeItem(Date startDate, Date endDate) {
        super(startDate, endDate);
    }
    
    public DateRangeItem(String label, Date startDate, Date endDate) {
        super(label, startDate, endDate);
    }
    
    public DateRangeItem(DateRange range) {
        this(range.getLabel(), range.getStartDate(), range.getEndDate());
    }
}
