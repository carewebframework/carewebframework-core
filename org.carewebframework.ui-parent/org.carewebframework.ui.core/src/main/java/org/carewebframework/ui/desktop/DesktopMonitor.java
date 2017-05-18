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
package org.carewebframework.ui.desktop;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.beanutils.ConvertUtils;
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
import org.carewebframework.ui.FrameworkController;
import org.carewebframework.ui.zk.MessageWindow;
import org.carewebframework.ui.zk.PromptDialog;
import org.carewebframework.ui.zk.ZKUtil;
import org.zkoss.zk.au.AuRequest;
import org.zkoss.zk.au.AuService;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Desktop;
import org.zkoss.zk.ui.HtmlBasedComponent;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.sys.SessionCtrl;
import org.zkoss.zul.Label;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Timer;

/**
 * Session inactivity timeout controller. Used to notify user regarding impending inactivity timeout
 * and take appropriate action.
 */
public class DesktopMonitor extends FrameworkController {
    
    private static final long serialVersionUID = 1L;

    private static final Log log = LogFactory.getLog(DesktopMonitor.class);
    
    /**
     * Reflects the different execution states for the session monitor.
     */
    private enum State {
        INITIAL, COUNTDOWN, TIMEDOUT
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
    
    private static final String TIMEOUT_WARNING = "cwf.desktopmonitor.%.warning.message";
    
    private static final String TIMEOUT_EXPIRATION = "cwf.desktopmonitor.%.reason.message";
    
    private static final String SCLASS_COUNTDOWN = "cwf-desktopmonitor-%-countdown";
    
    private static final String SCLASS_IDLE = "cwf-desktopmonitor-%-idle";
    
    /**
     * Activity that will not reset keepalive timer.
     */
    private static final String[] ignore = new String[] { "dummy", Events.ON_TIMER, Events.ON_CLIENT_INFO };
    
    private final Map<Mode, Long> countdownDuration = new HashMap<>();
    
    private final Map<Mode, Long> inactivityDuration = new HashMap<>();
    
    private boolean noAutoLock;
    
    private boolean shutdown;

    private Mode mode;
    
    // Start of auto-wired section
    
    private HtmlBasedComponent timeoutPanel;
    
    private Label lblDuration;
    
    private Label lblLocked;
    
    private Label lblInfo;
    
    private Textbox txtPassword;
    
    private Timer timer;
    
    // End of auto-wired section
    
    private HtmlBasedComponent timeoutWindow;
    
    private Desktop desktop;

    private SessionCtrl session;

    private long timerInterval;
    
    private long lastKeepAlive;
    
    private long countdown;
    
    private State state;
    
    private ISecurityService securityService;
    
    /**
     * Monitors Ajax traffic to determine client activity.
     */
    private final AuService desktopActivityMonitor = new AuService() {

        /**
         * Tracks desktop activity.
         *
         * @param request The asynchronous update request
         * @param everError Whether error occurred prior to processing request
         * @return whether the process has completed (always returns false)
         */
        @Override
        public boolean service(AuRequest request, boolean everError) {
            if (isKeepAliveRequest(request)) {
                resetActivity();
            }
            
            return false;
        }

        /**
         * Determines if request is a 'keep alive' request.
         *
         * @param request The inbound request.
         * @return keepAlive True if request should reset inactivity timeout.
         */
        private boolean isKeepAliveRequest(AuRequest request) {
            return !shutdown && !ArrayUtils.contains(ignore, request.getCommand());
        }
    };

    private final IGenericEvent<Object> applicationControlListener = (eventName, eventData) -> {
        DesktopControl applicationControl = DesktopControl.fromEvent(eventName);
        
        if (applicationControl != null) {
            switch (applicationControl) {
                case SHUTDOWN_ABORT:
                    abortShutdown((String) ConvertUtils.convert(eventData, String.class));
                    break;
                
                case SHUTDOWN_START:
                    startShutdown((Long) ConvertUtils.convert(eventData, Long.class));
                    break;
                
                case SHUTDOWN_PROGRESS:
                    updateShutdown(NumberUtils.toLong(StrUtil.piece((String) eventData, StrUtil.U)) * 1000);
                    break;

                case LOCK:
                    lockPage(eventData == null || (Boolean) ConvertUtils.convert(eventData, Boolean.class));
                    break;
            }
        }
    };
    
