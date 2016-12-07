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
package org.carewebframework.shell.layout;

import org.carewebframework.shell.designer.PropertyEditorMenubar;
import org.carewebframework.shell.property.PropertyTypeRegistry;
import org.carewebframework.web.component.Span;

/**
 * Implements a shared menubar.
 */
public class UIElementMenubar extends UIElementMenuBase {
    
    static {
        registerAllowedChildClass(UIElementMenubar.class, UIElementMenuItem.class);
        registerAllowedParentClass(UIElementMenubar.class, UIElementBase.class);
        PropertyTypeRegistry.register("menuitems", PropertyEditorMenubar.class);
    }
    
    /**
     * Creates the menu bar UI element. This consists of a ZK menu bar component. A help menu is
     * automatically created a pre-populated with references to the about dialog and table of
     * contents. We also attach an onOpen event handler to the help menu and use this to do
     * just-in-time sorting of dynamically added items.
     */
    public UIElementMenubar() {
        this(new Span());
    }
    
    public UIElementMenubar(Span menubar) {
        super(menubar);
    }
    
    @Override
    public void setDesignMode(boolean designMode) {
        super.setDesignMode(designMode);
        getMenubar().addStyle("min-width", designMode ? "40px" : null);
    }
    
}
