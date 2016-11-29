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

import java.io.IOException;

import org.carewebframework.api.spring.SpringUtil;
import org.carewebframework.help.HelpContext;
import org.carewebframework.help.HelpModule;
import org.carewebframework.help.HelpModuleRegistry;
import org.carewebframework.help.HelpSetCache;
import org.carewebframework.help.HelpViewType;
import org.carewebframework.help.IHelpSearch;
import org.carewebframework.help.IHelpSet;
import org.carewebframework.help.IHelpViewer;
import org.carewebframework.help.viewer.HelpViewer.HelpViewerMode;
import org.carewebframework.ui.FrameworkWebSupport;
import org.carewebframework.ui.command.CommandUtil;
import org.carewebframework.ui.event.InvocationRequest;
import org.carewebframework.ui.event.InvocationRequestQueue;
import org.carewebframework.ui.zk.ZKUtil;
import org.zkoss.image.AImage;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Desktop;
import org.zkoss.zk.ui.Execution;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.impl.XulElement;

/**
 * Utility class containing helper methods in support of online help infrastructure.
 */
public class HelpUtil {
    
    private static HelpViewerMode defaultMode = HelpViewerMode.POPUP;
    
    protected static final String RESOURCE_PREFIX = ZKUtil.getResourcePath(HelpUtil.class);
    
    protected static final String IMAGES_ROOT = RESOURCE_PREFIX + "images/";
    
    protected static final String VIEWER_URL = RESOURCE_PREFIX + "helpViewer.zul";
    
    protected static final String VIEWER_ATTRIB = VIEWER_URL;
    
    protected static final String EMBEDDED_ATTRIB = RESOURCE_PREFIX + "embedded";
    
    private static final String CSH_PREFIX = HelpUtil.class.getPackage().getName() + ".";
    
    private static final String CSH_TARGET = CSH_PREFIX + "target";
    
    /*package*/static final String HELP_QUEUE_PREFIX = "Help_Message_Queue";
    
    /*package*/static final InvocationRequest closeRequest = InvocationRequestQueue.createRequest("close");
    
    /**
     * Returns the desktop for the current execution.
     * 
     * @return The current desktop.
     */
    protected static Desktop getDesktop() {
        return FrameworkWebSupport.getDesktop();
    }
    
    /**
     * Sets the default help viewer mode.
     * 
     * @param embedded If true, set the viewer mode to embedded; if false, to proxied.
     */
    public static void setEmbeddedMode(boolean embedded) {
        defaultMode = embedded ? HelpViewerMode.EMBEDDED : HelpViewerMode.POPUP;
    }
    
    /**
     * Returns the help viewer mode for the specified desktop.
     * 
     * @param desktop The desktop to check. If null, the global setting is returned.
     * @return The help viewer mode.
     */
    public static HelpViewerMode getViewerMode(Desktop desktop) {
        return desktop == null || !desktop.hasAttribute(EMBEDDED_ATTRIB) ? defaultMode
                : (HelpViewerMode) desktop.getAttribute(EMBEDDED_ATTRIB);
    }
    
    /**
     * Sets the help viewer mode for the specified desktop. If different from the previous mode and
     * a viewer for this desktop exists, the viewer will be removed and recreated on the next
     * request.
     * 
     * @param desktop The target desktop.
     * @param mode The help viewer mode.
     */
    public static void setViewerMode(Desktop desktop, HelpViewerMode mode) {
        if (getViewerMode(desktop) != mode) {
            desktop.removeAttribute(VIEWER_ATTRIB);
        }
        
        desktop.setAttribute(EMBEDDED_ATTRIB, mode);
    }
    
    /**
     * Returns an instance of the viewer for the current desktop. If no instance yet exists, one is
     * created.
     * 
     * @return The help viewer.
     */
    public static IHelpViewer getViewer() {
        Desktop desktop = getDesktop();
        IHelpViewer viewer = (IHelpViewer) desktop.getAttribute(VIEWER_ATTRIB);
        
        if (viewer != null) {
            return viewer;
        }
        
        viewer = getViewerMode(desktop) == HelpViewerMode.POPUP ? new HelpViewerProxy(desktop)
                : (IHelpViewer) Executions.createComponents(VIEWER_URL, null, null);
        desktop.setAttribute(VIEWER_ATTRIB, viewer);
        return viewer;
    }
    
    protected static void removeViewer(IHelpViewer viewer) {
        Desktop desktop = getDesktop();
        
        if (desktop.getAttribute(VIEWER_ATTRIB) == viewer) {
            desktop.removeAttribute(VIEWER_ATTRIB);
        }
    }
    
