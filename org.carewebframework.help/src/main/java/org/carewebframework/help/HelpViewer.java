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

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.beanutils.MethodUtils;
import org.apache.commons.lang.StringUtils;

import org.carewebframework.common.MiscUtil;
import org.carewebframework.help.HelpHistory.ITopicListener;
import org.carewebframework.ui.event.InvocationRequestQueue;
import org.carewebframework.ui.zk.ZKUtil;

import org.zkoss.zk.au.out.AuInvoke;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
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
    
    private static final long serialVersionUID = 1L;
    
    private Tabbox tbxNavigator;
    
    private Iframe iframe;
    
    private Button btnPrevious;
    
    private Button btnNext;
    
    private Button btnPrint;
    
    private Button btnOpen;
    
    private Label lblLoading;
    
    private final List<IHelpSet> helpSets = new ArrayList<IHelpSet>();
    
    private final HelpHistory history = new HelpHistory();
    
    private boolean proxied;
    
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
            if (!proxied) {
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
     * @see org.carewebframework.help.IHelpViewer#show(IHelpSet, java.lang.String, java.lang.String)
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
            show(HelpViewType.History);
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
        if (!proxied) {
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
        
        findTab(HelpViewType.History, true).setVisible(false);
        lblLoading.setVisible(false);
        tbxNavigator.setVisible(true);
        selectTab(getTabs().get(0));
        onTopicSelected(null);
    }
    
    /**
     * Select the specified tab. Fires the onSelect event on the tab.
     * 
     * @param tab
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
        
        // Each supported view type will result in a dedicated tab
        
        for (IHelpView view : helpSet.getAllViews()) {
            HelpViewType viewType = view.getViewType();
            HelpTab helpTab = findTab(viewType, true);
            
            if (helpTab != null) {
                helpTab.addView(view);
            }
        }
        
        helpSets.add(helpSet);
    }
    
    /**
     * Returns the help set that matches the specified home id, or null if no match found.
     * 
     * @param homeId
     * @return
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
     * @return
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
     * @param viewType
     * @param doCreate
     * @return A help tab
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
     * @param viewType
     * @return A help tab
     */
    private HelpTab createTab(HelpViewType viewType) {
        HelpTab helpTab = HelpTab.createTab(this, viewType);
        return helpTab;
    }
    
    /**
     * Sets the currently viewed topic.
     * 
     * @param topic
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
        Executions.getCurrent().sendRedirect(iframe.getSrc(), "_blank");
    }
    
    /**
     * Returns a reference to the tab box.
     * 
     * @return
     */
    protected Tabbox getTabbox() {
        return tbxNavigator;
    }
    
    /**
     * Returns a reference to the view history.
     * 
     * @return
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
        proxied = proxyId != null;
        setWidth(proxied ? "100%" : lastWidth);
        setHeight(proxied ? "100%" : lastHeight);
        setSizable(!proxied);
        setClosable(!proxied);
        setMaximizable(!proxied);
        setMinimizable(!proxied);
        setTitle(proxied ? null : "Help");
        setVisible(proxied);
        ZKUtil.wireController(this);
        btnNext.setImageContent(HelpUtil.getImageContent("forward.png"));
        btnPrevious.setImageContent(HelpUtil.getImageContent("back.png"));
        btnPrint.setImageContent(HelpUtil.getImageContent("print.png"));
        btnOpen.setImageContent(HelpUtil.getImageContent("newwin.png"));
        btnPrint.setWidgetListener(Events.ON_CLICK, "cwf.printIframe('" + iframe.getName() + "');");
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
            
            proxyQueue.sendRequest("setRemoteQueue", new InvocationRequestQueue(this, HelpUtil.HELP_QUEUE_PREFIX,
                    HelpUtil.closeRequest));
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
     * @param event
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
     * @see org.carewebframework.help.HelpHistory.ITopicListener#onTopicSelected(HelpTopic)
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
     * @throws Exception
     */
    @Override
    public void onEvent(Event event) throws Exception {
        try {
            MethodUtils.invokeMethod(this, event.getName(), (Object[]) event.getData());
        } catch (Exception e) {}
    }
}
