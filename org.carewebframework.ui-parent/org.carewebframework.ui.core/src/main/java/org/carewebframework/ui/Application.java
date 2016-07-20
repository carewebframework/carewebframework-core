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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.carewebframework.api.FrameworkRuntimeException;
import org.carewebframework.common.DateUtil;
import org.carewebframework.common.DateUtil.ITimeZoneAccessor;
import org.carewebframework.common.StrUtil;
import org.carewebframework.ui.LifecycleEventListener.ILifecycleCallback;
import org.carewebframework.ui.action.ActionRegistry;
import org.carewebframework.ui.spring.AppContextFinder;
import org.carewebframework.ui.spring.FrameworkAppContext;

import org.zkoss.zk.au.AuRequest;
import org.zkoss.zk.au.AuService;
import org.zkoss.zk.au.out.AuClientInfo;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Desktop;
import org.zkoss.zk.ui.Execution;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.Session;
import org.zkoss.zk.ui.ShadowElement;
import org.zkoss.zk.ui.event.ClientInfoEvent;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zk.ui.util.UiLifeCycle;
import org.zkoss.zul.Fileupload;
import org.zkoss.zul.Label;

/**
 * Application singleton used to track active sessions and desktops.
 */
public class Application {
    
    private static final Log log = LogFactory.getLog(Application.class);
    
    private static final Application instance = new Application();
    
    private static final String ATTR_SERVER_PUSH = "@requires-server-push";
    
    private static final String ID_LOCK_MESSAGE = "_lock_message";
    
    public enum Command {
        CLOSE, LOCK, UNLOCK
    };
    
    private static final EventListener<Event> desktopCommandListener = new EventListener<Event>() {
        
        @Override
        public void onEvent(Event event) throws Exception {
            switch ((Command) event.getData()) {
                case CLOSE:
                    Clients.evalJavaScript("window.close();");
                    break;
                    
                case LOCK:
                    lock(true);
                    break;
                    
                case UNLOCK:
                    lock(false);
                    break;
            }
        }
        
        private void lock(boolean lock) {
            Desktop dtp = Executions.getCurrent().getDesktop();
            Component lbl = null;
            
            for (Page page : dtp.getPages()) {
                for (Component root : page.getRoots()) {
                    if (lbl == null && ID_LOCK_MESSAGE.equals(root.getId())) {
                        lbl = root;
                        lbl.setVisible(lock);
                    } else {
                        root.setVisible(!lock);
                    }
                }
            }
            
            if (lock && lbl == null) {
                Label label = new Label(StrUtil.formatMessage("@cwf.timeout.lock.spawned.message"));
                label.setSclass("cwf-timeout-lock-spawned");
                label.setId(ID_LOCK_MESSAGE);
                label.setPage(dtp.getFirstPage());
            }
        }
        
    };
    
    /**
     * Tracks information about a session.
     */
    public class SessionInfo {
        
        private final List<Desktop> desktops = new ArrayList<>();
        
        private final Session session;
        
        private final int maxInactiveInterval;
        
        private String remoteAddress;
        
        private String localAddress;
        
        private String localName;
        
        private String remoteHost;
        
        private String serverName;
        
        /**
         * Creates a session info instance for this session.
         * 
         * @param session The session.
         */
        private SessionInfo(Session session) {
            this.session = session;
            maxInactiveInterval = session.getMaxInactiveInterval();
        }
        
        /**
         * Adds a desktop to this session information.
         * 
         * @param desktop The desktop to add.
         */
        private void addDesktop(Desktop desktop) {
            synchronized (activeDesktops) {
                desktop.getSession().setMaxInactiveInterval(maxInactiveInterval);
                activeDesktops.put(desktop.getId(), desktop);
                this.desktops.add(desktop);
                DesktopInfo desktopInfo = new DesktopInfo(desktop);
                desktop.setAttribute(DesktopInfo.class.getName(), desktopInfo);
                desktop.addListener(desktopInfo);
                desktop.addListener(uiLifeCycle);
                
                if (remoteAddress == null) {
                    Execution exec = desktop.getExecution();
                    remoteAddress = exec.getRemoteAddr();
                    localAddress = exec.getLocalAddr();
                    localName = exec.getLocalName();
                    remoteHost = exec.getRemoteHost();
                    serverName = exec.getServerName();
                }
            }
        }
        
