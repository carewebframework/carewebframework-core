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

import org.fujion.common.MiscUtil;
import org.fujion.component.BaseLabeledImageComponent;
import org.fujion.component.Menu;
import org.fujion.component.Menuitem;

/**
 * Base class for representing a single menu item. The Menu class has been subclassed (MenuEx) to
 * allow it to behave as a menu and a menu item. This simplifies the creation and manipulation of
 * hierarchical menu trees.
 */
public class ElementMenuItem extends ElementActionBase {

    static {
        registerAllowedParentClass(ElementMenuItem.class, ElementMenubar.class);
        registerAllowedParentClass(ElementMenuItem.class, ElementMenuItem.class);
        registerAllowedChildClass(ElementMenuItem.class, ElementMenuItem.class, Integer.MAX_VALUE);
    }

    private BaseLabeledImageComponent<?> menu = new Menuitem();

    public ElementMenuItem() {
        super();
        autoHide = false;
        setOuterComponent(menu);
    }

    public String getLabel() {
        return menu.getLabel();
    }

    public void setLabel(String label) {
        menu.setLabel(label);
    }

    /**
     * The caption label is the instance name.
     */
    @Override
    public String getInstanceName() {
        return getLabel();
    }

    @Override
    public void bringToFront() {
        super.bringToFront();

        if (isDesignMode() && menu instanceof Menu) {
            ((Menu) menu).open();
        }
    }

    @Override
    protected void bind() {
        Class<?> clazz = getParent() instanceof ElementMenuItem ? Menuitem.class : Menu.class;

        if (!clazz.isInstance(menu)) {
            BaseLabeledImageComponent<?> oldMenu = menu;

            try {
                menu = (BaseLabeledImageComponent<?>) clazz.newInstance();
            } catch (Exception e) {
                throw MiscUtil.toUnchecked(e);
            }

            setOuterComponent(menu);
            rebindChildren();
            oldMenu.destroy();
            menu.setLabel(oldMenu.getLabel());
            menu.setImage(oldMenu.getImage());
            applyHint();
            applyColor();
            applyAction();
        }

        super.bind();
    }

    @Override
    protected void unbind() {
        menu.detach();
    }
}
