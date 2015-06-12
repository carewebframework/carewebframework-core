/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
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
import org.carewebframework.ui.zk.ZKUtil;

import org.springframework.core.ErrorCoded;
import org.springframework.core.NestedCheckedException;
import org.springframework.core.NestedRuntimeException;
import org.springframework.web.util.WebUtils;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Button;
import org.zkoss.zul.Label;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

/**
 * Controller to handle exceptions caught by the framework. This controller currently assumes the
 * listed ZK/ZUL members are properly wired. This class logs to our our logging appender with the
 * name defined by constant {@link Constants#EXCEPTION_LOG}.
 */
public class ExceptionController extends GenericForwardComposer<Component> {
    
    private static final long serialVersionUID = 1L;
    
    private static final Log log = LogFactory.getLog(ExceptionController.class);//TODO Constants.EXCEPTION_LOG);
    
    private Window root;
    
    //autowired members
    private Label lblExceptionClass;
    
    private Label lblMessage;
    
    private Label lblStatusCode;
    
    private Label lblCode;
    
    private Textbox txtStackTrace;
    
    private Component detail;
    
    private Button btnDetail;
    
    /**
     * Populate the display with information from the current execution.
     * 
     * @see org.zkoss.zk.ui.util.GenericAutowireComposer#doAfterCompose(org.zkoss.zk.ui.Component)
     */
    @Override
    public void doAfterCompose(final Component comp) throws Exception {
        super.doAfterCompose(comp);
        Clients.clearBusy();
        this.root = ZKUtil.findAncestor(comp, Window.class);
        final HttpServletRequest req = (HttpServletRequest) this.execution.getNativeRequest();
        
        Class<?> errClass = (Class<?>) req.getAttribute(WebUtils.ERROR_EXCEPTION_TYPE_ATTRIBUTE);
        String errMsg = (String) req.getAttribute(WebUtils.ERROR_MESSAGE_ATTRIBUTE);
        Throwable err = (Throwable) req.getAttribute(WebUtils.ERROR_EXCEPTION_ATTRIBUTE);
        
        final String errReqURI = (String) req.getAttribute(WebUtils.ERROR_REQUEST_URI_ATTRIBUTE);
        final String errServletName = (String) req.getAttribute(WebUtils.ERROR_SERVLET_NAME_ATTRIBUTE);
        final Integer errStatusCode = (Integer) req.getAttribute(WebUtils.ERROR_STATUS_CODE_ATTRIBUTE);
        
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
        
        final StringBuffer buffer = new StringBuffer();
        //generic exception info
        buffer.append("\nException class: ").append(errClass);
        buffer.append("\nMessage: ").append(errMsg);
        if (err instanceof ErrorCoded) {
            final String errorCode = ((ErrorCoded) err).getErrorCode();
            buffer.append("\nErrorCode: ").append(errorCode);
            this.lblCode.setValue(errorCode);
        }
        buffer.append("\nStatusCode: ").append(errStatusCode);
        buffer.append("\nServletName: ").append(errServletName);
        buffer.append("\nReqURI: ").append(errReqURI);
        
        final SessionInfo sessionInfo = Application.getInstance().getSessionInfo(this.desktop);
        buffer.append(sessionInfo);
        
        buffer.append("\nThrowableContext: " + throwableContext);
        buffer.append("\nStackTrace: ");
        appendStackTrace(err);
        
        log.error(buffer, err);
        this.lblExceptionClass.setValue(String.valueOf(errClass));
        this.lblMessage.setValue(errMsg);
        this.lblStatusCode.setValue(String.valueOf(errStatusCode));
        
        if (SecurityUtil.isGrantedAny(StrUtil.getLabel("cwf.error.dialog.expanded"))) {
            Events.echoEvent(Events.ON_CLICK, this.btnDetail, null);
        }
    }
    
    /**
     * Appends the stack trace for the specified exception to the display.
     * 
     * @param err Exception whose stack trace will be appended.
     */
    private void appendStackTrace(final Throwable err) {
        if (err != null) {
            final Class<?> clazz = err.getClass();
            final String msg = err.getMessage();
            //final Throwable cause = err.getCause();//should be null
            
            this.txtStackTrace.setValue(StringUtils.defaultString(this.txtStackTrace.getValue())
                    + StringUtils.trimToEmpty(clazz.getCanonicalName()) + ": " + StringUtils.trimToEmpty(msg) + "\n");
            
            for (final StackTraceElement element : err.getStackTrace()) {
                this.txtStackTrace.setValue(StringUtils.defaultString(this.txtStackTrace.getValue())
                        + String.valueOf(element) + "\n");
            }
        }
    }
    
    /**
     * Sets the detail open state.
     * 
     * @param doOpen The detail open state.
     */
    private void setDetail(final boolean doOpen) {
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
