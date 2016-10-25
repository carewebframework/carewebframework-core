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
package org.carewebframework.shell.plugins;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.WordUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.carewebframework.api.spring.SpringUtil;
import org.carewebframework.common.StrUtil;
import org.carewebframework.shell.CareWebShell;
import org.carewebframework.shell.CareWebUtil;
import org.carewebframework.shell.Constants;
import org.carewebframework.shell.layout.UIElementBase;
import org.carewebframework.shell.layout.UIElementCWFBase;
import org.carewebframework.shell.plugins.PluginEvent.PluginAction;
import org.carewebframework.shell.property.PropertyInfo;
import org.carewebframework.ui.FrameworkController;
import org.carewebframework.ui.command.CommandEvent;
import org.carewebframework.ui.command.CommandUtil;
import org.carewebframework.ui.zk.ZKUtil;
import org.carewebframework.web.ancillary.IDisable;
import org.carewebframework.web.client.ClientUtil;
import org.carewebframework.web.component.BaseComponent;
import org.carewebframework.web.component.BaseUIComponent;
import org.carewebframework.web.component.Container;
import org.carewebframework.web.event.EventUtil;
import org.carewebframework.web.page.PageUtil;
import org.springframework.util.StringUtils;

/**
 * Container that manages CareWeb plugins
 */
public class PluginContainer extends Container {
    
    private static final Log log = LogFactory.getLog(PluginContainer.class);
    
    /**
     * Used to hold property values prior to plugin initialization. When a plugin is subsequently
     * initialized and registers a property, the value in the corresponding proxy is used to
     * initialize the property. This allows the deserializer to initialize property values even
     * though the plug-in has not yet been instantiated.
     */
    private class PropertyProxy {
        
        private Object value;
        
        private final PropertyInfo propInfo;
        
        private PropertyProxy(PropertyInfo propInfo, Object value) {
            this.propInfo = propInfo;
            this.value = value;
        }
    };
    
    private final CareWebShell shell;
    
    private PluginDefinition definition;
    
    private ToolbarContainer tbarContainer;
    
    private List<IPluginEvent> pluginEventListeners1;
    
    private List<IPluginEventListener> pluginEventListeners2;
    
    private List<BaseUIComponent> registeredComponents;
    
    private List<IDisable> registeredActions;
    
    private Map<String, Object> registeredProperties;
    
    private Map<String, Object> registeredBeans;
    
    private boolean disabled;
    
    private boolean destroying;
    
    private boolean initialized;
    
    private String busyMessage;
    
    private boolean busyPending;
    
    private boolean busyDisabled;
    
    private String color;
    
    private class ToolbarContainer extends Container {
        
        private static final long serialVersionUID = 1L;
        
        public ToolbarContainer() {
            super();
            addClass("cwf-toolbar-container");
        }
    }
    
    /**
     * Returns the plugin container for the given component.
     * 
     * @param comp The component whose hosting container is sought.
     * @return The hosting plugin container, or null if no container hosts the component.
     */
    public static PluginContainer getContainer(BaseComponent comp) {
        return ZKUtil.findAncestor(comp, PluginContainer.class);
    }
    
    /**
     * Create the plugin container.
     */
    public PluginContainer() {
        super();
        shell = CareWebUtil.getShell();
        addClass("cwf-plugin-container");
        setVisible(false);
        setHeight("100%");
        setWidth("100%");
    }
    
    /**
     * Activate the plugin.
     */
    public void activate() {
        load();
        executeAction(PluginAction.ACTIVATE, true);
        setVisible(true);
    }
    
    /**
     * Inactivate the plugin.
     */
    public void inactivate() {
        setVisible(false);
        executeAction(PluginAction.INACTIVATE, true);
    }
    
    /**
     * Release contained resources.
     */
    @Override
    public void destroy() {
        if (!destroying) {
            destroying = true;
            shell.unregisterPlugin(this);
            executeAction(PluginAction.UNLOAD, false);
            CommandUtil.dissociateAll(this);
            
            if (pluginEventListeners1 != null) {
                pluginEventListeners1.clear();
                pluginEventListeners1 = null;
            }
            
            if (pluginEventListeners2 != null) {
                executeAction(PluginAction.UNSUBSCRIBE, false);
                pluginEventListeners2.clear();
                pluginEventListeners2 = null;
            }
            
            if (registeredProperties != null) {
                registeredProperties.clear();
                registeredProperties = null;
            }
            
            if (registeredBeans != null) {
                registeredBeans.clear();
                registeredBeans = null;
            }
            
            if (registeredComponents != null) {
                for (BaseComponent component : registeredComponents) {
                    component.detach();
                }
                
                registeredComponents.clear();
                registeredComponents = null;
            }
        }
    }
    
