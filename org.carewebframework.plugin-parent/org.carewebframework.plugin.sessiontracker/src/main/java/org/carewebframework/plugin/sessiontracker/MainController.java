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

import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.carewebframework.api.context.UserContext;
import org.carewebframework.api.domain.IUser;
import org.carewebframework.common.StrUtil;
import org.carewebframework.shell.elements.UIElementPlugin;
import org.carewebframework.shell.plugins.PluginController;
import org.carewebframework.web.annotation.EventHandler;
import org.carewebframework.web.annotation.WiredComponent;
import org.carewebframework.web.client.Session;
import org.carewebframework.web.client.WebSocketHandler;
import org.carewebframework.web.component.Grid;
import org.carewebframework.web.component.Label;
import org.carewebframework.web.component.Page;
import org.carewebframework.web.component.Row;
import org.carewebframework.web.event.Event;
import org.carewebframework.web.model.IComponentRenderer;
import org.carewebframework.web.model.ListModel;
import org.carewebframework.web.model.ModelAndView;
import org.carewebframework.web.page.PageRegistry;

/**
 * Controller class for session tracker.
 */
public class MainController extends PluginController {
    
    private static final Log log = LogFactory.getLog(MainController.class);
    
    //members
    private boolean isDelegationToModelDeferred;
    
    private IComponentRenderer<Row, Session> sessionTrackerRowRenderer;
    
    @WiredComponent
    private Label lblSessionSummary;
    
    @WiredComponent
    private Label lblMessage;
    
    @WiredComponent
    private Grid grid;
    
    private void doDelegationToModel() {
        IUser user = UserContext.getActiveUser();
        showMessage(null);
        
        if (user != null) {
            log.trace("Establishing ListModelList for Grid");
            
            Collection<Page> pages = PageRegistry.getPages();
            
            if (!pages.isEmpty()) {
                ListModel<Session> model = new ListModel<>(WebSocketHandler.getActiveSessions());
                new ModelAndView<Row, Session>(grid.getRows(), model, sessionTrackerRowRenderer);
                lblSessionSummary.setVisible(true);
                int size = pages.size();
                lblSessionSummary.setLabel(StrUtil.formatMessage("@cwf.sessiontracker.msg.sessions.total", size));
                
            } else { //shouldn't happen
                String message = StrUtil.formatMessage("@cwf.sessiontracker.msg.session.none");
                log.trace(message);
                showMessage(message);
            }
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
        doDelegationToModel();
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
    public void onLoad(UIElementPlugin plugin) {
        log.trace("onLoad");
        super.onLoad(plugin);
        isDelegationToModelDeferred = true;// onLoad happens prior to activation, defer until activated
    }
    
    @Override
    public void onActivate() {
        log.trace("Plugin Activated");
        super.onActivate();
        
        if (isDelegationToModelDeferred) {
            doDelegationToModel();
            isDelegationToModelDeferred = false;
        }
    }
    
    /**
     * Setter for RowRenderer
     * 
     * @param sessionTrackerRowRenderer RowRenderer
     */
    public void setSessionTrackerRowRenderer(IComponentRenderer<Row, Session> sessionTrackerRowRenderer) {
        this.sessionTrackerRowRenderer = sessionTrackerRowRenderer;
    }
    
}
