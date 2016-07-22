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

import java.util.List;

import org.carewebframework.ui.thread.ThreadListenerRegistry;
import org.carewebframework.web.component.BaseComponent;
import org.carewebframework.web.event.Event;

/**
 * ZK EventThreads suspend the servlet request thread. This class is meant to transfer any necessary
 * state from the request thread to the event thread.
 */
public class EventThreadListener implements EventThreadInit, EventThreadCleanup {
    
    /**
     * @see org.zkoss.zk.ui.event.EventThreadInit#init(org.zkoss.zk.ui.BaseComponent,
     *      org.zkoss.zk.ui.event.Event)
     */
    @Override
    public boolean init(BaseComponent comp, Event event) throws Exception {
        ThreadListenerRegistry.notifyListeners(true);
        return true;
    }
    
    /**
     * @see org.zkoss.zk.ui.event.EventThreadCleanup#cleanup(org.zkoss.zk.ui.BaseComponent,
     *      org.zkoss.zk.ui.event.Event, java.util.List)
     */
    @Override
    public void cleanup(BaseComponent comp, Event evt, @SuppressWarnings("rawtypes") List errs) {
        ThreadListenerRegistry.notifyListeners(false);
    }
    
    /**
     * @see org.zkoss.zk.ui.event.EventThreadCleanup#complete(org.zkoss.zk.ui.BaseComponent,
     *      org.zkoss.zk.ui.event.Event)
     */
    @Override
    public void complete(BaseComponent comp, Event evt) throws Exception {
    }
    
    /**
     * @see org.zkoss.zk.ui.event.EventThreadInit#prepare(org.zkoss.zk.ui.BaseComponent,
     *      org.zkoss.zk.ui.event.Event)
     */
    @Override
    public void prepare(BaseComponent comp, Event event) throws Exception {
    }
    
}