    /**
     * Forces url to open in new browser window.
     * 
     * @param url Url of web resource.
     * @param windowName Name of browser window.
     */
    protected static void openWindow(String url, String windowName) {
        Clients.evalJavaScript("window.open('" + url.replace("~./", "zkau/web/") + "', '" + windowName + "', 'dummy=1')");
    }
    
    /**
     * Returns a URL string representing the root URL and context path.
     * 
     * @return The base url.
     */
    public static String getBaseUrl() {
        Execution e = Executions.getCurrent();
        return e.getScheme() + "://" + e.getServerName() + ":" + e.getServerPort() + e.getContextPath();
    }
    
    /**
     * Returns image content for an embedded image resource.
     * 
     * @param image The name of the image file.
     * @return An AImage instance, or null if the image resource was not found.
     */
    public static AImage getImageContent(String image) {
        try {
            return new AImage(Executions.encodeToURL(IMAGES_ROOT + image));
        } catch (IOException e) {
            return null;
        }
    }
    
    /**
     * Show help for the given module and optional topic.
     * 
     * @param module The id of the help module.
     * @param topic The id of the desired topic. If null, the home topic is shown.
     * @param label The label to display for the topic. If null, the topic id is displayed as the
     *            label.
     */
    public static void show(String module, String topic, String label) {
        show(new HelpContext(module, topic, label));
    }
    
    /**
     * Show help for the given help target.
     * 
     * @param target The help target.
     */
    public static void show(HelpContext target) {
        HelpModule dx = HelpModuleRegistry.getInstance().get(target.module);
        IHelpSet hs = dx == null ? null : HelpSetCache.getInstance().get(dx);
        
        if (hs != null) {
            String label = target.label == null && target.topic == null ? dx.getTitle() : target.label;
            IHelpViewer viewer = getViewer();
            viewer.mergeHelpSet(hs);
            viewer.show(hs, target.topic, label);
        }
    }
    
    /**
     * Displays the help viewer's table of contents.
     */
    public static void showTOC() {
        getViewer().show(HelpViewType.TOC);
    }
    
    /**
     * Display context-sensitive help associated with the specified component. If none is associated
     * with the component, its parent is examined, and so on. If no help association is found, no
     * action is taken.
     * 
     * @param component Component whose CSH is to be displayed.
     */
    public static void showCSH(Component component) {
        while (component != null) {
            HelpContext target = (HelpContext) component.getAttribute(CSH_TARGET);
            
            if (target != null) {
                HelpUtil.show(target);
                break;
            }
            component = component.getParent();
        }
    }
    
    /**
     * Associates context-sensitive help topic with a component. Any existing association is
     * replaced.
     * 
     * @param component Component to be associated.
     * @param helpContext The help target.
     * @param commandTarget The command target.
     */
    public static void associateCSH(XulElement component, HelpContext helpContext, Component commandTarget) {
        if (component != null) {
            component.setAttribute(CSH_TARGET, helpContext);
            CommandUtil.associateCommand("help", component, commandTarget);
        }
    }
    
    /**
     * Dissociates context-sensitive help from a component.
     * 
     * @param component Component to dissociate.
     */
    public static void dissociateCSH(XulElement component) {
        if (component != null && component.hasAttribute(CSH_TARGET)) {
            CommandUtil.dissociateCommand("help", component);
            component.removeAttribute(CSH_TARGET);
        }
    }
    
    /**
     * Returns a reference to the help search service if one is available.
     * 
     * @return A reference to the help search service (possibily null).
     */
    public static IHelpSearch getSearchService() {
        return SpringUtil.getBean("helpSearchService", IHelpSearch.class);
    }
    
    /**
     * Returns the help tab class that services the given view type. For unsupported view types,
     * returns null.
     * 
     * @param type The view type.
     * @return A help tab class.
     */
    public static Class<? extends HelpTab> getTabClass(HelpViewType type) {
        switch (type) {
            case TOC:
                return HelpContentsTab.class;
            
            case Keyword:
                return HelpIndexTab.class;
            
            case Index:
                return HelpIndexTab.class;
            
            case Search:
                return HelpSearchTab.class;
            
            case History:
                return HelpHistoryTab.class;
            
            case Glossary:
                return HelpIndexTab.class;
            
            default:
                return null;
        }
    }
    
    /**
     * Static invocation only.
     */
    private HelpUtil() {
    }
}
