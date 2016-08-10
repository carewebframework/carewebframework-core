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
package org.carewebframework.ui;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.carewebframework.api.IThrowableContext;
import org.carewebframework.api.security.SecurityUtil;
import org.carewebframework.common.StrUtil;
import org.carewebframework.ui.Application.SessionInfo;
import org.carewebframework.web.ancillary.IAutoWired;
import org.carewebframework.web.annotation.WiredComponent;
import org.carewebframework.web.component.BaseComponent;
import org.carewebframework.web.component.BaseUIComponent;
import org.carewebframework.web.component.Button;
import org.carewebframework.web.component.Label;
import org.carewebframework.web.component.Textbox;
import org.carewebframework.web.component.Window;
import org.springframework.core.ErrorCoded;
import org.springframework.core.NestedCheckedException;
import org.springframework.core.NestedRuntimeException;
import org.springframework.web.util.WebUtils;

/**
 * Controller to handle exceptions caught by the framework. This controller currently assumes the
 * listed ZK/ZUL members are properly wired. This class logs to our our logging appender with the
 * name defined by constant {@link Constants#EXCEPTION_LOG}.
 */
public class ExceptionController implements IAutoWired {
    
    private static final Log log = LogFactory.getLog(ExceptionController.class);//TODO Constants.EXCEPTION_LOG);
    
    private Window root;
    
    @WiredComponent
    private Label lblExceptionClass;
    
    @WiredComponent
    private Label lblMessage;
    
    @WiredComponent
    private Label lblStatusCode;
    
    @WiredComponent
    private Label lblCode;
    
    @WiredComponent
    private Textbox txtStackTrace;
    
    @WiredComponent
    private BaseUIComponent detail;
    
    @WiredComponent
    private Button btnDetail;
    
    /**
     * Populate the display with information from the current execution.
     */
    @Override
    public void afterInitialized(BaseComponent comp) {
        Clients.clearBusy();
        this.root = comp.getAncestor(Window.class);
        HttpServletRequest req = (HttpServletRequest) this.execution.getNativeRequest();
        
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
        if (err instanceof ErrorCoded) {
            String errorCode = ((ErrorCoded) err).getErrorCode();
            buffer.append("\nErrorCode: ").append(errorCode);
            this.lblCode.setLabel(errorCode);
        }
        buffer.append("\nStatusCode: ").append(errStatusCode);
        buffer.append("\nServletName: ").append(errServletName);
        buffer.append("\nReqURI: ").append(errReqURI);
        
        SessionInfo sessionInfo = Application.getInstance().getSessionInfo(this.desktop);
        buffer.append(sessionInfo);
        
        buffer.append("\nThrowableContext: " + throwableContext);
        buffer.append("\nStackTrace: ");
        appendStackTrace(err);
        
        log.error(buffer, err);
        this.lblExceptionClass.setLabel(String.valueOf(errClass));
        this.lblMessage.setLabel(errMsg);
        this.lblStatusCode.setLabel(String.valueOf(errStatusCode));
        
        if (SecurityUtil.isGrantedAny(StrUtil.getLabel("cwf.error.dialog.expanded"))) {
            Events.echoEvent(Events.ON_CLICK, this.btnDetail, null);
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
        this.detail.setVisible(doOpen);
        this.btnDetail.setLabel(doOpen ? "Hide Detail" : "Show Detail");
    }
    
    /**
     * Event handler for close button
     */
    public void onClick$btnClose() {
        this.root.detach();
    }
    
    /**
     * Event handler for "show detail" button
     */
    public void onClick$btnDetail() {
        setDetail(!this.detail.isVisible());
    }
}
