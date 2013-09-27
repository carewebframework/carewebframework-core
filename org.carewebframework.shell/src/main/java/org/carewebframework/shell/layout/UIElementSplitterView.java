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

import org.carewebframework.shell.designer.PropertyEditorSplitterView;
import org.carewebframework.shell.property.PropertyTypeRegistry;
import org.carewebframework.ui.zk.SplitterView;

/**
 * A splitter view has either a vertical or horizontal orientation and can contain any number of
 * splitter panes which are placed side-by-side with splitter bars in between for manual sizing.
 */
public class UIElementSplitterView extends UIElementZKBase {
    
    static {
        registerAllowedParentClass(UIElementSplitterView.class, UIElementBase.class);
        registerAllowedChildClass(UIElementSplitterView.class, UIElementSplitterPane.class);
        PropertyTypeRegistry.register("panes", null, PropertyEditorSplitterView.class);
    }
    
    public enum Orientation {
        horizontal, vertical
    };
    
    private final SplitterView root = new SplitterView();
    
    private Orientation orientation;
    
    public UIElementSplitterView() {
        super();
        maxChildren = Integer.MAX_VALUE;
        setOrientation("horizontal");
        setOuterComponent(root);
    }
    
    public void setOrientation(String orientation) {
        this.orientation = Orientation.valueOf(orientation);
        root.setHorizontal(this.orientation == Orientation.horizontal);
    }
    
    public String getOrientation() {
        return orientation.toString();
    }
    
    public boolean isHorizontal() {
        return orientation == Orientation.horizontal;
    }
    
    /**
     * Adjust panes with relative dimensions after adding or removing a pane.
     */
    /*package*/void adjustPanes() {
        int relCount = 0;
        
        for (UIElementBase child : getChildren()) {
            UIElementSplitterPane splitterPane = (UIElementSplitterPane) child;
            
            if (splitterPane.isRelative()) {
                relCount++;
            }
        }
        
        if (relCount > 0) {
            double size = 100.0 / relCount;
            
            for (UIElementBase child : getChildren()) {
                UIElementSplitterPane splitterPane = (UIElementSplitterPane) child;
                
                if (splitterPane.isRelative()) {
                    splitterPane.setSize(size);
                }
            }
        }
    }
    
}
