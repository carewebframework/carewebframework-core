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
package org.carewebframework.plugin.sessiontracker;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.fujion.common.StrUtil;
import org.carewebframework.shell.plugins.PluginController;
import org.fujion.annotation.EventHandler;
import org.fujion.annotation.WiredComponent;
import org.fujion.component.Checkbox;
import org.fujion.component.Grid;
import org.fujion.component.Row;
import org.fujion.component.Rows;
import org.fujion.event.Event;
import org.fujion.event.EventUtil;
import org.fujion.model.IComponentRenderer;
import org.fujion.model.ListModel;
import org.fujion.websocket.ISessionLifecycle;
import org.fujion.websocket.Session;
import org.fujion.websocket.Sessions;

/**
 * Controller class for session tracker.
 */
public class MainController extends PluginController {

    private static final Log log = LogFactory.getLog(MainController.class);

    private boolean needsRefresh = true;

    private final Sessions sessions = Sessions.getInstance();

    private IComponentRenderer<Row, Session> sessionRenderer;
    
    private final ListModel<Session> model = new ListModel<>();

    private final ISessionLifecycle sessionTracker = new ISessionLifecycle() {
        
        @Override
        public void onSessionCreate(Session session) {
            fireEvent("sessionCreate", session);
        }
        
        @Override
        public void onSessionDestroy(Session session) {
            fireEvent("sessionDestroy", session);
        }

        private void fireEvent(String type, Session session) {
            Event event = new Event(type, root, session);
            EventUtil.post(event);
        }

    };

    @WiredComponent
    private Grid grid;

    @WiredComponent
    private Checkbox chkAutoRefresh;

    @Override
    public void refresh() {
        needsRefresh = false;
        Rows rows = grid.getRows();
        rows.setRenderer(sessionRenderer);
        model.clear();
        model.addAll(sessions.getActiveSessions());
        rows.setModel(model);
        updateCount();
    }

    private void updateCount() {
        grid.setTitle(StrUtil.formatMessage("@cwf.sessiontracker.msg.sessions.total", model.size()));
    }
    
    @EventHandler(value = "sessionCreate")
    @EventHandler(value = "sessionDestroy")
    private void onSessionUpdate(Event event) {
        Session session = (Session) event.getData();

        if ("sessionCreate".equals(event.getType())) {
            model.add(session);
        } else {
            model.remove(session);
        }
        
        updateCount();
    }

    /**
     * Event handler for refreshing session list
     *
     * @param event Event
     */
    @EventHandler(value = "click", target = "btnRefreshSessionView")
    private void onClick$btnRefreshSessionView(Event event) {
        log.trace("Refreshing active Session/Desktop view");
        refresh();
    }

    @Override
    public void onUnload() {
        super.onUnload();
        enableAutoRefresh(false);
    }

    @EventHandler(value = "change", target = "chkAutoRefresh")
    private void onCheck$chkAutoRefresh() {
        enableAutoRefresh(chkAutoRefresh.isChecked());
    }

    private void enableAutoRefresh(boolean enable) {
        if (enable) {
            sessions.addLifecycleListener(sessionTracker);
            refresh();
        } else {
            sessions.removeLifecycleListener(sessionTracker);
        }
    }
    
    @Override
    public void onActivate() {
        super.onActivate();

        if (needsRefresh) {
            refresh();
        }
    }

    /**
     * Setter for session renderer
     *
     * @param sessionRenderer The session renderer.
     */
    public void setSessionRenderer(IComponentRenderer<Row, Session> sessionRenderer) {
        this.sessionRenderer = sessionRenderer;
    }

}
