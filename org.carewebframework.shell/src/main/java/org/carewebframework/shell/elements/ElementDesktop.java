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

import org.apache.commons.lang.StringUtils;
import org.carewebframework.api.security.SecurityUtil;
import org.carewebframework.help.viewer.HelpUtil;
import org.carewebframework.help.viewer.HelpViewer.HelpViewerMode;
import org.carewebframework.shell.CareWebShell;
import org.carewebframework.shell.designer.DesignConstants;
import org.carewebframework.shell.designer.DesignMenu;
import org.carewebframework.shell.plugins.PluginResourceHelp;
import org.carewebframework.theme.ThemeUtil;
import org.carewebframework.ui.action.ActionUtil;
import org.carewebframework.ui.util.MenuUtil;
import org.fujion.annotation.EventHandler;
import org.fujion.annotation.WiredComponent;
import org.fujion.component.BaseComponent;
import org.fujion.component.BaseMenuComponent;
import org.fujion.component.BaseUIComponent;
import org.fujion.component.Cell;
import org.fujion.component.Image;
import org.fujion.component.Menu;
import org.fujion.component.Menuitem;
import org.fujion.component.Menuseparator;
import org.fujion.component.Span;
import org.fujion.component.Toolbar;

/**
 * This is the topmost component of the layout.
 */
public class ElementDesktop extends ElementUI {

    static {
        registerAllowedChildClass(ElementDesktop.class, ElementUI.class, Integer.MAX_VALUE);
    }

    private final BaseUIComponent desktopOuter;

    private String appId;

    @WiredComponent
    private BaseUIComponent desktopInner;

    @WiredComponent
    private Cell title;

    @WiredComponent
    private Span menubar0;

    @WiredComponent
    private Span menubar1;

    @WiredComponent
    private Span menubar2;

    @WiredComponent
    private Toolbar toolbar1;

    @WiredComponent
    private Image icon;

    @WiredComponent
    private BaseUIComponent titlebar;

    @WiredComponent
    private Menu helpMenu;

    @WiredComponent
    private Menuitem mnuTOC;

    @WiredComponent
    private Menuitem mnuAbout;

    @WiredComponent
    private Menuseparator helpSeparator;

    private final int fixedHelpItems;

    private boolean sortHelpMenu;

    private ThemeUtil.PanelStyle style = ThemeUtil.PanelStyle.DEFAULT;

    private final ElementMenubar menubar;

    private final ElementToolbar toolbar;

    private final CareWebShell shell;

    public ElementDesktop(CareWebShell shell) throws Exception {
        super();
        this.shell = shell;
        desktopOuter = createFromTemplate();
        setOuterComponent(desktopOuter);
        setInnerComponent(desktopInner);
        menubar = new ElementMenubar(menubar1);
        toolbar = new ElementToolbar(toolbar1);
        ActionUtil.addAction(mnuAbout, "groovy:org.carewebframework.shell.CareWebUtil.about();");
        ActionUtil.addAction(mnuTOC, "groovy:org.carewebframework.shell.CareWebUtil.showHelpTOC();");
        fixedHelpItems = helpMenu.getChildCount();
        sortHelpMenu = false;
        helpMenu.addEventListener("open", (event) -> {
            if (sortHelpMenu) {
                sortHelpMenu();
            }
        });

        if (SecurityUtil.isGrantedAny(DesignConstants.DESIGN_MODE_PRIVS)) {
            DesignMenu.create(this, menubar0);
        }

        addChild(menubar);
        addChild(toolbar);
        setTitle(getTitle());
        shell.addChild(desktopOuter);
    }

    /**
     * Returns the title text.
     *
     * @return The title text.
     */
    public String getTitle() {
        return title.getLabel();
    }

    /**
     * Sets the title text. This sets the title text of the desktop and the browser page.
     *
     * @param text The title text. Can be null;
     */
    public void setTitle(String text) {
        title.setLabel(text);

        if (desktopInner.getPage() != null) {
            desktopInner.getPage().setTitle(text);
        }
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
        ((BaseUIComponent) icon.getParent()).setVisible(!StringUtils.isEmpty(url));
    }

