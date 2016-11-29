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

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.beanutils.MethodUtils;
import org.apache.commons.lang.StringUtils;

import org.carewebframework.common.MiscUtil;
import org.carewebframework.help.HelpTopic;
import org.carewebframework.help.HelpViewType;
import org.carewebframework.help.IHelpSet;
import org.carewebframework.help.IHelpView;
import org.carewebframework.help.IHelpViewer;
import org.carewebframework.help.viewer.HelpHistory.ITopicListener;
import org.carewebframework.ui.event.InvocationRequestQueue;
import org.carewebframework.ui.zk.ZKUtil;

import org.zkoss.zk.au.out.AuInvoke;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.SizeEvent;
import org.zkoss.zk.ui.ext.AfterCompose;
import org.zkoss.zul.Button;
import org.zkoss.zul.Iframe;
import org.zkoss.zul.Label;
import org.zkoss.zul.Tabbox;
import org.zkoss.zul.Window;

/**
 * ZK-based viewer for viewing help content. Supports multiple help formats.
 */
public class HelpViewer extends Window implements IHelpViewer, AfterCompose, ITopicListener, EventListener<Event> {
    
    public enum HelpViewerMode {
        EMBEDDED, POPUP;
        
        @Override
        public String toString() {
            return name().toLowerCase();
        }
    };
    
    private static final long serialVersionUID = 1L;
    
    private Tabbox tbxNavigator;
    
    private Iframe iframe;
    
    private Button btnPrevious;
    
    private Button btnNext;
    
    private Button btnPrint;
    
    private Button btnOpen;
    
    private Label lblLoading;
    
    private final List<IHelpSet> helpSets = new ArrayList<>();
    
    private final HelpHistory history = new HelpHistory();
    
    private HelpViewerMode mode;
    
    private String lastURL;
    
    private String lastHeight = "400px";
    
    private String lastWidth = "1000px";
    
    private final AuInvoke auFocusWindow;
    
    private final AuInvoke auCloseWindow;
    
    public HelpViewer() {
        super();
        auFocusWindow = new AuInvoke(this, "_cwf_focus");
        auCloseWindow = new AuInvoke(this, "_cwf_close");
    }
    
    /**
     * @see org.carewebframework.help.IHelpViewer#show()
     */
    @Override
    public void show() {
        try {
            if (mode == HelpViewerMode.EMBEDDED) {
                doModal();
                setHeight(lastHeight);
                setWidth(lastWidth);
            } else {
                response(auFocusWindow);
            }
        } catch (Exception e) {}
    }
    
    /**
     * @see org.carewebframework.help.IHelpViewer#show(java.lang.String)
     */
    @Override
    public void show(String homeId) {
        show(homeId, null);
    }
    
    /**
     * @see org.carewebframework.help.IHelpViewer#show(IHelpSet)
     */
    @Override
    public void show(IHelpSet helpSet) {
        show(helpSet, null);
    }
    
    /**
     * @see org.carewebframework.help.IHelpViewer#show(java.lang.String, java.lang.String)
     */
    @Override
    public void show(String homeId, String topicId) {
        show(findHelpSet(homeId), topicId);
    }
    
    /**
     * @see org.carewebframework.help.IHelpViewer#show(IHelpSet, java.lang.String)
     */
    @Override
    public void show(IHelpSet helpSet, String topicId) {
        show(helpSet, topicId, null);
    }
    
    /**
     * @see org.carewebframework.help.IHelpViewer#show(IHelpSet, java.lang.String,
     *      java.lang.String)
     */
    @Override
    public void show(IHelpSet helpSet, String topicId, String topicLabel) {
        if (helpSet != null) {
            topicId = StringUtils.isEmpty(topicId) ? helpSet.getHomeID() : topicId;
            topicLabel = StringUtils.isEmpty(topicLabel) ? topicId : topicLabel;
            HelpTopic helpTopic = helpSet.getTopic(topicId);
            helpTopic = helpTopic == null ? null
                    : new HelpTopic(helpTopic.getURL(), helpTopic.getLabel(), helpSet.getName());
            setTopic(helpTopic);
            show(HelpViewType.HISTORY);
        }
    }
    
    /**
     * @see org.carewebframework.help.IHelpViewer#show(org.carewebframework.help.HelpViewType)
     */
    @Override
    public void show(HelpViewType viewType) {
        HelpTab tab = findTab(viewType, false);
        
        if (tab == null) {
            return;
        }
        
        selectTab(tab);
        show();
    }
    
    /**
     * Close the window. If this is a local window, simply hide it. If it is a remote window, close
     * the browser window entirely.
     * 
     * @see org.carewebframework.help.IHelpViewer#close()
     */
    @Override
    public void close() {
        if (mode == HelpViewerMode.EMBEDDED) {
            setVisible(false);
        } else {
            response(auCloseWindow);
        }
    }
    
