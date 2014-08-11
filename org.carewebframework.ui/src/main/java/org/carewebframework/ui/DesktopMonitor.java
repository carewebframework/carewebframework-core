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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.BooleanUtils;
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
import org.carewebframework.common.StrUtil;
import org.carewebframework.ui.Application.Command;
import org.carewebframework.ui.zk.MessageWindow;
import org.carewebframework.ui.zk.ZKUtil;

import org.zkoss.util.resource.Labels;
import org.zkoss.zk.au.AuRequest;
import org.zkoss.zk.au.AuService;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Desktop;
import org.zkoss.zk.ui.DesktopUnavailableException;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.HtmlBasedComponent;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.UiException;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.sys.SessionCtrl;
import org.zkoss.zk.ui.util.UiLifeCycle;
import org.zkoss.zul.Label;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

/**
 * Desktop timeout thread that evaluates ZK's Desktop and the time a request was last sent. Used to
 * notify user regarding their inactivity and take appropriate action.
 */
public class DesktopMonitor extends Thread {
    
    private static final Log log = LogFactory.getLog(DesktopMonitor.class);
    
    /**
     * Reflects the different execution states for the thread.
     */
    private enum State {
        INITIAL, COUNTDOWN, TIMEDOUT, DEAD
    };
    
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
        BASELINE, LOCK, SHUTDOWN;
        
        public void onTimeout(DesktopMonitor monitor) {
            switch (this) {
                case BASELINE:
                    monitor.setMode(Mode.LOCK);
                    break;
                    
                case LOCK:
                    monitor.requestLogout();
                    break;
                    
                case SHUTDOWN:
                    monitor.requestLogout();
                    break;
            }
        }
        
        /**
         * Returns a label from a label reference.
         * 
         * @param label Label reference with placeholder for mode.
         * @param params Optional parameters.
         * @return Fully formatted label value.
         */
        public String getLabel(String label, Object... params) {
            return Labels.getLabel(format(label), params);
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
     * Path to the zul code that will be used to display the count down.
     */
    private static final String DESKTOP_TIMEOUT_ZUL = org.carewebframework.ui.zk.Constants.RESOURCE_PREFIX
            + "desktopTimeoutWarning.zul";
    
    /**
     * Events that will not reset keepalive timer.
     */
    private static final String[] ignore = new String[] { "dummy", Events.ON_TIMER, Events.ON_CLIENT_INFO };
    
    private static final String ATTR_LOGGING_OUT = "@logging_out";
    
    private static final String TIMEOUT_WARNING = "cwf.timeout.%.warning.message";
    
    private static final String TIMEOUT_EXPIRATION = "cwf.timeout.%.expiration.message";
    
    private static final String SCLASS_COUNTDOWN = "cwf-timeout-%-countdown";
    
    private static final String SCLASS_IDLE = "cwf-timeout-%-idle";
    
    private final Map<Mode, Long> countdownDuration = new HashMap<Mode, Long>();
    
    private final Map<Mode, Long> inactivityDuration = new HashMap<Mode, Long>();
    
    /**
     * Maximum interval of inactivity after which a desktop is assumed to be dead (in ms).
     */
    private long maximumInactivityInterval = 300000;
    
    /**
     * How often to update the displayed count down timer (in ms).
     */
    private long countdownInterval = 2000;
    
    private Mode mode = Mode.BASELINE;
    
    private Mode previousMode = mode;
    
    private Window timeoutWindow;
    
    private HtmlBasedComponent timeoutPanel;
    
    private Label lblDuration;
    
    private Label lblLocked;
    
    private Label lblInfo;
    
    private Textbox txtPassword;
    
    private boolean terminate;
    
    private final Desktop desktop;
    
    private long pollingInterval;
    
    private long lastKeepAlive;
    
    private long lastActivity;
    
    private boolean desktopDead;
    
    private long countdown;
    
    private State state;
    
    private ISecurityService securityService;
    
    private IEventManager eventManager;
    
    private Set<String> desktopMonitorLockingExclusions = new TreeSet<String>();
    
    private final Object monitor = new Object();
    
    private final UiLifeCycle uiLifeCycle = new UiLifeCycle() {
        
        /**
         * Attaches the time out warning zul to the desktop root. This markup remains hidden until
         * the timeout count down commences.
         * 
         * @see org.zkoss.zk.ui.util.UiLifeCycle#afterPageAttached(org.zkoss.zk.ui.Page,
         *      org.zkoss.zk.ui.Desktop)
         */
        @Override
        public void afterPageAttached(final Page page, final Desktop desktop) {
            desktop.removeListener(this);
            timeoutWindow = (Window) desktop.getExecution().createComponents(DESKTOP_TIMEOUT_ZUL, null, null);
            ZKUtil.wireController(timeoutWindow, DesktopMonitor.this);
            IUser user = securityService.getAuthenticatedUser();
            lblLocked.setValue(Mode.BASELINE.getLabel(TIMEOUT_EXPIRATION, user.getFullName() + "@" + user.getDomainName()));
            desktop.enableServerPush(true);
            desktop.addListener(desktopActivityMonitor);
            ThreadUtil.startThread(DesktopMonitor.this);
        }
        
        /*
         * Not used.
         */
        @Override
        public void afterComponentAttached(final Component cmp, final Page page) {
        }
        
        /*
         * Not used.
         */
        @Override
        public void afterComponentDetached(final Component cmp, final Page page) {
        }
        
        /*
         * Not used.
         */
        @Override
        public void afterComponentMoved(final Component cmp1, final Component cmp2, final Component cmp3) {
        }
        
        /*
         * Not used.
         */
        @Override
        public void afterPageDetached(final Page page, final Desktop desktop) {
        }
        
    };
    
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
        public boolean service(final AuRequest request, final boolean everError) {
            resetActivity(isKeepAliveRequest(request));
            return false;
        }
        
        /**
         * Determines if request is a 'keep alive' request. The logic is based on ZK
         * org.zkoss.zk.au.http.DHtmlUpdateServlet (as of zk 3.5.2) keepAlive = keepAlive ||
         * (!(!timerKeepAlive && Events.ON_TIMER.equals(cmdId)) && !"dummy".equals(cmdId)); This
         * basically said that for ServerPush (i.e. COMET / Reverse AJAX), don't regard these
         * requests as dictating session expiration Assumes DHtmlUpdateServlet in web.xml is mapped
         * to /zkau
         * 
         * @param request The inbound request.
         * @return keepAlive True if request should reset inactivity timeout.
         */
        private boolean isKeepAliveRequest(final AuRequest request) {
            return !ArrayUtils.contains(ignore, request.getCommand());
        }
    };
    
