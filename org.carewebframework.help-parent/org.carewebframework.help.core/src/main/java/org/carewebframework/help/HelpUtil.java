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

import java.io.IOException;

import org.carewebframework.ui.FrameworkWebSupport;
import org.carewebframework.ui.event.InvocationRequest;
import org.carewebframework.ui.event.InvocationRequestQueue;
import org.carewebframework.ui.zk.ZKUtil;

import org.zkoss.image.AImage;
import org.zkoss.zk.ui.Desktop;
import org.zkoss.zk.ui.Execution;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.util.Clients;

/**
 * Utility class containing helper methods in support of online help infrastructure.
 */
public class HelpUtil {
    
    private static boolean useViewerProxy = true;
    
    protected static final String RESOURCE_PREFIX = ZKUtil.getResourcePath(HelpUtil.class);
    
    protected static final String IMAGES_ROOT = RESOURCE_PREFIX + "images/";
    
    protected static final String VIEWER_URL = HelpUtil.RESOURCE_PREFIX + "helpViewer.zul";
    
    protected static final String VIEWER_ATTRIB = VIEWER_URL;
    
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
     * Determines whether the help viewer is displayed as an embedded popup dialog in the same
     * browser viewport, or in a separate browser window.
     * 
     * @param value If true, displays the viewer in embedded mode.
     * @return Previous setting.
     */
    public static boolean embeddedMode(boolean value) {
        boolean oldValue = !useViewerProxy;
        
        if (value != oldValue) {
            useViewerProxy = !value;
            getDesktop().removeAttribute(VIEWER_ATTRIB);
        }
        
        return oldValue;
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
        
        viewer = useViewerProxy ? new HelpViewerProxy(desktop)
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
     * Static invocation only.
     */
    private HelpUtil() {
    };
}
