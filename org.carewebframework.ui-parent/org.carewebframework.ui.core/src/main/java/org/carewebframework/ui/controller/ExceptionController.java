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
package org.carewebframework.ui.controller;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.carewebframework.api.IThrowableContext;
import org.carewebframework.api.security.SecurityUtil;
import org.carewebframework.common.StrUtil;
import org.carewebframework.web.ancillary.IAutoWired;
import org.carewebframework.web.annotation.EventHandler;
import org.carewebframework.web.annotation.WiredComponent;
import org.carewebframework.web.client.ExecutionContext;
import org.carewebframework.web.component.BaseComponent;
import org.carewebframework.web.component.Detail;
import org.carewebframework.web.component.Label;
import org.carewebframework.web.component.Memobox;
import org.carewebframework.web.component.Window;
import org.carewebframework.web.core.RequestUtil;
import org.springframework.core.NestedCheckedException;
import org.springframework.core.NestedRuntimeException;
import org.springframework.web.util.WebUtils;

/**
 * Controller to handle exceptions caught by the framework.
 */
public class ExceptionController implements IAutoWired {

    private static final Log log = LogFactory.getLog(ExceptionController.class);

    private Window root;

    @WiredComponent
    private Label lblExceptionClass;

    @WiredComponent
    private Label lblMessage;

    @WiredComponent
    private Label lblStatusCode;

    @WiredComponent
    private Memobox txtStackTrace;

    @WiredComponent
    private Detail detail;

    /**
     * Populate the display with information from the current execution.
     */
    @Override
    public void afterInitialized(BaseComponent comp) {
        //ClientUtil.busy(null, null);
        root = comp.getAncestor(Window.class);
        HttpServletRequest req = RequestUtil.getRequest();
        
        if (root == null || req == null) {
            return;
        }
        
        Class<?> errClass = (Class<?>) req.getAttribute(WebUtils.ERROR_EXCEPTION_TYPE_ATTRIBUTE);
        String errMsg = (String) req.getAttribute(WebUtils.ERROR_MESSAGE_ATTRIBUTE);
        Throwable err = (Throwable) req.getAttribute(WebUtils.ERROR_EXCEPTION_ATTRIBUTE);

        String errReqURI = (String) req.getAttribute(WebUtils.ERROR_REQUEST_URI_ATTRIBUTE);
        String errServletName = (String) req.getAttribute(WebUtils.ERROR_SERVLET_NAME_ATTRIBUTE);
        Integer errStatusCode = (Integer) req.getAttribute(WebUtils.ERROR_STATUS_CODE_ATTRIBUTE);

        String throwableContext = null;

        if (err != null) {
            if (err instanceof IThrowableContext) {
                throwableContext = ((IThrowableContext) err).getThrowableContext();
            }

            //stack trace info
            if (err instanceof NestedCheckedException) {
                err = ((NestedCheckedException) err).getMostSpecificCause();
            } else if (err instanceof NestedRuntimeException) {
                err = ((NestedRuntimeException) err).getMostSpecificCause();
            }
        }
        if (err != null) {
            errClass = errClass == null ? err.getClass() : errClass;
            errMsg = StringUtils.trimToNull(errMsg) == null ? err.getMessage() : errMsg;
        }

        StringBuffer buffer = new StringBuffer();
        //generic exception info
        buffer.append("\nException class: ").append(errClass);
        buffer.append("\nMessage: ").append(errMsg);
        buffer.append("\nStatusCode: ").append(errStatusCode);
        buffer.append("\nServletName: ").append(errServletName);
        buffer.append("\nReqURI: ").append(errReqURI);

        Map<String, Object> browserInfo = ExecutionContext.getPage().getBrowserInfo();
        buffer.append(browserInfo);

        buffer.append("\nThrowableContext: " + throwableContext);
        buffer.append("\nStackTrace: ");
        appendStackTrace(err);

        log.error(buffer, err);
        this.lblExceptionClass.setLabel(String.valueOf(errClass));
        this.lblMessage.setLabel(errMsg);
        this.lblStatusCode.setLabel(String.valueOf(errStatusCode));

        if (SecurityUtil.isGrantedAny(StrUtil.getLabel("cwf.error.dialog.expanded"))) {
            setDetail(true);
        }
    }

    /**
     * Appends the stack trace for the specified exception to the display.
     *
     * @param err Exception whose stack trace will be appended.
     */
    private void appendStackTrace(Throwable err) {
        if (err != null) {
            Class<?> clazz = err.getClass();
            String msg = err.getMessage();
            //Throwable cause = err.getCause();//should be null

            this.txtStackTrace.setValue(StringUtils.defaultString(this.txtStackTrace.getValue())
                    + StringUtils.trimToEmpty(clazz.getCanonicalName()) + ": " + StringUtils.trimToEmpty(msg) + "\n");

            for (StackTraceElement element : err.getStackTrace()) {
                this.txtStackTrace
                        .setValue(StringUtils.defaultString(this.txtStackTrace.getValue()) + String.valueOf(element) + "\n");
            }
        }
    }

    /**
     * Sets the detail open state.
     *
     * @param doOpen The detail open state.
     */
    private void setDetail(boolean doOpen) {
        detail.setOpen(doOpen);
        detail.setLabel(
            StrUtil.getLabel(doOpen ? "cwf.error.dialog.detail.open.label" : "cwf.error.dialog.detail.closed.label"));
    }

    /**
     * Event handler for close button
     */
    public void onClick$btnClose() {
        root.detach();
    }

    /**
     * Event handler for detail open/close
     */
    @EventHandler(value = { "open", "close" }, target = "@detail")
    public void onOpenOrClose$detail() {
        setDetail(detail.isOpen());
    }
}
