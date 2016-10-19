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

import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
     * Return the time zone set for the specified page.
     * 
     * @param page The page.
     * @return The time zone.
     */
    public static TimeZone getTimeZone(Page page) {
        int tzoffset = page.getBrowserInfo("timezoneOffset", Integer.class) * 60000;
        String[] ids = TimeZone.getAvailableIDs(tzoffset);
        return ids.length > 0 ? TimeZone.getTimeZone(ids[0]) : null;
    }
    
    /**
     * Set time zone resolver and add lifecycle callbacks.
     */
    private Application() {
        super();
        DateUtil.localTimeZone = localTimeZone;
        ActionRegistry.register(true, "cwf.refresh", "@cwf.btn.refresh.label",
            "zscript:org.carewebframework.api.event.EventManager.getInstance().fireLocalEvent(\"" + Constants.REFRESH_EVENT
                    + "\", null);");
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
            
            if (isManaged(page)) {
                AppContextFinder.createAppContext(page);
                page.setAttribute(ATTR_SERVER_PUSH, true);
            }
            
        } else {
            if (FrameworkAppContext.getAppContext(page) != null) {
                AppContextFinder.destroyAppContext(page);
            }
        }
    }
    
    public String getMasterId(String slaveId) {
        Page slave = getPage(slaveId);
        PageInfo dto = slave == null ? null : getPageInfo(slave);
        return dto == null ? null : dto.getOwner();
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
    
}