    /**
     * Event listener to handle desktop actions in event thread.
     */
    private final EventListener<Event> actionHandler = new EventListener<Event>() {
        
        @Override
        public void onEvent(Event event) throws Exception {
            Action action = (Action) event.getData();
            trace(action.name());
            
            //appName is not set by afterPageAttached so, checking each event
            final String appName = StringUtils.trimToEmpty(FrameworkUtil.getAppName());
            final boolean isDesktopLockingDisabled = DesktopMonitor.this.desktopMonitorLockingExclusions.contains(appName);
            
            switch (action) {
                case UPDATE_COUNTDOWN:
                    String s = mode.getLabel(TIMEOUT_WARNING, DateUtil.formatDuration(countdown, TimeUnit.SECONDS));
                    lblDuration.setValue(s);
                    if (!isDesktopLockingDisabled) {
                        setSclass(SCLASS_COUNTDOWN);
                        ZKUtil.updateStyle(timeoutPanel, "background", countdown <= 10000 ? "red" : "yellow");
                    }
                    resetActivity(false);
                    break;
                    
                case UPDATE_MODE:
                    if (!isDesktopLockingDisabled) {
                        setSclass(SCLASS_IDLE);
                        timeoutWindow.setMode(mode == Mode.LOCK ? "highlighted" : "embedded");
                        txtPassword.setFocus(mode == Mode.LOCK);
                        Application.getDesktopInfo(desktop).sendToSpawned(mode == Mode.LOCK ? Command.LOCK : Command.UNLOCK);
                    }
                    break;
                    
                case LOGOUT:
                    if (isDesktopLockingDisabled) {
                        log.info(String.format("App[%s] Desktop[%s] was excluded from pre-invalidation locking", appName,
                            DesktopMonitor.this.desktop));
                    }
                    terminate = true;
                    timeoutWindow.setVisible(false);
                    securityService.logout(true, null, mode.getLabel(TIMEOUT_EXPIRATION));
                    break;
            }
        }
        
        private void setSclass(String sclass) {
            if (mode == Mode.SHUTDOWN && previousMode == Mode.LOCK) {
                timeoutWindow.setSclass(mode.format(sclass) + " " + previousMode.format(sclass));
            } else {
                timeoutWindow.setSclass(mode.format(sclass));
            }
        }
        
    };
    