    /**
     * Calls the hosting UI element to bring the plugin to the front of the UI.
     */
    public void bringToFront() {
        UIElementBase host = getHost();
        
        if (host != null) {
            host.bringToFront();
        }
    }
    
    /**
     * Returns the UI element hosting this container.
     * 
     * @return The hosting UI element (could be null).
     */
    public UIElementBase getHost() {
        return UIElementCWFBase.getAssociatedUIElement(this);
    }
    
    /**
     * Sets the visibility of the contained resource and any registered components.
     * 
     * @param visible Visibility state to set
     */
    @Override
    public void setVisible(boolean visible) {
        if (visible != isVisible()) {
            super.setVisible(visible);
            
            if (registeredComponents != null) {
                for (BaseUIComponent component : registeredComponents) {
                    if (!visible) {
                        component.setAttribute(Constants.ATTR_VISIBLE, component.isVisible());
                        component.setVisible(false);
                    } else {
                        component.setVisible((Boolean) component.getAttribute(Constants.ATTR_VISIBLE));
                    }
                }
            }
            
            if (visible) {
                checkBusy();
            }
            
        }
    }
    
    /**
     * Returns true if any plugin event listeners are registered.
     * 
     * @return True if any plugin event listeners are registered.
     */
    private boolean hasListeners() {
        return pluginEventListeners1 != null || pluginEventListeners2 != null;
    }
    
    /**
     * Notify all plugin callbacks of the specified action.
     * 
     * @param action Action to perform.
     * @param async If true, callbacks are done asynchronously.
     */
    private void executeAction(PluginAction action, boolean async) {
        executeAction(action, async, null);
    }
    
    /**
     * Notify all plugin callbacks of the specified action.
     * 
     * @param action Action to perform.
     * @param data Event-dependent data (may be null).
     * @param async If true, callbacks are done asynchronously.
     */
    private void executeAction(PluginAction action, boolean async, Object data) {
        if (hasListeners() || action == PluginAction.LOAD) {
            PluginEvent event = new PluginEvent(this, action, data);
            
            if (async) {
                EventUtil.post(event);
            } else {
                onAction(event);
            }
        }
    }
    
    /**
     * Notify listeners of plugin events.
     * 
     * @param event The plugin event containing the action.
     */
    public void onAction(PluginEvent event) {
        PluginException exception = null;
        PluginAction action = event.getAction();
        boolean debug = log.isDebugEnabled();
        
        if (pluginEventListeners1 != null) {
            for (IPluginEvent listener : new ArrayList<>(pluginEventListeners1)) {
                try {
                    if (debug) {
                        log.debug("Invoking IPluginEvent.on" + WordUtils.capitalizeFully(action.name()) + " for listener "
                                + listener);
                    }
                    
                    switch (action) {
                        case LOAD:
                            listener.onLoad(this);
                            continue;
                        
                        case UNLOAD:
                            listener.onUnload();
                            continue;
                        
                        case ACTIVATE:
                            listener.onActivate();
                            continue;
                        
                        case INACTIVATE:
                            listener.onInactivate();
                            continue;
                    }
                } catch (Throwable e) {
                    exception = createChainedException(action.name(), e, exception);
                }
            }
        }
        
        if (pluginEventListeners2 != null) {
            for (IPluginEventListener listener : new ArrayList<>(pluginEventListeners2)) {
                try {
                    if (debug) {
                        log.debug("Delivering " + action.name() + " event to IPluginEventListener listener " + listener);
                    }
                    listener.onPluginEvent(event);
                } catch (Throwable e) {
                    exception = createChainedException(action.name(), e, exception);
                }
            }
        }
        
        if (action == PluginAction.LOAD) {
            doAfterLoad();
        }
        
        if (exception != null) {
            throw exception;
        }
    }
    
    /**
     * Actions to perform after the container is loaded.
     */
    protected void doAfterLoad() {
        registerProperty(this, "color", false);
    }
    
    /**
     * Forward onCommand events to first level children of the container.
     * 
     * @param event The command event.
     */
    public void onCommand(CommandEvent event) {
        if (!disabled) {
            for (BaseComponent child : this.getChildren()) {
                EventUtil.send(event, child);
                
                if (event.isStopped()) {
                    break;
                }
            }
        }
    }
    
