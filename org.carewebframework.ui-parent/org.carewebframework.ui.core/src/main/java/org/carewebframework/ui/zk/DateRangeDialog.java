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

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Window;

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
        ZKUtil.wireController(this);
        startDate.setValue(startDefault);
        endDate.setValue(endDefault);
        Events.echoEvent("onShow", this, null);
    }
    
    /**
     * Selects all of the text in the start date box and sets the focus to that box. This is done as
     * an asynch event because the selection doesn't appear to work otherwise.
     */
    public void onShow() {
        startDate.select();
        startDate.setFocus(true);
    }
    
    /**
     * Displays the date range dialog.
     * 
     * @param parent Parent component.
     * @return A date range reflecting the inputs from the dialog. This will be null if the input is
     *         cancelled or if an unexpected error occurs.
     */
    public static DateRange show(Component parent) {
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
    public static DateRange show(Component parent, Date startDefault, Date endDefault) {
        DateRangeDialog dlg = null;
        
        try {
            dlg = (DateRangeDialog) ZKUtil.loadZulPage(Constants.RESOURCE_PREFIX + "dateRangeDialog.zul", null);
            dlg.startDefault = startDefault;
            dlg.endDefault = endDefault;
            dlg.setPage(parent.getPage());
            dlg.doModal();
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
     * 
     * @param event The onOK event.
     */
    public void onOK$startDate(Event event) {
        endDate.setFocus(true);
    }
    
    /**
     * Entering return in the end date box is same as clicking the OK button.
     * 
     * @param event The onOK event.
     */
    public void onOK$endDate(Event event) {
        onClick$btnOK(event);
    }
    
    /**
     * Clicking the cancel button aborts the input.
     * 
     * @param event the onClick event.
     */
    public void onClick$btnCancel(Event event) {
        detach();
    }
    
    /**
     * Clicking the OK button creates a DateRangeItem object with the responses from the dialog and
     * closes the dialog.
     * 
     * @param event The onClick event.
     */
    public void onClick$btnOK(Event event) {
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
     * 
     * @param event The onClick event.
     */
    public void onClick$btnSameAsStart(Event event) {
        endDate.setValue(startDate.getValue());
    }
    
    /**
     * Sets the start date value equal to the end date value.
     * 
     * @param event The onClick event.
     */
    public void onClick$btnSameAsEnd(Event event) {
        startDate.setValue(endDate.getValue());
    }
}
