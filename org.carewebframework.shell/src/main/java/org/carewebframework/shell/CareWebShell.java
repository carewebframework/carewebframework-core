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
package org.carewebframework.shell;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.carewebframework.api.AppFramework;
import org.carewebframework.api.FrameworkUtil;
import org.carewebframework.api.context.ISurveyResponse;
import org.carewebframework.api.context.UserContext.IUserContextEvent;
import org.carewebframework.api.event.EventManager;
import org.carewebframework.api.event.IEventManager;
import org.carewebframework.api.property.PropertyUtil;
import org.carewebframework.api.security.SecurityUtil;
import org.carewebframework.api.spring.SpringUtil;
import org.carewebframework.help.HelpModule;
import org.carewebframework.help.HelpSetCache;
import org.carewebframework.help.IHelpSet;
import org.carewebframework.help.IHelpViewer;
import org.carewebframework.help.viewer.HelpUtil;
import org.carewebframework.shell.elements.ElementDesktop;
import org.carewebframework.shell.elements.ElementPlugin;
import org.carewebframework.shell.layout.Layout;
import org.carewebframework.shell.layout.LayoutParser;
import org.carewebframework.shell.plugins.PluginDefinition;
import org.carewebframework.shell.plugins.PluginResourceHelp;
import org.carewebframework.ui.command.CommandEvent;
import org.carewebframework.ui.command.CommandRegistry;
import org.carewebframework.ui.command.CommandUtil;
import org.carewebframework.ui.dialog.DialogUtil;
import org.carewebframework.ui.session.SessionControl;
import org.fujion.ancillary.INamespace;
import org.fujion.annotation.Component;
import org.fujion.annotation.Component.ChildTag;
import org.fujion.annotation.Component.PropertyGetter;
import org.fujion.annotation.Component.PropertySetter;
import org.fujion.client.ClientUtil;
import org.fujion.common.MiscUtil;
import org.fujion.common.StrUtil;
import org.fujion.component.BaseComponent;
import org.fujion.component.BaseMenuComponent;
import org.fujion.component.Div;
import org.fujion.component.MessageWindow;
import org.fujion.component.Page;
import org.fujion.component.Span;
import org.fujion.component.Style;
import org.fujion.event.KeycaptureEvent;

/**
 * Implements a generic UI shell that can be dynamically extended with plug-ins.
 */
@Component(tag = "cwfShell", widgetClass = "Div", parentTag = "*", childTag = @ChildTag("*"))
public class CareWebShell extends Div implements INamespace {
    
    protected static final Log log = LogFactory.getLog(CareWebShell.class);
    
    public final String LBL_NO_LAYOUT = StrUtil.getLabel("cwf.shell.nolayout.message");
    
    public final String LBL_LOGOUT_CONFIRMATION = StrUtil.getLabel("cwf.shell.logout.confirmation.message");
    
    public final String LBL_LOGOUT_CONFIRMATION_CAPTION = StrUtil.getLabel("cwf.shell.logout.confirmation.caption");
    
    public final String LBL_LOGOUT_CANCEL = StrUtil.getLabel("cwf.shell.logout.cancel.message");
    
    private final AppFramework appFramework = FrameworkUtil.getAppFramework();
    
    private final IEventManager eventManager = EventManager.getInstance();
    
    private final CommandRegistry commandRegistry = SpringUtil.getBean("commandRegistry", CommandRegistry.class);
    
    private final List<ElementPlugin> plugins = new ArrayList<>();
    
    private final Set<HelpModule> helpModules = new HashSet<>();
    
    private final Set<IHelpSet> helpSets = new HashSet<>();
    
    private final List<String> propertyGroups = new ArrayList<>();
    
    private Layout layout = new Layout();
    
    private ElementDesktop desktop;
    
    private final BaseComponent registeredStyles = new Span();
    
    private CareWebStartup startupRoutines;
    
    private MessageWindow messageWindow;
    
    private String defaultLayoutName;
    
    private boolean autoStart;

    private boolean logoutConfirm = true;
    
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
        public void pending(ISurveyResponse response) {
            if (response.isSilent()) {
                response.accept();
            } else {
                response.defer();
                
                DialogUtil.confirm(LBL_LOGOUT_CONFIRMATION, LBL_LOGOUT_CONFIRMATION_CAPTION, "LOGOUT.CONFIRM", (confirm) -> {
                    if (confirm) {
                        response.accept();
                    } else {
                        response.reject(LBL_LOGOUT_CANCEL);
                    }
                    
                });
            }
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
    }
    
    @Override
    protected void onAttach(Page page) {
        try {
            CommandUtil.associateCommand("help", this);
            getPage().addChild(messageWindow = new MessageWindow());
            addChild(registeredStyles);
            desktop = new ElementDesktop(this);
            setLogoutConfirm(logoutConfirm);
            String confirmClose = getAppProperty("confirmClose", "CAREWEB.CONFIRM.CLOSE");
            
            if (StringUtils.isEmpty(confirmClose) || BooleanUtils.toBoolean(confirmClose)) {
                ClientUtil.canClose(false);
            }
            
            String layout = defaultLayoutName != null ? defaultLayoutName
                    : getAppProperty("layout", "CAREWEB.LAYOUT.DEFAULT");
            
            if (!StringUtils.isEmpty(layout)) {
                loadLayout(layout);
            }
            
        } catch (Exception e) {
            log.error("Error initializing the shell.", e);
            throw MiscUtil.toUnchecked(e);
        }
    }
    