    /**
     * Creates a chained exception.
     * 
     * @param action Action being performed at the time of the exception.
     * @param newException Exception just thrown.
     * @param previousException Previous exception (may be null).
     * @return Top level exception in chain.
     */
    private PluginException createChainedException(String action, Throwable newException,
                                                   PluginException previousException) {
        String msg = action + " event generated an error.";
        log.error(msg, newException);
        PluginException wrapper = new PluginException(msg, previousException == null ? newException : previousException,
                null);
        wrapper.setStackTrace(newException.getStackTrace());
        return wrapper;
    }
    
    /**
     * Initializes a plugin, if not already done. This loads the plugin's principal zul page,
     * attaches any event listeners, and sends a load event to subscribers.
     */
    public void load() {
        if (!initialized && definition != null) {
            try {
                initialized = true;
                
                if (getFirstChild() == null) {
                    PageUtil.createPage(definition.getUrl(), this);
                }
            } catch (Throwable e) {
                destroyChildren();
                throw createChainedException("Initialize", e, null);
            }
            
            findListeners(this);
            executeAction(PluginAction.LOAD, true);
        }
    }
    
    /**
     * Search the plugin's component tree for components (or their controllers) implementing the
     * IPluginEvent interface. Those that are found are registered as listeners.
     * 
     * @param cmpt BaseComponent to search
     */
    private void findListeners(BaseComponent cmpt) {
        for (BaseComponent child : cmpt.getChildren()) {
            tryRegisterListener(child, true);
            tryRegisterListener(FrameworkController.getController(child), true);
            findListeners(child);
        }
    }
    
    /**
     * Adds the specified component to the toolbar container. The component is registered to this
     * container and will visible only when the container is active.
     * 
     * @param component BaseComponent to add.
     */
    public void addToolbarComponent(BaseComponent component) {
        if (tbarContainer == null) {
            tbarContainer = new ToolbarContainer();
            shell.addToolbarComponent(tbarContainer);
            registerComponent(tbarContainer);
        }
        
        tbarContainer.addChild(component);
    }
    
    /**
     * Register a component with the container. The container will control the visibility of the
     * component according to when it is active/inactive.
     * 
     * @param component BaseComponent to register.
     */
    public void registerComponent(BaseUIComponent component) {
        if (registeredComponents == null) {
            registeredComponents = new ArrayList<>();
        }
        
        registeredComponents.add(component);
        component.setAttribute(Constants.ATTR_CONTAINER, this);
        component.setAttribute(Constants.ATTR_VISIBLE, component.isVisible());
        component.setVisible(isVisible());
    }
    
    /**
     * Allows auto-wire to work even if component is not a child of the container.
     * 
     * @param id BaseComponent id.
     * @param component BaseComponent to be registered.
     */
    public void registerId(String id, BaseComponent component) {
        if (!StringUtils.isEmpty(id) && !hasAttribute(id)) {
            setAttribute(id, component);
        }
    }
    
    /**
     * Registers an action element. Action elements implement the Disable interface and are
     * automatically enabled/disabled when the owning container is enabled/disabled.
     * 
     * @param actionElement A component implementing the Disable interface.
     */
    public void registerAction(IDisable actionElement) {
        if (registeredActions == null) {
            registeredActions = new ArrayList<>();
        }
        
        registeredActions.add(actionElement);
        actionElement.setDisabled(isDisabled());
    }
    
    /**
     * Registers a listener for the IPluginEvent callback event. If the listener has already been
     * registered, the request is ignored.
     * 
     * @param listener Listener to be registered.
     */
    public void registerListener(IPluginEvent listener) {
        if (pluginEventListeners1 == null) {
            pluginEventListeners1 = new ArrayList<>();
        }
        
        if (!pluginEventListeners1.contains(listener)) {
            pluginEventListeners1.add(listener);
        }
    }
    
    /**
     * Registers a listener for the IPluginEventListener callback event. If the listener has already
     * been registered, the request is ignored.
     * 
     * @param listener Listener to be registered.
     */
    public void registerListener(IPluginEventListener listener) {
        if (pluginEventListeners2 == null) {
            pluginEventListeners2 = new ArrayList<>();
        }
        
        if (!pluginEventListeners2.contains(listener)) {
            pluginEventListeners2.add(listener);
            listener.onPluginEvent(new PluginEvent(this, PluginAction.SUBSCRIBE));
        }
    }
    
