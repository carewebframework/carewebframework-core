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

import org.zkoss.lang.Objects;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.UiException;
import org.zkoss.zk.ui.event.AfterSizeEvent;
import org.zkoss.zk.ui.sys.ContentRenderer;
import org.zkoss.zul.impl.XulElement;

/**
 * A single pane on a splitter view component. This is based on (but does not extend) the layout
 * region component. Unlike layout region, any number of panes may appear on a splitter view.
 */
public class SplitterPane extends XulElement {
    
    private static final long serialVersionUID = 1L;
    
    private boolean _horizontal = true;
    
    private int currentWidth = -1;
    
    private int currentHeight = -1;
    
    private final String _border = "normal";
    
    private String _title;
    
    private boolean _relative;
    
    private double _size = -1.0;
    
    public SplitterPane() {
        super();
    }
    
    @Override
    protected void renderProperties(ContentRenderer renderer) throws IOException {
        super.renderProperties(renderer);
        
        if (!"normal".equals(_border)) {
            render(renderer, "border", _border);
        }
        render(renderer, "title", _title);
    }
    
    @Override
    public void beforeChildAdded(Component child, Component refChild) {
        if (getChildren().size() > 0) {
            throw new UiException("Only one child is allowed: " + this);
        }
        super.beforeChildAdded(child, refChild);
    }
    
    @Override
    public void beforeParentChanged(Component parent) {
        if (parent != null && !(parent instanceof SplitterView)) {
            throw new UiException("Wrong parent: " + parent);
        }
        super.beforeParentChanged(parent);
    }
    
    @Override
    public String getZclass() {
        return _zclass == null ? (_horizontal ? "cwf-splitterpane-horz" : "cwf-splitterpane-vert") : _zclass;
    }
    
    @Override
    public void setWidth(String width) {
        throw new UnsupportedOperationException("readonly");
    }
    
    @Override
    public void setHeight(String height) {
        throw new UnsupportedOperationException("readonly");
    }
    
    public void onAfterSize(AfterSizeEvent event) {
        currentHeight = event.getHeight();
        currentWidth = event.getWidth();
    }
    
    /**
     * Returns the title.
     * <p>
     * Default: null.
     * 
     * @return The title.
     */
    public String getTitle() {
        return _title;
    }
    
    /**
     * Sets the title.
     * 
     * @param title
     */
    public void setTitle(String title) {
        if (!Objects.equals(_title, title)) {
            _title = title;
            smartUpdate("title", _title);
        }
    }
    
    /**
     * Returns the orientation of the pane.
     * 
     * @return If true, the pane has a horizontal orientation; if false, a vertical orientation.
     */
    public boolean isHorizontal() {
        return _horizontal;
    }
    
    /**
     * Sets the orientation of the pane. This may only be set by the parent splitter view.
     * 
     * @param horizontal If true, the panes are oriented horizontally; if false, vertically.
     */
    /*package*/void setHorizontal(boolean horizontal) {
        _horizontal = horizontal;
        updateSize();
    }
    
    /**
     * Sets the absolute size of this pane.
     * 
     * @param size Size in pixels.
     */
    public void setAbsoluteSize(int size) {
        updateSize(size, false);
    }
    
    /**
     * Returns the absolute size of the pane in pixels.
     * 
     * @return Size in pixels.
     */
    public int getAbsoluteSize() {
        return _horizontal ? currentWidth : currentHeight;
    }
    
    /**
     * Sets the relative size of this pane.
     * 
     * @param size Relative size in %.
     */
    public void setRelativeSize(double size) {
        updateSize(size, true);
    }
    
    /**
     * Returns the relative size of this pane.
     * 
     * @return Relative size in %.
     */
    public double getRelativeSize() {
        SplitterView parent = _relative ? (SplitterView) getParent() : null;
        
        return parent == null ? -1.0 : toPercentage(_horizontal ? currentWidth : currentHeight,
            _horizontal ? parent.getCurrentWidth() : parent.getCurrentHeight());
    }
    
    /**
     * Returns true if relative dimensions are in effect.
     * 
     * @return True if relative dimensions in effect.
     */
    public boolean isRelative() {
        return _relative;
    }
    
    /**
     * Send size update to client.
     * 
     * @param size The new size.
     * @param relative If true, this is a relative size (as %); if false, an absolute size (in px).
     */
    private void updateSize(double size, boolean relative) {
        if (size != _size || relative != _relative) {
            _size = size < 0.0 ? 0.0 : size;
            _relative = relative;
            updateSize();
        }
    }
    
    /**
     * Send current size to client.
     */
    private void updateSize() {
        if (_size < 0.0) {
            return;
        }
        
        String dim = _relative ? _size + "%" : Long.toString(Math.round(_size)) + "px";
        
        if (_horizontal) {
            super.setWidth(dim);
        } else {
            super.setHeight(dim);
        }
    }
    
    /**
     * Convert an absolute dimension to relative.
     * 
     * @param numerator Pane dimension.
     * @param denominator Parent view dimension.
     * @return The computed percentage.
     */
    private double toPercentage(double numerator, double denominator) {
        return denominator == 0.0 ? 0.0 : numerator / denominator * 100.0;
    }
    
}
