/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
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
import org.carewebframework.shell.layout.UIElementZKBase;
import org.carewebframework.shell.plugins.PluginEvent.PluginAction;
import org.carewebframework.shell.property.PropertyInfo;
import org.carewebframework.ui.FrameworkController;
import org.carewebframework.ui.command.CommandEvent;
import org.carewebframework.ui.command.CommandUtil;
import org.carewebframework.ui.zk.ZKUtil;

import org.springframework.util.StringUtils;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.ext.Disable;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Idspace;

/**
 * Container that manages CareWeb plugins
 */
public class PluginContainer extends Idspace {
    
    private static final long serialVersionUID = 1L;
    
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
    
    private List<Component> registeredComponents;
    
    private List<Disable> registeredActions;
    
    private Map<String, Object> registeredProperties;
    
    private Map<String, Object> registeredBeans;
    
    private boolean disabled;
    
    private boolean destroying;
    
    private boolean initialized;
    
    private String busyMessage;
    
    private boolean busyPending;
    
    private boolean busyDisabled;
    
    private String color;
    
    private class ToolbarContainer extends Idspace {
        
        private static final long serialVersionUID = 1L;
        
        public ToolbarContainer() {
            super();
            setZclass("cwf-toolbar-container");
        }
    }
    
    /**
     * Returns the plugin container for the given component.
     * 
     * @param comp The component whose hosting container is sought.
     * @return The hosting plugin container, or null if no container hosts the component.
     */
    public static PluginContainer getContainer(Component comp) {
        return ZKUtil.findAncestor(comp, PluginContainer.class);
    }
    