    /**
     * Unregisters a listener for the IPluginEvent callback event.
     * 
     * @param listener Listener to be unregistered.
     */
    public void unregisterListener(IPluginEvent listener) {
        if (pluginEventListeners1 != null) {
            pluginEventListeners1.remove(listener);
        }
    }
    
    /**
     * Unregisters a listener for the IPluginEvent callback event.
     * 
     * @param listener Listener to be unregistered.
     */
    public void unregisterListener(IPluginEventListener listener) {
        if (pluginEventListeners2 != null && pluginEventListeners2.contains(listener)) {
            pluginEventListeners2.remove(listener);
            listener.onPluginEvent(new PluginEvent(this, PluginAction.UNSUBSCRIBE));
        }
    }
    
    /**
     * Attempts to register or unregister an object as an event listener.
     * 
     * @param object Object to register/unregister.
     * @param register If true, we are attempting to register. If false, unregister.
     * @return True if operation was successful. False if the object supports none of the recognized
     *         event listeners.
     */
    public boolean tryRegisterListener(Object object, boolean register) {
        boolean success = false;
        
        if (object instanceof IPluginEvent) {
            if (register) {
                registerListener((IPluginEvent) object);
            } else {
                unregisterListener((IPluginEvent) object);
            }
            success = true;
        }
        
        if (object instanceof IPluginEventListener) {
            if (register) {
                registerListener((IPluginEventListener) object);
            } else {
                unregisterListener((IPluginEventListener) object);
            }
            success = true;
        }
        
        return success;
    }
    
    /**
     * Registers one or more named properties to the container. Using this, a plugin can expose
     * properties for serialization and deserialization.
     * 
     * @param instance The object instance holding the property accessors. If null, any existing
     *            registration will be removed.
     * @param propertyNames One or more property names to register.
     */
    public void registerProperties(Object instance, String... propertyNames) {
        for (String propertyName : propertyNames) {
            registerProperty(instance, propertyName, true);
        }
    }
    
    /**
     * Registers a named property to the container. Using this, a plugin can expose a property for
     * serialization and deserialization.
     * 
     * @param instance The object instance holding the property accessors. If null, any existing
     *            registration will be removed.
     * @param propertyName Name of property to register.
     * @param override If the property is already registered to a non-proxy, the previous
     *            registration will be replaced if this is true; otherwise the request is ignored.
     */
    public void registerProperty(Object instance, String propertyName, boolean override) {
        if (registeredProperties == null) {
            registeredProperties = new HashMap<>();
        }
        
        if (instance == null) {
            registeredProperties.remove(propertyName);
        } else {
            Object oldInstance = registeredProperties.get(propertyName);
            PropertyProxy proxy = oldInstance instanceof PropertyProxy ? (PropertyProxy) oldInstance : null;
            
            if (!override && oldInstance != null && proxy == null) {
                return;
            }
            
            registeredProperties.put(propertyName, instance);
            
            // If previous registrant was a property proxy, transfer its value to new registrant.
            if (proxy != null) {
                try {
                    proxy.propInfo.setPropertyValue(instance, proxy.value);
                } catch (Exception e) {
                    throw createChainedException("Register Property", e, null);
                }
            }
        }
    }
    
    /**
     * Registers a helper bean with this container.
     * 
     * @param beanId The bean's id.
     * @param isRequired If true and the bean is not found, an exception is raised.
     */
    public void registerBean(String beanId, boolean isRequired) {
        if (beanId == null || beanId.isEmpty()) {
            return;
        }
        
        Object bean = SpringUtil.getBean(beanId);
        
        if (bean == null && isRequired) {
            throw new PluginException("Required bean resouce not found: " + beanId);
        }
        
        Object oldBean = getAssociatedBean(beanId);
        
        if (bean == oldBean) {
            return;
        }
        
        if (registeredBeans == null) {
            registeredBeans = new HashMap<>();
        }
        
        tryRegisterListener(oldBean, false);
        
        if (bean == null) {
            registeredBeans.remove(beanId);
        } else {
            registeredBeans.put(beanId, bean);
            tryRegisterListener(bean, true);
        }
    }
    
    /**
     * Returns a bean that has been associated (via registerBean) with this plugin.
     * 
     * @param beanId The id of the bean.
     * @return The bean instance, or null if not found.
     */
    public Object getAssociatedBean(String beanId) {
        return registeredBeans == null ? null : registeredBeans.get(beanId);
    }
    
