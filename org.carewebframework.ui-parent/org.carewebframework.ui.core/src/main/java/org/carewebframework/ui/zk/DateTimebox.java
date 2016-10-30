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
import java.util.TimeZone;

import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.lang.time.FastDateFormat;
import org.carewebframework.common.DateUtil;
import org.carewebframework.common.StrUtil;
import org.carewebframework.web.ancillary.INamespace;
import org.carewebframework.web.component.Datebox;
import org.carewebframework.web.component.Popupbox;
import org.carewebframework.web.page.PageDefinition;

/**
 * Presents a date/time input element.
 */
public class DateTimebox extends Popupbox implements INamespace {
    
    private static final long serialVersionUID = 1L;
    
    private Datebox datebox;
    
    private Timebox timebox;
    
    private boolean requireTime;
    
    private boolean ok;
    
    private Constraint dateConstraint;
    
    private Constraint timeConstraint;
    
    private String dateFormat = "dd-MMM-yyyy";
    
    private String timeFormat = "HH:mm";
    
    private Component[] inputElements;
    
    private final TimeZone timezone = DateUtil.getLocalTimeZone();
    
    private final String requireTimeError = StrUtil.getLabel("cwf.datetime.error.no.time");
    
    private final String requireDateError = StrUtil.getLabel("cwf.datetime.error.no.date");
    
    /**
     * Sets default property values.
     */
    public DateTimebox() {
        super();
        setReadonly(true);
        setSclass("cwf-datetimebox");
    }
    
    /**
     * Returns true if a time component is required.
     * 
     * @return True if a time component is required.
     */
    public boolean getRequireTime() {
        return requireTime;
    }
    
    /**
     * Set to true to indicate a time component is required.
     * 
     * @param requireTime True if time component is required.
     */
    public void setRequireTime(boolean requireTime) {
        this.requireTime = requireTime;
    }
    
    /**
     * Returns the constraint for the date component.
     * 
     * @return Date constraint.
     */
    public Constraint getDateConstraint() {
        return dateConstraint;
    }
    
    /**
     * Sets the constraint for the date component.
     * 
     * @param constraint The date constraint.
     */
    public void setDateConstraint(Constraint constraint) {
        dateConstraint = constraint;
        
        if (datebox != null) {
            datebox.setConstraint(constraint);
        }
    }
    
    /**
     * Sets the constraint for the date component.
     * 
     * @param constraint The date constraint.
     */
    public void setDateConstraint(String constraint) {
        setDateConstraint(new SimpleDateConstraint(constraint));
    }
    
    /**
     * Returns the constraint for the time component.
     * 
     * @return Time constraint.
     */
    public Constraint getTimeConstraint() {
        return timeConstraint;
    }
    
    /**
     * Sets the constraint for the time component.
     * 
     * @param constraint The time constraint.
     */
    public void setTimeConstraint(Constraint constraint) {
        timeConstraint = constraint;
        
        if (timebox != null) {
            timebox.setConstraint(constraint);
        }
    }
    
    /**
     * Sets the constraint for the time component.
     * 
     * @param constraint The time constraint.
     */
    public void setTimeConstraint(String constraint) {
        setTimeConstraint(new SimpleDateConstraint(constraint));
    }
    
    /**
     * Returns the display format for date values.
     * 
     * @return The date format.
     */
    public String getDateFormat() {
        return dateFormat;
    }
    
    /**
     * Sets the display format for date values.
     * 
     * @param format The date format.
     */
    public void setDateFormat(String format) {
        dateFormat = format;
        
        if (datebox != null) {
            datebox.setFormat(format);
        }
    }
    
    /**
     * Returns the display format for time values.
     * 
     * @return The time format.
     */
    public String getTimeFormat() {
        return timeFormat;
    }
    
    /**
     * Sets the display format for time values.
     * 
     * @param format The time format.
     */
    public void setTimeFormat(String format) {
        timeFormat = format;
        
        if (timebox != null) {
            timebox.setFormat(format);
        }
    }
    
    /**
     * Returns the current date value.
     * 
     * @return The current date value.
     */
    public Date getDate() {
        return asDate(getRawValue());
    }
    
    /**
     * Sets the current date value.
     * 
     * @param date The date value.
     */
    public void setDate(Date date) {
        validate(date);
        setRawValue(date);
    }
    
    /**
     * Validates that a time component exists if one is required.
     */
    @Override
    public void validate(Object value) {
        super.validate(value);
        
        if (requireTime && !DateUtil.hasTime((Date) value)) {
            throw showCustomError(new WrongValueException(this, requireTimeError));
        }
    }
    
    /**
     * Displays (or clears) a validation error.
     * 
     * @param message The error text. If null, any existing validation errors will be cleared.
     * @param inputElement The input element that caused the validation error.
     */
    private void showError(String message, InputElement inputElement) {
        Clients.clearWrongValue(inputElements);
        ok = message == null;
        
        if (!ok) {
            Clients.wrongValue(inputElement, message);
            inputElement.setFocus(true);
        }
    }
    
    /**
     * Clears all validation errors.
     */
    private void clearError() {
        showError(null, null);
    }
    
