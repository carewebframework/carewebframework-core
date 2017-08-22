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
package org.carewebframework.ui.util;

import java.util.List;

import org.carewebframework.common.MiscUtil;
import org.carewebframework.web.component.BaseComponent;
import org.carewebframework.web.component.BaseMenuComponent;
import org.carewebframework.web.component.Menu;
import org.carewebframework.web.component.Menuitem;

/**
 * Useful menu functions.
 */
public class MenuUtil {

    private static final String[] NULL_PATH = { "null" };

    /**
     * Adds an element (menu or menu item) to the main menu.
     *
     * @param path Determines the position of the element in the menu tree.
     * @param ele The element to be added. If null, a new element of the specified type is created.
     * @param parent The parent to receive the new element.
     * @param insertBefore The sibling before which the new element is to be inserted. If null, or
     *            the specified component is not at the same level as the element to be added, the
     *            new element will be added after any existing siblings.
     * @return The element that was added.
     */
    public static BaseMenuComponent addMenuOrMenuItem(String path, BaseMenuComponent ele, BaseComponent parent,
                                                      BaseComponent insertBefore) {
        String pcs[] = path == null ? NULL_PATH : path.split("\\\\");
        int last = pcs.length - 1;

        for (int i = 0; i < last; i++) {
            parent = findMenu(parent, pcs[i], insertBefore);
        }

        ele = ele == null ? createMenuOrMenuitem(parent) : ele;
        ele.setLabel(pcs[last]);
        parent.addChild(ele, insertBefore);
        return ele;
    }

    /**
     * Recursively remove empty menu container elements. This is done after removing menu items to
     * keep the menu structure lean.
     *
     * @param parent The starting menu container.
     */
    public static void pruneMenus(BaseComponent parent) {
        while (parent != null && parent instanceof BaseMenuComponent) {
            if (parent.getChildren().isEmpty()) {
                BaseComponent newParent = parent.getParent();
                parent.destroy();
                parent = newParent;
            } else {
                break;
            }
        }
    }

    /**
     * Returns the menu with the specified label, or creates one if it does not exist.
     *
     * @param parent The parent component under which to search. May be a Toolbar or a Menupopup
     *            component.
     * @param label Label of menu to search.
     * @param insertBefore If not null, the new menu is inserted before this one. If null, the menu
     *            is appended.
     * @return Menu or menu item with the specified label.
     */
    public static BaseMenuComponent findMenu(BaseComponent parent, String label, BaseComponent insertBefore) {

        for (BaseMenuComponent child : parent.getChildren(BaseMenuComponent.class)) {
            if (label.equalsIgnoreCase(child.getLabel())) {
                return child;
            }
        }

        BaseMenuComponent cmp = createMenuOrMenuitem(parent);
        cmp.setLabel(label);
        parent.addChild(cmp, insertBefore);
        return cmp;
    }

    private static BaseMenuComponent createMenuOrMenuitem(BaseComponent parent) {
        try {
            Class<?> clazz = parent instanceof BaseMenuComponent ? Menuitem.class : Menu.class;
            return (BaseMenuComponent) clazz.newInstance();
        } catch (Exception e) {
            throw MiscUtil.toUnchecked(e);
        }
    }

    /**
     * Alphabetically sorts a range of menu items.
     *
     * @param parent Parent whose children are to be sorted alphabetically.
     * @param startIndex Index of first child to be sorted.
     * @param endIndex Index of last child to be sorted.
     */
    public static void sortMenu(BaseComponent parent, int startIndex, int endIndex) {
        List<BaseComponent> items = parent.getChildren();
        int bottom = startIndex + 1;

        for (int i = startIndex; i < endIndex;) {
            BaseComponent item1 = items.get(i++);
            BaseComponent item2 = items.get(i);

            if (item1 instanceof BaseMenuComponent && item2 instanceof BaseMenuComponent && ((BaseMenuComponent) item1)
                    .getLabel().compareToIgnoreCase(((BaseMenuComponent) item2).getLabel()) > 0) {
                parent.swapChildren(i - 1, i);

                if (i > bottom) {
                    i -= 2;
                }
            }
        }
    }

    /**
     * Returns the path of the given menu or menu item.
     *
     * @param comp A menu or menu item.
     * @return The full path of the menu item.
     */
    public static String getPath(BaseMenuComponent comp) {
        StringBuilder sb = new StringBuilder();
        getPath(comp, sb);
        return sb.toString();
    }

    /**
     * Recurses parent menu nodes to build the menu path.
     *
     * @param comp Current component in menu tree.
     * @param sb String builder to receive path.
     */
    private static void getPath(BaseComponent comp, StringBuilder sb) {
        while (comp instanceof BaseMenuComponent) {
            sb.insert(0, "\\" + ((BaseMenuComponent) comp).getLabel());
            comp = comp.getParent();
        }
    }

    /**
     * Enforce static class.
     */
    private MenuUtil() {
    }
}
