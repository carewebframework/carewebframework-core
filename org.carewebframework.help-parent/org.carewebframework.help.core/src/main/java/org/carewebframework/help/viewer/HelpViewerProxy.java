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
package org.carewebframework.help.viewer;

import java.util.ArrayList;
import java.util.List;

import org.carewebframework.help.HelpViewType;
import org.carewebframework.help.IHelpSet;
import org.carewebframework.help.IHelpViewer;
import org.carewebframework.web.component.Page;
import org.carewebframework.web.ipc.InvocationRequest;
import org.carewebframework.web.ipc.InvocationRequestQueue;

/**
 * Acts as a proxy for a help viewer instance residing in another browser window. Uses event queues
 * to enable communication between the proxy and the remote viewer.
 */
public class HelpViewerProxy implements IHelpViewer {
    
    private static final String SHOW_METHOD = "show";
    
    private final Page owner;
    
    private final String remoteWindowName;
    
    private InvocationRequestQueue remoteQueue;
    
    private final InvocationRequestQueue proxyQueue;
    
    private InvocationRequest helpRequest;
    
    private final List<IHelpSet> helpSets = new ArrayList<>();
    
    private final InvocationRequest loadRequest = new InvocationRequest("load", helpSets);
    
    private final InvocationRequest mergeRequest = new InvocationRequest("mergeHelpSet", helpSets);
    
    /**
     * Creates a proxy for the help viewer with the specified page as owner.
     * 
     * @param owner Page that will own this proxy.
     */
    public HelpViewerProxy(Page owner) {
        super();
        this.owner = owner;
        remoteWindowName = "help" + owner.getId();
        proxyQueue = new InvocationRequestQueue(remoteWindowName, owner, this, HelpUtil.closeRequest);
    }
    
    /**
     * Requests the creation of the remote viewer window, passing it the owner's page id.
     */
    private void startRemoteViewer() {
        HelpUtil.openWindow(HelpUtil.VIEWER_URL + "?proxy=" + owner.getId(), remoteWindowName);
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
        sendRequest(new InvocationRequest(methodName, params), true);
        
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
     * @return True if remove viewer is active.
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
        sendRequest(SHOW_METHOD);
    }
    
    /**
     * @see org.carewebframework.help.IHelpViewer#show(IHelpSet)
     */
    @Override
    public void show(IHelpSet helpSet) {
        sendRequest(SHOW_METHOD, helpSet);
    }
    
    /**
     * @see org.carewebframework.help.IHelpViewer#show(IHelpSet, java.lang.String)
     */
    @Override
    public void show(IHelpSet helpSet, String topicId) {
        sendRequest(SHOW_METHOD, helpSet, topicId);
    }
    
    /**
     * @see org.carewebframework.help.IHelpViewer#show(IHelpSet, java.lang.String, java.lang.String)
     */
    @Override
    public void show(IHelpSet helpSet, String topicId, String topicLabel) {
        sendRequest(SHOW_METHOD, helpSet, topicId, topicLabel);
    }
    
    /**
     * @see org.carewebframework.help.IHelpViewer#show(HelpViewType)
     */
    @Override
    public void show(HelpViewType viewType) {
        sendRequest(SHOW_METHOD, viewType);
    }
    
    /**
     * @see org.carewebframework.help.IHelpViewer#show(java.lang.String)
     */
    @Override
    public void show(String homeId) {
        sendRequest(SHOW_METHOD, homeId);
    }
    
    /**
     * @see org.carewebframework.help.IHelpViewer#show(java.lang.String, java.lang.String)
     */
    @Override
    public void show(String homeId, String topicId) {
        sendRequest(SHOW_METHOD, homeId, topicId);
    }
    
    /**
     * @see org.carewebframework.help.IHelpViewer#close()
     */
    @Override
    public void close() {
        proxyQueue.close();
        sendRequest(HelpUtil.closeRequest, false);
        HelpUtil.removeViewer(owner, this, false);
    }
    
    /**
     * Sets the remote queue associated with the proxy.
     * 
     * @param remoteQueue The remote queue.
     */
    public void setRemoteQueue(InvocationRequestQueue remoteQueue) {
        this.remoteQueue = remoteQueue;
        InvocationRequest deferredRequest = helpRequest;
        sendRequest(loadRequest, false);
        sendRequest(deferredRequest, false);
    }
    
}
