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
package org.carewebframework.shell.elements;

import org.carewebframework.shell.designer.PropertyEditorMenubar;
import org.carewebframework.shell.property.PropertyTypeRegistry;
import org.fujion.component.Span;

/**
 * Base implementation of a menu bar.
 */
public class ElementMenubar extends ElementUI {

    static {
        registerAllowedChildClass(ElementMenubar.class, ElementMenuItem.class, Integer.MAX_VALUE);
        registerAllowedParentClass(ElementMenubar.class, ElementUI.class);
        PropertyTypeRegistry.register("menuitems", PropertyEditorMenubar.class);
    }

    private final Span menubar;

    public ElementMenubar() {
        this(new Span());
    }

    public ElementMenubar(Span menubar) {
        setOuterComponent(this.menubar = menubar);
        menubar.addClass("cwf-menubar");
    }

    /**
     * Returns a reference to the menu bar.
     *
     * @return The menu bar.
     */
    public Span getMenubar() {
        return menubar;
    }

}
