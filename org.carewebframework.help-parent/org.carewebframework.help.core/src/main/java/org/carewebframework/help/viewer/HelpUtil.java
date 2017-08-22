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

import javax.servlet.ServletContext;

import org.carewebframework.api.spring.SpringUtil;
import org.carewebframework.help.HelpContext;
import org.carewebframework.help.HelpViewType;
import org.carewebframework.help.IHelpSearch;
import org.carewebframework.help.IHelpViewer;
import org.carewebframework.help.viewer.HelpViewer.HelpViewerMode;
import org.carewebframework.ui.command.CommandUtil;
import org.carewebframework.ui.util.CWFUtil;
import org.carewebframework.web.client.ClientUtil;
import org.carewebframework.web.client.ExecutionContext;
import org.carewebframework.web.component.BaseComponent;
import org.carewebframework.web.component.BaseUIComponent;
import org.carewebframework.web.component.Image;
import org.carewebframework.web.component.Page;
import org.carewebframework.web.ipc.InvocationRequest;
import org.carewebframework.web.page.PageUtil;

/**
 * Utility class containing helper methods in support of online help infrastructure.
 */
public class HelpUtil {

    private static HelpViewerMode defaultMode = HelpViewerMode.POPUP;

    protected static final String RESOURCE_PREFIX = CWFUtil.getResourcePath(HelpUtil.class);

    protected static final String IMAGES_ROOT = RESOURCE_PREFIX + "images/";

    protected static final String VIEWER_URL = RESOURCE_PREFIX + "helpViewer.cwf";

    protected static final String VIEWER_ATTRIB = VIEWER_URL;

    protected static final String EMBEDDED_ATTRIB = RESOURCE_PREFIX + "embedded";

    private static final String CSH_PREFIX = HelpUtil.class.getPackage().getName() + ".";

    private static final String CSH_TARGET = CSH_PREFIX + "target";

    /*package*/static final InvocationRequest closeRequest = new InvocationRequest("close");

    /**
     * Returns the page for the current execution.
     *
     * @return The current page.
     */
    protected static Page getPage() {
        return ExecutionContext.getPage();
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
     * Returns the help viewer mode for the specified page.
     *
     * @param page The page to check. If null, the global setting is returned.
     * @return The help viewer mode.
     */
    public static HelpViewerMode getViewerMode(Page page) {
        return page == null || !page.hasAttribute(EMBEDDED_ATTRIB) ? defaultMode
                : (HelpViewerMode) page.getAttribute(EMBEDDED_ATTRIB);
    }

    /**
     * Sets the help viewer mode for the specified page. If different from the previous mode and a
     * viewer for this page exists, the viewer will be removed and recreated on the next request.
     *
     * @param page The target page.
     * @param mode The help viewer mode.
     */
    public static void setViewerMode(Page page, HelpViewerMode mode) {
        if (getViewerMode(page) != mode) {
            removeViewer(page, true);
        }

        page.setAttribute(EMBEDDED_ATTRIB, mode);
    }

    /**
     * Returns an instance of the viewer for the current page. If no instance yet exists and
     * forceCreate is true, one is created.
     *
     * @param forceCreate If true, a viewer instance will be created if it does not exist.
     * @return The help viewer (may be null).
     */
    public static IHelpViewer getViewer(boolean forceCreate) {
        Page page = getPage();
        IHelpViewer viewer = (IHelpViewer) page.getAttribute(VIEWER_ATTRIB);
        return viewer != null ? viewer : forceCreate ? createViewer(page) : null;
    }

    /**
     * Creates an instance of the help viewer, associating it with the specified page.
     *
     * @param page Page owning the help viewer instance.
     * @return The newly created help viewer.
     */
    private static IHelpViewer createViewer(Page page) {
        IHelpViewer viewer;

        if (getViewerMode(page) == HelpViewerMode.POPUP) {
            viewer = new HelpViewerProxy(page);
        } else {
            BaseComponent root = PageUtil.createPage(VIEWER_URL, page).get(0);
            viewer = (IHelpViewer) root.getAttribute("controller");
        }

        page.setAttribute(VIEWER_ATTRIB, viewer);
        return viewer;
    }

    /**
     * Remove and close the help viewer associated with the current page.
     */
    public static void removeViewer() {
        removeViewer(getPage(), true);
    }

    /**
     * Remove and optionally close the help viewer associated with the specified page.
     *
     * @param page The page owning the help viewer.
     * @param close If true, close the help viewer after removing it.
     */
    protected static void removeViewer(Page page, boolean close) {
        IHelpViewer viewer = (IHelpViewer) page.removeAttribute(VIEWER_ATTRIB);

        if (viewer != null && close) {
            viewer.close();
        }
    }

    /**
     * Remove and optionally close the specified help viewer if is the active viewer for the
     * specified page.
     *
     * @param page The page owner the help viewer.
     * @param viewer The viewer to remove.
     * @param close If true, close the help viewer after removing it.
     */
    protected static void removeViewer(Page page, IHelpViewer viewer, boolean close) {
        if (viewer != null && viewer == page.getAttribute(VIEWER_ATTRIB)) {
            removeViewer(page, close);
        }
    }

    /**
     * Forces url to open in new browser window.
     *
     * @param url Url of web resource.
     * @param windowName Name of browser window.
     */
    protected static void openWindow(String url, String windowName) {
        ClientUtil.eval("window.open('" + url + "', '" + windowName + "', 'dummy=1')");
    }

    /**
     * Returns a URL string representing the root path and context path.
     *
     * @param path Relative path to expand.
     * @return The fully expanded URL.
     */
    public static String getUrl(String path) {
        if (path == null) {
            return path;
        }

        ServletContext sc = ExecutionContext.getSession().getServletContext();

        if (path.startsWith("jar:")) {
            int i = path.indexOf("!");
            path = i < 0 ? path : path.substring(++i);
        }

        path = sc.getContextPath() + path;
        return path;
    }

    /**
     * Returns image content for an embedded image resource.
     *
     * @param image The name of the image file.
     * @return An Image instance, or null if the image resource was not found.
     */
    public static Image getImageContent(String image) {
        return new Image(IMAGES_ROOT + image);
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
        getViewer(true).show(module, topic, label);
    }

    /**
     * Show help for the given help target.
     *
     * @param target The help target.
     */
    public static void show(HelpContext target) {
        getViewer(true).show(target);
    }

    /**
     * Displays the help viewer's table of contents.
     */
    public static void showTOC() {
        getViewer(true).show(HelpViewType.TOC);
    }

    /**
     * Display context-sensitive help associated with the specified component. If none is associated
     * with the component, its parent is examined, and so on. If no help association is found, no
     * action is taken.
     *
     * @param component Component whose CSH is to be displayed.
     */
    public static void showCSH(BaseComponent component) {
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
    public static void associateCSH(BaseUIComponent component, HelpContext helpContext, BaseUIComponent commandTarget) {
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
    public static void dissociateCSH(BaseUIComponent component) {
        if (component != null && component.hasAttribute(CSH_TARGET)) {
            CommandUtil.dissociateCommand("help", component);
            component.removeAttribute(CSH_TARGET);
        }
    }

    /**
     * Returns a reference to the help search service if one is available.
     *
     * @return A reference to the help search service (possibly null).
     */
    public static IHelpSearch getSearchService() {
        return SpringUtil.getBean("helpSearchService", IHelpSearch.class);
    }

    /**
     * Static invocation only.
     */
    private HelpUtil() {
    }
}
