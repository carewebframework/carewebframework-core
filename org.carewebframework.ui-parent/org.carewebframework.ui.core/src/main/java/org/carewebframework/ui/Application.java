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
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.carewebframework.api.FrameworkRuntimeException;
import org.carewebframework.common.DateUtil;
import org.carewebframework.common.DateUtil.ITimeZoneAccessor;
import org.carewebframework.common.StrUtil;
import org.carewebframework.ui.action.ActionRegistry;
import org.carewebframework.web.client.ClientUtil;
import org.carewebframework.web.component.BaseComponent;
import org.carewebframework.web.component.BaseUIComponent;
import org.carewebframework.web.component.Label;
import org.carewebframework.web.component.Page;
import org.carewebframework.web.core.ExecutionContext;
import org.carewebframework.web.event.Event;
import org.carewebframework.web.event.IEventListener;
import org.carewebframework.web.spring.AppContextFinder;
import org.carewebframework.web.spring.FrameworkAppContext;

/**
 * Application singleton used to track active sessions.
 */
public class Application {
    
    private static final Log log = LogFactory.getLog(Application.class);
    
    private static final Application instance = new Application();
    
    private static final String ATTR_SERVER_PUSH = "@requires-server-push";
    
    private static final String ID_LOCK_MESSAGE = "_lock_message";
    
    public enum Command {
        CLOSE, LOCK, UNLOCK
    };
    
