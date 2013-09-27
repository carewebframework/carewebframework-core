/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.shell;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.carewebframework.api.AppFramework;
import org.carewebframework.api.FrameworkUtil;
import org.carewebframework.api.context.UserContext.IUserContextEvent;
import org.carewebframework.api.event.EventManager;
import org.carewebframework.api.event.IEventManager;
import org.carewebframework.api.property.PropertyUtil;
import org.carewebframework.api.security.SecurityUtil;
import org.carewebframework.api.spring.SpringUtil;
import org.carewebframework.help.IHelpSet;
import org.carewebframework.shell.help.HelpDefinition;
import org.carewebframework.shell.help.HelpUtil;
import org.carewebframework.shell.layout.UIElementDesktop;
import org.carewebframework.shell.layout.UIElementZKBase;
import org.carewebframework.shell.layout.UILayout;
import org.carewebframework.shell.plugins.PluginContainer;
import org.carewebframework.shell.plugins.PluginDefinition;
import org.carewebframework.shell.plugins.PluginResource.CSSResource;
import org.carewebframework.shell.plugins.PluginResource.HelpResource;
import org.carewebframework.ui.Application;
import org.carewebframework.ui.FrameworkWebSupport;
import org.carewebframework.ui.command.CommandEvent;
import org.carewebframework.ui.command.CommandRegistry;
import org.carewebframework.ui.command.CommandUtil;
import org.carewebframework.ui.zk.MessageWindow;
import org.carewebframework.ui.zk.PromptDialog;
import org.carewebframework.ui.zk.ZKUtil;

import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.ClientInfoEvent;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.KeyEvent;
import org.zkoss.zk.ui.ext.AfterCompose;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Div;
import org.zkoss.zul.Menu;
import org.zkoss.zul.Span;
import org.zkoss.zul.Style;
import org.zkoss.zul.impl.XulElement;

/**
 * Implements a generic UI shell that can be dynamically extended with plug-ins.
 */
public class CareWebShell extends Div implements AfterCompose {
    
    private static final long serialVersionUID = 1L;
    
    protected static final Log log = LogFactory.getLog(CareWebShell.class);
    
    public final String LBL_CONFIRM_CLOSE = Labels.getLabel("cwf.shell.confirmclose.message");
    
    public final String LBL_NO_LAYOUT = Labels.getLabel("cwf.shell.nolayout.message");
    
    public final String LBL_LOGOUT_CONFIRMATION = Labels.getLabel("cwf.shell.logout.confirmation.message");
    
    public final String LBL_LOGOUT_CONFIRMATION_CAPTION = Labels.getLabel("cwf.shell.logout.confirmation.caption");
    
    public final String LBL_LOGOUT_CANCEL = Labels.getLabel("cwf.shell.logout.cancel.message");
    
    private final AppFramework appFramework = FrameworkUtil.getAppFramework();
    
    private final IEventManager eventManager = EventManager.getInstance();
    
    private final CommandRegistry commandRegistry = SpringUtil.getBean("commandRegistry", CommandRegistry.class);
    
    private final List<PluginContainer> plugins = new ArrayList<PluginContainer>();
    
    private final List<HelpDefinition> helpModules = new ArrayList<HelpDefinition>();
    
    private final List<String> propertyGroups = new ArrayList<String>();
    
    private UILayout layout = new UILayout();
    
    private UIElementDesktop desktop;
    
    private final Component registeredStyles = new Span();
    
    private CareWebStartup startupRoutines;
    
    private MessageWindow messageWindow;
    
    private String defaultLayoutName;
    
    private final IUserContextEvent userContextListener = new IUserContextEvent() {
        
        /**
         * @see IUserContextEvent#canceled()
         */
        @Override
        public void canceled() {
        }
        
        /**
         * @see IUserContextEvent#committed()
         */
        @Override
        public void committed() {
            reset();
        }
        
        /**
         * Prompt user for logout confirmation (unless suppressed).
         * 
         * @see IUserContextEvent#pending
         */
        @Override
        public String pending(boolean silent) {
            if (silent || PromptDialog.confirm(LBL_LOGOUT_CONFIRMATION, LBL_LOGOUT_CONFIRMATION_CAPTION, "LOGOUT.CONFIRM")) {
                return null;
            }
            
            return LBL_LOGOUT_CANCEL;
        }
        
    };
    
    /**
     * Returns the application name for this instance of the CareWeb shell.
     * 
     * @return Application name, or null if not set.
     */
    public static String getApplicationName() {
        return FrameworkUtil.getAppName();
    }
    
    /**
     * Create the shell instance.
     */
    public CareWebShell() {
        super();
        CareWebUtil.setShell(this);
        Executions.getCurrent().getDesktop().enableServerPush(true);
    }
    