        /**
         * Removes a desktop from this session information.
         * 
         * @param desktop The desktop to remove.
         */
        private void removeDesktop(Desktop desktop) {
            synchronized (activeDesktops) {
                activeDesktops.remove(desktop.getId());
                
                if (this.desktops.remove(desktop)) {
                    DesktopInfo desktopInfo = getDesktopInfo(desktop);
                    
                    if (desktopInfo != null) {
                        desktopInfo.destroy();
                    }
                    
                    if (desktops.isEmpty()) {
                        session.setMaxInactiveInterval(30);
                        log.debug("Session marked for invalidation: " + session);
                    }
                }
            }
        }
        
        /**
         * Called when session info object is to be destroyed.
         */
        private void destroy() {
            for (Desktop desktop : getDesktops()) {
                removeDesktop(desktop);
            }
            
            session.invalidate();
        }
        
        public int getMaxInactiveInterval() {
            return maxInactiveInterval;
        }
        
        public String getRemoteAddress() {
            return remoteAddress;
        }
        
        public String getLocalAddress() {
            return localAddress;
        }
        
        public String getLocalName() {
            return localName;
        }
        
        public String getRemoteHost() {
            return remoteHost;
        }
        
        public String getServerName() {
            return serverName;
        }
        
        /**
         * Returns the native session associated with this session information.
         * 
         * @return HttpSession
         */
        public HttpSession getNativeSession() {
            return this.session == null ? null : (HttpSession) this.session.getNativeSession();
        }
        
        /**
         * Returns the native session associated with this session information.
         * 
         * @return Session
         */
        public Session getSession() {
            return this.session;
        }
        
        /**
         * Returns a list of active desktops associated with this session. We return a copy of the
         * desktop list, to avoid concurrency issues.
         * 
         * @return List of Desktops
         */
        public synchronized List<Desktop> getDesktops() {
            return new ArrayList<>(this.desktops);
        }
        
        /**
         * String representation of <code>Application</code>
         * 
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            StringBuffer buffer = new StringBuffer();
            
            if (this.session != null) {
                buffer.append("\nSessionInfo");
                HttpSession httpSession = getNativeSession();
                
                if (httpSession != null) {//precaution, shouldn't ever be null
                    buffer.append("\n\tSession ID: ").append(httpSession.getId());
                    buffer.append("\n\tCreationTime: ").append(String.valueOf(new Date(httpSession.getCreationTime())));
                    buffer.append("\n\tLastAccessedTime: ")
                            .append(String.valueOf(new Date(httpSession.getLastAccessedTime())));
                }
                
                String deviceType = this.session.getDeviceType();
                int maxInactiveInterval = this.session.getMaxInactiveInterval();
                Map<?, ?> attributes = this.session.getAttributes();
                
                if (attributes != null) {
                    for (Object key : attributes.keySet()) {
                        buffer.append("\n\tSession Attribute Key=").append(key);
                        Object attribute = attributes.get(key);
                        buffer.append(", Value=").append(attribute);
                        buffer.append(", Session Attribute Class=").append(attribute == null ? null : attribute.getClass());
                    }
                }
                
                buffer.append("\n\tSession(").append(localName).append("):MaxInactiveInterval=").append(maxInactiveInterval)
                        .append(", DeviceType=").append(deviceType).append(", LocalAddresss=").append(localAddress)
                        .append(", RemotedAddress=").append(remoteAddress).append(", RemoteHost=").append(remoteHost)
                        .append(", ServerName=").append(serverName);
                        
                for (Desktop desktop : getDesktops()) {
                    DesktopInfo desktopInfo = Application.getDesktopInfo(desktop);
                    buffer.append(desktopInfo);
                }
            }
            return buffer.toString();
        }
        
    }
    
    /**
     * Tracks information about a desktop that is only available during an active execution.
     */
    public class DesktopInfo implements AuService {
        
        /**
         * Error Code for illegal state exception
         */
        private static final String EXC_ILLEGAL_STATE = "@cwf.error.ui.illegal.state";
        
        private final String id;
        
