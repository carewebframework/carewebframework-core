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
package org.carewebframework.ui.session;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.carewebframework.api.domain.IUser;
import org.carewebframework.api.event.IGenericEvent;
import org.carewebframework.api.security.ISecurityService;
import org.carewebframework.common.DateUtil;
import org.carewebframework.common.DateUtil.TimeUnit;
import org.carewebframework.common.StrUtil;
import org.carewebframework.ui.controller.FrameworkController;
import org.carewebframework.ui.dialog.DialogUtil;
import org.carewebframework.web.ancillary.ConvertUtil;
import org.carewebframework.web.annotation.EventHandler;
import org.carewebframework.web.annotation.WiredComponent;
import org.carewebframework.web.client.ClientInvocation;
import org.carewebframework.web.client.ClientRequest;
import org.carewebframework.web.component.BaseComponent;
import org.carewebframework.web.component.BaseUIComponent;
import org.carewebframework.web.component.Label;
import org.carewebframework.web.component.MessagePane;
import org.carewebframework.web.component.MessageWindow;
import org.carewebframework.web.component.Page;
import org.carewebframework.web.component.Textbox;
import org.carewebframework.web.component.Timer;
import org.carewebframework.web.event.EventUtil;
import org.carewebframework.web.event.TimerEvent;
import org.carewebframework.web.websocket.ISessionListener;
import org.carewebframework.web.websocket.Session;

/**
 * Session inactivity timeout controller. Used to notify user regarding impending inactivity timeout
 * and take appropriate action.
 */
public class SessionMonitor extends FrameworkController {
    
    private static final Log log = LogFactory.getLog(SessionMonitor.class);
    
    /**
     * Reflects the different execution states for the session monitor.
     */
    private enum State {
        INITIAL, COUNTDOWN, TIMEDOUT, DEAD
    }
    
    /**
     * Available timeout modes.
     */
    private enum Mode {
        BASELINE, LOCK, LOGOUT;
        
        /**
         * Returns a label from a label reference.
         *
         * @param label Label reference with placeholder for mode.
         * @param params Optional parameters.
         * @return Fully formatted label value.
         */
        public String getLabel(String label, Object... params) {
            return StrUtil.getLabel(format(label), params);
        }
        
        /**
         * Formats a string containing a placeholder ('%') for mode.
         *
         * @param value Value to format.
         * @return Formatted value with placeholder(s) replaced by mode.
         */
        public String format(String value) {
            return value.replace("%", name().toLowerCase());
        }
    }
    
    public static final String ATTR_NO_AUTO_LOCK = "@no_auto_lock";
    
    private static final String ATTR_LOGGING_OUT = "@logging_out";
    
    private static final String TIMEOUT_WARNING = "cwf.sessionmonitor.%.warning.message";
    
    private static final String TIMEOUT_EXPIRATION = "cwf.sessionmonitor.%.reason.message";
    
    private static final String SCLASS_COUNTDOWN = "cwf-sessionmonitor-%-countdown";
    
    private static final String SCLASS_IDLE = "cwf-sessionmonitor-%-idle";
    
    private static final String[] ignoredEvents = { "timer", "statechange" };
    
    private final Map<Mode, Long> countdownDuration = new HashMap<>();
    
    private final Map<Mode, Long> inactivityDuration = new HashMap<>();
    
    private boolean noAutoLock;
    
    private boolean shutdown;

    private Mode mode;
    
    private BaseUIComponent timeoutWindow;
    
    @WiredComponent
    private BaseUIComponent timeoutPanel;
    
    @WiredComponent
    private Label lblDuration;
    
    @WiredComponent
    private Label lblLocked;
    
    @WiredComponent
    private Label lblInfo;
    
    @WiredComponent
    private Textbox txtPassword;
    
    @WiredComponent
    private Timer timer;
    
    private Page page;

    private Session session;

    private long timerInterval;
    
    private long lastKeepAlive;
    
    private long countdown;
    
    private State state;
    
    private ISecurityService securityService;
    
    private final ISessionListener sessionListener = new ISessionListener() {

        @Override
        public void onClientRequest(ClientRequest request) {
            String eventType = shutdown ? null : EventUtil.getEventType(request);
            
            if (eventType != null && !ArrayUtils.contains(ignoredEvents, eventType)) {
                resetActivity();
            }
        }

        @Override
        public void onDestroy() {
            // NOP
        }

        @Override
        public void onClientInvocation(ClientInvocation invocation) {
            // NOP
        }

    };
    
