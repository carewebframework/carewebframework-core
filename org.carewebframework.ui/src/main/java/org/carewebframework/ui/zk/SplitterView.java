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

import java.io.IOException;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.UiException;
import org.zkoss.zk.ui.event.AfterSizeEvent;
import org.zkoss.zk.ui.sys.ContentRenderer;
import org.zkoss.zul.impl.XulElement;

/**
 * Component that is based on (but does not extend) ZK's BorderLayout component. Unlike
 * BorderLayout, it may have any number of splitter pane components (similar to layout regions).
 */
public class SplitterView extends XulElement {
    
    private static final long serialVersionUID = 1L;
    
    private int currentHeight;
    
    private int currentWidth;
    
    private boolean _horizontal = true;
    
    public SplitterView() {
        super();
    }
    
    public SplitterView(boolean horizontal) {
        this();
        _horizontal = horizontal;
    }
    
    public void onAfterSize(AfterSizeEvent event) {
        currentHeight = event.getHeight();
        currentWidth = event.getWidth();
    }
    
    /**
     * Re-size this layout component.
     */
    public void resize() {
        smartUpdate("resize", true);
    }
    
    /**
     * Adds the horizontal property to those passed to the client.
     * 
     * @see org.zkoss.zk.ui.HtmlBasedComponent#renderProperties(org.zkoss.zk.ui.sys.ContentRenderer)
     */
    @Override
    protected void renderProperties(ContentRenderer renderer) throws IOException {
        super.renderProperties(renderer);
        renderer.render("horizontal", _horizontal);
    }
    
    /**
     * A splitter view may only contain splitter panes. A runtime exception is raised if the child
     * component is of any other type.
     * 
     * @see org.zkoss.zk.ui.AbstractComponent#beforeChildAdded(org.zkoss.zk.ui.Component,
     *      org.zkoss.zk.ui.Component)
     */
    @Override
    public void beforeChildAdded(Component child, Component refChild) {
        if (!(child instanceof SplitterPane)) {
            throw new UiException("Unsupported child for SplitterView: " + child);
        }
        
        ((SplitterPane) child).setHorizontal(_horizontal);
        super.beforeChildAdded(child, refChild);
    }
    
    /*package*/int getCurrentHeight() {
        return currentHeight;
    }
    
    /*package*/int getCurrentWidth() {
        return currentWidth;
    }
    
    @Override
    public String getZclass() {
        return _zclass == null ? "cwf-splitterview" : _zclass;
    }
    
    public void setHorizontal(boolean horizontal) {
        if (_horizontal != horizontal) {
            _horizontal = horizontal;
            
            for (Component child : getChildren()) {
                ((SplitterPane) child).setHorizontal(horizontal);
            }
            
            smartUpdate("horizontal", horizontal);
            
        }
    }
    
    public boolean isHorizontal() {
        return _horizontal;
    }
}
