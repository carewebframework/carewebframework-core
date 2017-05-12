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
package org.carewebframework.ui.dialog;

import java.util.Date;

import org.apache.commons.lang.time.DateUtils;
import org.carewebframework.common.DateUtil;
import org.carewebframework.common.StrUtil;
import org.carewebframework.ui.util.DateTimeUtil;
import org.carewebframework.web.ancillary.IAutoWired;
import org.carewebframework.web.ancillary.INamespace;
import org.carewebframework.web.annotation.EventHandler;
import org.carewebframework.web.annotation.OnFailure;
import org.carewebframework.web.annotation.WiredComponent;
import org.carewebframework.web.component.BaseComponent;
import org.carewebframework.web.component.BaseUIComponent;
import org.carewebframework.web.component.Datebox;
import org.carewebframework.web.component.Popup;
import org.carewebframework.web.component.Popupbox;
import org.carewebframework.web.component.Timebox;
import org.carewebframework.web.event.Event;

/**
 * Presents a date/time input element.
 */
public class DateTimebox extends Popupbox implements INamespace, IAutoWired {
    
    @WiredComponent
    private Popup popup;

    @WiredComponent("popup.datebox")
    private Datebox datebox;
    
    @WiredComponent("popup.timebox")
    private Timebox timebox;
    
    private boolean requireTime;
    
    private boolean ok;
    
    private String dateFormat;
    
    private String timeFormat;
    
    private final String requireTimeError = StrUtil.getLabel("cwf.datetime.error.no.time");
    
    private final String requireDateError = StrUtil.getLabel("cwf.datetime.error.no.date");
    
    /**
     * Sets default property values.
     */
    public DateTimebox() {
        super();
        setReadonly(true);
    }
    
    /**
     * Creates and wires all child components.
     */
    @Override
    public void afterInitialized(BaseComponent comp) {
        datebox.setPattern(dateFormat);
        timebox.setPattern(timeFormat);
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
    public String getDateConstraint() {
        return datebox.getPattern();
    }
    
    /**
     * Sets the constraint for the date component.
     *
     * @param constraint The date constraint.
     */
    public void setDateConstraint(String constraint) {
        datebox.setPattern(constraint);
    }
    
    /**
     * Returns the constraint for the time component.
     *
     * @return Time constraint.
     */
    public String getTimeConstraint() {
        return timebox.getPattern();
    }
    
    /**
     * Sets the constraint for the time component.
     *
     * @param constraint The time constraint.
     */
    public void setTimeConstraint(String constraint) {
        timebox.setPattern(constraint);
    }
    
    /**
     * Returns the current date value.
     *
     * @return The current date value.
     */
    public Date getDate() {
        return DateTimeUtil.getTime(datebox, timebox);
    }
    
    /**
     * Sets the current date value.
     *
     * @param date The date value.
     */
    public void setDate(Date date) {
        validateDate(date);
        setValue(DateUtil.formatDate(date));
    }
    
    /**
     * Validates that a time component exists if one is required.
     *
     * @param value The date value.
     */
    public void validateDate(Date value) {
        if (requireTime && !DateUtil.hasTime(value)) {
            throw new IllegalArgumentException(requireTimeError);
        }
    }
    
    /**
     * Displays (or clears) a validation error.
     *
     * @param message The error text. If null, any existing validation errors will be cleared.
     * @param inputElement The input element that caused the validation error.
     */
    private void showError(String message, BaseUIComponent inputElement) {
        ok = message == null;
        datebox.setBalloon(inputElement == datebox ? message : null);
        timebox.setBalloon(inputElement == timebox ? message : null);
        
        if (!ok) {
            inputElement.setFocus(true);
        }
    }
    
    /**
     * Clears all validation errors.
     */
    private void clearError() {
        showError(null, datebox);
        showError(null, timebox);
    }
    
    private boolean validateInput() {
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
     * Transfers input state between the input box and the drop down dialog. If drop down is true,
     * the date value from the input box is copied to the drop down. If false, the reverse is done.
     *
     * @param open The state of the drop down dialog.
     */
    private void update(boolean open) {
        if (open) {
            datebox.setFocus(true);
            datebox.selectAll();
            Date date = getDate();
            updateDatebox(date);
            updateTimebox(DateUtil.hasTime(date) ? date : null);
            clearError();
        } else if (ok) {
            Date date = DateTimeUtil.getTime(datebox, timebox);
            date = timebox.getValue() != null ? DateUtils.setMilliseconds(date, 1) : date;
            setDate(date);
        }
    }
    
    /**
     * Update input elements when drop down dialog opens or closes.
     *
     * @param event The open or close event.
     */
    @EventHandler(value = { "open", "close" }, target = "popup", onFailure = OnFailure.IGNORE)
    private void onOpenOrClose(Event event) {
        update(isOpen());
    }
    
    /**
     * Automatic drop down when input box receives focus.
     */
    @EventHandler("focus")
    public void onFocus() {
        if (!isOpen()) {
            setOpen(true);
            update(true);
        }
    }
    
    /**
     * Clear any validation error upon changing inputs.
     */
    @EventHandler(value = "change", target = { "@datebox", "@timebox" }, onFailure = OnFailure.IGNORE)
    private void onChange() {
        clearError();
    }
    
    /**
     * Close drop down and update input box if validation successful
     */
    @EventHandler(value = "click", target = "popup.btnOK", onFailure = OnFailure.IGNORE)
    private void onClick$btnOK() {
        if (validateInput()) {
            setOpen(false);
        }
    }
    
    /**
     * Close drop down, ignoring all changes.
     */
    @EventHandler(value = "click", target = "popup.btnCancel", onFailure = OnFailure.IGNORE)
    private void onClick$btnCancel() {
        ok = false;
        setOpen(false);
    }
    
    /**
     * Populate datebox with today's date while clearing timebox.
     */
    @EventHandler(value = "click", target = "popup.btnToday", onFailure = OnFailure.IGNORE)
    private void onClick$btnToday() {
        updateDatebox(DateUtil.today());
        updateTimebox(null);
        validateInput();
        close();
    }
    
    /**
     * Populate datebox and timebox with current date and time.
     */
    @EventHandler(value = "click", target = "popup.btnNow", onFailure = OnFailure.IGNORE)
    private void onClick$btnNow() {
        Date now = DateUtil.now();
        updateDatebox(now);
        updateTimebox(now);
        validateInput();
        close();
    }
    
    /**
     * Clear the time box.
     */
    @EventHandler(value = "click", target = "popup.btnTimeClear", onFailure = OnFailure.IGNORE)
    private void onClick$btnTimeClear() {
        updateTimebox(null);
    }
    
    /**
     * Close the input box.
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
    
}
