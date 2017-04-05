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

import org.apache.commons.lang.StringUtils;
import org.carewebframework.common.MiscUtil;
import org.carewebframework.common.StrUtil;
import org.carewebframework.help.HelpTopic;
import org.carewebframework.help.HelpViewType;
import org.carewebframework.help.IHelpSet;
import org.carewebframework.help.IHelpView;
import org.carewebframework.help.IHelpViewer;
import org.carewebframework.help.viewer.HelpHistory.ITopicListener;
import org.carewebframework.web.ancillary.IAutoWired;
import org.carewebframework.web.annotation.EventHandler;
import org.carewebframework.web.annotation.WiredComponent;
import org.carewebframework.web.client.ClientUtil;
import org.carewebframework.web.client.ExecutionContext;
import org.carewebframework.web.component.BaseComponent;
import org.carewebframework.web.component.Button;
import org.carewebframework.web.component.Iframe;
import org.carewebframework.web.component.Label;
import org.carewebframework.web.component.Page;
import org.carewebframework.web.component.Tab;
import org.carewebframework.web.component.Tabview;
import org.carewebframework.web.component.Window;
import org.carewebframework.web.component.Window.CloseAction;
import org.carewebframework.web.component.Window.Size;
import org.carewebframework.web.event.Event;
import org.carewebframework.web.event.ResizeEvent;
import org.carewebframework.web.ipc.InvocationRequestQueue;
import org.carewebframework.web.ipc.InvocationRequestQueueRegistry;

/**
 * Help content viewer. Supports multiple help formats.
 */
public class HelpViewer implements IAutoWired, IHelpViewer, ITopicListener {

    public enum HelpViewerMode {
        EMBEDDED, POPUP;

        @Override
        public String toString() {
            return name().toLowerCase();
        }
    }

    @WiredComponent
    private Tabview tvNavigator;

    @WiredComponent
    private Iframe iframe;

    @WiredComponent
    private Button btnPrevious;

    @WiredComponent
    private Button btnNext;

    @WiredComponent
    private Button btnPrint;

    @WiredComponent
    private Button btnOpen;

    @WiredComponent
    private Label lblLoading;

    private Window root;

    private final List<IHelpSet> helpSets = new ArrayList<>();

    private final List<HelpViewBase> views = new ArrayList<>();

    private InvocationRequestQueue requestQueue;

    private HelpHistory history;

    private HelpViewerMode mode;

    private String lastURL;

    private double lastHeight = 600;

    private double lastWidth = 1000;

    public HelpViewer() {
        super();
    }

    @Override
    public void afterInitialized(BaseComponent comp) {
        root = (Window) comp;
        root.setAttribute("controller", this);
        init();
    }

