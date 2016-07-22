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
package org.carewebframework.ui.zk;

import java.util.Hashtable;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.carewebframework.api.AppFramework;
import org.carewebframework.api.FrameworkUtil;
import org.carewebframework.web.component.BaseComponent;
import org.carewebframework.web.component.Page;
import org.carewebframework.web.component.Window;
import org.carewebframework.web.core.ExecutionContext;
import org.carewebframework.web.page.PageDefinition;
import org.springframework.context.ApplicationContext;

/**
 * Base class for a popup window.
 */
public class PopupDialog extends Window {
    
    private static final Log log = LogFactory.getLog(PopupDialog.class);
    
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
    public static Window popup(PageDefinition zulPageDefinition, Map<Object, Object> args, boolean closable,
                               boolean sizable) {
        return popup(zulPageDefinition, args, closable, sizable, true);
    }
    
    /**
     * Can be used to popup any zul page definition as a modal dialog.
     * 
     * @param pageDefinition Page definition used to construct the dialog.
     * @param args Argument list (may be null)
     * @param closable If true, window closure button appears.
     * @param sizable If true, window sizing grips appear.
     * @param show If true, the window is displayed modally. If false, the window is created but not
     *            displayed.
     * @return Reference to the opened window, if successful.
     */
    public static Window popup(PageDefinition pageDefinition, Map<Object, Object> args, boolean closable, boolean sizable,
                               boolean show) {
        Window parent = new Window(); // Temporary parent in case createComponents fails, so can cleanup.
        Page currentPage = ExecutionContext.getPage();
        parent.setParent(currentPage);
        Window window = null;
        
        try {
            pageDefinition.materialize(parent);
            window = ZKUtil.findChild(parent, Window.class);
            
            if (window != null) { // If any top component is a window, discard temp parent
                window.setParent(null);
                BaseComponent child;
                
                while ((child = parent.getFirstChild()) != null) {
                    child.setParent(window);
                }
                
                parent.detach();
                window.setParent(currentPage);
            } else { // Otherwise, use the temp parent as the window
                window = parent;
            }
            
            window.setClosable(closable);
            window.setSizable(sizable);
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
    public static Window popup(PageDefinition zulPageDefinition) {
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
    public static Window popup(String zulPage, boolean closable, boolean sizable) {
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
    public static Window popup(String zulPage, boolean closable, boolean sizable, boolean show) {
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
    public static Window popup(String zulPage, Map<Object, Object> args, boolean closable, boolean sizable, boolean show) {
        if (args == null) {
            args = new Hashtable<>();
        }
        
        try {
            PageDefinition pageDefinition = ZKUtil.loadPageDefinition(zulPage);
            return popup(pageDefinition, args, closable, sizable, show);
        } catch (Exception e) {
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
    public static Window popup(String zulPage) {
        return popup(zulPage, true, true);
    }
    
    /**
     * Create popup window with specified parent and title and with default attributes. Defaults are
     * used: reference loadDefaults()
     * 
     * @param owner Component that requested creation (may be null)
     * @param title Window title
     */
    public PopupDialog(BaseComponent owner, String title) {
        super();
        loadDefaults();
        setTitle(title);
        
        if (owner != null) {
            setParent(owner.getPage());
        } else {
            setParent(ExecutionContext.getPage());
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
        //setContentStyle("overflow:auto");
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
            doModal();
        } catch (Exception e) {}
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
     * Closes the window with the specified cancel status.
     * 
     * @param canceled Cancel status for the closed window.
     */
    public void close(boolean canceled) {
        this.cancelled = canceled;
        close();
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