        private final String owner;
        
        private final String userAgent;
        
        private final String remoteAddress;
        
        private final String remoteHost;
        
        private final String remoteUser;
        
        private boolean infoRequested;
        
        private final boolean isExplorer;
        
        private final boolean isGecko;
        
        private List<String> spawned;
        
        private ClientInfoEvent clientInformation;
        
        /**
         * Copies information from the desktop's execution so that it is available to monitoring
         * applications. Also, captures information from the onClientInfo event when sent to the
         * desktop.
         * <p>
         * Note: Do not keep a reference to the desktop here.
         * 
         * @param desktop The desktop.
         */
        private DesktopInfo(Desktop desktop) {
            Execution exec = desktop.getExecution();
            
            if (exec == null) {
                throw new FrameworkRuntimeException(EXC_ILLEGAL_STATE, null, DesktopInfo.this.toString());
            }
            userAgent = exec.getUserAgent();
            remoteAddress = exec.getRemoteAddr();
            remoteHost = exec.getRemoteHost();
            remoteUser = exec.getRemoteUser();
            isExplorer = exec.getBrowser("ie") != null;
            isGecko = exec.getBrowser("gecko") != null;
            id = desktop.getId();
            
            if (isManaged(desktop)) {
                owner = null;
            } else {
                String qs = desktop.getQueryString();
                owner = qs == null ? null : FrameworkWebSupport.queryStringToMap(qs).get("owner");
                
                if (registerWithOwner(true)) {
                    desktop.setAttribute(ATTR_SERVER_PUSH, true);
                }
            }
        }
        
        public void destroy() {
            Desktop dtp = getDesktop(id);
            
            if (dtp != null) {
                dtp.removeListener(this);
            }
            registerWithOwner(false);
            sendToSpawned(Command.CLOSE);
        }
        
        public void sendToSpawned(Command command) {
            if (spawned != null && !spawned.isEmpty()) {
                for (String dtid : new ArrayList<>(spawned)) {
                    Desktop dtp = getDesktop(dtid);
                    
                    if (dtp != null) {
                        try {
                            Executions.schedule(dtp, desktopCommandListener, new Event("ON_COMMAND", null, command));
                        } catch (Exception e) {
                            log.error("Error sending command to spawned desktop.", e);
                        }
                    }
                }
            }
            
        }
        
        private boolean registerWithOwner(boolean register) {
            if (owner != null && !owner.equals(id)) {
                Desktop dtp = getDesktop(owner);
                DesktopInfo dto = dtp == null ? null : getDesktopInfo(dtp);
                
                if (dto != null) {
                    if (register) {
                        dto.registerSpawned(id);
                    } else {
                        dto.unregisterSpawned(id);
                    }
                    
                    return true;
                }
            }
            
            return false;
        }
        
        /**
         * @return the id
         */
        public String getId() {
            return this.id;
        }
        
        /**
         * @return the owner id
         */
        public String getOwner() {
            return this.owner;
        }
        
        /**
         * @return the isExplorer
         */
        public boolean isExplorer() {
            return this.isExplorer;
        }
        
        /**
         * @return the userAgent
         */
        public String getUserAgent() {
            return this.userAgent;
        }
        
        /**
         * @return the remoteAddress
         */
        public String getRemoteAddress() {
            return this.remoteAddress;
        }
        
        /**
         * @return the remoteHost
         */
        public String getRemoteHost() {
            return this.remoteHost;
        }
        
        /**
         * @return the remoteUser
         */
        public String getRemoteUser() {
            return this.remoteUser;
        }
        
        /**
         * @return the infoRequested
         */
        public boolean isInfoRequested() {
            return this.infoRequested;
        }
        
        /**
         * @return the clientInformation
         */
        public ClientInfoEvent getClientInformation() {
            return this.clientInformation;
        }
        
        /**
         * @return the isGecko
         */
        public boolean isGecko() {
            return this.isGecko;
        }
        
        /**
         * Registers a desktop spawned from this one.
         * 
         * @param id The id of the spawned desktop to register.
         */
        private void registerSpawned(String id) {
            if (spawned == null) {
                spawned = new ArrayList<>();
            }
            
            spawned.add(id);
        }
        
