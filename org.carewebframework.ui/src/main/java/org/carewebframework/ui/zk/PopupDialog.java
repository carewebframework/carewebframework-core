/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.ui.zk;

import java.util.Hashtable;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.carewebframework.api.AppFramework;
import org.carewebframework.api.FrameworkUtil;

import org.springframework.context.ApplicationContext;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.HtmlBasedComponent;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.SizeEvent;
import org.zkoss.zk.ui.metainfo.PageDefinition;
import org.zkoss.zk.ui.sys.ExecutionsCtrl;
import org.zkoss.zul.Window;

/**
 * Base class for a ZK popup window.
 */
public class PopupDialog extends Window {
    
    private static final Log log = LogFactory.getLog(PopupDialog.class);
    
    private static final long serialVersionUID = 1L;
    
    private boolean cancelled = true;
    
    private final AppFramework appFramework = FrameworkUtil.getAppFramework();
    
    private final ApplicationContext appContext = appFramework.getApplicationContext();
    
    /**
     * Can be used to popup any zul page definition as a modal dialog.
     * 
     * @param zulPageDefinition Page definition used to construct the dialog.
     * @param args Argument list (may be null)
     * @param closable If true, window closure button appears.
     * @param sizable If true, window sizing grips appear.
     * @return Reference to the opened window, if successful.
     */
    public static Window popup(final PageDefinition zulPageDefinition, final Map<Object, Object> args,
                               final boolean closable, final boolean sizable) {
        return popup(zulPageDefinition, args, closable, sizable, true);
    }
    
    /**
     * Can be used to popup any zul page definition as a modal dialog.
     * 
     * @param zulPageDefinition Page definition used to construct the dialog.
     * @param args Argument list (may be null)
     * @param closable If true, window closure button appears.
     * @param sizable If true, window sizing grips appear.
     * @param show If true, the window is displayed modally. If false, the window is created but not
     *            displayed.
     * @return Reference to the opened window, if successful.
     */
    public static Window popup(final PageDefinition zulPageDefinition, final Map<Object, Object> args,
                               final boolean closable, final boolean sizable, final boolean show) {
        Window parent = new Window(); // Temporary parent in case createComponents fails, so can cleanup.
        Window window = null;
        
        try {
            Executions.getCurrent().createComponents(zulPageDefinition, parent, args);
            window = ZKUtil.findChild(parent, Window.class);
            
            if (window != null) { // If any top component is a window, discard temp parent
                window.setParent(null);
                
                for (Component child : parent.getChildren()) {
                    child.setParent(window);
                }
            } else { // Otherwise, use the temp parent as the window
                window = parent;
            }
            
            Page currentPage = ExecutionsCtrl.getCurrentCtrl().getCurrentPage();
            window.setClosable(closable);
            window.setSizable(sizable);
            window.setPage(currentPage);
            PopupManager.getInstance().registerPopup(window);
            
            if (show) {
                window.doModal();
            }
        } catch (Exception e) {
            if (window != null) {
                window.detach();
                window = null;
            }
            
            if (parent != null) {
                parent.detach();
            }
            
            PromptDialog.showError(e);
        }
        
        return window;
    }
    
    /**
     * Can be used to popup any zul page definition as a modal dialog.
     * 
     * @param zulPageDefinition Page definition used to construct the dialog.
     * @return Reference to the opened window, if successful.
     */
    public static Window popup(final PageDefinition zulPageDefinition) {
        return popup(zulPageDefinition, null, true, true);
    }
    
    /**
     * Can be used to popup any zul page as a modal dialog.
     * 
     * @param zulPage Url of zul page.
     * @param closable If true, window closure button appears.
     * @param sizable If true, window sizing grips appear.
     * @return Reference to the opened window, if successful.
     */
    public static Window popup(final String zulPage, final boolean closable, final boolean sizable) {
        return popup(zulPage, closable, sizable, true);
    }
    