    private final IGenericEvent<Object> desktopEventListener = new IGenericEvent<Object>() {
        
        @Override
        public void eventCallback(String eventName, Object eventData) {
            if (eventName.equals(Constants.SHUTDOWN_ABORT_EVENT)) {
                abortShutdown(eventData == null ? null : eventData.toString());
            } else if (eventName.equals(Constants.SHUTDOWN_START_EVENT)) {
                startShutdown(NumberUtils.toLong(eventData == null ? "" : eventData.toString()));
            } else if (eventName.equals(Constants.SHUTDOWN_EVENT)) {
                updateShutdown(NumberUtils.toLong(StrUtil.piece(eventData.toString(), StrUtil.U)) * 1000);
            } else if (eventName.equals(Constants.LOCK_EVENT)) {
                lockDesktop(eventData == null || BooleanUtils.toBoolean(eventData.toString()));
            }
        }
    };
    
    private void setMode(Mode newMode) {
        resetActivity(true);
        mode = newMode == null ? previousMode : newMode;
        pollingInterval = mode == newMode ? pollingInterval : inactivityDuration.get(mode);
        countdown = countdownDuration.get(mode);
        previousMode = mode == Mode.SHUTDOWN ? previousMode : mode;
        queueDesktopAction(Action.UPDATE_MODE);
        wakeup();
    }
    
    /**
     * Create a monitor instance for the specified desktop.
     * 
     * @param desktop Desktop with which this thread will be associated.
     */
    public DesktopMonitor(final Desktop desktop) {
        this.desktop = desktop;
        setName("DesktopMonitor-" + desktop.getId());
        inactivityDuration.put(Mode.BASELINE, 900000L);
        inactivityDuration.put(Mode.LOCK, 900000L);
        inactivityDuration.put(Mode.SHUTDOWN, 0L);
        countdownDuration.put(Mode.BASELINE, 60000L);
        countdownDuration.put(Mode.LOCK, 60000L);
    }
    