    /**
     * Resets the viewer to its baseline state. All registered help sets are removed, the history is
     * cleared, and all tabs are removed.
     */
    private void reset() {
        tbxNavigator.setVisible(false);
        lblLoading.setVisible(true);
        tbxNavigator.getTabpanels().getChildren().clear();
        tbxNavigator.getTabs().getChildren().clear();
        helpSets.clear();
        history.clear();
    }
    
    /**
     * @see org.carewebframework.help.IHelpViewer#load(java.lang.Iterable)
     */
    @Override
    public void load(Iterable<IHelpSet> helpSets) {
        reset();
        
        if (helpSets != null) {
            for (IHelpSet helpSet : helpSets) {
                mergeHelpSet(helpSet);
            }
        }
        
        findTab(HelpViewType.HISTORY, true).setVisible(false);
        lblLoading.setVisible(false);
        tbxNavigator.setVisible(true);
        selectTab(getTabs().get(0));
        onTopicSelected(null);
    }
    
    /**
     * Select the specified tab. Fires the onSelect event on the tab.
     * 
     * @param tab Tab to select.
     */
    private void selectTab(HelpTab tab) {
        tbxNavigator.setSelectedPanel(tab);
        tab.onSelect();
    }
    
    /**
     * @see org.carewebframework.help.IHelpViewer#mergeHelpSet(IHelpSet)
     */
    @Override
    public void mergeHelpSet(IHelpSet helpSet) {
        if (helpSet == null || MiscUtil.containsInstance(helpSets, helpSet)) {
            return;
        }
        
        if (HelpUtil.getSearchService() != null) {
            HelpSearchTab searchTab = (HelpSearchTab) findTab(HelpViewType.SEARCH, true);
            searchTab.mergeHelpSet(helpSet);
        }
        
        // Each supported view type will result in a dedicated tab
        
        for (IHelpView view : helpSet.getAllViews()) {
            HelpTab helpTab = findTab(view.getViewType(), true);
            
            if (helpTab != null) {
                helpTab.addView(view);
            }
        }
        
        helpSets.add(helpSet);
    }
    
    /**
     * Returns the help set that matches the specified home id, or null if no match found.
     * 
     * @param homeId The home id.
     * @return The home help set, or null if none.
     */
    private IHelpSet findHelpSet(String homeId) {
        for (IHelpSet helpSet : helpSets) {
            if (homeId.equals(helpSet.getHomeID())) {
                return helpSet;
            }
        }
        
        return null;
    }
    
    /**
     * Returns the list of existing help tabs.
     * 
     * @return List of help tabs.
     */
    @SuppressWarnings("unchecked")
    private List<HelpTab> getTabs() {
        return (List<HelpTab>) (List<?>) tbxNavigator.getTabpanels().getChildren();
    }
    
    /**
     * Returns the help tab associated with the specified view type. If the tab does not exist and
     * doCreate is true and the view type is supported, a new tab is created. Otherwise, null is
     * returned.
     * 
     * @param viewType The view type.
     * @param doCreate If true, create if does not already exist.
     * @return A help tab (possibly null).
     */
    private HelpTab findTab(HelpViewType viewType, boolean doCreate) {
        for (HelpTab helpTab : getTabs()) {
            if (helpTab.getViewType().equals(viewType)) {
                return helpTab;
            }
        }
        
        return doCreate ? createTab(viewType) : null;
    }
    
    /**
     * Creates a help tab for the specified view type. If the view type is not supported, null is
     * returned.
     * 
     * @param viewType The view type.
     * @return A help tab
     */
    private HelpTab createTab(HelpViewType viewType) {
        HelpTab helpTab = HelpTab.createTab(this, viewType);
        return helpTab;
    }
    
    /**
     * Sets the currently viewed topic.
     * 
     * @param topic Help topic.
     */
    public void setTopic(HelpTopic topic) {
        history.add(topic);
    }
    
    /**
     * Moves to the next topic in the view history.
     */
    public void onClick$btnNext() {
        history.next();
    }
    
    /**
     * Moves to the previous topic in the view history.
     */
    public void onClick$btnPrevious() {
        history.previous();
    }
    
    /**
     * Opens the iFrame contents in a separate window.
     */
    public void onClick$btnOpen() {
        HelpUtil.openWindow(iframe.getSrc(), "_blank");
    }
    
    /**
     * Fired when the iFrame's URL changes.
     * 
     * @param event The change event.
     */
    public void onURLChange$iframe(Event event) {
        event = ZKUtil.getEventOrigin(event);
        String url = (String) event.getData();
        
        if (url.equals(lastURL)) {
            return;
        }
        
        lastURL = url;
        HelpTopic topic = findTopic(url);
        
        if (topic != null) {
            setTopic(topic);
        }
    }
    
