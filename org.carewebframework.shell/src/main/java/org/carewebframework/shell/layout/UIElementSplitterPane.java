/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.shell.layout;

import org.carewebframework.ui.zk.SplitterPane;

/**
 * A child of the UIElementSplitterView.
 */
public class UIElementSplitterPane extends UIElementZKBase {
    
    static {
        registerAllowedParentClass(UIElementSplitterPane.class, UIElementSplitterView.class);
        registerAllowedChildClass(UIElementSplitterPane.class, UIElementBase.class);
    }
    
    private final SplitterPane pane = new SplitterPane();
    
    private double size;
    
    private boolean relative;
    
    private boolean deserializing;
    
    public UIElementSplitterPane() {
        super();
        setRelative(true);
        setOuterComponent(pane);
    }
    
    public void setSize(double size) {
        this.size = size;
        updateSize();
    }
    
    public double getSize() {
        return relative ? pane.getRelativeSize() : pane.getAbsoluteSize();
    }
    
    @Override
    public String getInstanceName() {
        return "Pane #" + (getParent().indexOfChild(this) + 1);
    }
    
    @Override
    public void beforeInitialize(boolean deserializing) throws Exception {
        super.beforeInitialize(deserializing);
        this.deserializing = deserializing;
    }
    
    @Override
    public void afterInitialize(boolean deserializing) throws Exception {
        deserializing = false;
        super.afterInitialize(deserializing);
    }
    
    @Override
    public void afterParentChanged(UIElementBase oldParent) {
        if (!deserializing) {
            notifyParent((UIElementSplitterView) oldParent);
            notifyParent((UIElementSplitterView) getParent());
        }
    }
    
    private void notifyParent(UIElementSplitterView parent) {
        if (parent != null) {
            parent.adjustPanes();
        }
    }
    
    public void setRelative(boolean relative) {
        this.relative = relative;
        updateSize();
    }
    
    public boolean isRelative() {
        return relative;
    }
    
    public String getCaption() {
        return pane.getTitle();
    }
    
    public void setCaption(String caption) {
        pane.setTitle(caption);
    }
    
    private void updateSize() {
        if (relative) {
            pane.setRelativeSize(size);
        } else {
            pane.setAbsoluteSize((int) Math.round(size));
        }
    }
}
