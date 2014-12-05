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

import org.apache.commons.lang.StringUtils;

import org.carewebframework.api.security.SecurityUtil;
import org.carewebframework.shell.CareWebShell;
import org.carewebframework.shell.designer.DesignConstants;
import org.carewebframework.shell.designer.DesignMenu;
import org.carewebframework.shell.plugins.PluginResourceHelp;
import org.carewebframework.ui.action.ActionListener;
import org.carewebframework.ui.zk.MenuEx;
import org.carewebframework.ui.zk.MenuUtil;
import org.carewebframework.ui.zk.ZKUtil;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.HtmlBasedComponent;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.OpenEvent;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Image;
import org.zkoss.zul.Label;
import org.zkoss.zul.Menu;
import org.zkoss.zul.Menubar;
import org.zkoss.zul.Menupopup;
import org.zkoss.zul.Menuseparator;
import org.zkoss.zul.Toolbar;

/**
 * This is the topmost component of the layout.
 */
public class UIElementDesktop extends UIElementZKBase {
    
    static {
        registerAllowedChildClass(UIElementDesktop.class, UIElementBase.class);
    }
    
    private final Component desktopOuter;
    
    private Component desktopInner;
    
    private String appId;
    
    private DesignMenu designMenu;
    
    private Label title;
    
    private HtmlBasedComponent titleCell;
    
    private Menubar menubar1;
    
    private Menubar menubar2;
    
    private Toolbar toolbar1;
    
    private Image icon;
    
    private Component titlebar;
    
    private Menu helpMenu;
    
    private Menupopup helpMenuRoot;
    
    private Menu mnuTOC;
    
    private Menu mnuAbout;
    
    private Menuseparator helpSeparator;
    
    private final int fixedHelpItems;
    
    private boolean sortHelpMenu;
    
    private final UIElementMenubar menubar;
    
    private final UIElementToolbar toolbar;
    
    private final CareWebShell shell;
    
    public UIElementDesktop(CareWebShell shell) throws Exception {
        super();
        this.shell = shell;
        maxChildren = Integer.MAX_VALUE;
        desktopOuter = createFromTemplate();
        setOuterComponent(desktopOuter);
        setInnerComponent(desktopInner);
        menubar = new UIElementMenubar(menubar1);
        toolbar = new UIElementToolbar(toolbar1);
        ActionListener.addAction(mnuAbout, "zscript:org.carewebframework.shell.CareWebUtil.about();");
        ActionListener.addAction(mnuTOC, "zscript:org.carewebframework.shell.help.HelpUtil.showTOC();");
        fixedHelpItems = helpMenuRoot.getChildren().size();
        sortHelpMenu = false;
        helpMenuRoot.addEventListener(Events.ON_OPEN, new EventListener<OpenEvent>() {
            
            @Override
            public void onEvent(OpenEvent event) throws Exception {
                if (sortHelpMenu && event.isOpen()) {
                    sortHelpMenu();
                }
            }
        });
        
        if (SecurityUtil.isGrantedAny(DesignConstants.DESIGN_MODE_PRIVS)) {
            designMenu = DesignMenu.create(this);
            menubar2.appendChild(designMenu);
        }
        
        addChild(menubar);
        addChild(toolbar);
        setTitle(getTitle());
        shell.appendChild(desktopOuter);
    }
    
    /**
     * Returns the title text.
     * 
     * @return The title text.
     */
    public String getTitle() {
        return title.getValue();
    }
    
    /**
     * Sets the title text. This sets the title text of the desktop and the browser page.
     * 
     * @param text The title text. Can be null;
     */
    public void setTitle(String text) {
        title.setValue(text);
        desktopOuter.getPage().setTitle(text);
        ZKUtil.toggleSclass(titleCell, "cwf-desktop-notitle", "cwf-desktop-title", StringUtils.isEmpty(text));
    }
    
    /**
     * Returns the url for the title bar icon.
     * 
     * @return Url of the title bar icon.
     */
    public String getIcon() {
        return icon.getSrc();
    }
    
    /**
     * Sets the url for the title bar icon.
     * 
     * @param url Url of the title bar icon.
     */
    public void setIcon(String url) {
        this.icon.setSrc(url);
        icon.getParent().setVisible(!StringUtils.isEmpty(url));
    }
    
    /**
     * Sets the application id of this instance.
     * 
     * @param appId The application id.
     */
    public void setAppId(String appId) {
        this.appId = appId;
    }
    
