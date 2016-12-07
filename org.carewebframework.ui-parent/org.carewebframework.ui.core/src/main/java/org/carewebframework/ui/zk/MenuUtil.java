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

import java.util.List;

import org.carewebframework.common.MiscUtil;
import org.carewebframework.ui.zk.ZKUtil.MatchMode;
import org.carewebframework.web.component.BaseComponent;
import org.carewebframework.web.component.BaseLabeledImageComponent;
import org.carewebframework.web.component.Menu;
import org.carewebframework.web.component.Menuitem;
import org.carewebframework.web.component.Menupopup;
import org.carewebframework.web.component.Toolbar;

/**
 * Useful menu functions.
 */
public class MenuUtil {
    
    private static final String[] NULL_PATH = { "null" };
    
    /**
     * Adds a ZK menu item to the main menu.
     * 
     * @param path Determines the position of the menu item in the menu tree.
     * @param menuItem The menu item to be added. If null, a new menu item is created.
     * @param menubar The menubar to receive the new menu item.
     * @param insertBefore The sibling before which the new menu item is to be inserted. If null, or
     *            the specified component is not at the same level as the item to be added, the new
     *            menu item will be added after any existing siblings.
     * @return The menu item added. If the menuItem parameter was not null, this is the value
     *         returned. Otherwise, it is a reference to the newly created menu item.
     */
    public static Menuitem addMenuItem(String path, Menuitem menuItem, Toolbar menubar, BaseComponent insertBefore) {
        return addMenuOrMenuItem(path, menuItem, menubar, insertBefore, Menuitem.class);
    }
    
    /**
     * Adds a ZK menu to the main menu.
     * 
     * @param path Determines the position of the menu in the menu tree.
     * @param menu The menu to be added. If null, a new menu is created.
     * @param menubar The menubar to receive the new menu.
     * @param insertBefore The sibling before which the new menu is to be inserted. If null, or the
     *            specified component is not at the same level as the menu to be added, the new menu
     *            will be added after any existing siblings.
     * @return The menu added. If the menu parameter was not null, this is the value returned.
     *         Otherwise, it is a reference to the newly created menu.
     */
    public static Menu addMenu(String path, Menu menu, Toolbar menubar, BaseComponent insertBefore) {
        return addMenuOrMenuItem(path, menu, menubar, insertBefore, Menu.class);
    }
    
    /**
     * Adds an element (menu or menu item) to the main menu.
     *
     * @param <T> Class of element to be added.
     * @param path Determines the position of the element in the menu tree.
     * @param ele The element to be added. If null, a new element of the specified type is created.
     * @param menubar The menubar to receive the new element.
     * @param insertBefore The sibling before which the new element is to be inserted. If null, or
     *            the specified component is not at the same level as the element to be added, the
     *            new element will be added after any existing siblings.
     * @param clazz Class of the element to be added.
     * @return The element that was added.
     */
    public static <T extends BaseLabeledImageComponent> T addMenuOrMenuItem(String path, T ele, Toolbar menubar,
                                                                            BaseComponent insertBefore, Class<T> clazz) {
        String pcs[] = path == null ? NULL_PATH : path.split(Constants.PATH_DELIMITER_REGEX);
        int last = pcs.length - 1;
        BaseComponent parent = menubar;
        
        if (ele == null) {
            try {
                ele = clazz.newInstance();
            } catch (Exception e) {
                throw MiscUtil.toUnchecked(e);
            }
        }
        
        for (int i = 0; i < last; i++) {
            parent = findMenu(parent, pcs[i], insertBefore);
        }
        
        ele.setLabel(pcs[last]);
        parent = getRealParent(parent);
        
        if (insertBefore != null && parent == insertBefore.getParent()) {
            parent.addChild(ele, insertBefore);
        } else {
            parent.addChild(ele);
        }
        
        return ele;
    }
    
    /**
     * Returns a parent that can receive a menu item.
     * 
     * @param parent The parent component.
     * @return The true parent.
     */
    private static BaseComponent getRealParent(BaseComponent parent) {
        if (!(parent instanceof Menu)) {
            return parent;
        }
        
        Menupopup menuPopup = parent.getChild(Menupopup.class);
        
        if (menuPopup == null) {
            menuPopup = new Menupopup();
            menuPopup.setParent(parent);
        }
        
        return menuPopup;
    }
    
    /**
     * Recursively remove empty menu container elements. This is done after removing menu items to
     * keep the menu structure lean.
     * 
     * @param parent The starting menu container.
     */
    public static void pruneMenus(BaseComponent parent) {
        while (parent != null && !(parent instanceof Toolbar)) {
            if (parent.getChildren().isEmpty()) {
                BaseComponent newParent = parent.getParent();
                parent.detach();
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
     * @return Menu with the specified label.
     */
    public static Menu findMenu(BaseComponent parent, String label, BaseComponent insertBefore) {
        BaseComponent realParent = getRealParent(parent);
        
        for (Menu child : realParent.getChildren(Menu.class)) {
            if (child.getLabel().equalsIgnoreCase(label)) {
                return child;
            }
        }
        
        Menu menu = new Menu();
        menu.setLabel(label);
        
        if (insertBefore != null && realParent == insertBefore.getParent()) {
            realParent.addChild(menu, insertBefore.getIndex());
        } else {
            realParent.addChild(menu);
        }
        
        return menu;
    }
    
    /**
     * Returns the menu associated with the specified \-delimited path.
     * 
     * @param menubar Menu bar to search.
     * @param path \-delimited path to search.
     * @param create If true, menus are created if they do not already exist.
     * @param clazz Class of menu to create.
     * @param matchMode The match mode.
     * @return The menu corresponding to the specified path, or null if not found.
     */
    public static Menu findMenu(Toolbar menubar, String path, boolean create, Class<? extends Menu> clazz,
                                MatchMode matchMode) {
        return ZKUtil.findNode(menubar, Menupopup.class, clazz, path, create, matchMode);
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
            
            if (item1 instanceof BaseLabeledImageComponent && item2 instanceof BaseLabeledImageComponent
                    && ((BaseLabeledImageComponent) item1).getLabel()
                            .compareToIgnoreCase(((BaseLabeledImageComponent) item2).getLabel()) > 0) {
                parent.addChild(item2, item1);
                
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
    public static String getPath(BaseLabeledImageComponent comp) {
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
        if (comp == null || comp instanceof Toolbar) {
            return;
        }
        
        getPath(comp.getParent(), sb);
        
        if (comp instanceof BaseLabeledImageComponent) {
            sb.append(Constants.PATH_DELIMITER).append(((BaseLabeledImageComponent) comp).getLabel());
        }
    }
    
    /**
     * Opens a menu and all menus that precede it.
     * 
     * @param menu Menu to open.
     * @return True if the menu was opened.
     */
    public static void open(Menu menu) {
        BaseComponent parent = menu.getParent();
        
        if (parent instanceof Menu) {
            ((Menu) parent).open();
            open((Menu) parent);
        }
    }
    
    /**
     * Closes a menu.
     * 
     * @param menu Menu to close.
     */
    public static void close(Menu menu) {
    }
    
    /**
     * Closes the top level menu popup.
     * 
     * @param comp BaseComponent within menu tree.
     */
    private static void closeMenu(BaseComponent comp) {
    }
    
    /**
     * Enforce static class.
     */
    private MenuUtil() {
    }
}