    public DesktopMonitor() {
        inactivityDuration.put(Mode.BASELINE, 900000L);
        inactivityDuration.put(Mode.LOCK, 900000L);
        inactivityDuration.put(Mode.LOGOUT, 0L);
        countdownDuration.put(Mode.BASELINE, 60000L);
        countdownDuration.put(Mode.LOCK, 60000L);
        countdownDuration.put(Mode.LOGOUT, 60000L);
    }

    @Override
    public void doAfterCompose(Component root) throws Exception {
        super.doAfterCompose(root);
        desktop = root.getDesktop();
        session = (SessionCtrl) desktop.getSession();
        noAutoLock = ZKUtil.getAttributeBoolean(root, ATTR_NO_AUTO_LOCK);
        timeoutWindow = (HtmlBasedComponent) root;
        getEventManager().subscribe(DesktopControl.EVENT_ROOT, applicationControlListener);
        IUser user = securityService.getAuthenticatedUser();
        lblLocked.setValue(user == null ? null
                : Mode.BASELINE.getLabel(TIMEOUT_EXPIRATION, user.getFullName() + "@" + user.getSecurityDomain().getName()));
        setMode(Mode.BASELINE);
        desktop.addListener(desktopActivityMonitor);
    }
    
    private void updateClass() {
        String sclass = state == State.COUNTDOWN ? SCLASS_COUNTDOWN : SCLASS_IDLE;
        sclass = "cwf-desktopmonitor " + mode.format(sclass);
        
        if (shutdown) {
            sclass += " cwf-desktopmonitor-shutdown";
        }
        
        timeoutWindow.setSclass(sclass);
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
            message = StringUtils.isEmpty(message) ? StrUtil.getLabel("cwf.desktopmonitor.shutdown.abort.message") : message;
            
            if (getEventManager().hasSubscribers(MessageWindow.EVENT_SHOW)) {
                getEventManager().fireLocalEvent(MessageWindow.EVENT_SHOW, message);
            } else {
                PromptDialog.showInfo(message);
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
            lblDuration.setValue(s);
            ZKUtil.toggleSclass(timeoutPanel, "alert-danger", "alert-warning", countdown <= 10000);
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
                    timer.setRepeats(false);
                } else {
                    timerInterval = delta;
                }
                
                break;
            
            case COUNTDOWN:
                if (stateChanged) {
                    timerInterval = 1000;
                    timer.setRepeats(true);
                } else {
                    countdown -= 1000;
                }
                
                if (countdown > 0) {
                    updateCountdown();
                    break;
                } else {
                    timer.stop();
                }
                
                // fall through is intentional here.
                
            case TIMEDOUT:
                timer.setRepeats(false);
                setMode(nextMode());
                
                if (mode == Mode.LOGOUT) {
                    requestLogout();
                    return;
                }
                
                break;
            
        }

        timer.setDelay((int) timerInterval);
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
        boolean loggingOut = desktop.hasAttribute(ATTR_LOGGING_OUT);
        
        if (!loggingOut) {
            desktop.setAttribute(ATTR_LOGGING_OUT, true);
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
        session.notifyClientRequest(true);
    }
    
    public void onTimer$timer() {
        updateState(false);
    }

    /**
     * Resets the mode when the "keep open" button is clicked.
     */
    public void onClick$btnKeepOpen() {
        setMode(Mode.BASELINE);
    }
    
    public void onClick$btnLogout() {
        securityService.logout(true, null, StrUtil.getLabel("cwf.desktopmonitor.logout.reason.message"));
    }
    
    public void onClick$btnUnlock() {
        String s = txtPassword.getValue();
        txtPassword.setValue(null);
        lblInfo.setValue(null);
        txtPassword.focus();
        
        if (!StringUtils.isEmpty(s)) {
            if (securityService.validatePassword(s)) {
                setMode(Mode.BASELINE);
            } else {
                lblInfo.setValue(StrUtil.getLabel("cwf.desktopmonitor.lock.badpassword.message"));
            }
        }
    }
    
    /**
     * Called by IOC container during bean destruction.
     */
    @Override
    public void cleanup() {
        desktop.removeListener(desktopActivityMonitor);
        getEventManager().unsubscribe(DesktopControl.EVENT_ROOT, applicationControlListener);
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
