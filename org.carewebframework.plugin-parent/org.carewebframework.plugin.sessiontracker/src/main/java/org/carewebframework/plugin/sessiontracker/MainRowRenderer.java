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

import java.util.Date;

import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.carewebframework.api.context.UserContext;
import org.carewebframework.api.domain.IUser;
import org.carewebframework.common.StrUtil;
import org.carewebframework.ui.Application;
import org.carewebframework.ui.Application.DesktopInfo;
import org.carewebframework.ui.Application.SessionInfo;
import org.carewebframework.ui.cwf.AbstractRowRenderer;
import org.carewebframework.ui.spring.FrameworkAppContext;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Desktop;
import org.zkoss.zk.ui.event.ClientInfoEvent;
import org.zkoss.zul.Detail;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Row;
import org.zkoss.zul.Rows;

/**
 * RowRenderer to define rows within the Session/Desktop Tracking Grid
 */
public class MainRowRenderer extends AbstractRowRenderer<SessionInfo, Object> {
    
    private static final String[] DETAIL_COL_WIDTHS = { "12%", "10%", "10%", "38%", "15%", "15%" };
    
    private static final String[] DETAIL_COL_LABELS = { "@cwf.sessiontracker.detail.col1.label",
            "@cwf.sessiontracker.detail.col2.label", "@cwf.sessiontracker.detail.col3.label",
            "@cwf.sessiontracker.detail.col4.label", "@cwf.sessiontracker.detail.col5.label",
            "@cwf.sessiontracker.detail.col6.label" };
            
    private static final Log log = LogFactory.getLog(MainRowRenderer.class);
    
    /**
     * @see AbstractRowRenderer#renderRow
     */
    @Override
    protected Component renderRow(Row row, SessionInfo sInfo) {
        HttpSession nativeSession = sInfo.getNativeSession();
        //Because it's possible that the session could be invalidated but yet still in the list
        String sessionId = null;
        String institution = StrUtil.formatMessage("@cwf.sessiontracker.msg.unknown");
        Date creationTime = null;
        Date lastAccessedTime = null;
        int maxInactiveInterval = 0;
        String clientAddress = sInfo.getRemoteAddress();
        
        try {
            if (nativeSession != null) {
                sessionId = nativeSession.getId();
                creationTime = new Date(nativeSession.getCreationTime());
                lastAccessedTime = new Date(nativeSession.getLastAccessedTime());
                maxInactiveInterval = nativeSession.getMaxInactiveInterval();
            }
        } catch (IllegalStateException e) {
            log.warn(
                "The following session was still in the list of activeSessions yet was invalidated: " + sInfo.getSession());
            return null;
        }
        
        createCell(row, sessionId);
        createCell(row, clientAddress);
        createCell(row, institution);
        createCell(row, creationTime);
        createCell(row, lastAccessedTime);
        createCell(row, String.valueOf(maxInactiveInterval));
        return sInfo.getDesktops().isEmpty() ? null : row;
    }
    
    @Override
    protected void renderDetail(Detail detail, SessionInfo sInfo) {
        detail.setOpen(true);
        Grid detailGrid = createDetailGrid(detail, DETAIL_COL_WIDTHS, DETAIL_COL_LABELS);
        Rows detailRows = detailGrid.getRows();
        
        for (Desktop desktop : sInfo.getDesktops()) {
            DesktopInfo desktopInfo = Application.getDesktopInfo(desktop);
            ClientInfoEvent clientInfo = desktopInfo == null ? null : desktopInfo.getClientInformation();
            String screenDimensions = clientInfo == null ? ""
                    : (clientInfo.getScreenWidth() + "x" + clientInfo.getScreenHeight());
            IUser user = getUser(desktop);
            String usr = user == null ? StrUtil.formatMessage("@cwf.sessiontracker.msg.unknown") : (user.toString());
            Row detailRow = new Row();
            detailRow.setParent(detailRows);
            createCell(detailRow, desktop.getId());
            createCell(detailRow, desktop.getDeviceType());
            createCell(detailRow, usr);
            createCell(detailRow, desktopInfo == null ? "" : desktopInfo.getUserAgent());
            createCell(detailRow, clientInfo == null ? "" : clientInfo.getTimeZone().getDisplayName());
            createCell(detailRow, screenDimensions);
        }
    }
    
    private IUser getUser(Desktop desktop) {
        try {
            FrameworkAppContext ctx = FrameworkAppContext.getAppContext(desktop);
            UserContext uctx = ctx == null ? null : ctx.getBean("userContext", UserContext.class);
            return uctx == null ? null : uctx.getContextObject(false);
        } catch (Exception e) {
            return null;
        }
    }
    
}
