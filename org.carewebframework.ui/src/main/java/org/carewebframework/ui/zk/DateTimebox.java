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

import org.carewebframework.common.DateUtil;

import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.IdSpace;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.event.OpenEvent;
import org.zkoss.zk.ui.metainfo.PageDefinition;
import org.zkoss.zul.Bandbox;
import org.zkoss.zul.Bandpopup;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Label;
import org.zkoss.zul.Timebox;

/**
 * Presents a date/time input element.
 */
public class DateTimebox extends Bandbox implements IdSpace {
    
    private static final long serialVersionUID = 1L;
    
    private Datebox datebox;
    
    private Timebox timebox;
    
    private Label error;
    
    private boolean requireTime;
    
    private Date date;
    
    private boolean hasTime;
    
    private boolean ok;
    
    /**
     * Creates all required child components.
     * 
     * @throws Exception
     */
    public void onCreate() throws Exception {
        date = now();
        hasTime = true;
        setText(DateUtil.formatDate(date));
        Bandpopup bp = new Bandpopup();
        appendChild(bp);
        PageDefinition def = ZKUtil.loadCachedPageDefinition(Constants.RESOURCE_PREFIX + "dateTimebox.zul");
        Executions.createComponents(def, bp, null);
        ZKUtil.wireController(this);
        datebox.setFormat("dd-MMM-yyyy");
        timebox.setFormat("HH:mm");
    }
    
    public void setRequireTime(boolean requireTime) {
        this.requireTime = requireTime;
    }
    
    public boolean getRequireTime() {
        return requireTime;
    }
    
    public void setDate(Date date) {
        this.date = date;
        setRawValue(DateUtil.formatDate(date));
    }
    
    public Date getDate() {
        return date;
    }
    
    public boolean hasTime() {
        return hasTime;
    }
    
    private void showError(String message) {
        error.setValue(message);
        ok = false;
    }
    
    private boolean validate() {
        ok = true;
        
        if (requireTime && timebox.getValue() == null) {
            showError("Time is required.");
        }
        
        return ok;
    }
    
    public void onOpen(OpenEvent event) {
        update(event.isOpen());
    }
    
    private void update(boolean open) {
        if (open) {
            datebox.setConstraint(getConstraint());
            updateDatebox(date);
            updateTimebox(hasTime ? date : null);
            ok = true;
        } else if (ok) {
            date = ZKDateUtil.getTime(datebox, timebox);
            hasTime = timebox.getValue() != null;
            setText(DateUtil.formatDate(date));
        }
    }
    
    public void onClick$btnOK() {
        if (validate()) {
            close();
        }
    }
    
    public void onClick$btnCancel() {
        ok = false;
        close();
    }
    
    @Override
    public void close() {
        super.close();
        update(false);
    }
    
    private Date now() {
        return new Date();
    }
    
    private void updateDatebox(Date value) {
        datebox.setValue(DateUtil.stripTime(value));
    }
    
    private void updateTimebox(Date value) {
        timebox.setValue(value);
    }
    
    public void onClick$btnToday() {
        updateDatebox(now());
        updateTimebox(null);
    }
    
    public void onClick$btnNow() {
        Date now = now();
        updateDatebox(now);
        updateTimebox(now);
    }
    
    public void onClick$btnClear() {
        updateTimebox(null);
    }
    
    @Override
    public void setValue(String value) {
        date = DateUtil.parseDate(value);
        super.setValue(value);
    }
    
    @Override
    protected Object marshall(Object value) {
        return value instanceof Date ? coerceToString(value) : super.marshall(value);
    }
    
    @Override
    protected Object coerceFromString(String value) throws WrongValueException {
        if (value == null) {
            return null;
        }
        
        date = DateUtil.parseDate(value);
        
        if (date == null) {
            throw new WrongValueException(value);
        }
        
        return date;
    }
    
    @Override
    protected String coerceToString(Object value) {
        return DateUtil.formatDate((Date) value, false, !hasTime);
    }
}