    /**
     * Create the plugin container.
     */
    public PluginContainer() {
        super();
        shell = CareWebUtil.getShell();
        setZclass("cwf-plugin-container");
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
                for (Component component : registeredComponents) {
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
        UIElementBase associated = UIElementZKBase.getAssociatedUIElement(this);
        
        if (associated != null) {
            associated.bringToFront();
        }
    }
    
    /**
     * Sets the visibility of the contained resource and any registered components.
     * 
     * @param visible Visibility state to set
     */
    @Override
    public boolean setVisible(boolean visible) {
        boolean result = super.setVisible(visible);
        
        if (result != visible && registeredComponents != null) {
            for (Component component : registeredComponents) {
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
        
        return result;
    }
    
    /**
     * Returns true if any plugin event listeners are registered.
     * 
     * @return
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
                Events.postEvent(event);
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
    public void onAction(final PluginEvent event) {
        PluginLifecycleEventException exception = null;
        PluginAction action = event.getAction();
        boolean debug = log.isDebugEnabled();
        
        if (pluginEventListeners1 != null) {
            for (IPluginEvent listener : new ArrayList<IPluginEvent>(pluginEventListeners1)) {
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
            for (IPluginEventListener listener : new ArrayList<IPluginEventListener>(pluginEventListeners2)) {
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
     * @param event
     */
    public void onCommand(CommandEvent event) {
        if (!disabled) {
            for (Component child : this.getChildren()) {
                Events.sendEvent(child, event);
                
                if (!event.isPropagatable()) {
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
    private PluginLifecycleEventException createChainedException(final String action, final Throwable newException,
                                                                 final PluginLifecycleEventException previousException) {
        String msg = action + " event generated an error.";
        log.error(msg, newException);
        PluginLifecycleEventException wrapper = new PluginLifecycleEventException(Executions.getCurrent(), msg,
                previousException == null ? newException : previousException);
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
                    ZKUtil.loadZulPage(definition.getUrl(), this);
                }
            } catch (Throwable e) {
                ZKUtil.detachChildren(this);
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
     * @param cmpt Component to search
     */
    private void findListeners(Component cmpt) {
        for (Component child : cmpt.getChildren()) {
            tryRegisterListener(child, true);
            tryRegisterListener(FrameworkController.getController(child), true);
            findListeners(child);
        }
    }
    
    /**
     * Adds the specified component to the toolbar container. The component is registered to this
     * container and will visible only when the container is active.
     * 
     * @param component Component to add.
     */
    public void addToolbarComponent(Component component) {
        if (tbarContainer == null) {
            tbarContainer = new ToolbarContainer();
            shell.addToolbarComponent(tbarContainer);
            registerComponent(tbarContainer);
        }
        
        tbarContainer.appendChild(component);
    }
    
    /**
     * Register a component with the container. The container will control the visibility of the
     * component according to when it is active/inactive.
     * 
     * @param component Component to register.
     */
    public void registerComponent(Component component) {
        if (registeredComponents == null) {
            registeredComponents = new ArrayList<Component>();
        }
        
        registeredComponents.add(component);
        component.setAttribute(Constants.ATTR_CONTAINER, this);
        component.setAttribute(Constants.ATTR_VISIBLE, component.isVisible());
        component.setVisible(isVisible());
    }
    
    /**
     * Allows auto-wire to work even if component is not a child of the container.
     * 
     * @param id Component id.
     * @param component Component to be registered.
     */
    /*package*/void registerId(String id, Component component) {
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
    public void registerAction(Disable actionElement) {
        if (registeredActions == null) {
            registeredActions = new ArrayList<Disable>();
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
            pluginEventListeners1 = new ArrayList<IPluginEvent>();
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
            pluginEventListeners2 = new ArrayList<IPluginEventListener>();
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
            registeredProperties = new HashMap<String, Object>();
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
    /*package*/void registerBean(String beanId, boolean isRequired) {
        if (beanId == null || beanId.isEmpty()) {
            return;
        }
        
        Object bean = SpringUtil.getBean(beanId);
        
        if (bean == null && isRequired) {
            throw new PluginLifecycleEventException(Executions.getCurrent(), "Required bean resouce not found: " + beanId);
        }
        
        Object oldBean = getAssociatedBean(beanId);
        
        if (bean == oldBean) {
            return;
        }
        
        if (registeredBeans == null) {
            registeredBeans = new HashMap<String, Object>();
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
     * @param propInfo
     * @return The property value.
     * @throws Exception
     */
    public Object getPropertyValue(PropertyInfo propInfo) throws Exception {
        Object obj = registeredProperties == null ? null : registeredProperties.get(propInfo.getId());
        return obj == null ? null : obj instanceof PropertyProxy ? ((PropertyProxy) obj).value : propInfo
                .getPropertyValue(obj);
    }
    
    /**
     * Sets a value for a registered property.
     * 
     * @param propInfo
     * @param value
     * @throws Exception
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
        
        setSclass("cwf-plugin-" + definition.getId());
        shell.registerPlugin(this);
    }
    
    /**
     * Enables/disables the container and all registered action elements.
     * 
     * @param disabled Disable status.
     */
    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
        disableActions(disabled);
    }
    
    /**
     * Enables/disables registered actions elements. Note that if the container is disabled, the
     * action elements will not be enabled by this call. It may be used, however, to temporarily
     * disable action elements that would otherwise be enabled.
     * 
     * @param disable
     */
    public void disableActions(boolean disable) {
        if (registeredActions != null) {
            for (Disable registeredAction : registeredActions) {
                registeredAction.setDisabled(disable || disabled);
            }
        }
    }
    
    /**
     * Returns the disable status of the container.
     * 
     * @return True if this plugin is disabled.
     */
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
     * @param message
     */
    public void setBusy(String message) {
        busyMessage = message = StrUtil.formatMessage(message);
        
        if (busyDisabled) {
            busyPending = true;
        } else if (message != null) {
            disableActions(true);
            Clients.showBusy(this, message);
            busyPending = !isVisible();
        } else {
            disableActions(false);
            Clients.clearBusy(this);
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
     * @return
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
        ZKUtil.updateStyle(this, "background-color", color);
    }
    
}
