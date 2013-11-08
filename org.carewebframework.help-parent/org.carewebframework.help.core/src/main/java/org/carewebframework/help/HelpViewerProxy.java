/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.help;

import java.util.ArrayList;
import java.util.List;

import org.carewebframework.ui.event.InvocationRequest;
import org.carewebframework.ui.event.InvocationRequestQueue;

import org.zkoss.zk.ui.Desktop;
import org.zkoss.zk.ui.Executions;

/**
 * Acts as a proxy for a help viewer instance residing in another browser window. Uses event queues
 * to enable communication between the proxy and the remote viewer.
 */
public class HelpViewerProxy implements IHelpViewer {
    
    private final String ownerId;
    
    private final String remoteWindowName;
    
    private InvocationRequestQueue remoteQueue;
    
    private InvocationRequest helpRequest;
    
    private final List<IHelpSet> helpSets = new ArrayList<IHelpSet>();
    
    private final InvocationRequest loadRequest = InvocationRequestQueue.createRequest("load", helpSets);
    
    private final InvocationRequest mergeRequest = InvocationRequestQueue.createRequest("mergeHelpSet", helpSets);
    
    /**
     * Creates a proxy for the help viewer with the specified desktop as owner.
     * 
     * @param owner Desktop that will own this proxy.
     */
    public HelpViewerProxy(Desktop owner) {
        super();
        ownerId = owner.getId();
        remoteWindowName = new InvocationRequestQueue(owner, this, HelpUtil.HELP_QUEUE_PREFIX, HelpUtil.closeRequest)
                .getQualifiedQueueName();
    }
    
    /**
     * Requests the creation of the remote viewer window, passing it the owner's desktop id.
     */
    private void startRemoteViewer() {
        Executions.getCurrent().sendRedirect(HelpUtil.VIEWER_URL + "?proxy=" + ownerId, remoteWindowName);
    }
    
    /**
     * Fire an event to the remote viewer to request execution of the specified method.
     * 
     * @param methodName Name of the method to execute.
     */
    private void sendRequest(String methodName) {
        sendRequest(methodName, (Object[]) null);
    }
    
    /**
     * Send a request to the remote viewer to request execution of the specified method.
     * 
     * @param methodName Name of the method to execute.
     * @param params Parameters to pass to the method (may be null).
     */
    private void sendRequest(String methodName, Object... params) {
        sendRequest(InvocationRequestQueue.createRequest(methodName, params), true);
        
    }
    
    /**
     * Sends a request to the remote viewer.
     * 
     * @param helpRequest The request to send.
     * @param startRemoteViewer If true and the remote viewer is not running, start it.
     */
    private void sendRequest(InvocationRequest helpRequest, boolean startRemoteViewer) {
        this.helpRequest = helpRequest;
        
        if (helpRequest != null) {
            if (remoteViewerActive()) {
                remoteQueue.sendRequest(helpRequest);
                this.helpRequest = null;
            } else if (startRemoteViewer) {
                startRemoteViewer();
            } else {
                this.helpRequest = null;
            }
        }
    }
    
    /**
     * Returns true if the remote viewer is active (i.e., its event queue exists).
     * 
     * @return
     */
    private boolean remoteViewerActive() {
        if (remoteQueue == null) {
            return false;
        }
        
        if (!remoteQueue.isAlive()) {
            remoteQueue = null;
            return false;
        }
        
        return true;
    }
    
    /**
     * @see org.carewebframework.help.IHelpViewer#load(java.lang.Iterable)
     */
    @Override
    public void load(Iterable<IHelpSet> helpSets) {
        this.helpSets.clear();
        
        if (helpSets != null) {
            for (IHelpSet helpSet : helpSets) {
                this.helpSets.add(helpSet);
            }
        }
        
        sendRequest(loadRequest, false);
    }
    
    /**
     * @see org.carewebframework.help.IHelpViewer#mergeHelpSet
     */
    @Override
    public void mergeHelpSet(IHelpSet helpSet) {
        helpSets.add(helpSet);
        sendRequest(mergeRequest, false);
    }
    
    /**
     * @see org.carewebframework.help.IHelpViewer#show()
     */
    @Override
    public void show() {
        sendRequest("show");
    }
    
    /**
     * @see org.carewebframework.help.IHelpViewer#show(IHelpSet)
     */
    @Override
    public void show(IHelpSet helpSet) {
        sendRequest("show", helpSet);
    }
    
    /**
     * @see org.carewebframework.help.IHelpViewer#show(IHelpSet, java.lang.String)
     */
    @Override
    public void show(IHelpSet helpSet, String topicId) {
        sendRequest("show", helpSet, topicId);
    }
    
    /**
     * @see org.carewebframework.help.IHelpViewer#show(IHelpSet, java.lang.String,
     *      java.lang.String)
     */
    @Override
    public void show(IHelpSet helpSet, String topicId, String topicLabel) {
        sendRequest("show", helpSet, topicId, topicLabel);
    }
    
    /**
     * @see org.carewebframework.help.IHelpViewer#show(HelpViewType)
     */
    @Override
    public void show(HelpViewType viewType) {
        sendRequest("show", viewType);
    }
    
    /**
     * @see org.carewebframework.help.IHelpViewer#show(java.lang.String)
     */
    @Override
    public void show(String homeId) {
        sendRequest("show", homeId);
    }
    
    /**
     * @see org.carewebframework.help.IHelpViewer#show(java.lang.String, java.lang.String)
     */
    @Override
    public void show(String homeId, String topicId) {
        sendRequest("show", homeId, topicId);
    }
    
    /**
     * @see org.carewebframework.help.IHelpViewer#close()
     */
    @Override
    public void close() {
        sendRequest(HelpUtil.closeRequest, false);
    }
    
    /**
     * Sets the remote queue associated with the proxy.
     * 
     * @param remoteQueue
     */
    public void setRemoteQueue(InvocationRequestQueue remoteQueue) {
        this.remoteQueue = remoteQueue;
        InvocationRequest deferredRequest = helpRequest;
        sendRequest(loadRequest, false);
        sendRequest(deferredRequest, false);
    }
    
}