    /**
     * @see org.carewebframework.help.IHelpViewer#show()
     */
    @Override
    public void show() {
        lblLoading.setVisible(false);
        tvNavigator.setVisible(true);

        if (mode == HelpViewerMode.EMBEDDED) {
            root.setHeight(lastHeight + "px");
            root.setWidth(lastWidth + "px");
            root.setSize(Size.NORMAL);
            root.popup(null);
        } else {
            ClientUtil.invoke("window.focus");
        }
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
            show(HelpViewType.HISTORY);
        }
    }

    /**
     * @see org.carewebframework.help.IHelpViewer#show(org.carewebframework.help.viewer.HelpViewType)
     */
    @Override
    public void show(HelpViewType viewType) {
        HelpViewBase view = findView(viewType, false, true);

        if (view != null) {
            selectView(view);
            show();
        }
    }

    /**
     * Close the window. If this is a local window, simply hide it. If it is a remote window, close
     * the browser window entirely.
     *
     * @see org.carewebframework.help.IHelpViewer#close()
     */
    @Override
    public void close() {
        root.close();

        if (mode != HelpViewerMode.EMBEDDED) {
            ClientUtil.invoke("window.close");
        }
    }

    /**
     * Resets the viewer to its baseline state. All registered help sets are removed, the history is
     * cleared, and all views are hidden.
     */
    private void reset() {
        tvNavigator.setVisible(false);
        lblLoading.setVisible(true);
        helpSets.clear();
        history.clear();

        for (Tab tab : tvNavigator.getChildren(Tab.class)) {
            tab.setVisible(false);
        }
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

        lblLoading.setVisible(false);
        tvNavigator.setVisible(true);
        selectView(views.get(0));
        onTopicSelected(null);
    }

    /**
     * Select the tab associated with the view. Fires the onSelect event on the tab.
     *
     * @param view The help view.
     */
    private void selectView(HelpViewBase view) {
        view.getContainer().getAncestor(Tab.class).setSelected(true);
        view.onSelect();
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
            HelpViewSearch searchTab = (HelpViewSearch) findView(HelpViewType.SEARCH, true, false);
            searchTab.mergeHelpSet(helpSet);
        }

        // Each supported view type will result in a dedicated tab

        for (IHelpView view : helpSet.getAllViews()) {
            HelpViewBase helpView = findView(view.getViewType(), true, true);

            if (helpView != null) {
                helpView.addView(view);
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
     * Returns the help view associated with the specified view type. If the view does not exist and
     * doCreate is true and the view type is supported, a new view is created. Otherwise, null is
     * returned.
     *
     * @param viewType The view type.
     * @param doCreate If true, create if does not already exist.
     * @param visible Determines visibility of view.
     * @return A help view (possibly null).
     */
    private HelpViewBase findView(HelpViewType viewType, boolean doCreate, boolean visible) {
        HelpViewBase result = null;

        for (HelpViewBase view : views) {
            if (view.getViewType().equals(viewType)) {
                result = view;
                break;
            }
        }

        result = result != null ? result : doCreate ? createView(viewType) : null;

        if (result != null) {
            result.getContainer().getAncestor(Tab.class).setVisible(visible);
        }

        return result;
    }

    /**
     * Creates a help tab for the specified view type. If the view type is not supported, null is
     * returned.
     *
     * @param viewType The view type.
     * @return A help tab
     */
    private HelpViewBase createView(HelpViewType viewType) {
        HelpViewBase newView = HelpViewBase.createView(this, viewType);
        int pos = -1;

        for (int i = 0; i < views.size(); i++) {
            HelpViewBase view = views.get(i);

            if (viewType.compareTo(view.getViewType()) <= 0) {
                pos = i;
                break;
            }
        }

        if (pos < 0) {
            views.add(newView);
        } else {
            views.add(pos, newView);
        }

        Tab tab = new Tab();
        tab.addStyle("overflow", "auto");
        String label = StrUtil.getLabel("cwf.help.tab." + viewType.name().toLowerCase() + ".label");
        tab.setLabel(label == null ? viewType.name() : label);
        tab.addChild(newView.getContainer());
        tab.setVisible(viewType != HelpViewType.HISTORY);
        tvNavigator.addChild(tab, pos);
        return newView;
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
     * Allows help history tab to inject model.
     *
     * @param history Help history instance.
     */
    protected void setHelpHistory(HelpHistory history) {
        this.history = history;
    }

    /**
     * Moves to the next topic in the view history.
     */
    @EventHandler(value = "click", target = "@btnNext")
    private void onClick$btnNext() {
        history.next();
    }

    /**
     * Moves to the previous topic in the view history.
     */
    @EventHandler(value = "click", target = "@btnPrevious")
    private void onClick$btnPrevious() {
        history.previous();
    }

    /**
     * Opens the iFrame contents in a separate window.
     */
    @EventHandler(value = "click", target = "@btnOpen")
    private void onClick$btnOpen() {
        HelpUtil.openWindow(iframe.getSrc(), "_blank");
    }

    /**
     * Fired when the iFrame's URL changes.
     *
     * @param event The change event.
     */
    @EventHandler(value = "load", target = "@iframe")
    private void onLoad$iframe(Event event) {
        String url = (String) event.getData();

        if (url == null || url.equals(lastURL)) {
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
        int i = url.indexOf("/web/");
        url = i == -1 ? url : url.substring(i);

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
    protected Tabview getTabview() {
        return tvNavigator;
    }

    /**
     * Initializes the UI after initial loading.
     */
    private void init() {
        Page page = ExecutionContext.getPage();
        String proxyId = page.getQueryParam("proxy");
        boolean proxied = proxyId != null;
        mode = proxied ? HelpViewerMode.POPUP : HelpViewerMode.EMBEDDED;
        root.setWidth(proxied ? "100%" : lastWidth + "px");
        root.setHeight(proxied ? "100%" : lastHeight + "px");
        root.setSizable(!proxied);
        root.setClosable(!proxied);
        root.setMaximizable(!proxied);
        root.setMinimizable(!proxied);
        root.setTitle(proxied ? null : "Help");
        root.setVisible(proxied);
        findView(HelpViewType.HISTORY, true, false);
        history.addTopicListener(this);
        reset();

        if (proxied) {
            root.setCloseAction(CloseAction.DESTROY);
            page.setTitle("Help");
            InvocationRequestQueue proxyQueue = InvocationRequestQueueRegistry.getInstance().get("help" + proxyId);

            if (proxyQueue == null || !proxyQueue.isAlive()) {
                proxyQueue = null;
                close();
            } else {
                requestQueue = new InvocationRequestQueue("help" + page.getId(), page, this, HelpUtil.closeRequest);
                proxyQueue.sendRequest("setRemoteQueue", requestQueue);
            }
        } else {
            root.setCloseAction(CloseAction.HIDE);
        }

    }

    /**
     * Save height and width to use next time window is shown.
     *
     * @param event The size event.
     */
    @EventHandler("resize")
    private void onResize(ResizeEvent event) {
        lastHeight = event.getHeight();
        lastWidth = event.getWidth();
    }

    /**
     * Remove the viewer reference when it is detached.
     */
    @EventHandler("destroy")
    private void onDestroy() {
        HelpUtil.removeViewer(root.getPage(), this, false);

        if (requestQueue != null) {
            requestQueue.close();
            requestQueue = null;
        }
    }

    /**
     * Invoked by the view history whenever the selected topic changes.
     */
    @Override
    public void onTopicSelected(HelpTopic topic) {
        URL url = null;

        try {
            String src = topic == null ? null : HelpUtil.getUrl(topic.getURL().toString());
            iframe.setSrc(src);
        } catch (Exception e) {
            iframe.setSrc("about:" + e);
        }

        lastURL = iframe.getSrc();
        btnPrevious.setDisabled(!history.hasPrevious());
        btnNext.setDisabled(!history.hasNext());
        btnPrint.setDisabled(url == null);
        btnOpen.setDisabled(url == null);

        for (HelpViewBase view : views) {
            view.onTopicSelected(topic);
        }
    }

}
