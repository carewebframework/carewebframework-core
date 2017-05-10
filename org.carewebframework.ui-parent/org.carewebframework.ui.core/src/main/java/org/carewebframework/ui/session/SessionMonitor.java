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
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.carewebframework.api.FrameworkUtil;
import org.carewebframework.api.domain.IUser;
import org.carewebframework.api.event.IEventManager;
import org.carewebframework.api.event.IGenericEvent;
import org.carewebframework.api.security.ISecurityService;
import org.carewebframework.api.thread.ThreadUtil;
import org.carewebframework.common.DateUtil;
import org.carewebframework.common.DateUtil.TimeUnit;
import org.carewebframework.common.MiscUtil;
import org.carewebframework.common.StrUtil;
import org.carewebframework.ui.Constants;
import org.carewebframework.ui.dialog.DialogUtil;
import org.carewebframework.web.ancillary.ConvertUtil;
import org.carewebframework.web.annotation.EventHandler;
import org.carewebframework.web.annotation.WiredComponent;
import org.carewebframework.web.client.ClientInvocation;
import org.carewebframework.web.client.ClientRequest;
import org.carewebframework.web.component.BaseUIComponent;
import org.carewebframework.web.component.Label;
import org.carewebframework.web.component.MessagePane;
import org.carewebframework.web.component.MessageWindow;
import org.carewebframework.web.component.Page;
import org.carewebframework.web.component.Textbox;
import org.carewebframework.web.component.Window;
import org.carewebframework.web.event.Event;
import org.carewebframework.web.event.EventUtil;
import org.carewebframework.web.page.PageUtil;
import org.carewebframework.web.websocket.ISessionListener;

/**
 * Session inactivity timeout thread. Used to notify user regarding impending inactivity timeout and
 * take appropriate action.
 */
public class SessionMonitor extends Thread {
    
    private static final Log log = LogFactory.getLog(SessionMonitor.class);
    
    /**
     * Reflects the different execution states for the thread.
     */
    private enum State {
        INITIAL, COUNTDOWN, TIMEDOUT, DEAD
    }
    
    /**
     * Actions to be performed in an event thread.
     */
    private enum Action {
        UPDATE_COUNTDOWN, UPDATE_MODE, LOGOUT
    }
    
    /**
     * Available timeout modes.
     */
    private enum Mode {
        BASELINE, LOCK, LOGOUT, SHUTDOWN;
        
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
    
    /**
     * Path to the cwf template that will be used to display the count down.
     */
    private static final String MONITOR_CWF = Constants.RESOURCE_PREFIX + "cwf/sessionMonitor.cwf";
    
    /**
     * Events that will not reset keepalive timer.
     */
    private static final String[] ignore = new String[] { "ping" };
    
    private static final String ATTR_LOGGING_OUT = "@logging_out";
    
    private static final String TIMEOUT_WARNING = "cwf.sessionmonitor.%.warning.message";
    
    private static final String TIMEOUT_EXPIRATION = "cwf.sessionmonitor.%.reason.message";
    
    private static final String SCLASS_COUNTDOWN = "cwf-sessionmonitor-%-countdown";
    
    private static final String SCLASS_IDLE = "cwf-sessionmonitor-%-idle";
    
    private final Map<Mode, Long> countdownDuration = new HashMap<>();
    
    private final Map<Mode, Long> inactivityDuration = new HashMap<>();
    
    /**
     * How often to update the displayed count down timer (in ms).
     */
    private long countdownInterval = 2000;
    
    private boolean canAutoLock = true;
    
    private Mode mode = Mode.BASELINE;
    
    private Mode previousMode = mode;
    
    private Window timeoutWindow;
    
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
    
    private boolean terminate;
    
    private final Page page;
    
    private long pollingInterval;
    
    private long lastKeepAlive;
    
    private long countdown;
    
    private State state;
    
    private ISecurityService securityService;
    
    private IEventManager eventManager;
    
    private Set<String> autoLockingExclusions = new TreeSet<>();
    
    private final Object monitor = new Object();
    
