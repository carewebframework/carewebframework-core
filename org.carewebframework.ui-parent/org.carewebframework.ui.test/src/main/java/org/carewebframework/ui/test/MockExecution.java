/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.ui.test;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.zkoss.zk.au.AuResponse;
import org.zkoss.zk.au.out.AuEcho;
import org.zkoss.zk.ui.Desktop;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.http.ExecutionImpl;

/**
 * Overrides ExecutionImpl to provide access to echoed events.
 */
public class MockExecution extends ExecutionImpl {
    
    private final List<AuEcho> echoedEvents = new ArrayList<>();
    
    public MockExecution(ServletContext ctx, HttpServletRequest request, HttpServletResponse response, Desktop desktop,
        Page creating) {
        super(ctx, request, response, desktop, creating);
    }
    
    @Override
    public void addAuResponse(AuResponse response) {
        super.addAuResponse(response);
        
        if (response instanceof AuEcho) {
            echoedEvents.add((AuEcho) response);
        }
    }
    
    protected List<AuEcho> getEchoedEvents() {
        return echoedEvents;
    }
}