    /**
     * Returns the application id of this instance.
     * 
     * @return The application id.
     */
    public String getAppId() {
        return appId;
    }
    
    /**
     * Returns true if the application id specified matches the desktop's application id. Handles
     * nulls.
     * 
     * @param appId The application id.
     * @return True if the application ids match.
     */
    public boolean hasAppId(String appId) {
        return appId == null ? this.appId == null : appId.equals(this.appId);
    }
    
    /**
     * Returns the shell that contains this desktop.
     * 
     * @return The owning shell.
     */
    public CareWebShell getShell() {
        return shell;
    }
    
    /**
     * Resets the desktop by removing all children and help menu items.
     */
    public void clear() {
        removeChildren();
        clearHelpMenu();
        setTitle("");
        setIcon(null);
    }
    
    /**
     * Returns the desktop's tool bar.
     * 
     * @return Desktop tool bar.
     */
    public UIElementToolbar getToolbar() {
        return toolbar;
    }
    
    /**
     * Returns the desktop's menu bar.
     * 
     * @return Desktop menu bar.
     */
    public UIElementMenubar getMenubar() {
        return menubar;
    }
    
    /**
     * Overrides addChild to suppress onAddChild events for internally created children.
     */
    @Override
    public void addChild(UIElementBase child) {
        addChild(child, child != toolbar && child != menubar);
    }
    
    /**
     * Forces a recalculation of window size when activation state changes.
     */
    @Override
    public void activate(boolean activate) {
        Clients.resize(desktopOuter);
        super.activate(activate);
    }
    
    @Override
    public void setDesignMode(boolean designMode) {
        super.setDesignMode(designMode);
        Clients.resize(titlebar);
    }
    
    /**
     * Adds a ZK menu item to the main menu.
     * 
     * @param namePath Determines the position of the menu item in the menu tree by caption text.
     * @param action The action to be executed when the menu item is clicked.
     * @param fixed If true, add to fixed menu. Otherwise, add to configurable menu.
     * @return The menu item added. If the menu parameter was not null, this is the value returned.
     *         Otherwise, it is a reference to the newly created menu item.
     */
    public Menu addMenu(String namePath, String action, boolean fixed) {
        Menubar menubar = fixed ? menubar2 : menubar1;
        Menu menu = MenuUtil.addMenuOrMenuItem(namePath, null, menubar, null, MenuEx.class);
        MenuUtil.updateStyles(menu);
        ActionListener.addAction(menu, action);
        return menu;
    }
    
    /**
     * Alphabetically sorts the variable portion of the help menu. This is called on-the-fly when
     * the help menu is clicked and has been flagged for sorting.
     */
    private void sortHelpMenu() {
        sortHelpMenu = false;
        MenuUtil.sortMenu(helpMenuRoot, fixedHelpItems, helpMenuRoot.getChildren().size() - 1);
    }
    
    /**
     * Adds a menu item to the help menu subtree. This action also sets an internal flag to cause
     * the help menu subtree to be automatically sorted when it is opened.
     * 
     * @param namedPath Determines the position of the new menu item within the help menu subtree.
     * @param action The action to be invoked when the menu item is clicked.
     * @return The newly created menu item.
     */
    public Menu addHelpMenu(String namedPath, String action) {
        Menu menu = !StringUtils.isEmpty(namedPath) && !StringUtils.isEmpty(action) ? addMenu(helpMenu.getLabel() + "\\"
                + namedPath, action, true) : null;
        sortHelpMenu |= menu != null;
        helpSeparator.setVisible(menu != null || helpSeparator.isVisible());
        return menu;
    }
    
    /**
     * Adds a menu item for the specified help resource.
     * 
     * @param resource The help resource.
     * @return The newly created menu item.
     */
    public Menu addHelpMenu(PluginResourceHelp resource) {
        Menu menu = addHelpMenu(resource.getPath(), resource.getAction());
        mnuTOC.setVisible(menu != null);
        return menu;
    }
    
    /**
     * Remove all non-fixed items from help menu.
     */
    protected void clearHelpMenu() {
        while (helpMenuRoot.getChildren().size() > fixedHelpItems) {
            helpMenuRoot.getChildren().remove(fixedHelpItems);
        }
        
        mnuTOC.setVisible(false);
        helpSeparator.setVisible(false);
    }
    
}
