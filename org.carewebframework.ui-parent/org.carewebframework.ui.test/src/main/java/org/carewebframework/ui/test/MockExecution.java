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
