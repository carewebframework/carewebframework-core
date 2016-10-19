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
import org.carewebframework.shell.plugins.PluginContainer;
import org.carewebframework.shell.plugins.PluginController;
import org.carewebframework.web.component.Label;
import org.carewebframework.web.component.Page;
import org.carewebframework.web.component.Table;
import org.carewebframework.web.page.PageRegistry;

/**
 * Controller class for session tracker.
 */
public class MainController extends PluginController {
    
    private static final long serialVersionUID = 1L;
    
    private static final Log log = LogFactory.getLog(MainController.class);
    
    //members
    private boolean isDelegationToModelDeferred;
    
    private RowRenderer<SessionInfo> sessionTrackerRowRenderer;
    
    private Label lblSessionSummary;
    
    private Label lblMessage;
    
    private Table grid;
    
    private void doDelegationToModel() {
        IUser user = UserContext.getActiveUser();
        showMessage(null);
        
        if (user != null) {
            log.trace("Establishing ListModelList for Grid");
            
            Collection<Page> pages = PageRegistry.getPages();
            
            if (!pages.isEmpty()) {
                grid.setModel(new ListModelList<>(sessions));
                grid.setRowRenderer(sessionTrackerRowRenderer);
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
    public void onClick$btnRefreshSessionView(Event event) {
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
    
    /**
     * @see org.carewebframework.shell.plugins.IPluginEvent#onLoad(org.carewebframework.shell.plugins.PluginContainer)
     */
    @Override
    public void onLoad(PluginContainer container) {
        log.trace("onLoad");
        super.onLoad(container);
        isDelegationToModelDeferred = true;// onLoad happens prior to activation, defer until activated
    }
    
    /**
     * @see org.carewebframework.shell.plugins.IPluginEvent#onActivate()
     */
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
    public void setSessionTrackerRowRenderer(RowRenderer<SessionInfo> sessionTrackerRowRenderer) {
        this.sessionTrackerRowRenderer = sessionTrackerRowRenderer;
    }
    
}