    /**
     * Handle help requests.
     *
     * @param event A command event.
     */
    public void onCommand(CommandEvent event) {
        if ("help".equals(event.getCommandName())) {
            BaseComponent ref = event.getReference();
            HelpUtil.showCSH(ref == null ? event.getTarget() : ref);
        }
    }
    
    /**
     * Capture unhandled shortcut key press events.
     *
     * @param event Control key event.
     */
    public void onKeycapture(KeycaptureEvent event) {
        String shortcut = event.getKeycapture();
        
        for (ElementPlugin plugin : getActivatedPlugins(null)) {
            commandRegistry.fireCommands(shortcut, event, plugin.getOuterComponent());
        }
    }
    
    /**
     * Returns a reference to the current UI desktop.
     *
     * @return The current UI desktop.
     */
    public ElementDesktop getDesktop() {
        return desktop;
    }
    
    /**
     * Returns a reference to the current UI layout.
     *
     * @return The current UI layout.
     */
    public Layout getUILayout() {
        return layout;
    }
    
    /**
     * Executed once all plugins are loaded.
     */
    public void start() {
        desktop.activate(true);
        String initialPlugin = PropertyUtil.getValue("CAREWEB.INITIAL.SECTION", getApplicationName());
        
        if (!StringUtils.isEmpty(initialPlugin)) {
            for (ElementPlugin plugin : plugins) {
                if (initialPlugin.equals(plugin.getDefinition().getId())) {
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
     * Loads a layout from the specified resource.
     *
     * @param resource The layout resource to load.
     * @throws Exception Unspecified exception.
     */
    public void loadLayout(String resource) throws Exception {
        layout = LayoutParser.parseResource(resource);
        FrameworkUtil.setAppName(layout.getName());
        
        if (layout.isEmpty()) {
            DialogUtil.showError(LBL_NO_LAYOUT);
        } else {
            buildUI(layout);
        }
    }
    
    /**
     * Returns the name of the layout to be loaded.
     *
     * @return Name of layout to be loaded.
     */
    @PropertyGetter("layout")
    public String getLayout() {
        return defaultLayoutName;
    }
    
    /**
     * Sets the layout to be loaded. If null, the layout specified by the configuration will be
     * loaded.
     *
     * @param defaultLayoutName The default layout name.
     * @throws Exception Unspecified exception.
     */
    @PropertySetter("layout")
    public void setLayout(String defaultLayoutName) throws Exception {
        this.defaultLayoutName = defaultLayoutName;
        
        if (desktop != null && !StringUtils.isEmpty(defaultLayoutName)) {
            loadLayout(defaultLayoutName);
        }
    }
    
    /**
     * Returns the auto-start setting.
     *
     * @return True if the start method is to be called automatically after loading a layout. False
     *         if the start method must be called manually.
     */
    public boolean isAutoStart() {
        return autoStart;
    }
    
    /**
     * Sets the auto-start setting.
     *
     * @param autoStart True if the start method is to be called automatically after loading a
     *            layout. False if the start method must be called manually.
     */
    public void setAutoStart(boolean autoStart) {
        this.autoStart = autoStart;
    }
    
    /**
     * Returns true if confirmation is required upon user-initiated logout.
     *
     * @return True if confirmation is required upon user-initiated logout.
     */
    public boolean isLogoutConfirm() {
        return logoutConfirm;
    }
    
    /**
     * Set to true if confirmation is required upon user-initiated logout.
     *
     * @param logoutConfirm True if confirmation is required upon user-initiated logout.
     */
    public void setLogoutConfirm(boolean logoutConfirm) {
        this.logoutConfirm = logoutConfirm;

        if (logoutConfirm) {
            appFramework.registerObject(userContextListener);
        } else {
            appFramework.unregisterObject(userContextListener);
        }
    }
    
    /**
     * Build the UI based on the specified layout.
     *
     * @param layout Layout for building UI.
     */
    public void buildUI(Layout layout) {
        this.layout = layout;
        reset();
        layout.materialize(desktop);
        desktop.setAppId(FrameworkUtil.getAppName());
        desktop.activate(true);
        
        if (autoStart) {
            start();
        }
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
            helpSets.clear();
            desktop.afterInitialize(false);
            HelpUtil.removeViewer();
            propertyGroups.clear();
            registerPropertyGroup("CAREWEB.CONTROLS");
            registeredStyles.destroyChildren();
            plugins.clear();
        } catch (Exception e) {}
    }
    
    /**
     * Registers a plugin and its resources. Called internally when a plugin is instantiated.
     *
     * @param plugin Plugin to register.
     */
    public void registerPlugin(ElementPlugin plugin) {
        plugins.add(plugin);
    }
    
    /**
     * Unregisters a plugin and its resources. Called internally when a plugin is destroyed.
     *
     * @param plugin Plugin to unregister.
     */
    public void unregisterPlugin(ElementPlugin plugin) {
        plugins.remove(plugin);
    }
    
    /**
     * Adds a component to the common tool bar.
     *
     * @param component Component to add
     */
    public void addToolbarComponent(BaseComponent component) {
        desktop.getToolbar().addToolbarComponent(component, null);
    }
    
    /**
     * Registers a help resource.
     *
     * @param resource Resource defining the help menu item to be added.
     */
    public void registerHelpResource(PluginResourceHelp resource) {
        HelpModule def = HelpModule.getModule(resource.getModule());
        
        if (def != null && helpModules.add(def)) {
            IHelpSet hs = HelpSetCache.getInstance().get(def);
            
            if (hs != null) {
                helpSets.add(hs);
                IHelpViewer viewer = HelpUtil.getViewer(false);
                
                if (viewer != null) {
                    viewer.mergeHelpSet(hs);
                }
            }
        }
        
        desktop.addHelpMenu(resource);
    }
    
    /**
     * Registers an external style sheet. If the style sheet has not already been registered,
     * creates a style component and adds it to the current page.
     *
     * @param url URL of style sheet.
     */
    public void registerStyleSheet(String url) {
        if (findStyleSheet(url) == null) {
            Style ss = new Style();
            ss.setSrc(url);
            registeredStyles.addChild(ss);
        }
    }
    
    /**
     * Returns the style sheet associated with the specified URL.
     *
     * @param url URL of style sheet.
     * @return The associated style sheet, or null if not found.
     */
    private Style findStyleSheet(String url) {
        for (Style ss : registeredStyles.getChildren(Style.class)) {
            if (ss.getSrc().equals(url)) {
                return ss;
            }
        }
        
        return null;
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
    public BaseMenuComponent addMenu(String path, String action) {
        return desktop.addMenu(path, action, false);
    }
    
    /**
     * Returns a list of all plugins currently loaded into the UI.
     *
     * @return Currently loaded plugins.
     */
    public Iterable<ElementPlugin> getLoadedPlugins() {
        return plugins;
    }
    
    /**
     * Locates a loaded plugin with the specified id.
     *
     * @param id Id of plugin to locate.
     * @return A reference to the loaded plugin, or null if not found.
     */
    public ElementPlugin getLoadedPlugin(String id) {
        for (ElementPlugin plugin : plugins) {
            if (id.equals(plugin.getDefinition().getId())) {
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
    public ElementPlugin getLoadedPlugin(String id, boolean forceInit) {
        ElementPlugin plugin = getLoadedPlugin(id);
        
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
    public ElementPlugin getActivatedPlugin(String id) {
        for (ElementPlugin plugin : plugins) {
            if (id.equals(plugin.getDefinition().getId()) && plugin.isActivated()) {
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
    public Iterable<ElementPlugin> getActivatedPlugins() {
        return getActivatedPlugins(null);
    }
    
    /**
     * Populates a list of all activated plugins.
     *
     * @param list The list to be populated. If null, a new list is created.
     * @return A list of active plugins.
     */
    public Collection<ElementPlugin> getActivatedPlugins(Collection<ElementPlugin> list) {
        if (list == null) {
            list = new ArrayList<>();
        } else {
            list.clear();
        }
        
        for (ElementPlugin plugin : plugins) {
            if (plugin.isActivated()) {
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
        List<PluginDefinition> result = new ArrayList<>();
        
        for (ElementPlugin plugin : plugins) {
            PluginDefinition def = plugin.getDefinition();
            
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
    
    public String getAppProperty(String queryParam, String propName) {
        String result = getPage().getQueryParam(queryParam);
        return result == null ? PropertyUtil.getValue(propName) : result;
    }
    
    /**
     * Logout user after confirmation prompt.
     */
    public void logout() {
        // Ensure that shell is last context subscriber (should be a better way
        // to do this).

        if (logoutConfirm) {
            setLogoutConfirm(false);
            setLogoutConfirm(true);
        }

        SecurityUtil.getSecurityService().logout(false, null, null);
    }
    
    /**
     * Lock the desktop.
     */
    public void lock() {
        eventManager.fireLocalEvent(SessionControl.LOCK.getEventName(), true);
    }
    
    /**
     * Returns the message window instance for managing slide-down messages.
     *
     * @return A message window instance.
     */
    public MessageWindow getMessageWindow() {
        return messageWindow;
    }
    
    /**
     * Returns reference to the help viewer. If not already created, one will be created and
     * initialized with the registered help sets.
     *
     * @return A help viewer reference.
     */
    protected IHelpViewer getHelpViewer() {
        IHelpViewer viewer = HelpUtil.getViewer(false);
        
        if (viewer != null) {
            return viewer;
        }
        
        viewer = HelpUtil.getViewer(true);
        viewer.load(helpSets);
        return viewer;
    }
    
}
