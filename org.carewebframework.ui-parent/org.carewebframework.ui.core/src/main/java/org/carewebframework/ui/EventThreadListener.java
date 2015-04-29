/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.ui;

import java.util.List;

import org.carewebframework.ui.thread.ThreadListenerRegistry;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventThreadCleanup;
import org.zkoss.zk.ui.event.EventThreadInit;

/**
 * ZK EventThreads suspend the servlet request thread. This class is meant to transfer any necessary
 * state from the request thread to the event thread.
 */
public class EventThreadListener implements EventThreadInit, EventThreadCleanup {
    
    /**
     * @see org.zkoss.zk.ui.event.EventThreadInit#init(org.zkoss.zk.ui.Component,
     *      org.zkoss.zk.ui.event.Event)
     */
    @Override
    public boolean init(final Component comp, final Event event) throws Exception {
        ThreadListenerRegistry.notifyListeners(true);
        return true;
    }
    
    /**
     * @see org.zkoss.zk.ui.event.EventThreadCleanup#cleanup(org.zkoss.zk.ui.Component,
     *      org.zkoss.zk.ui.event.Event, java.util.List)
     */
    @Override
    public void cleanup(final Component comp, final Event evt, @SuppressWarnings("rawtypes") final List errs) {
        ThreadListenerRegistry.notifyListeners(false);
    }
    
    /**
     * @see org.zkoss.zk.ui.event.EventThreadCleanup#complete(org.zkoss.zk.ui.Component,
     *      org.zkoss.zk.ui.event.Event)
     */
    @Override
    public void complete(final Component comp, final Event evt) throws Exception {
    }
    
    /**
     * @see org.zkoss.zk.ui.event.EventThreadInit#prepare(org.zkoss.zk.ui.Component,
     *      org.zkoss.zk.ui.event.Event)
     */
    @Override
    public void prepare(final Component comp, final Event event) throws Exception {
    }
    
}