    /**
     * Returns the panel style to use for the desktop.
     *
     * @return The panel style.
     */
    public ThemeUtil.PanelStyle getStyle() {
        return style;
    }

    /**
     * Sets the panel style to use for the desktop.
     *
     * @param style The panel style.
     */
    public void setStyle(ThemeUtil.PanelStyle style) {
        this.style = style;
        desktopOuter.addClass("cwf-desktop " + style.getThemeClass());
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
    public ElementToolbar getToolbar() {
        return toolbar;
    }

    /**
     * Returns the desktop's menu bar.
     *
     * @return Desktop menu bar.
     */
    public ElementMenubar getMenubar() {
        return menubar;
    }

    /**
     * Overrides addChild to suppress onAddChild events for internally created children.
     */
    @Override
    public void addChild(ElementBase child) {
        addChild(child, child != toolbar && child != menubar);
    }

    /**
     * Adds a menu item to the main menu.
     *
     * @param namePath Determines the position of the menu item in the menu tree by caption text.
     * @param action The action to be executed when the menu item is clicked.
     * @param fixed If true, add to fixed menu. Otherwise, add to configurable menu.
     * @return The menu item added. If the menu parameter was not null, this is the value returned.
     *         Otherwise, it is a reference to the newly created menu item.
     */
    public BaseMenuComponent addMenu(String namePath, String action, boolean fixed) {
        BaseComponent menubar = fixed ? menubar2 : menubar1;
        BaseMenuComponent menu = MenuUtil.addMenuOrMenuItem(namePath, null, menubar, null);
        ActionUtil.addAction(menu, action);
        return menu;
    }

    /**
     * Alphabetically sorts the variable portion of the help menu. This is called on-the-fly when
     * the help menu is clicked and has been flagged for sorting.
     */
    private void sortHelpMenu() {
        sortHelpMenu = false;
        MenuUtil.sortMenu(helpMenu, fixedHelpItems, helpMenu.getChildCount() - 1);
    }

    /**
     * Adds a menu item to the help menu subtree. This action also sets an internal flag to cause
     * the help menu subtree to be automatically sorted when it is opened.
     *
     * @param namedPath Determines the position of the new menu item within the help menu subtree.
     * @param action The action to be invoked when the menu item is clicked.
     * @return The newly created menu item.
     */
    public Menuitem addHelpMenu(String namedPath, String action) {
        Menuitem menuitem = !StringUtils.isEmpty(namedPath) && !StringUtils.isEmpty(action)
                ? (Menuitem) addMenu(helpMenu.getLabel() + "\\" + namedPath, action, true) : null;
        sortHelpMenu |= menuitem != null;
        helpSeparator.setVisible(menuitem != null || helpSeparator.isVisible());
        return menuitem;
    }

    /**
     * Adds a menu item for the specified help resource.
     *
     * @param resource The help resource.
     * @return The newly created menu item.
     */
    public Menuitem addHelpMenu(PluginResourceHelp resource) {
        Menuitem menuitem = addHelpMenu(resource.getPath(), resource.getAction());
        mnuTOC.setVisible(menuitem != null);
        return menuitem;
    }

    /**
     * Remove all non-fixed items from help menu.
     */
    protected void clearHelpMenu() {
        while (helpMenu.getChildCount() > fixedHelpItems) {
            helpMenu.getChildren().remove(fixedHelpItems);
        }

        mnuTOC.setVisible(false);
        helpSeparator.setVisible(false);
    }

    /**
     * Returns the help viewer display mode for this desktop.
     *
     * @return The help viewer display mode.
     */
    public HelpViewerMode getHelpViewerMode() {
        return HelpUtil.getViewerMode(desktopOuter.getPage());
    }

    /**
     * Sets the help viewer display mode for this desktop.
     *
     * @param mode The new help viewer display mode.
     */
    public void setHelpViewerMode(HelpViewerMode mode) {
        HelpUtil.setViewerMode(desktopOuter.getPage(), mode);
    }

    @EventHandler(value = "attach", target = "@desktopInner")
    private void onAttach() {
        desktopInner.getPage().setTitle(title.getLabel());
    }
}