    /**
     * Returns the value for a registered property.
     * 
     * @param propInfo Property info.
     * @return The property value.
     * @throws Exception Unspecified exception.
     */
    public Object getPropertyValue(PropertyInfo propInfo) throws Exception {
        Object obj = registeredProperties == null ? null : registeredProperties.get(propInfo.getId());
        
        if (obj instanceof PropertyProxy) {
            Object value = ((PropertyProxy) obj).value;
            return value instanceof String ? propInfo.getPropertyType().getSerializer().deserialize((String) value) : value;
        } else {
            return obj == null ? null : propInfo.getPropertyValue(obj);
        }
    }
    
    /**
     * Sets a value for a registered property.
     * 
     * @param propInfo Property info.
     * @param value The value to set.
     * @throws Exception Unspecified exception.
     */
    public void setPropertyValue(PropertyInfo propInfo, Object value) throws Exception {
        String propId = propInfo.getId();
        Object obj = registeredProperties == null ? null : registeredProperties.get(propId);
        
        if (obj == null) {
            obj = new PropertyProxy(propInfo, value);
            registerProperties(obj, propId);
        } else if (obj instanceof PropertyProxy) {
            ((PropertyProxy) obj).value = value;
        } else {
            propInfo.setPropertyValue(obj, value);
        }
    }
    
    /**
     * Return the definition associated with this plugin.
     * 
     * @return The associated plugin definition.
     */
    public PluginDefinition getPluginDefinition() {
        return definition;
    }
    
    /**
     * Sets the plugin definition the container will use to instantiate the plugin. If there is a
     * status bean associated with the plugin, it is registered with the container at this time. If
     * there are style sheet resources associated with the plugin, they will be added to the
     * container at this time.
     * 
     * @param definition The plugin definition.
     */
    public void setPluginDefinition(PluginDefinition definition) {
        this.definition = definition;
        
        if (definition == null) {
            return;
        }
        
        addClass("cwf-plugin-" + definition.getId());
        shell.registerPlugin(this);
    }
    
    /**
     * Enables/disables the container and all registered action elements.
     * 
     * @param disabled Disable status.
     */
    @Override
    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
        disableActions(disabled);
    }
    
    /**
     * Enables/disables registered actions elements. Note that if the container is disabled, the
     * action elements will not be enabled by this call. It may be used, however, to temporarily
     * disable action elements that would otherwise be enabled.
     * 
     * @param disable The disable status.
     */
    public void disableActions(boolean disable) {
        if (registeredActions != null) {
            for (IDisable registeredAction : registeredActions) {
                registeredAction.setDisabled(disable || disabled);
            }
        }
    }
    
    /**
     * Returns the disable status of the container.
     * 
     * @return True if this plugin is disabled.
     */
    @Override
    public boolean isDisabled() {
        return disabled;
    }
    
    /**
     * Temporarily disables setBusy function.
     * 
     * @param disable If true, disable setBusy function. If false, enables the function and
     *            processes any pending busy operation.
     */
    private void disableBusy(boolean disable) {
        busyDisabled = disable;
        busyPending |= disable;
        checkBusy();
    }
    
    /**
     * Processes any pending busy operation if enabled.
     */
    private void checkBusy() {
        if (!busyDisabled && busyPending) {
            setBusy(busyMessage);
        }
    }
    
    /**
     * If message is not null, disables the plugin and displays the busy message. If message is
     * null, removes any previous message and returns the plugin to its previous state.
     * 
     * @param message The message to display, or null to clear previous message.
     */
    public void setBusy(String message) {
        busyMessage = message = StrUtil.formatMessage(message);
        
        if (busyDisabled) {
            busyPending = true;
        } else if (message != null) {
            disableActions(true);
            ClientUtil.busy(this, message);
            busyPending = !isVisible();
        } else {
            disableActions(false);
            ClientUtil.busy(this, null);
            busyPending = false;
        }
    }
    
    /**
     * Sets design mode for the container.
     * 
     * @param designMode If true, associated actions and busy mask are disabled.
     */
    public void setDesignMode(boolean designMode) {
        disableActions(designMode);
        disableBusy(designMode);
    }
    
    /**
     * Returns the shell instance that hosts this container.
     * 
     * @return The shell instance.
     */
    public CareWebShell getShell() {
        return shell;
    }
    
    /**
     * Returns the color (as an HTML-formatted RGB string) for this element.
     * 
     * @return An HTML-formatted color specification (e.g., #0F134E). May be null.
     */
    public String getColor() {
        return color;
    }
    
    /**
     * Sets the container's background color.
     * 
     * @param value A correctly formatted HTML color specification.
     */
    public void setColor(String value) {
        color = value;
        addStyle("background-color", color);
    }
    
}
