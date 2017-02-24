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
import org.carewebframework.common.StrUtil;
import org.carewebframework.shell.plugins.PluginController;
import org.carewebframework.web.annotation.EventHandler;
import org.carewebframework.web.annotation.WiredComponent;
import org.carewebframework.web.client.ISessionTracker;
import org.carewebframework.web.client.Session;
import org.carewebframework.web.client.WebSocketHandler;
import org.carewebframework.web.component.Checkbox;
import org.carewebframework.web.component.Grid;
import org.carewebframework.web.component.Label;
import org.carewebframework.web.component.Row;
import org.carewebframework.web.component.Rows;
import org.carewebframework.web.event.Event;
import org.carewebframework.web.event.EventUtil;
import org.carewebframework.web.model.IComponentRenderer;
import org.carewebframework.web.model.ListModel;

/**
 * Controller class for session tracker.
 */
public class MainController extends PluginController {
    
    private static final Log log = LogFactory.getLog(MainController.class);
    
    private boolean needsRefresh = true;
    
    private IComponentRenderer<Row, Session> sessionRenderer;

    private final ListModel<Session> model = new ListModel<>();
    
    private final ISessionTracker sessionTracker = new ISessionTracker() {

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
    private Label lblSessionSummary;
    
    @WiredComponent
    private Label lblMessage;
    
    @WiredComponent
    private Grid grid;
    
    @WiredComponent
    private Checkbox chkAutoRefresh;
    
    @Override
    public void refresh() {
        needsRefresh = false;
        showMessage(null);
        Rows rows = grid.getRows();
        rows.setRenderer(sessionRenderer);
        model.clear();
        model.addAll(WebSocketHandler.getActiveSessions());
        rows.setModel(model);
        lblSessionSummary.setLabel(StrUtil.formatMessage("@cwf.sessiontracker.msg.sessions.total", model.size()));
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
            WebSocketHandler.registerSessionTracker(sessionTracker);
            refresh();
        } else {
            WebSocketHandler.unregisterSessionTracker(sessionTracker);
        }
    }

    /**
     * Displays message to client
     *
     * @param message Message to display to client.
     * @param params Message parameters.
     */
    private void showMessage(String message, Object... params) {
        if (message == null) {
            lblMessage.setVisible(false);
        } else {
            lblMessage.setVisible(true);
            lblMessage.setLabel(StrUtil.formatMessage(message, params));
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
