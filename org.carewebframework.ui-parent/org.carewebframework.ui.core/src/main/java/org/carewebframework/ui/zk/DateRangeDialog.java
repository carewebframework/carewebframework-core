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
package org.carewebframework.ui.zk;

import java.util.Date;

import org.carewebframework.common.DateRange;
import org.carewebframework.common.DateUtil;
import org.carewebframework.web.component.BaseComponent;
import org.carewebframework.web.component.Datebox;
import org.carewebframework.web.component.Window;
import org.carewebframework.web.event.EventUtil;
import org.carewebframework.web.page.PageUtil;

/**
 * Presents a date range dialog.
 */
public class DateRangeDialog extends Window {
    
    private static final long serialVersionUID = 1L;
    
    private Date startDefault;
    
    private Date endDefault;
    
    private Datebox startDate;
    
    private Datebox endDate;
    
    private DateRange dateRange;
    
    /**
     * Wire events and variables and set default values for input boxes.
     */
    public void onCreate() {
        startDate.setValue(startDefault);
        endDate.setValue(endDefault);
        EventUtil.post("onShow", this, null);
    }
    
    /**
     * Selects all of the text in the start date box and sets the focus to that box. This is done as
     * an asynch event because the selection doesn't appear to work otherwise.
     */
    public void onShow() {
        startDate.selectAll();
        startDate.setFocus(true);
    }
    
    /**
     * Displays the date range dialog.
     * 
     * @param parent Parent component.
     * @return A date range reflecting the inputs from the dialog. This will be null if the input is
     *         cancelled or if an unexpected error occurs.
     */
    public static DateRange show(BaseComponent parent) {
        Date today = DateUtil.stripTime(new Date());
        return show(parent, today, today);
    }
    
    /**
     * Displays the date range dialog.
     * 
     * @param parent Parent component.
     * @param startDefault Default start date.
     * @param endDefault Default end date.
     * @return A date range reflecting the inputs from the dialog. This will be null if the input is
     *         cancelled or if an unexpected error occurs.
     */
    public static DateRange show(BaseComponent parent, Date startDefault, Date endDefault) {
        DateRangeDialog dlg = null;
        
        try {
            dlg = (DateRangeDialog) PageUtil.createPage(Constants.RESOURCE_PREFIX + "dateRangeDialog.cwf", parent).get(0);
            dlg.startDefault = startDefault;
            dlg.endDefault = endDefault;
            dlg.modal(null);
            return dlg.dateRange;
        } catch (Exception e) {
            if (dlg != null) {
                dlg.detach();
            }
            
            return null;
        }
    }
    
    /**
     * Entering return in the start date box sets focus to the end date box.
     */
    public void onOK$startDate() {
        endDate.setFocus(true);
    }
    
    /**
     * Entering return in the end date box is same as clicking the OK button.
     * 
     * @param event The onOK event.
     */
    public void onOK$endDate() {
        onClick$btnOK();
    }
    
    /**
     * Clicking the cancel button aborts the input.
     */
    public void onClick$btnCancel() {
        detach();
    }
    
    /**
     * Clicking the OK button creates a DateRangeItem object with the responses from the dialog and
     * closes the dialog.
     */
    public void onClick$btnOK() {
        if (startDate.getValue().after(endDate.getValue())) {
            Datebox temp = startDate;
            startDate = endDate;
            endDate = temp;
        }
        
        dateRange = new DateRange(null, startDate.getValue(), endDate.getValue());
        detach();
    }
    
    /**
     * Sets the end date value equal to the start date value.
     */
    public void onClick$btnSameAsStart() {
        endDate.setValue(startDate.getValue());
    }
    
    /**
     * Sets the start date value equal to the end date value.
     */
    public void onClick$btnSameAsEnd() {
        startDate.setValue(endDate.getValue());
    }
}