    /**
     * Aborts any shutdown in progress.
     * 
     * @param message Optional message to send to user.
     */
    public void abortShutdown(String message) {
        if (mode == Mode.SHUTDOWN) {
            updateShutdown(0);
            eventManager.fireLocalEvent(MessageWindow.EVENT_SHOW,
                StringUtils.isEmpty(message) ? Labels.getLabel("cwf.timeout.shutdown.abort.message") : message);
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
     * Resets the keep alive timer. Note that we always keep the session alive since the desktop
     * monitor is responsible for handling the session timeout.
     * 
     * @param resetKeepAlive
     */
    private synchronized void resetActivity(final boolean resetKeepAlive) {
        lastActivity = System.currentTimeMillis();
        ((SessionCtrl) desktop.getSession()).notifyClientRequest(true);
        
        if (resetKeepAlive) {
            lastKeepAlive = lastActivity;
        }
    }
    
    /**
     * Processes a polling request.
     * 
     * @throws Exception
     */
    private void process() throws Exception {
        long now = System.currentTimeMillis();
        long silence = now - lastActivity;
        long interval = now - lastKeepAlive;
        State oldState = state;
        long delta = inactivityDuration.get(mode) - interval;
        state = silence >= maximumInactivityInterval ? State.DEAD : delta > 0 ? State.INITIAL
                : countdown <= 0 ? State.TIMEDOUT : State.COUNTDOWN;
        boolean stateChanged = oldState != state;
        
        switch (state) {
            case INITIAL:
                if (stateChanged) {
                    pollingInterval = inactivityDuration.get(mode);
                    countdown = countdownDuration.get(mode);
                    queueDesktopAction(Action.UPDATE_MODE);
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
                    queueDesktopAction(Action.UPDATE_COUNTDOWN);
                    break;
                }
                
                // fall through is intentional here.
                
            case TIMEDOUT:
                mode.onTimeout(this);
                break;
                
            case DEAD:
                this.terminate = true;
                this.desktopDead = true;
                break;
        }
    }
    
    /**
     * Queues a logout request.
     */
    private void requestLogout() {
        boolean inProgress = desktop.hasAttribute(ATTR_LOGGING_OUT);
        
        if (!inProgress) {
            desktop.setAttribute(ATTR_LOGGING_OUT, true);
            queueDesktopAction(Action.LOGOUT);
        } else {
            log.debug("Logout already underway");
        }
    }
    
    /**
     * Queues a desktop action for deferred execution.
     * 
     * @param action
     */
    private void queueDesktopAction(Action action) {
        Event actionEvent = new Event("onAction", null, action);
        
        if (Events.inEventListener()) {
            try {
                actionHandler.onEvent(actionEvent);
            } catch (Exception e) {
                log.error("Error executing desktop action.", e);
            }
        } else {
            Executions.schedule(desktop, actionHandler, actionEvent);
        }
    }
    
    /**
     * Resets the keep alive timer when the "keep open" button is clicked.
     */
    public void onClick$btnKeepOpen() {
        resetActivity(true);
        wakeup();
    }
    
    public void onClick$btnLogout() {
        securityService.logout(true, null, null);
    }
    
    public void onClick$btnUnlock() {
        String s = txtPassword.getText();
        txtPassword.setText(null);
        lblInfo.setValue(null);
        txtPassword.focus();
        
        if (!StringUtils.isEmpty(s)) {
            if (securityService.validatePassword(s)) {
                setMode(Mode.BASELINE);
            } else {
                lblInfo.setValue(Labels.getLabel("cwf.timeout.lock.badpassword.message"));
            }
        }
    }
    
    /**
     * Log a trace message.
     * 
     * @param message The text message to log.
     */
    private void trace(final String message) {
        trace(message, null);
    }
    
    /**
     * Log a trace message.
     * 
     * @param label Text to precede the displayed value.
     * @param value The value to display (may be null).
     */
    private void trace(final String label, final Object value) {
        if (log.isTraceEnabled()) {
            log.trace(this.desktop + " " + label + (value == null ? "" : " = " + value));
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
            trace("The DesktopMonitor has started", getName());
        }
        
        setMode(Mode.BASELINE);
        
        synchronized (monitor) {
            while (!terminate && desktop.isAlive() && desktop.isServerPushEnabled()
                    && !Thread.currentThread().isInterrupted()) {
                try {
                    process();
                    monitor.wait(pollingInterval);
                } catch (final DesktopUnavailableException e) {
                    log.warn(desktop + " DesktopUnavailableException: " + e.getMessage());
                } catch (final InterruptedException e) {
                    log.warn(String.format("%s interrupted. Terminating.", this.getClass().getName()));
                    terminate = true;
                    Thread.currentThread().interrupt();
                } catch (final Exception e) {
                    log.error(desktop + " : " + e.getMessage(), e);
                    throw UiException.Aide.wrap(e);
                }
            }
        }
        
        if (log.isTraceEnabled()) {
            trace("The DesktopMonitor terminated", getName());
        }
        
        if (desktopDead && desktop.isAlive()) {
            log.warn("Desktop presumed dead due to prolonged inactivity: " + this.desktop);
            Application.getInstance().register(this.desktop, false);
        }
    }
    
    /**
     * Wakes up the background thread.
     * 
     * @return
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
        desktop.addListener(uiLifeCycle);
        eventManager.subscribe(Constants.DESKTOP_EVENT, desktopEventListener);
    }
    
    /**
     * Called by IOC container during bean destruction.
     */
    public void tearDown() {
        eventManager.unsubscribe(Constants.DESKTOP_EVENT, desktopEventListener);
        desktop.removeListener(uiLifeCycle);
        terminate = true;
        wakeup();
    }
    
    public void lockDesktop(boolean lock) {
        if (mode != Mode.SHUTDOWN) {
            setMode(lock ? Mode.LOCK : Mode.BASELINE);
        }
    }
    
    /**
     * Return the maximum interval of inactivity after which a desktop is assumed to be dead (in
     * ms). Note: setting this too short will result in a desktop whose event thread is busy in a
     * prolonged operation being prematurely discarded.
     * 
     * @return maximum inactivity interval
     */
    public long getMaximumInactivityInterval() {
        return maximumInactivityInterval;
    }
    
    /**
     * Set the maximum interval of inactivity after which a desktop is assumed to be dead (in ms).
     * Note: setting this too short will result in a desktop whose event thread is busy in a
     * prolonged operation being prematurely discarded.
     * 
     * @param maximumInactivityInterval - maximum inactivity
     */
    public void setMaximumInactivityInterval(final long maximumInactivityInterval) {
        this.maximumInactivityInterval = maximumInactivityInterval;
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
     * @param countdownInterval - The countdown interval
     */
    public void setCountdownInterval(final long countdownInterval) {
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
     * @return the desktopMonitorLockingExclusions
     */
    public Set<String> getDesktopMonitorLockingExclusions() {
        return desktopMonitorLockingExclusions;
    }
    
    /**
     * Set of application names (FrameworkUtil.getAppName) to exclude from locking
     * 
     * @param desktopMonitorLockingExclusions the desktopMonitorLockingExclusions to set
     * @see FrameworkUtil#getAppName()
     */
    public void setDesktopMonitorLockingExclusions(Set<String> desktopMonitorLockingExclusions) {
        this.desktopMonitorLockingExclusions = desktopMonitorLockingExclusions;
    }
    
}