        /**
         * Unregisters a desktop spawned from this one.
         * 
         * @param id The id of the spawned desktop to unregister.
         */
        private void unregisterSpawned(String id) {
            if (spawned != null) {
                spawned.remove(id);
            }
        }
        
        /**
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            StringBuffer buffer = new StringBuffer();
            ClientInfoEvent clientInfo = getClientInformation();
            String screenDimensions = clientInfo == null ? ""
                    : (clientInfo.getScreenWidth() + "x" + clientInfo.getScreenHeight());
            Desktop desktop = getDesktop(id);
            buffer.append("\n\t\tDesktopInfo");
            buffer.append("\n\t\t\tDesktop: ").append(desktop);//includes Id
            buffer.append("\n\t\t\tOwner: ").append(owner == null ? "none" : owner);
            buffer.append("\n\t\t\tisAlive: ").append(desktop == null || !desktop.isAlive() ? false : true);
            buffer.append("\n\t\t\tisServerPushEnabled: ")
                    .append(desktop == null || !desktop.isServerPushEnabled() ? false : true);
            buffer.append("\n\t\t\tUserAgent: ").append(getUserAgent());
            buffer.append("\n\t\t\tUserAgent (According to ZK) ").append(
                isGecko() ? "is Gecko based (i.e. Firefox)" : (isExplorer() ? "is IE based" : " may not be IE or Firefox"));
            buffer.append("\n\t\t\tUserAgent screen dimensions: ").append(screenDimensions);
            return buffer.toString();
        }
        
        /**
         * Listens to client requests. Serves two functions:
         * <ol>
         * <li>Makes a request to the client for information about the client's operating
         * environment.</li>
         * <li>Processes the client response, storing the returned information in clientInformation.
         * </li>
         * </ol>
         * Once the requested information is processed, stops listening to further client requests.
         */
        @Override
        public boolean service(AuRequest request, boolean everError) {
            if (Events.ON_CLIENT_INFO.equals(request.getCommand())) {
                this.clientInformation = ClientInfoEvent.getClientInfoEvent(request);
                request.getDesktop().removeListener(this);
            } else if (!this.infoRequested) {
                this.infoRequested = true;
                request.getDesktop().getExecution().addAuResponse(new AuClientInfo(request.getDesktop()));
            }
            return false;
        }
    }
    
    private final ITimeZoneAccessor localTimeZone = new ITimeZoneAccessor() {
        
        @Override
        public TimeZone getTimeZone() {
            TimeZone tz = Application.getTimeZone(FrameworkWebSupport.getDesktop());
            return tz == null ? TimeZone.getDefault() : tz;
        }
        
        @Override
        public void setTimeZone(TimeZone timezone) {
            throw new UnsupportedOperationException();
        }
        
    };
    
    /**
     * Attaches the default variable resolver to the desktop's page.
     */
    private final UiLifeCycle uiLifeCycle = new UiLifeCycle() {
        
        @Override
        public void afterPageAttached(Page page, Desktop desktop) {
            desktop.removeListener(this);
            page.addVariableResolver(new FrameworkVariableResolver());
            
            if (desktop.hasAttribute(ATTR_SERVER_PUSH)) {
                desktop.enableServerPush(true);
            }
        }
        
        @Override
        public void afterPageDetached(Page page, Desktop prevdesktop) {
        }
        
        @Override
        public void afterComponentAttached(Component comp, Page page) {
        }
        
        @Override
        public void afterComponentDetached(Component comp, Page prevpage) {
        }
        
        @Override
        public void afterComponentMoved(Component parent, Component child, Component prevparent) {
        }
        
        @Override
        public void afterShadowAttached(ShadowElement shadow, Component host) {
        }
        
        @Override
        public void afterShadowDetached(ShadowElement shadow, Component prevhost) {
        }
        
    };
    
    private final ILifecycleCallback<Desktop> desktopLifeCycle = new ILifecycleCallback<Desktop>() {
        
        /**
         * The desktop is registered and the associated session's active desktop count is
         * incremented by one.
         * 
         * @param desktop Desktop to register.
         */
        @Override
        public void onInit(Desktop desktop) {
            register(desktop, true);
        }
        
        /**
         * The desktop is unregistered, the associated session's active desktop count is decremented
         * by one and, if the count has reached zero, the session is invalidated.
         * 
         * @param desktop Desktop to unregister.
         */
        @Override
        public void onCleanup(Desktop desktop) {
            register(desktop, false);
        }
        
        @Override
        public int getPriority() {
            return -100;
        }
    };
    
