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

import java.util.List;

import org.carewebframework.ui.zk.ZKUtil.MatchMode;

import org.zkoss.zk.ui.Component;
import org.zkoss.zul.Menu;
import org.zkoss.zul.Menubar;
import org.zkoss.zul.Menuitem;
import org.zkoss.zul.Menupopup;
import org.zkoss.zul.impl.LabelImageElement;

/**
 * Useful ZK menu functions.
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
    public static Menuitem addMenuItem(String path, Menuitem menuItem, Menubar menubar, Component insertBefore) {
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
    public static Menu addMenu(String path, Menu menu, Menubar menubar, Component insertBefore) {
        return addMenuOrMenuItem(path, menu, menubar, insertBefore, Menu.class);
    }
    
    /**
     * Adds an element (menu or menu item) to the main menu.
     * 
     * @param path Determines the position of the element in the menu tree.
     * @param ele The element to be added. If null, a new element of the specified type is created.
     * @param menubar The menubar to receive the new element.
     * @param insertBefore The sibling before which the new element is to be inserted. If null, or
     *            the specified component is not at the same level as the element to be added, the
     *            new element will be added after any existing siblings.
     * @param clazz Class of the element to be added.
     * @return The element that was added.
     */
    public static <T extends LabelImageElement> T addMenuOrMenuItem(String path, T ele, Menubar menubar,
                                                                    Component insertBefore, Class<T> clazz) {
        String pcs[] = path == null ? NULL_PATH : path.split(Constants.PATH_DELIMITER_REGEX);
        int last = pcs.length - 1;
        Component parent = menubar;
        
        if (ele == null) {
            try {
                ele = clazz.newInstance();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        
        for (int i = 0; i < last; i++) {
            parent = findMenu(parent, pcs[i], insertBefore);
        }
        
        ele.setLabel(pcs[last]);
        parent = getRealParent(parent);
        
        if (insertBefore != null && parent == insertBefore.getParent()) {
            parent.insertBefore(ele, insertBefore);
        } else {
            parent.appendChild(ele);
        }
        
        return ele;
    }
    
    /**
     * Returns a parent that can receive a menu item.
     * 
     * @param parent The parent component.
     * @return The true parent.
     */
    private static Component getRealParent(Component parent) {
        if (!(parent instanceof Menu)) {
            return parent;
        }
        
        Menupopup menuPopup = ZKUtil.findChild(parent, Menupopup.class);
        
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
    public static void pruneMenus(Component parent) {
        while (parent != null && !(parent instanceof Menubar)) {
            if (parent.getChildren().isEmpty()) {
                Component newParent = parent.getParent();
                parent.detach();
                parent = newParent;
            } else {
                break;
            }
        }
    }
    
    /**
     * Creates a menu based on the given path.
     * 
     * @param menubar The menu bar that will contain the menu.
     * @param path The menu path.
     * @param insertBefore If not null, the new menu is inserted before this one. If null, the menu
     *            is appended.
     * @return The menu corresponding to the specified path. All missing menu nodes are created
     *         automatically.
     */
    public static Menu createMenu(Menubar menubar, String path, Component insertBefore) {
        String[] pcs = path.split(Constants.PATH_DELIMITER_REGEX);
        Menu menu = null;
        
        for (int i = 0; i < pcs.length; i++) {
            menu = findMenu(menu == null ? menubar : menu, pcs[i], insertBefore);
        }
        
        return menu;
    }
    
    /**
     * Returns the menu with the specified label, or creates one if it does not exist.
     * 
     * @param parent The parent component under which to search. May be a Menubar or a Menupopup
     *            component.
     * @param label Label of menu to search.
     * @param insertBefore If not null, the new menu is inserted before this one. If null, the menu
     *            is appended.
     * @return Menu with the specified label.
     */
    public static Menu findMenu(Component parent, String label, Component insertBefore) {
        Component child = null;
        Component realParent = getRealParent(parent);
        
        while ((child = ZKUtil.findChild(realParent, Menu.class, child)) != null) {
            if (((Menu) child).getLabel().equalsIgnoreCase(label)) {
                return (Menu) child;
            }
        }
        
        Menu menu = new Menu();
        menu.setLabel(label);
        
        if (insertBefore != null && realParent == insertBefore.getParent()) {
            realParent.insertBefore(menu, insertBefore);
        } else {
            realParent.appendChild(menu);
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
    public static Menu findMenu(Menubar menubar, String path, boolean create, Class<? extends Menu> clazz,
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
    public static void sortMenu(Component parent, int startIndex, int endIndex) {
        List<?> items = parent.getChildren();
        int bottom = startIndex + 1;
        
        for (int i = startIndex; i < endIndex;) {
            Object item1 = items.get(i++);
            Object item2 = items.get(i);
            
            if (item1 instanceof LabelImageElement
                    && item2 instanceof LabelImageElement
                    && ((LabelImageElement) item1).getLabel().compareToIgnoreCase(((LabelImageElement) item2).getLabel()) > 0) {
                parent.insertBefore((LabelImageElement) item2, (LabelImageElement) item1);
                
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
    public static String getPath(LabelImageElement comp) {
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
    private static void getPath(Component comp, StringBuilder sb) {
        if (comp == null || comp instanceof Menubar) {
            return;
        }
        
        getPath(comp.getParent(), sb);
        
        if (comp instanceof LabelImageElement) {
            sb.append(Constants.PATH_DELIMITER).append(((LabelImageElement) comp).getLabel());
        }
    }
    
    /**
     * Closes a menu.
     * 
     * @param menu
     */
    public static void close(Menu menu) {
        Component target = menu.getMenupopup();
        closeMenu(target == null ? menu : target);
    }
    
    /**
     * Closes the top level menu popup.
     * 
     * @param comp Component within menu tree.
     */
    private static void closeMenu(Component comp) {
        Menupopup menuPopup = null;
        
        while (comp != null) {
            if (comp instanceof Menupopup) {
                menuPopup = (Menupopup) comp;
            } else if (comp instanceof Menubar) {
                break;
            }
            
            comp = comp.getParent();
        }
        
        if (menuPopup != null) {
            menuPopup.close();
        }
    }
    
    /**
     * Recursively synchronizes menu styles.
     * 
     * @param comp Current menu tree component.
     */
    public static void updateStyles(Component comp) {
        if (comp instanceof Menupopup) {
            boolean hasChildren = comp.getFirstChild() != null;
            ((Menupopup) comp).setZclass(hasChildren ? null : "cwf-menupopup-empty");
        } else if (comp instanceof Menu) {
            Component child = comp.getFirstChild();
            boolean hasChildren = child != null && child.getFirstChild() != null;
            ((Menu) comp).setZclass(hasChildren ? null : "z-menuitem");
        }
        
        for (Component child : comp.getChildren()) {
            updateStyles(child);
        }
    }
    
    /**
     * Enforce static class.
     */
    private MenuUtil() {
    }
}