    /**
     * Returns a topic matching the specified URL.
     * 
     * @param url The URL.
     * @return The topic matching the URL, or null if not found.
     */
    private HelpTopic findTopic(String url) {
        int i = url.indexOf("/zkau/");
        url = i == -1 ? url : url.substring(i + 6);
        
        for (IHelpSet hs : helpSets) {
            try {
                HelpTopic topic = hs.getTopic(url);
                
                if (topic != null) {
                    return topic;
                }
            } catch (Exception e) {}
        }
        
        return null;
    }
    
    /**
     * Returns a reference to the tab box.
     * 
     * @return The tab box.
     */
    protected Tabbox getTabbox() {
        return tbxNavigator;
    }
    
    /**
     * Returns a reference to the view history.
     * 
     * @return The help history view.
     */
    protected HelpHistory getHistory() {
        return history;
    }
    
    /**
     * Initializes the UI after initial loading.
     * 
     * @see org.zkoss.zk.ui.ext.AfterCompose#afterCompose()
     */
    @Override
    public void afterCompose() {
        String proxyId = Executions.getCurrent().getParameter("proxy");
        boolean proxied = proxyId != null;
        mode = proxied ? HelpViewerMode.POPUP : HelpViewerMode.EMBEDDED;
        setWidth(proxied ? "100%" : lastWidth);
        setHeight(proxied ? "100%" : lastHeight);
        setSizable(!proxied);
        setClosable(!proxied);
        setMaximizable(!proxied);
        setMinimizable(!proxied);
        setTitle(proxied ? null : "Help");
        setVisible(proxied);
        ZKUtil.wireController(this);
        setWidgetOverride("_cwf_focus", "function() {window.focus();}");
        setWidgetOverride("_cwf_close", "function() {window.close();}");
        history.addTopicListener(this);
        reset();
        
        if (proxied) {
            getPage().setTitle("Help");
            InvocationRequestQueue proxyQueue = InvocationRequestQueue.getQueue(proxyId, HelpUtil.HELP_QUEUE_PREFIX);
            
            if (proxyQueue == null) {
                detach();
                return;
            }
            
            proxyQueue.sendRequest("setRemoteQueue",
                new InvocationRequestQueue(this, HelpUtil.HELP_QUEUE_PREFIX, HelpUtil.closeRequest));
        }
        
    }
    
    /**
     * @see org.zkoss.zul.Window#onClose()
     */
    @Override
    public void onClose() {
        close();
    }
    
    /**
     * Save height and width to use next time window is shown.
     * 
     * @param event The size event.
     */
    public void onSize(Event event) {
        SizeEvent size = (SizeEvent) ZKUtil.getEventOrigin(event);
        lastHeight = size.getHeight();
        lastWidth = size.getWidth();
    }
    
    /**
     * Remove the viewer reference when it is detached.
     * 
     * @see org.zkoss.zul.Window#onPageDetached(org.zkoss.zk.ui.Page)
     */
    @Override
    public void onPageDetached(Page page) {
        HelpUtil.removeViewer(this);
        super.onPageDetached(page);
    }
    
    /**
     * Invoked by the view history whenever the selected topic changes.
     * 
     * @see org.carewebframework.help.viewer.HelpHistory.ITopicListener#onTopicSelected(HelpTopic)
     */
    @Override
    public void onTopicSelected(HelpTopic topic) {
        URL url = null;
        
        try {
            url = topic == null ? null : topic.getURL();
            String src = url == null ? null : url.toString();
            
            if (src != null && src.startsWith("jar:")) {
                int i = src.indexOf("!");
                src = i < 0 ? null : HelpUtil.getBaseUrl() + "/zkau" + src.substring(++i);
            }
            iframe.setSrc(src);
        } catch (Exception e) {
            iframe.setSrc("about:" + e);
        }
        
        lastURL = iframe.getSrc();
        btnPrevious.setDisabled(!history.hasPrevious());
        btnNext.setDisabled(!history.hasNext());
        btnPrint.setDisabled(url == null);
        btnOpen.setDisabled(url == null);
        
        for (HelpTab tab : getTabs()) {
            tab.onTopicSelected(topic);
        }
    }
    
    /**
     * Process remote help requests.
     * 
     * @param event The event object that embodies the request. The event name is the method name to
     *            be invoked and the event data is an array of the method parameters.
     * @throws Exception Unspecified exception.
     */
    @Override
    public void onEvent(Event event) throws Exception {
        try {
            MethodUtils.invokeMethod(this, event.getName(), (Object[]) event.getData());
        } catch (Exception e) {}
    }
}
