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
package org.carewebframework.ui.dialog;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.fujion.client.ExecutionContext;
import org.fujion.component.BaseComponent;
import org.fujion.component.Page;
import org.fujion.component.Window;
import org.fujion.event.IEventListener;
import org.fujion.event.ResizeEvent;
import org.fujion.page.PageUtil;

/**
 * Base class for a popup window.
 */
public class PopupDialog extends Window {
    
    private static final Log log = LogFactory.getLog(PopupDialog.class);
    
    private boolean cancelled = true;
    
    /**
     * Can be used to popup any page definition as a modal dialog.
     * 
     * @param dialog String Page used to construct the dialog.
     * @param args Argument list (may be null)
     * @param closable If true, window closure button appears.
     * @param sizable If true, window sizing grips appear.
     * @param show If true, the window is displayed modally. If false, the window is created but not
     *            displayed.
     * @param closeListener Called upon window closure.
     * @return Reference to the opened window, if successful.
     */
    public static Window show(String dialog, Map<String, Object> args, boolean closable, boolean sizable, boolean show,
                              IEventListener closeListener) {
        Window parent = new Window(); // Temporary parent in case materialize fails, so can cleanup.
        Page currentPage = ExecutionContext.getPage();
        parent.setParent(currentPage);
        Window window = null;
        
        try {
            PageUtil.createPage(dialog, parent, args);
            window = parent.getChild(Window.class);
            
            if (window != null) { // If any top component is a window, discard temp parent
                window.setParent(null);
                BaseComponent child;
                
                while ((child = parent.getFirstChild()) != null) {
                    child.setParent(window);
                }
                
                parent.destroy();
                window.setParent(currentPage);
            } else { // Otherwise, use the temp parent as the window
                window = parent;
            }
            
            window.setClosable(closable);
            window.setSizable(sizable);
            
            if (show) {
                window.modal(closeListener);
            }
        } catch (Exception e) {
            if (window != null) {
                window.destroy();
                window = null;
            }
            
            if (parent != null) {
                parent.destroy();
            }
            
            DialogUtil.showError(e);
            log.error("Error materializing page", e);
        }
        
        return window;
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
        addEventListener(ResizeEvent.TYPE, (event) -> {
            ResizeEvent sizeEvent = (ResizeEvent) event;
            onResize(sizeEvent.getHeight(), sizeEvent.getWidth());
        });
    }
    
    /**
     * Show the window modally.
     * 
     * @param closeListener The close listener.
     */
    public void show(IEventListener closeListener) {
        try {
            modal(closeListener);
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
    public void onResize(double newHeight, double newWidth) {
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
    
}