    private final ISessionListener sessionListener = new ISessionListener() {

        @Override
        public void onClientRequest(ClientRequest request) {
            if (!ArrayUtils.contains(ignore, request.getType())) {
                resetActivity();
            }
        }

        @Override
        public void onDestroy() {
            SessionMonitor.this.terminate = true;
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
    
    /**
     * Create a monitor instance for the specified page.
     *
     * @param page Page with which this thread will be associated.
     */
    public SessionMonitor(Page page) {
        this.page = page;
        setName("PageMonitor-" + page.getId());
        inactivityDuration.put(Mode.BASELINE, 900000L);
        inactivityDuration.put(Mode.LOCK, 900000L);
        inactivityDuration.put(Mode.LOGOUT, 0L);
        inactivityDuration.put(Mode.SHUTDOWN, 0L);
        countdownDuration.put(Mode.BASELINE, 60000L);
        countdownDuration.put(Mode.LOCK, 60000L);
        countdownDuration.put(Mode.LOGOUT, 60000L);
    }

    private void setSclass(String sclass) {
        String clazz = "mode:" + mode.format(sclass);
        
        if (mode == Mode.SHUTDOWN && previousMode == Mode.LOCK) {
            clazz += " " + previousMode.format(sclass);
        }
        
        timeoutWindow.addClass(clazz);
    }
    
    private void setMode(Mode newMode) {
        resetActivity();
        mode = newMode == null ? previousMode : newMode;
        pollingInterval = mode == newMode ? pollingInterval : inactivityDuration.get(mode);
        countdown = countdownDuration.get(mode);
        previousMode = mode == Mode.SHUTDOWN ? previousMode : mode;
        queuePageAction(Action.UPDATE_MODE);
        wakeup();
    }

    /**
     * Aborts any shutdown in progress.
     *
     * @param message Optional message to send to user.
     */
    public void abortShutdown(String message) {
        if (mode == Mode.SHUTDOWN) {
            updateShutdown(0);
            message = StringUtils.isEmpty(message) ? StrUtil.getLabel("cwf.sessionmonitor.shutdown.abort.message") : message;
            MessageWindow mw = page.getChild(MessageWindow.class);
            
            if (mw != null) {
                MessagePane mp = new MessagePane();
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
    
    /**
     * Updates the shutdown state.
     *
     * @param delay If positive integer, enables shutdown and begins countdown at specified # of ms.
     *            If zero or negative, aborts any shutdown in progress.
     */
    private void updateShutdown(long delay) {
        countdownDuration.put(Mode.SHUTDOWN, delay);
        setMode(delay > 0 ? Mode.SHUTDOWN : previousMode);
    }
    
    /**
     * Processes a polling request.
     *
     * @throws Exception Unspecified exception.
     */
    private void process() throws Exception {
        long now = System.currentTimeMillis();
        long interval = now - lastKeepAlive;
        State oldState = state;
        long delta = inactivityDuration.get(mode) - interval;
        state = delta > 0 ? State.INITIAL : countdown <= 0 ? State.TIMEDOUT : State.COUNTDOWN;
        boolean stateChanged = oldState != state;
        
        switch (state) {
            case INITIAL:
                if (stateChanged) {
                    pollingInterval = inactivityDuration.get(mode);
                    countdown = countdownDuration.get(mode);
                    queuePageAction(Action.UPDATE_MODE);
                } else {
                    pollingInterval = delta;
                }
                
                break;
            
            case COUNTDOWN:
                if (stateChanged) {
                    pollingInterval = countdownInterval;
                } else {
                    countdown -= countdownInterval;
                }
                
                if (countdown > 0) {
                    queuePageAction(Action.UPDATE_COUNTDOWN);
                    break;
                }
                
                // fall through is intentional here.
                
            case TIMEDOUT:
                setMode(nextMode());
                
                if (mode == Mode.LOGOUT) {
                    requestLogout();
                }
                
                break;
            
        }
    }
    
    /**
     * Returns the next mode in the timeout sequence.
     *
     * @return Next mode.
     */
    private Mode nextMode() {
        switch (mode) {
            case BASELINE:
                return canAutoLock ? Mode.LOCK : Mode.LOGOUT;
            
            default:
                return Mode.LOGOUT;
        }
    }
    
    /**
     * Queues a logout request.
     */
    private void requestLogout() {
        boolean inProgress = page.hasAttribute(ATTR_LOGGING_OUT);
        
        if (!inProgress) {
            page.setAttribute(ATTR_LOGGING_OUT, true);
            queuePageAction(Action.LOGOUT);
        } else {
            log.debug("Logout already underway");
        }
    }
    
    /**
     * Queues a page action for deferred execution.
     *
     * @param action The action to queue.
     */
    private void queuePageAction(Action action) {
        Event actionEvent = new Event("action", timeoutWindow, action);
        EventUtil.post(actionEvent);
    }
    
    private void resetActivity() {
        lastKeepAlive = System.currentTimeMillis();
    }
    
    /**
     * Resets the keep alive timer when the "keep open" button is clicked.
     */
    @EventHandler(value = "click", target = "btnKeepOpen")
    private void onClick$btnKeepOpen() {
        setMode(Mode.BASELINE);
        wakeup();
    }
    
    @EventHandler(value = "click", target = "btnLogout")
    private void onClick$btnLogout() {
        securityService.logout(true, null, null);
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
     * Event listener to handle page actions in event thread.
     *
     * @param event The action event.
     */
    @EventHandler("action")
    private void actionEventHandler(Event event) {
        Action action = (Action) event.getData();
        trace(action.name());
        
        switch (action) {
            case UPDATE_COUNTDOWN:
                String s = nextMode().getLabel(TIMEOUT_WARNING, DateUtil.formatDuration(countdown, TimeUnit.SECONDS));
                lblDuration.setLabel(s);
                setSclass(SCLASS_COUNTDOWN);
                timeoutPanel.addClass("alert:" + (countdown <= 10000 ? "alert-danger" : "alert-warning"));
                break;
            
            case UPDATE_MODE:
                setSclass(SCLASS_IDLE);
                timeoutWindow.setMode(mode == Mode.LOCK ? Window.Mode.POPUP : Window.Mode.INLINE);
                txtPassword.setFocus(mode == Mode.LOCK);
                //Application.getPageInfo(page).sendToSpawned(mode == Mode.LOCK ? Command.LOCK : Command.UNLOCK);
                break;
            
            case LOGOUT:
                terminate = true;
                timeoutWindow.setVisible(false);
                securityService.logout(true, null, mode.getLabel(TIMEOUT_EXPIRATION));
                break;
        }
    }
    
    /**
     * Log a trace message.
     *
     * @param message The text message to log.
     */
    private void trace(String message) {
        trace(message, null);
    }
    
    /**
     * Log a trace message.
     *
     * @param label Text to precede the displayed value.
     * @param value The value to display (may be null).
     */
    private void trace(String label, Object value) {
        if (log.isTraceEnabled()) {
            log.trace(this.page + " " + label + (value == null ? "" : " = " + value));
        }
    }
    
    /**
     * Thread execution loop.
     *
     * @see java.lang.Thread#run()
     */
    @Override
    public void run() {
        if (log.isTraceEnabled()) {
            trace("The Session Monitor has started", getName());
        }
        
        setMode(null);
        
        synchronized (monitor) {
            while (!terminate && !page.isDead() && !Thread.currentThread().isInterrupted()) {
                try {
                    process();
                    monitor.wait(pollingInterval);
                } catch (InterruptedException e) {
                    log.warn(String.format("%s interrupted. Terminating.", this.getClass().getName()));
                    terminate = true;
                    Thread.currentThread().interrupt();
                } catch (Exception e) {
                    log.error(page + " : " + e.getMessage(), e);
                    throw MiscUtil.toUnchecked(e);
                }
            }
        }
        
        if (log.isTraceEnabled()) {
            trace("The PageMonitor terminated", getName());
        }
    }
    
    /**
     * Wakes up the background thread.
     *
     * @return True if the operation was successful.
     */
    private synchronized boolean wakeup() {
        try {
            synchronized (monitor) {
                monitor.notify();
            }
            return true;
        } catch (Throwable t) {
            log.warn("Unexpected exception.", t);
            return false;
        }
    }
    
    /**
     * Called by the IOC container after all properties have been set.
     */
    public void init() {
        eventManager.subscribe(SessionControl.EVENT_ROOT, applicationControlListener);
        String path = page.getBrowserInfo("requestURL");
        canAutoLock = path == null || !autoLockingExclusions.contains(path.substring(path.lastIndexOf("/") + 1));
        timeoutWindow = (Window) PageUtil.createPage(MONITOR_CWF, page, null).get(0);
        timeoutWindow.wireController(this);
        IUser user = securityService.getAuthenticatedUser();
        lblLocked.setLabel(user == null ? null
                : Mode.BASELINE.getLabel(TIMEOUT_EXPIRATION, user.getFullName() + "@" + user.getSecurityDomain().getName()));
        page.getSession().addSessionListener(sessionListener);
        ThreadUtil.startThread(this);
    }
    
    /**
     * Called by IOC container during bean destruction.
     */
    public void tearDown() {
        eventManager.unsubscribe(SessionControl.EVENT_ROOT, applicationControlListener);
        page.getSession().removeSessionListener(sessionListener);
        terminate = true;
        wakeup();
    }
    
    public void lockPage(boolean lock) {
        if (mode != Mode.SHUTDOWN) {
            setMode(lock ? Mode.LOCK : Mode.BASELINE);
        }
    }
    
    /**
     * Return how often to update the displayed count down timer (in ms).
     *
     * @return countdown interval
     */
    public long getCountdownInterval() {
        return countdownInterval;
    }
    
    /**
     * Set how often to update the displayed count down timer (in ms).
     *
     * @param countdownInterval The countdown interval in ms.
     */
    public void setCountdownInterval(long countdownInterval) {
        this.countdownInterval = countdownInterval;
    }
    
    public ISecurityService getSecurityService() {
        return securityService;
    }
    
    public void setSecurityService(ISecurityService securityService) {
        this.securityService = securityService;
    }
    
    public IEventManager getEventManager() {
        return eventManager;
    }
    
    public void setEventManager(IEventManager eventManager) {
        this.eventManager = eventManager;
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
    
    /**
     * @return Set of application names (FrameworkUtil.getAppName) to exclude from automatic
     *         locking.
     * @see FrameworkUtil#getAppName()
     */
    public Set<String> getAutoLockingExclusions() {
        return autoLockingExclusions;
    }
    
    /**
     * Set of application names (FrameworkUtil.getAppName) to exclude from automatic locking.
     *
     * @param autoLockingExclusions The set of exclusions.
     * @see FrameworkUtil#getAppName()
     */
    public void setAutoLockingExclusions(Set<String> autoLockingExclusions) {
        this.autoLockingExclusions = autoLockingExclusions;
    }
    
}