    private final IGenericEvent<Object> applicationControlListener = (eventName, eventData) -> {
        SessionControl applicationControl = SessionControl.fromEvent(eventName);
        
        if (applicationControl != null) {
            switch (applicationControl) {
                case SHUTDOWN_ABORT:
                    abortShutdown(ConvertUtil.convert(eventData, String.class));
                    break;
                
                case SHUTDOWN_START:
                    startShutdown(ConvertUtil.convert(eventData, Long.class));
                    break;
                
                case SHUTDOWN_PROGRESS:
                    updateShutdown(NumberUtils.toLong(StrUtil.piece((String) eventData, StrUtil.U)) * 1000);
                    break;

                case LOCK:
                    lockPage(eventData == null || ConvertUtil.convert(eventData, Boolean.class));
                    break;
            }
        }
    };
    
    public SessionMonitor() {
        inactivityDuration.put(Mode.BASELINE, 900000L);
        inactivityDuration.put(Mode.LOCK, 900000L);
        inactivityDuration.put(Mode.LOGOUT, 0L);
        countdownDuration.put(Mode.BASELINE, 60000L);
        countdownDuration.put(Mode.LOCK, 60000L);
        countdownDuration.put(Mode.LOGOUT, 60000L);
    }

    @Override
    public void afterInitialized(BaseComponent root) {
        super.afterInitialized(root);
        page = root.getPage();
        session = page.getSession();
        noAutoLock = page.getAttribute(ATTR_NO_AUTO_LOCK, false);
        timeoutWindow = (BaseUIComponent) root;
        getEventManager().subscribe(SessionControl.EVENT_ROOT, applicationControlListener);
        IUser user = securityService.getAuthenticatedUser();
        lblLocked.setLabel(user == null ? null
                : Mode.BASELINE.getLabel(TIMEOUT_EXPIRATION, user.getFullName() + "@" + user.getSecurityDomain().getName()));
        setMode(Mode.BASELINE);
        session.addSessionListener(sessionListener);
    }
    
    private void updateClass() {
        String sclass = state == State.COUNTDOWN ? SCLASS_COUNTDOWN : SCLASS_IDLE;
        String clazz = "mode:" + mode.format(sclass);
        
        if (shutdown) {
            clazz += " cwf-sessionmonitor-shutdown";
        }
        
        timeoutWindow.addClass(clazz);
        timeoutWindow.setVisible(mode != Mode.BASELINE || state != State.INITIAL);
    }
    
    private void setMode(Mode newMode) {
        resetActivity();
        
        if (newMode != mode) {
            mode = newMode;
            updateState(true);
            txtPassword.setFocus(mode == Mode.LOCK);
        }
    }

    /**
     * Aborts any shutdown in progress.
     *
     * @param message Optional message to send to user.
     */
    public void abortShutdown(String message) {
        if (shutdown) {
            updateShutdown(0);
            message = StringUtils.isEmpty(message) ? StrUtil.getLabel("cwf.sessionmonitor.shutdown.abort.message") : message;
            MessageWindow mw = page.getChild(MessageWindow.class);
            
            if (mw != null) {
                MessagePane mp = new MessagePane();
                mp.setTitle(StrUtil.getLabel("cwf.sessionmonitor.shutdown.abort.title"));
                mp.addClass("flavor:alert-success");
                mp.addChild(new Label(message));
                mw.addChild(mp);
            } else {
                DialogUtil.showInfo(message);
            }
        }
    }
    
    /**
     * Starts the shutdown sequence based on the specified delay.
     *
     * @param delay Delay in ms. If zero or negative, uses the default delay of 5 minutes.
     */
    public void startShutdown(long delay) {
        updateShutdown(delay > 0 ? delay : 5 * 60000);
    }
    
    private void logout() {
        timeoutWindow.setVisible(false);
        securityService.logout(true, null, mode.getLabel(TIMEOUT_EXPIRATION));
    }
    
    /**
     * Updates the shutdown state.
     *
     * @param delay If positive integer, enables shutdown and begins countdown at specified # of ms.
     *            If zero or negative, aborts any shutdown in progress.
     */
    private void updateShutdown(long delay) {
        countdown = delay;
        shutdown = delay > 0;
        updateState(true);
    }
    
    private void updateCountdown() {
        if (state == State.COUNTDOWN) {
            String s = nextMode().getLabel(TIMEOUT_WARNING, DateUtil.formatDuration(countdown, TimeUnit.SECONDS));
            lblDuration.setLabel(s);
            timeoutPanel.addClass("alert:" + (countdown <= 10000 ? "alert-danger" : "alert-warning"));
        }
    }
    