    /**
     * Perform additional initializations.
     */
    @Override
    public void afterCompose() {
        try {
            CommandRegistry.getInstance().setRoot(this);
            CommandUtil.associateCommand("help", this);
            appendChild(registeredStyles);
            appendChild(messageWindow = new MessageWindow());
            desktop = new UIElementDesktop(this);
            ZKUtil.suppressContextMenu(this);
            appFramework.registerObject(userContextListener);
            String confirmClose = FrameworkWebSupport.getFrameworkProperty("confirmClose", "CAREWEB.CONFIRMCLOSE");
            
            if (StringUtils.isEmpty(confirmClose) || BooleanUtils.toBoolean(confirmClose)) {
                Clients.confirmClose(LBL_CONFIRM_CLOSE);
            }
            
            String layout = defaultLayoutName != null ? defaultLayoutName : FrameworkWebSupport.getFrameworkProperty(
                "layout", "CAREWEB.LAYOUT.DEFAULT");
            
            if (!StringUtils.isEmpty(layout)) {
                loadLayoutFromResource(layout);
            }
            
        } catch (Exception e) {
            log.error("Error initializing the shell.", e);
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Handle help requests.
     * 
     * @param event
     */
    public void onCommand(CommandEvent event) {
        if ("help".equals(event.getCommandName())) {
            Component ref = event.getReference();
            HelpUtil.showCSH(ref == null ? event.getTarget() : ref);
        }
    }
    
    /**
     * Return information about the browser client.
     * 
     * @return Client information.
     */
    public ClientInfoEvent getClientInformation() {
        return Application.getDesktopInfo(getDesktop()).getClientInformation();
    }
    
    /**
     * Capture unhandled shortcut key press events.
     * 
     * @param event
     */
    public void onCtrlKey(Event event) {
        KeyEvent keyEvent = (KeyEvent) ZKUtil.getEventOrigin(event);
        String shortcut = CommandUtil.getShortcut(keyEvent);
        Collection<? extends XulElement> plugins = getActivatedPlugins(null);
        
        if (!plugins.isEmpty()) {
            commandRegistry.fireCommands(shortcut, keyEvent, plugins);
        }
    }
    
    /**
     * Returns a reference to the current UI desktop.
     * 
     * @return The current UI desktop.
     */
    public UIElementDesktop getUIDesktop() {
        return desktop;
    }
    
    /**
     * Returns a reference to the current UI layout.
     * 
     * @return The current UI layout.
     */
    public UILayout getUILayout() {
        return layout;
    }
    
    /**
     * Executed once all plugins are loaded.
     */
    public void start() {
        desktop.activate(true);
        String initialPlugin = PropertyUtil.getValue("CAREWEB.INITIAL.SECTION", getApplicationName());
        
        if (!StringUtils.isEmpty(initialPlugin)) {
            for (PluginContainer plugin : plugins) {
                if (initialPlugin.equals(plugin.getPluginDefinition().getId())) {
                    plugin.bringToFront();
                    break;
                }
            }
        }
        
        if (startupRoutines == null) {
            startupRoutines = SpringUtil.getBean("careWebStartup", CareWebStartup.class);
        }
        
        startupRoutines.execute();
    }
    
    /**
     * Loads a configuration from the specified resource.
     * 
     * @param resource URL of the resource containing the layout configuration.
     *            <p>
     *            If url of format "app:xxx", then layout associated with application id "xxx" is
     *            loaded.
     *            <p>
     *            If url of format "shared:xxx", then shared layout named "xxx" is loaded.
     *            <p>
     *            If url of format "private:xxx", then user layout named "xxx" is loaded.
     *            <p>
     *            Otherwise, resource is assumed to be a resource url.
     * @throws Exception
     */
    public void loadLayoutFromResource(String resource) throws Exception {
        if (resource.startsWith("app:")) {
            layout.loadByAppId(resource.substring(4));
        } else if (resource.startsWith("shared:")) {
            layout.loadFromProperty(resource.substring(7), true);
        } else if (resource.startsWith("private:")) {
            layout.loadFromProperty(resource.substring(8), false);
        } else {
            layout.loadFromUrl(resource);
        }
        
        buildUI(layout);
        FrameworkUtil.setAppName(layout.getName());
    }
    
    /**
     * Loads the layout associated with the given application id.
     * 
     * @param appId An application id.
     * @throws Exception
     */
    public void loadLayoutByAppId(String appId) throws Exception {
        FrameworkUtil.setAppName(appId);
        
        if (!layout.loadByAppId(appId).isEmpty()) {
            buildUI(layout);
        } else {
            PromptDialog.showError(LBL_NO_LAYOUT);
        }
    }
    
    /**
     * Loads a layout based on the appId query parameter setting.
     * 
     * @throws Exception
     */
    public void loadLayoutByAppId() throws Exception {
        loadLayoutByAppId(FrameworkWebSupport.getFrameworkProperty("appId", "CAREWEB.APPID.DEFAULT"));
    }
    
    /**
     * Returns the layout to be loaded.
     * 
     * @return
     */
    public String getLayout() {
        return defaultLayoutName;
    }
    
    /**
     * Sets the layout to be loaded. If null, the layout specified by the configuration will be
     * loaded.
     * 
     * @param defaultLayoutName
     * @throws Exception
     */
    public void setLayout(String defaultLayoutName) throws Exception {
        this.defaultLayoutName = defaultLayoutName;
        
        if (desktop != null && !StringUtils.isEmpty(defaultLayoutName)) {
            loadLayoutFromResource(defaultLayoutName);
        }
    }
    
    /**
     * Build the UI based on the specified layout.
     * 
     * @param layout Layout for building UI.
     * @throws Exception
     */
    public void buildUI(UILayout layout) throws Exception {
        this.layout = layout;
        reset();
        layout.deserialize(desktop);
        layout.moveTop();
        desktop.setTitle(layout.readString("title", ""));
        desktop.setAppId(FrameworkUtil.getAppName());
        desktop.activate(true);
    }
    
    /**
     * Resets the desktop to its baseline state and clears registered help modules and property
     * groups.
     */
    public void reset() {
        FrameworkUtil.setAppName(null);
        
        try {
            desktop.activate(false);
            desktop.clear();
            helpModules.clear();
            HelpUtil.getViewer().load(null);
            propertyGroups.clear();
            registerPropertyGroup("CAREWEB.CONTROLS");
            ZKUtil.detachChildren(registeredStyles);
            plugins.clear();
        } catch (Exception e) {}
    }
    
    /**
     * Registers a plugin and its resources. Called internally when a plugin is instantiated.
     * 
     * @param plugin
     */
    public void registerPlugin(PluginContainer plugin) {
        plugins.add(plugin);
    }
    
    /**
     * Unregisters a plugin and its resources. Called internally when a plugin is destroyed.
     * 
     * @param plugin
     */
    public void unregisterPlugin(PluginContainer plugin) {
        plugins.remove(plugin);
    }
    
    /**
     * Adds a component to the common tool bar.
     * 
     * @param component Component to add
     */
    public void addToolbarComponent(Component component) {
        desktop.getToolbar().addToolbarComponent(component, null);
    }
    
    /**
     * Registers a help resource.
     * 
     * @param resource Resource defining the help menu item to be added.
     */
    public void registerHelpResource(HelpResource resource) {
        HelpDefinition def = resource.getHelpDefinition();
        
        if (def != null) {
            if (helpModules.contains(def)) {
                return;
            }
            
            IHelpSet hs = resource.getHelpDefinition().getHelpSet();
            
            if (hs == null) {
                return;
            }
            
            HelpUtil.getViewer().mergeHelpSet(hs);
            helpModules.add(def);
        }
        
        desktop.addHelpMenu(resource);
    }
    
    /**
     * Registers a CSS resource. If the resource has not already been registered, creates a style
     * component and adds it to the current page.
     * 
     * @param resource
     */
    public void registerCSSResource(CSSResource resource) {
        String url = resource.getUrl();
        
        for (Object style : registeredStyles.getChildren()) {
            if (((Style) style).getSrc().equals(url)) {
                return;
            }
        }
        
        registeredStyles.appendChild(new Style(url));
    }
    
    /**
     * Registers a property group.
     * 
     * @param propertyGroup Property group to register.
     */
    public void registerPropertyGroup(String propertyGroup) {
        if (!propertyGroups.contains(propertyGroup)) {
            propertyGroups.add(propertyGroup);
            eventManager.fireLocalEvent(Constants.EVENT_RESOURCE_PROPGROUP_ADD, propertyGroup);
        }
    }
    
    /**
     * Adds a menu.
     * 
     * @param path Path for the menu.
     * @param action Associated action for the menu.
     * @return Created menu item.
     */
    public Menu addMenu(String path, String action) {
        return desktop.addMenu(path, action, false);
    }
    
    /**
     * Returns a list of all plugins currently loaded into the UI.
     * 
     * @return Currently loaded plugins.
     */
    public Iterable<PluginContainer> getLoadedPlugins() {
        return plugins;
    }
    
    /**
     * Locates a loaded plugin with the specified id.
     * 
     * @param id Id of plugin to locate.
     * @return A reference to the loaded plugin, or null if not found.
     */
    public PluginContainer getLoadedPlugin(String id) {
        for (PluginContainer plugin : plugins) {
            if (id.equals(plugin.getPluginDefinition().getId())) {
                return plugin;
            }
        }
        
        return null;
    }
    
    /**
     * Locates a loaded plugin with the specified id.
     * 
     * @param id Id of plugin to locate.
     * @param forceInit If true the plugin will be initialized if not already so.
     * @return A reference to the loaded and fully initialized plugin, or null if not found.
     */
    public PluginContainer getLoadedPlugin(String id, boolean forceInit) {
        PluginContainer plugin = getLoadedPlugin(id);
        
        if (plugin != null && forceInit) {
            plugin.load();
        }
        
        return plugin;
    }
    
    /**
     * Locates an activated plugin with the specified id.
     * 
     * @param id Id of plugin to locate.
     * @return The requested plugin, or null if not found.
     */
    public PluginContainer getActivatedPlugin(String id) {
        for (PluginContainer plugin : plugins) {
            if (id.equals(plugin.getPluginDefinition().getId())
                    && UIElementZKBase.getAssociatedUIElement(plugin).isActivated()) {
                return plugin;
            }
        }
        
        return null;
    }
    
    /**
     * Returns a list of all active plugins.
     * 
     * @return List of all active plugins.
     */
    public Iterable<PluginContainer> getActivatedPlugins() {
        return getActivatedPlugins(null);
    }
    
    /**
     * Populates a list of all activated plugins.
     * 
     * @param list The list to be populated. If null, a new list is created.
     * @return A list of active plugins.
     */
    public Collection<PluginContainer> getActivatedPlugins(Collection<PluginContainer> list) {
        if (list == null) {
            list = new ArrayList<PluginContainer>();
        } else {
            list.clear();
        }
        
        for (PluginContainer plugin : plugins) {
            if (UIElementZKBase.getAssociatedUIElement(plugin).isActivated()) {
                list.add(plugin);
            }
        }
        
        return list;
    }
    
    /**
     * Returns a list of all plugin definitions that are currently in use (i.e., have associated
     * plugins loaded) in the environment.
     * 
     * @return List of PluginDefinition objects that have corresponding plugins loaded in the
     *         environment. May be empty, but never null.
     */
    public Iterable<PluginDefinition> getLoadedPluginDefinitions() {
        List<PluginDefinition> result = new ArrayList<PluginDefinition>();
        
        for (PluginContainer plugin : plugins) {
            PluginDefinition def = plugin.getPluginDefinition();
            
            if (!result.contains(def)) {
                result.add(def);
            }
        }
        
        return result;
    }
    
    /**
     * Returns a list of property groups bound to loaded plugins. Guarantees each group name will
     * appear at most once in the list.
     * 
     * @return List of all property groups bound to loaded plugins.
     */
    public List<String> getPropertyGroups() {
        return propertyGroups;
    }
    
    /**
     * Logout user after confirmation prompt.
     */
    public void logout() {
        // Ensure that shell is last context subscriber (should be a better way
        // to do this).
        appFramework.unregisterObject(userContextListener);
        appFramework.registerObject(userContextListener);
        SecurityUtil.getSecurityService().logout(false, null, null);
    }
    
    /**
     * Lock the desktop.
     */
    public void lock() {
        eventManager.fireLocalEvent(org.carewebframework.ui.Constants.LOCK_EVENT, true);
    }
    
    /**
     * Shows a slide-down message.
     * 
     * @param message
     * @see org.carewebframework.ui.zk.MessageWindow#show(String)
     */
    public void showMessage(String message) {
        messageWindow.show(message);
    }
    
    /**
     * Shows a slide-down message.
     * 
     * @param message
     * @param color
     * @see org.carewebframework.ui.zk.MessageWindow#show(String, String)
     */
    public void showMessage(String message, String color) {
        messageWindow.show(message, color);
    }
    
    /**
     * Shows a slide-down message.
     * 
     * @param message
     * @param color
     * @param duration
     * @see org.carewebframework.ui.zk.MessageWindow#show(String, String, int)
     */
    public void showMessage(String message, String color, int duration) {
        messageWindow.show(message, color, duration);
    }
    
    /**
     * Shows a slide-down message.
     * 
     * @param message
     * @param color
     * @param duration
     * @param tag
     * @see org.carewebframework.ui.zk.MessageWindow#show(String, String, Integer, String)
     */
    public void showMessage(String message, String color, Integer duration, String tag) {
        messageWindow.show(message, color, duration, tag);
    }
    
    /**
     * Clears all slide-down messages;
     * 
     * @see org.carewebframework.ui.zk.MessageWindow#clear
     */
    public void clearMessages() {
        messageWindow.clear();
    }
    
    /**
     * Clears all slide-down messages with specified tag;
     * 
     * @param tag Messages with this tag will be cleared.
     * @see org.carewebframework.ui.zk.MessageWindow#clear(String)
     */
    public void clearMessages(String tag) {
        messageWindow.clear(tag);
    }
    
}