    private final ILifecycleCallback<Session> sessionLifeCycle = new ILifecycleCallback<Session>() {
        
        /**
         * Registers a session upon creation.
         * 
         * @param session Session to register.
         */
        @Override
        public void onInit(Session session) {
            addSession(session);
        }
        
        /**
         * Unregisters a session upon destruction.
         * 
         * @param session Session to unregister.
         */
        @Override
        public void onCleanup(Session session) {
            removeSession(session);
        }
        
        @Override
        public int getPriority() {
            return -100;
        }
    };
    
    private final Map<String, SessionInfo> activeSessions = new ConcurrentHashMap<>();
    
    private final Map<String, Desktop> activeDesktops = new ConcurrentHashMap<>();
    
    /**
     * Returns the singleton instance of the Application object.
     * 
     * @return Application instance
     */
    public static Application getInstance() {
        return instance;
    }
    
    /**
     * Returns the desktop info instance associated with the specified desktop.
     * 
     * @param desktop Desktop whose information is requested.
     * @return The DesktopInfo instance for the specified desktop.
     */
    public static DesktopInfo getDesktopInfo(Desktop desktop) {
        return (DesktopInfo) desktop.getAttribute(DesktopInfo.class.getName());
    }
    
    /**
     * Return the time zone set for the specified desktop.
     * 
     * @param desktop The desktop.
     * @return The time zone.
     */
    public static TimeZone getTimeZone(Desktop desktop) {
        DesktopInfo dti = desktop == null ? null : getDesktopInfo(desktop);
        return dti == null || dti.clientInformation == null ? null : dti.clientInformation.getTimeZone();
    }
    
    /**
     * Set time zone resolver and add lifecycle callbacks.
     */
    private Application() {
        super();
        DateUtil.localTimeZone = localTimeZone;
        Fileupload.setTemplate("~./org/carewebframework/ui/zk/fileuploaddlg.zul");
        LifecycleEventDispatcher.addDesktopCallback(desktopLifeCycle);
        LifecycleEventDispatcher.addSessionCallback(sessionLifeCycle);
        ActionRegistry.register(true, "cwf.refresh", "@cwf.btn.refresh.label",
            "zscript:org.carewebframework.api.event.EventManager.getInstance().fireLocalEvent(\"" + Constants.REFRESH_EVENT
                    + "\", null);");
    }
    
    /**
     * Returns a list of SessionInfo objects for active sessions. This is a copy of the underlying
     * list to avoid concurrency issues.
     * 
     * @return A list of SessionInfo objects.
     */
    public List<SessionInfo> getActiveSessions() {
        return new ArrayList<>(activeSessions.values());
    }
    
    /**
     * Adds a session to the list of active sessions.
     * 
     * @param session Session to add.
     */
    private void addSession(Session session) {
        synchronized (activeSessions) {
            String id = ((HttpSession) session.getNativeSession()).getId();
            
            if (!activeSessions.containsKey(id)) {
                SessionInfo sessionInfo = new SessionInfo(session);
                activeSessions.put(id, sessionInfo);
                log.debug(sessionInfo);
            }
        }
    }
    
    /**
     * Removes a session from the list of active sessions.
     * 
     * @param session Session to remove.
     */
    private void removeSession(Session session) {
        removeSessionInfo(getSessionInfo(session));
    }
    
    /**
     * Removes a session from the list of active sessions.
     * 
     * @param sessionInfo Session info to remove.
     */
    private void removeSessionInfo(SessionInfo sessionInfo) {
        if (sessionInfo != null) {
            sessionInfo.destroy();
            
            synchronized (activeSessions) {
                activeSessions.remove(sessionInfo.getNativeSession().getId());
            }
        }
    }
    