    /**
     * Updates the current state, if necessary.
     *
     * @param force If true, treat as a state change regardless of previous state.
     */
    private void updateState(boolean force) {
        long now = System.currentTimeMillis();
        long interval = now - lastKeepAlive;
        State oldState = state;
        long delta = shutdown ? -1 : inactivityDuration.get(mode) - interval;
        state = delta > 0 ? State.INITIAL : countdown <= 0 ? State.TIMEDOUT : State.COUNTDOWN;
        boolean stateChanged = force || oldState != state;

        switch (state) {
            case INITIAL:
                if (stateChanged) {
                    timerInterval = inactivityDuration.get(mode);
                    countdown = countdownDuration.get(mode);
                    timer.setRepeat(0);
                } else {
                    timerInterval = delta;
                }
                
                break;
            
            case COUNTDOWN:
                if (stateChanged) {
                    timerInterval = 1000;
                    timer.setRepeat((int) countdown / 1000);
                } else {
                    countdown -= 1000;
                }
                
                if (countdown > 0) {
                    updateCountdown();
                    break;
                }
                
                // fall through is intentional here.
                
            case TIMEDOUT:
                timer.setRepeat(0);
                setMode(nextMode());
                
                if (mode == Mode.LOGOUT) {
                    requestLogout();
                    return;
                }
                
                break;
            
        }

        timer.setInterval(timerInterval);
        timer.start();
        
        if (stateChanged) {
            updateClass();
        }
    }
    
    /**
     * Returns the next mode in the timeout sequence.
     *
     * @return Next mode.
     */
    private Mode nextMode() {
        return mode == Mode.BASELINE && !noAutoLock ? Mode.LOCK : Mode.LOGOUT;
    }
    
    /**
     * Queues a logout request.
     */
    private void requestLogout() {
        boolean loggingOut = page.hasAttribute(ATTR_LOGGING_OUT);
        
        if (!loggingOut) {
            page.setAttribute(ATTR_LOGGING_OUT, true);
            logout();
        } else {
            log.debug("Logout already underway");
        }
    }
    
    /**
     * Reset the keep-alive timer.
     */
    private void resetActivity() {
        lastKeepAlive = System.currentTimeMillis();
    }
    
    @EventHandler(value = "timer", target = "@timer")
    private void onTimer(TimerEvent event) {
        updateState(false);
    }

    /**
     * Resets the mode when the "keep open" button is clicked.
     */
    @EventHandler(value = "click", target = "btnKeepOpen")
    private void onClick$btnKeepOpen() {
        setMode(Mode.BASELINE);
    }
    
    @EventHandler(value = "click", target = "btnLogout")
    private void onClick$btnLogout() {
        securityService.logout(true, null, StrUtil.getLabel("cwf.sessionmonitor.logout.reason.message"));
    }
    
    @EventHandler(value = "click", target = "btnUnlock")
    private void onClick$btnUnlock() {
        String s = txtPassword.getValue();
        txtPassword.setValue(null);
        lblInfo.setLabel(null);
        txtPassword.focus();
        
        if (!StringUtils.isEmpty(s)) {
            if (securityService.validatePassword(s)) {
                setMode(Mode.BASELINE);
            } else {
                lblInfo.setLabel(StrUtil.getLabel("cwf.sessionmonitor.lock.badpassword.message"));
            }
        }
    }
    
    /**
     * Called by IOC container during bean destruction.
     */
    @Override
    protected void cleanup() {
        session.removeSessionListener(sessionListener);
        getEventManager().unsubscribe(SessionControl.EVENT_ROOT, applicationControlListener);
        super.cleanup();
    }
    
    public void lockPage(boolean lock) {
        setMode(lock ? Mode.LOCK : Mode.BASELINE);
    }
    
    public ISecurityService getSecurityService() {
        return securityService;
    }
    
    public void setSecurityService(ISecurityService securityService) {
        this.securityService = securityService;
    }
    
    public long getBaselineInactivityDuration() {
        return inactivityDuration.get(Mode.BASELINE);
    }
    
    public void setBaselineInactivityDuration(long duration) {
        inactivityDuration.put(Mode.BASELINE, duration);
    }
    
    public long getLockInactivityDuration() {
        return inactivityDuration.get(Mode.LOCK);
    }
    
    public void setLockInactivityDuration(long duration) {
        inactivityDuration.put(Mode.LOCK, duration);
    }
    
    public long getBaselineCountdownDuration() {
        return countdownDuration.get(Mode.BASELINE);
    }
    
    public void setBaselineCountdownDuration(long duration) {
        countdownDuration.put(Mode.BASELINE, duration);
    }
    
    public long getLockCountdownDuration() {
        return countdownDuration.get(Mode.LOCK);
    }
    
    public void setLockCountdownDuration(long duration) {
        countdownDuration.put(Mode.LOCK, duration);
    }
    
}