    @Override
    private boolean validate() {
        ok = true;
        boolean hasTime = timebox.getValue() != null;
        
        if (datebox.getValue() == null && hasTime) {
            showError(requireDateError, datebox);
        } else if (requireTime && !hasTime) {
            showError(requireTimeError, timebox);
        }
        
        return ok;
    }
    
    /**
     * Transfers input state between the bandbox and the drop down dialog. If drop down is true, the
     * date value from the bandbox is copied to the drop down. If false, the reverse is done.
     * 
     * @param open The state of the drop down dialog.
     */
    private void update(boolean open) {
        if (open) {
            datebox.setFocus(true);
            datebox.select();
            Date date = getDate();
            updateDatebox(date);
            updateTimebox(DateUtil.hasTime(date) ? date : null);
            clearError();
        } else if (ok) {
            Date date = ZKDateUtil.getTime(datebox, timebox);
            date = timebox.getValue() != null ? DateUtils.setMilliseconds(date, 1) : date;
            setDate(date);
        }
    }
    
    /**
     * Creates and wires all child components.
     */
    public void onCreate() {
        Bandpopup bp = new Bandpopup();
        appendChild(bp);
        PageDefinition def = ZKUtil.loadCachedPageDefinition(Constants.RESOURCE_PREFIX + "dateTimebox.cwf");
        Executions.createComponents(def, bp, null);
        ZKUtil.wireController(this);
        datebox.setTimeZone(timezone);
        datebox.setFormat(getDateFormat());
        datebox.setConstraint(dateConstraint);
        timebox.setFormat(timeFormat);
        timebox.setTimeZone(timezone);
        timebox.setConstraint(timeConstraint);
        inputElements = new Component[] { datebox, timebox };
    }
    
    /**
     * Update input elements when drop down dialog opens or closes.
     * 
     * @param event The open event.
     */
    public void onOpen(OpenEvent event) {
        update(event.isOpen());
    }
    
    /**
     * Automatic drop down when bandbox receives focus.
     */
    public void onFocus() {
        if (!isOpen()) {
            setOpen(true);
            update(true);
        }
    }
    
    /**
     * Clear any validation error upon changing date entry.
     */
    public void onChanging$datebox() {
        clearError();
    }
    
    /**
     * Clear any validation error upon changing time entry.
     */
    public void onChanging$timebox() {
        clearError();
    }
    
    /**
     * Close drop down and update bandbox if validation successful
     */
    public void onClick$btnOK() {
        if (validate()) {
            setOpen(false);
        }
    }
    
    /**
     * Close drop down, ignoring all changes.
     */
    public void onClick$btnCancel() {
        ok = false;
        setOpen(false);
    }
    
    /**
     * Populate datebox with today's date while clearing timebox.
     */
    public void onClick$btnToday() {
        updateDatebox(DateUtil.today());
        updateTimebox(null);
    }
    
    /**
     * Populate datebox and timebox with current date and time.
     */
    public void onClick$btnNow() {
        Date now = DateUtil.now();
        updateDatebox(now);
        updateTimebox(now);
    }
    
    /**
     * Clear the time box.
     */
    public void onClick$btnTimeClear() {
        updateTimebox(null);
    }
    
    /**
     * Close the bandbox.
     */
    @Override
    public void close() {
        super.close();
        update(false);
    }
    
    /**
     * Update the datebox with the new value.
     * 
     * @param date New date value.
     */
    private void updateDatebox(Date date) {
        datebox.setValue(DateUtil.stripTime(date));
    }
    
    /**
     * Update the timebox with the new time.
     * 
     * @param time The new time value.
     */
    private void updateTimebox(Date time) {
        timebox.setValue(time);
    }
    
    @Override
    protected Object marshall(Object value) {
        return value instanceof Date ? coerceToString(value) : super.marshall(value);
    }
    
    @Override
    protected Object unmarshall(Object value) {
        return value instanceof String ? asDate(value) : super.unmarshall(value);
    }
    
    @Override
    protected Date coerceFromString(String value) throws WrongValueException {
        if (value == null || value.isEmpty()) {
            return null;
        }
        
        Date date = DateUtil.parseDate(value);
        
        if (date == null) {
            throw showCustomError(
                new WrongValueException(this, MZul.DATE_REQUIRED, new Object[] { value, datebox.getFormat() }));
        }
        
        return date;
    }
    
    @Override
    protected String coerceToString(Object value) {
        return value == null ? null : value instanceof Date ? formatDate((Date) value) : value.toString();
    }
    
    /**
     * Formats the date using the date and time format settings.
     * 
     * @param date Date to format.
     * @return Formatted date.
     */
    private String formatDate(Date date) {
        String format = dateFormat + (DateUtil.hasTime(date) ? " " + timeFormat : "");
        return date == null ? null : FastDateFormat.getInstance(format).format(date);
    }
    
    /**
     * Coerce the value to a Date.
     * 
     * @param value The object value.
     * @return The coerced date value.
     */
    private Date asDate(Object value) {
        return value == null ? null : value instanceof Date ? (Date) value : DateUtil.parseDate(value.toString());
    }
}