    /**
     * Can be used to popup any zul page as a modal dialog.
     * 
     * @param zulPage Url of zul page.
     * @param closable If true, window closure button appears.
     * @param sizable If true, window sizing grips appear.
     * @param show If true, the window is displayed modally. If false, the window is created but not
     *            displayed.
     * @return Reference to the opened window, if successful.
     */
    public static Window popup(final String zulPage, final boolean closable, final boolean sizable, final boolean show) {
        return popup(zulPage, null, closable, sizable, show);
    }
    
    /**
     * Can be used to popup any zul page as a modal dialog.
     * 
     * @param zulPage Url of zul page.
     * @param args Argument list (may be null)
     * @param closable If true, window closure button appears.
     * @param sizable If true, window sizing grips appear.
     * @param show If true, the window is displayed modally. If false, the window is created but not
     *            displayed.
     * @return Reference to the opened window, if successful.
     */
    public static Window popup(final String zulPage, Map<Object, Object> args, final boolean closable,
                               final boolean sizable, final boolean show) {
        if (args == null) {
            args = new Hashtable<Object, Object>();
        }
        
        try {
            PageDefinition pageDefinition = ZKUtil.loadZulPageDefinition(zulPage, args);
            return popup(pageDefinition, args, closable, sizable, show);
        } catch (final Exception e) {
            log.error(e);
            return null;
        }
    }
    
    /**
     * Opens any arbitrary zul page in a modal window.
     * 
     * @param zulPage Url of zul page.
     * @return Reference to the opened window, if successful.
     */
    public static Window popup(final String zulPage) {
        return popup(zulPage, true, true);
    }
    
    /**
     * Create popup window with specified parent and title and with default attributes. Defaults are
     * used: reference loadDefaults()
     * 
     * @param owner Component that requested creation (may be null)
     * @param title Window title
     */
    public PopupDialog(final Component owner, final String title) {
        super();
        loadDefaults();
        setTitle(title);
        
        if (owner != null) {
            setPage(owner.getPage());
        } else {
            setPage(ExecutionsCtrl.getCurrentCtrl().getCurrentPage());
        }
    }
    
    /**
     * Empty Constructors are useful when you don't have a handle on the owner yet ZK's ZUL
     * components follow this convention in it's API Defaults are used: reference loadDefaults()
     */
    public PopupDialog() {
        super();
        loadDefaults();
    }
    
    private void loadDefaults() {
        setContentStyle("overflow:auto");
        setVisible(false);
        setClosable(true);
        setSizable(true);
        setMaximizable(true);
        PopupManager.getInstance().registerPopup(this);
        addEventListener(Events.ON_SIZE, new EventListener<Event>() {
            
            @Override
            public void onEvent(Event event) throws Exception {
                SizeEvent sizeEvent = (SizeEvent) event;
                onResize(sizeEvent.getHeight(), sizeEvent.getWidth());
            }
            
        });
    }
    
    /**
     * Show the window modally.
     */
    public void show() {
        try {
            if (getWidth() == null && Executions.getCurrent().getBrowser("ie") != null) { // Hack to fix window sizing issue in IE
                HtmlBasedComponent child = ZKUtil.findChild(this, HtmlBasedComponent.class, null);
                
                if (child != null) {
                    setWidth(child.getWidth());
                }
            }
            
            doModal();
        } catch (final Exception e) {}
    }
    
    /**
     * Returns true if window action was canceled.
     * 
     * @return True if window action was canceled.
     */
    public boolean isCanceled() {
        return cancelled;
    }
    
    /**
     * Fired when the window is resized. Override to perform any special reformatting.
     * 
     * @param newHeight New height of window.
     * @param newWidth New width of window.
     */
    public void onResize(String newHeight, String newWidth) {
    }
    
    /**
     * Closes the window with the specify cancel status.
     * 
     * @param canceled Cancel status for the closed window.
     */
    public void close(final boolean canceled) {
        this.cancelled = canceled;
        onClose();
        detach();
    }
    
    /**
     * Returns a reference to the application framework.
     * 
     * @return Application framework
     */
    protected AppFramework getAppFramework() {
        return appFramework;
    }
    
    /**
     * Returns the application context associated with the current framework instance.
     * 
     * @return An application context instance.
     */
    protected ApplicationContext getAppContext() {
        return appContext;
    }
    
}