    private static final IEventListener pageCommandListener = new IEventListener() {
        
        @Override
        public void onEvent(Event event) {
            switch ((Command) event.getData()) {
                case CLOSE:
                    ClientUtil.invoke("window.close");
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
            Page page = ExecutionContext.getPage();
            BaseUIComponent lbl = null;
            
            for (BaseComponent root : page.getChildren()) {
                if (root instanceof BaseUIComponent) {
                    BaseUIComponent cmp = (BaseUIComponent) root;
                    
                    if (lbl == null && ID_LOCK_MESSAGE.equals(root.getId())) {
                        lbl = cmp;
                        lbl.setVisible(lock);
                    } else {
                        cmp.setVisible(!lock);
                    }
                }
            }
            if (lock && lbl == null) {
                Label label = new Label(StrUtil.formatMessage("@cwf.timeout.lock.spawned.message"));
                label.addClass("cwf-timeout-lock-spawned");
                label.setName(ID_LOCK_MESSAGE);
                label.setParent(page);
            }
        }
        
    };
    
    /**
     * Tracks information about a session.
     */
    public class SessionInfo {
        
        private final List<Page> pages = new ArrayList<>();
        
        private final HttpSession session;
        
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
        private SessionInfo(HttpSession session) {
            this.session = session;
            maxInactiveInterval = session.getMaxInactiveInterval();
        }
        
        /**
         * Adds a page to this session information.
         * 
         * @param page The page to add.
         */
        private void addPage(Page page) {
            synchronized (activePages) {
                page.getSession().setMaxInactiveInterval(maxInactiveInterval);
                activePages.put(page.getId(), page);
                this.pages.add(page);
                PageInfo pageInfo = new PageInfo(page);
                page.setAttribute(PageInfo.class.getName(), pageInfo);
                page.addListener(pageInfo);
                page.addListener(uiLifeCycle);
                
                if (remoteAddress == null) {
                    Execution exec = page.getExecution();
                    remoteAddress = exec.getRemoteAddr();
                    localAddress = exec.getLocalAddr();
                    localName = exec.getLocalName();
                    remoteHost = exec.getRemoteHost();
                    serverName = exec.getServerName();
                }
            }
        }
        
        /**
         * Removes a page from this session information.
         * 
         * @param page The page to remove.
         */
        private void removePage(Page page) {
            synchronized (activePages) {
                activePages.remove(page.getId());
                
                if (this.pages.remove(page)) {
                    PageInfo pageInfo = getPageInfo(page);
                    
                    if (pageInfo != null) {
                        pageInfo.destroy();
                    }
                    
                    if (pages.isEmpty()) {
                        session.setMaxInactiveInterval(30);
                        log.debug("HttpSession marked for invalidation: " + session);
                    }
                }
            }
        }
        
        /**
         * Called when session info object is to be destroyed.
         */
        private void destroy() {
            for (Page page : getPages()) {
                removePage(page);
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
        public HttpSession getSession() {
            return this.session;
        }
        
        /**
         * Returns a list of active pages associated with this session. We return a copy of the page
         * list, to avoid concurrency issues.
         * 
         * @return List of Pages
         */
        public synchronized List<Page> getPages() {
            return new ArrayList<>(this.pages);
        }
        
        /**
         * String representation of <code>Application</code>
         * 
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            StringBuffer buffer = new StringBuffer();
            
            if (session != null) {
                buffer.append("\nSessionInfo");
                HttpSession httpSession = getSession();
                
                if (httpSession != null) {//precaution, shouldn't ever be null
                    buffer.append("\n\tSession ID: ").append(httpSession.getId());
                    buffer.append("\n\tCreationTime: ").append(String.valueOf(new Date(httpSession.getCreationTime())));
                    buffer.append("\n\tLastAccessedTime: ")
                            .append(String.valueOf(new Date(httpSession.getLastAccessedTime())));
                }
                
                int maxInactiveInterval = session.getMaxInactiveInterval();
                Enumeration<String> attrNames = session.getAttributeNames();
                
                if (attrNames != null) {
                    while (attrNames.hasMoreElements()) {
                        String key = attrNames.nextElement();
                        buffer.append("\n\tSession Attribute Key=").append(key);
                        Object attribute = session.getAttribute(key);
                        buffer.append(", Value=").append(attribute);
                        buffer.append(", HttpSession Attribute Class=")
                                .append(attribute == null ? null : attribute.getClass());
                    }
                }
                
                buffer.append("\n\tSession(").append(localName).append("):MaxInactiveInterval=").append(maxInactiveInterval)
                        .append(", LocalAddresss=").append(localAddress).append(", RemotedAddress=").append(remoteAddress)
                        .append(", RemoteHost=").append(remoteHost).append(", ServerName=").append(serverName);
                
                for (Page page : getPages()) {
                    PageInfo pageInfo = Application.getPageInfo(page);
                    buffer.append(pageInfo);
                }
            }
            return buffer.toString();
        }
        
    }
    
    /**
     * Tracks information about a page that is only available during an active execution.
     */
    public class PageInfo implements AuService {
        
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
         * Copies information from the page's execution so that it is available to monitoring
         * applications. Also, captures information from the onClientInfo event when sent to the
         * page.
         * <p>
         * Note: Do not keep a reference to the page here.
         * 
         * @param page The page.
         */
        private PageInfo(Page page) {
            Execution exec = page.getExecution();
            
            if (exec == null) {
                throw new FrameworkRuntimeException(EXC_ILLEGAL_STATE, null, PageInfo.this.toString());
            }
            userAgent = exec.getUserAgent();
            remoteAddress = exec.getRemoteAddr();
            remoteHost = exec.getRemoteHost();
            remoteUser = exec.getRemoteUser();
            isExplorer = exec.getBrowser("ie") != null;
            isGecko = exec.getBrowser("gecko") != null;
            id = page.getId();
            
            if (isManaged(page)) {
                owner = null;
            } else {
                String qs = page.getQueryString();
                owner = qs == null ? null : FrameworkWebSupport.queryStringToMap(qs).get("owner");
                
                if (registerWithOwner(true)) {
                    page.setAttribute(ATTR_SERVER_PUSH, true);
                }
            }
        }
        
        public void destroy() {
            Page dtp = getPage(id);
            
            if (dtp != null) {
                dtp.removeListener(this);
            }
            registerWithOwner(false);
            sendToSpawned(Command.CLOSE);
        }
        
        public void sendToSpawned(Command command) {
            if (spawned != null && !spawned.isEmpty()) {
                for (String dtid : new ArrayList<>(spawned)) {
                    Page dtp = getPage(dtid);
                    
                    if (dtp != null) {
                        try {
                            Executions.schedule(dtp, pageCommandListener, new Event("ON_COMMAND", null, command));
                        } catch (Exception e) {
                            log.error("Error sending command to spawned page.", e);
                        }
                    }
                }
            }
            
        }
        
        private boolean registerWithOwner(boolean register) {
            if (owner != null && !owner.equals(id)) {
                Page dtp = getPage(owner);
                PageInfo dto = dtp == null ? null : getPageInfo(dtp);
                
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
         * Registers a page spawned from this one.
         * 
         * @param id The id of the spawned page to register.
         */
        private void registerSpawned(String id) {
            if (spawned == null) {
                spawned = new ArrayList<>();
            }
            
            spawned.add(id);
        }
        
        /**
         * Unregisters a page spawned from this one.
         * 
         * @param id The id of the spawned page to unregister.
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
            Page page = getPage(id);
            buffer.append("\n\t\tPageInfo");
            buffer.append("\n\t\t\tPage: ").append(page);//includes Id
            buffer.append("\n\t\t\tOwner: ").append(owner == null ? "none" : owner);
            buffer.append("\n\t\t\tisAlive: ").append(page != null && !page.isDead());
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
                request.getPage().removeListener(this);
            } else if (!this.infoRequested) {
                this.infoRequested = true;
                request.getPage().getExecution().addAuResponse(new AuClientInfo(request.getPage()));
            }
            return false;
        }
    }
    
    private final ITimeZoneAccessor localTimeZone = new ITimeZoneAccessor() {
        
        @Override
        public TimeZone getTimeZone() {
            TimeZone tz = Application.getTimeZone(ExecutionContext.getPage());
            return tz == null ? TimeZone.getDefault() : tz;
        }
        
        @Override
        public void setTimeZone(TimeZone timezone) {
            throw new UnsupportedOperationException();
        }
        
    };
    
    private final Map<String, SessionInfo> activeSessions = new ConcurrentHashMap<>();
    
    private final Map<String, Page> activePages = new ConcurrentHashMap<>();
    
    /**
     * Returns the singleton instance of the Application object.
     * 
     * @return Application instance
     */
    public static Application getInstance() {
        return instance;
    }
    
    /**
     * Returns the page info instance associated with the specified page.
     * 
     * @param page Page whose information is requested.
     * @return The PageInfo instance for the specified page.
     */
    public static PageInfo getPageInfo(Page page) {
        return (PageInfo) page.getAttribute(PageInfo.class.getName());
    }
    
    /**
     * Return the time zone set for the specified page.
     * 
     * @param page The page.
     * @return The time zone.
     */
    public static TimeZone getTimeZone(Page page) {
        PageInfo dti = page == null ? null : getPageInfo(page);
        return dti == null || dti.clientInformation == null ? null : dti.clientInformation.getTimeZone();
    }
    
    /**
     * Set time zone resolver and add lifecycle callbacks.
     */
    private Application() {
        super();
        DateUtil.localTimeZone = localTimeZone;
        Fileupload.setTemplate("~./org/carewebframework/ui/zk/fileuploaddlg.zul");
        LifecycleEventDispatcher.addPageCallback(pageLifeCycle);
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
     * @param session HttpSession to add.
     */
    private void addSession(HttpSession session) {
        synchronized (activeSessions) {
            String id = session.getId();
            
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
     * @param session HttpSession to remove.
     */
    private void removeSession(HttpSession session) {
        removeSessionInfo(getSessionInfo(session));
    }
    
    /**
     * Removes a session from the list of active sessions.
     * 
     * @param sessionInfo HttpSession info to remove.
     */
    private void removeSessionInfo(SessionInfo sessionInfo) {
        if (sessionInfo != null) {
            sessionInfo.destroy();
            
            synchronized (activeSessions) {
                activeSessions.remove(sessionInfo.getSession().getId());
            }
        }
    }
    
    /**
     * Register or unregister a page. This keeps count of all active pages for each session. When a
     * session's active page count reaches 0, the session is invalidated.
     * 
     * @param page Page to register/unregister.
     * @param doRegister If true, the page is registered and the associated session's active page
     *            count is incremented by one. If false, the page is unregistered and the associated
     *            session's active page count is decremented by one and, if the count has reached
     *            zero, the session is invalidated.
     */
    public void register(Page page, boolean doRegister) {
        if (doRegister) {
            addPage(page);
            
            if (isManaged(page)) {
                AppContextFinder.createAppContext(page);
                page.setAttribute(ATTR_SERVER_PUSH, true);
            }
            
        } else {
            if (FrameworkAppContext.getAppContext(page) != null) {
                AppContextFinder.destroyAppContext(page);
            }
            
            removePage(page);
        }
    }
    
    public String getMasterId(String slaveId) {
        Page slave = getPage(slaveId);
        PageInfo dto = slave == null ? null : getPageInfo(slave);
        return dto == null ? null : dto.getOwner();
    }
    
    /**
     * Adds a page to the list of active pages (under the SessionInfo object for the associated
     * session).
     * 
     * @param page Page to add.
     */
    private void addPage(Page page) {
        SessionInfo sessionInfo = getSessionInfo(page);
        
        if (sessionInfo != null) {
            sessionInfo.addPage(page);
        }
        log.debug(sessionInfo);
    }
    
    /**
     * Removes a page from the list of active pages.
     * 
     * @param page Page to remove.
     */
    private void removePage(Page page) {
        SessionInfo sessionInfo = getSessionInfo(page);
        
        if (sessionInfo != null) {
            sessionInfo.removePage(page);
        }
        log.debug(sessionInfo);
    }
    
    /**
     * Returns the page instance from the list of registered pages.
     * 
     * @param id Page id
     * @return Corresponding page instance, or null if not found.
     */
    private Page getPage(String id) {
        if (id == null) {
            return null;
        }
        
        synchronized (activePages) {
            return activePages.get(id);
        }
    }
    
    /**
     * Returns the count of active pages registered to the specified session.
     * 
     * @param session HttpSession whose page count is sought.
     * @return The number of active pages for this session, or 0 if the session is not known.
     */
    public int getPageCount(HttpSession session) {
        SessionInfo sessionInfo = getSessionInfo(session);
        return sessionInfo == null ? 0 : sessionInfo.pages.size();
    }
    
    /**
     * Returns the SessionInfo object appropriate for the specified page (based on the session
     * associated with the page).
     * 
     * @param page Page whose associated SessionInfo is sought.
     * @return A SessionInfo instance, or null if one was not found.
     */
    public SessionInfo getSessionInfo(Page page) {
        return getSessionInfo(page.getSession());
    }
    
    /**
     * Returns the SessionInfo object appropriate for the specified page (based on the session
     * associated with the page).
     * 
     * @param session HttpSession whose associated SessionInfo is sought.
     * @return A SessionInfo instance, or null if one was not found.
     */
    public SessionInfo getSessionInfo(HttpSession session) {
        return session == null ? null : this.activeSessions.get(session.getId());
    }
}
