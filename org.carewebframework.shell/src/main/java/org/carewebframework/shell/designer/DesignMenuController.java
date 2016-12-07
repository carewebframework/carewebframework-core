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
package org.carewebframework.shell.designer;

import java.util.Collections;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.carewebframework.api.security.SecurityUtil;
import org.carewebframework.common.StrUtil;
import org.carewebframework.shell.CareWebShell;
import org.carewebframework.shell.layout.LayoutIdentifier;
import org.carewebframework.shell.layout.UIElementDesktop;
import org.carewebframework.shell.layout.UILayout;
import org.carewebframework.ui.dialog.DialogUtil;
import org.carewebframework.ui.xml.XMLViewer;
import org.carewebframework.web.ancillary.IAutoWired;
import org.carewebframework.web.annotation.EventHandler;
import org.carewebframework.web.annotation.WiredComponent;
import org.carewebframework.web.component.BaseComponent;
import org.carewebframework.web.component.BaseUIComponent;
import org.carewebframework.web.component.Menu;
import org.carewebframework.web.component.Menuitem;
import org.carewebframework.web.event.EventUtil;
import org.carewebframework.web.page.PageUtil;

/**
 * This is the design menu that appears in the desktop's menu bar.
 */
public class DesignMenuController implements IAutoWired {
    
    private static final Log log = LogFactory.getLog(DesignMenuController.class);
    
    private CareWebShell shell;
    
    private UIElementDesktop owner;
    
    @WiredComponent
    private Menu menu;
    
    @WiredComponent
    private Menuitem mnuDesignMode;
    
    @WiredComponent
    private Menuitem mnuShowMarkup;
    
    /**
     * Creates the design menu with the specified desktop as owner.
     * 
     * @param owner Desktop UI element owner.
     * @return Design menu instance.
     */
    public static BaseComponent createMenu(UIElementDesktop owner) {
        BaseComponent designMenu = null;
        
        try {
            Map<String, Object> args = Collections.singletonMap("owner", owner);
            designMenu = PageUtil.createPage(DesignConstants.RESOURCE_PREFIX + "DesignMenu.cwf", null, args).get(0);
        } catch (Exception e) {
            log.error("Error creating design menu.", e);
            
            if (designMenu != null) {
                designMenu.destroy();
                designMenu = null;
            }
        }
        return designMenu;
    }
    
    /**
     * Initialize the design menu.
     */
    @Override
    public void afterInitialized(BaseComponent comp) {
        this.owner = comp.getAttribute("owner", UIElementDesktop.class);
        shell = owner.getShell();
        
        if (!SecurityUtil.hasDebugRole()) {
            mnuShowMarkup.destroy();
        }
        
        updateMenus(false);
    }
    
    /**
     * Toggles design mode.
     */
    @EventHandler(value = "click", target = "mnuDesignMode")
    private void onClick$mnuDesignMode() {
        boolean enabled = !mnuDesignMode.isChecked();
        mnuDesignMode.setChecked(enabled);
        owner.setDesignMode(enabled);
        updateMenus(enabled);
        
        if (enabled) {
            EventUtil.post("afterEnabled", menu, null);
        } else {
            LayoutDesigner.closeDialog();
        }
    }
    
    @EventHandler(value = "afterEnabled", target = "@menu")
    private void onAfterEnabled() {
        menu.setOpen(true);
    }
    
    /**
     * Clear desktop.
     */
    @EventHandler(value = "click", target = "mnuClearDesktop")
    private void onClick$mnuClearDesktop() {
        DialogUtil.confirm(DesignConstants.MSG_DESKTOP_CLEAR, DesignConstants.CAP_DESKTOP_CLEAR, (confirm) -> {
            if (confirm) {
                shell.reset();
            }
        });
    }
    
    /**
     * Brings up property editor for desktop.
     * 
     * @throws Exception Unspecified exception.
     */
    @EventHandler(value = "click", target = "mnuDesktopProperties")
    private void onClick$mnuDesktopProperties() {
        PropertyGrid.create(owner, null);
    }
    
    /**
     * Brings up layout designer for desktop.
     * 
     * @throws Exception Unspecified exception.
     */
    @EventHandler(value = "click", target = "mnuLayoutDesigner")
    private void onClick$mnuLayoutDesigner() {
        LayoutDesigner.execute(owner);
    }
    
    /**
     * Brings up layout manager.
     * 
     * @throws Exception Unspecified exception.
     */
    @EventHandler(value = "click", target = "mnuLayoutManager")
    private void onClick$mnuLayoutManager() throws Exception {
        LayoutManager.show(true, shell.getUILayout().getName(), null);
    }
    
    /**
     * Performs logout.
     */
    @EventHandler(value = "click", target = "mnuLogout")
    private void onClick$mnuLogout() {
        shell.logout();
    }
    
    /**
     * Prompts to save a layout.
     * 
     * @throws Exception Unspecified exception.
     */
    @EventHandler(value = "click", target = "mnuSaveLayout")
    private void onClick$mnuSaveLayout() {
        LayoutManager.saveLayout(UILayout.serialize(owner),
            new LayoutIdentifier(shell.getUILayout().getName(), LayoutManager.defaultIsShared()), false);
    }
    
    /**
     * Prompts to load layout.
     * 
     * @throws Exception Unspecified exception.
     */
    @EventHandler(value = "click", target = "mnuLoadLayout")
    private void onClick$mnuLoadLayout() {
        LayoutManager.show(false, shell.getUILayout().getName(), (event) -> {
            LayoutIdentifier layoutId = event.getTarget().getAttribute("layoutId", LayoutIdentifier.class);
            
            if (layoutId != null) {
                UILayout newLayout = new UILayout();
                newLayout.loadFromProperty(layoutId);
                shell.buildUI(newLayout);
            }
        });
    }
    
    /**
     * Shows CWF markup for current page.
     */
    @EventHandler(value = "click", target = "mnuShowMarkup")
    private void onClick$mnuShowMarkup() {
        XMLViewer.showCWF(owner.getOuterComponent());
    }
    
    /**
     * Updates the visibility of menu items
     * 
     * @param enabled The enabled status.
     */
    private void updateMenus(boolean enabled) {
        menu.setImage(enabled ? DesignConstants.DESIGN_ICON_ACTIVE : DesignConstants.DESIGN_ICON_INACTIVE);
        mnuDesignMode.addStyle("border-bottom", enabled ? "2px solid lightgray" : null);
        menu.setHint(
            StrUtil.formatMessage(enabled ? DesignConstants.DESIGN_HINT_ACTIVE : DesignConstants.DESIGN_HINT_INACTIVE));
        BaseUIComponent child = (BaseUIComponent) menu.getFirstChild();
        
        while (child != null) {
            child.setVisible(enabled || child == mnuDesignMode);
            child = (BaseUIComponent) child.getNextSibling();
        }
    }
}