    /**
     * Register or unregister a desktop. This keeps count of all active desktops for each session.
     * When a session's active desktop count reaches 0, the session is invalidated.
     * 
     * @param desktop Desktop to register/unregister.
     * @param doRegister If true, the desktop is registered and the associated session's active
     *            desktop count is incremented by one. If false, the desktop is unregistered and the
     *            associated session's active desktop count is decremented by one and, if the count
     *            has reached zero, the session is invalidated.
     */
    public void register(Desktop desktop, boolean doRegister) {
        if (doRegister) {
            addDesktop(desktop);
            
            if (isManaged(desktop)) {
                AppContextFinder.createAppContext(desktop);
                desktop.setAttribute(ATTR_SERVER_PUSH, true);
            }
            
        } else {
            if (FrameworkAppContext.getAppContext(desktop) != null) {
                AppContextFinder.destroyAppContext(desktop);
            }
            
            removeDesktop(desktop);
        }
    }
    
    public String getMasterId(String slaveId) {
        Desktop slave = getDesktop(slaveId);
        DesktopInfo dto = slave == null ? null : getDesktopInfo(slave);
        return dto == null ? null : dto.getOwner();
    }
    
    /**
     * Returns true if we are managing the lifecycle of the specified desktop.
     * 
     * @param desktop Desktop instance.
     * @return True if this is a managed desktop.
     */
    private boolean isManaged(Desktop desktop) {
        HttpServletRequest request = (HttpServletRequest) desktop.getExecution().getNativeRequest();
        String url = request.getRequestURI();
        return (url != null) && !url.contains("/zkau/");
    }
    
    /**
     * Adds a desktop to the list of active desktops (under the SessionInfo object for the
     * associated session).
     * 
     * @param desktop Desktop to add.
     */
    private void addDesktop(Desktop desktop) {
        SessionInfo sessionInfo = getSessionInfo(desktop);
        
        if (sessionInfo != null) {
            sessionInfo.addDesktop(desktop);
        }
        log.debug(sessionInfo);
    }
    
    /**
     * Removes a desktop from the list of active desktops.
     * 
     * @param desktop Desktop to remove.
     */
    private void removeDesktop(Desktop desktop) {
        SessionInfo sessionInfo = getSessionInfo(desktop);
        
        if (sessionInfo != null) {
            sessionInfo.removeDesktop(desktop);
        }
        log.debug(sessionInfo);
    }
    
    /**
     * Returns the desktop instance from the list of registered desktops.
     * 
     * @param id Desktop id
     * @return Corresponding desktop instance, or null if not found.
     */
    private Desktop getDesktop(String id) {
        if (id == null) {
            return null;
        }
        
        synchronized (activeDesktops) {
            return activeDesktops.get(id);
        }
    }
    
    /**
     * Returns the count of active desktops registered to the specified session.
     * 
     * @param session Session whose desktop count is sought.
     * @return The number of active desktops for this session, or 0 if the session is not known.
     */
    public int getDesktopCount(HttpSession session) {
        SessionInfo sessionInfo = getSessionInfo(session);
        return sessionInfo == null ? 0 : sessionInfo.desktops.size();
    }
    
    /**
     * Returns the SessionInfo object appropriate for the specified desktop (based on the session
     * associated with the desktop).
     * 
     * @param desktop Desktop whose associated SessionInfo is sought.
     * @return A SessionInfo instance, or null if one was not found.
     */
    public SessionInfo getSessionInfo(Desktop desktop) {
        return getSessionInfo(desktop.getSession());
    }
    
    /**
     * Returns the SessionInfo object appropriate for the specified desktop (based on the session
     * associated with the desktop).
     * 
     * @param session Session whose associated SessionInfo is sought.
     * @return A SessionInfo instance, or null if one was not found.
     */
    public SessionInfo getSessionInfo(Session session) {
        return session == null ? null : getSessionInfo((HttpSession) session.getNativeSession());
    }
    
    /**
     * Returns the SessionInfo object appropriate for the specified desktop (based on the session
     * associated with the desktop).
     * 
     * @param session Session whose associated SessionInfo is sought.
     * @return A SessionInfo instance, or null if one was not found.
     */
    public SessionInfo getSessionInfo(HttpSession session) {
        return session == null ? null : this.activeSessions.get(session.getId());
    }
}
