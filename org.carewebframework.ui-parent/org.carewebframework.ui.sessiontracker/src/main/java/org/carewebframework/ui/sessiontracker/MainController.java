/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.ui.sessiontracker;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.carewebframework.api.context.UserContext;
import org.carewebframework.api.domain.IUser;
import org.carewebframework.common.StrUtil;
import org.carewebframework.shell.plugins.PluginContainer;
import org.carewebframework.shell.plugins.PluginController;
import org.carewebframework.ui.Application;
import org.carewebframework.ui.Application.SessionInfo;

import org.zkoss.zk.ui.event.Event;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.RowRenderer;

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
    
    private Grid grid;
    
    private void doDelegationToModel() {
        log.trace("Delegating work to model");
        IUser user = UserContext.getActiveUser();
        showMessage(null);
        
        if (user != null) {
            log.trace("Establishing ListModelList for Grid");
            //TODO: service to getActiveSessions and return DTO.  To store a serializable listModel
            List<SessionInfo> sessions = Application.getInstance().getActiveSessions();
            
            if (!sessions.isEmpty()) {
                grid.setModel(new ListModelList<SessionInfo>(sessions));
                grid.setRowRenderer(sessionTrackerRowRenderer);
                lblSessionSummary.setVisible(true);
                int size = sessions.size();
                lblSessionSummary.setValue(StrUtil.formatMessage("@cwf.sessiontracker.msg.sessions.total", size));
                
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
            lblMessage.setValue(StrUtil.formatMessage(message, params));
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
