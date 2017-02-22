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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.WordUtils;
import org.carewebframework.api.spring.SpringUtil;
import org.carewebframework.common.StrUtil;
import org.carewebframework.shell.AboutDialog;
import org.carewebframework.shell.CareWebShell;
import org.carewebframework.shell.CareWebUtil;
import org.carewebframework.shell.Constants;
import org.carewebframework.shell.designer.DesignMask.MaskMode;
import org.carewebframework.shell.plugins.IPluginController;
import org.carewebframework.shell.plugins.IPluginEvent;
import org.carewebframework.shell.plugins.IPluginEventListener;
import org.carewebframework.shell.plugins.PluginDefinition;
import org.carewebframework.shell.plugins.PluginEvent;
import org.carewebframework.shell.plugins.PluginEvent.PluginAction;
import org.carewebframework.shell.plugins.PluginException;
import org.carewebframework.shell.property.IPropertyAccessor;
import org.carewebframework.shell.property.PropertyInfo;
import org.carewebframework.shell.property.PropertyProxy;
import org.carewebframework.ui.FrameworkController;
import org.carewebframework.ui.command.CommandEvent;
import org.carewebframework.ui.command.CommandUtil;
import org.carewebframework.web.ancillary.IDisable;
import org.carewebframework.web.annotation.EventHandler;
import org.carewebframework.web.client.ClientUtil;
import org.carewebframework.web.component.BaseComponent;
import org.carewebframework.web.component.BaseUIComponent;
import org.carewebframework.web.component.Namespace;
import org.carewebframework.web.event.EventUtil;
import org.carewebframework.web.page.PageUtil;
import org.springframework.util.StringUtils;

/**
 * This class is used for all container-hosted plugins.
 */
public class UIElementPlugin extends UIElementBase implements IDisable, IPropertyAccessor {

    static {
        registerAllowedParentClass(UIElementPlugin.class, UIElementBase.class);
    }

    private class ToolbarContainer extends Namespace {

        public ToolbarContainer() {
            super();
            addClass("cwf-toolbar-container");
        }
    }

    public class PluginContainer extends Namespace {};

    private final PluginContainer container = new PluginContainer();

    private final CareWebShell shell;

    private List<IDisable> registeredActions;

    private ToolbarContainer tbarContainer;

    private List<IPluginEvent> pluginEventListeners1;

    private List<IPluginEventListener> pluginEventListeners2;

    private List<IPluginController> pluginControllers;

    private List<BaseUIComponent> registeredComponents;

    private Map<String, Object> registeredProperties;

    private Map<String, Object> registeredBeans;

    private boolean initialized;

    private String busyMessage;

    private boolean busyPending;

    private boolean busyDisabled;

    /**
     * Sets the container as the wrapped component and registers itself to receive action
     * notifications from the container.
     */
    public UIElementPlugin() {
        super();
        shell = CareWebUtil.getShell();
        setOuterComponent(container);
        setMaskMode(MaskMode.ENABLE);
        container.addClass("cwf-plugin-container");
        fullSize(container);
        container.wireController(this);
    }

    /**
     * @see org.carewebframework.shell.elements.UIElementBase#about()
     */
    @Override
    public void about() {
        AboutDialog.execute(getDefinition());
    }

    /**
     * Passes the activation request to the container.
     *
     * @see org.carewebframework.shell.elements.UIElementBase#activateChildren(boolean)
     */
    @Override
    public void activateChildren(boolean active) {
        if (active != isActivated()) {
            if (active) {
                activate();
            } else {
                inactivate();
            }
        }
    }

    /**
     * Activate the plugin.
     */
    public void activate() {
        load();
        executeAction(PluginAction.ACTIVATE, true);
        container.setVisible(true);
    }

    /**
     * Inactivate the plugin.
     */
    public void inactivate() {
        container.setVisible(false);
        executeAction(PluginAction.INACTIVATE, true);
    }

    /**
     * Additional processing of the plugin after it is initialized.
     *
     * @throws Exception Unspecified exception.
     * @see org.carewebframework.shell.elements.UIElementBase#afterInitialize
     */
    @Override
    public void afterInitialize(boolean deserializing) throws Exception {
        super.afterInitialize(deserializing);

        if (!getDefinition().isLazyLoad()) {
            load();
        }
    }

    /**
     * Release contained resources.
     */
    @Override
    public void destroy() {
        shell.unregisterPlugin(this);
        executeAction(PluginAction.UNLOAD, false);
        CommandUtil.dissociateAll(container);

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
                component.destroy();
            }

            registeredComponents.clear();
            registeredComponents = null;
        }

        super.destroy();
    }

    /**
     * Passes design mode setting to the container.
     *
     * @param designMode If true, associated actions and busy mask are disabled.
     */
    @Override
    public void setDesignMode(boolean designMode) {
        super.setDesignMode(designMode);
        disableActions(designMode);
        disableBusy(designMode);
    }

    /**
     * Returns the value for a registered property.
     *
     * @param propInfo Property info.
     * @return The property value.
     * @throws Exception Unspecified exception.
     */
    @Override
    public Object getPropertyValue(PropertyInfo propInfo) throws Exception {
        Object obj = registeredProperties == null ? null : registeredProperties.get(propInfo.getId());

        if (obj instanceof PropertyProxy) {
            Object value = ((PropertyProxy) obj).getValue();
            return value instanceof String ? propInfo.getPropertyType().getSerializer().deserialize((String) value) : value;
        } else {
            return obj == null ? null : propInfo.getPropertyValue(obj, obj == this);
        }
    }

    /**
     * Sets a value for a registered property.
     *
     * @param propInfo Property info.
     * @param value The value to set.
     * @throws Exception Unspecified exception.
     */
    @Override
    public void setPropertyValue(PropertyInfo propInfo, Object value) throws Exception {
        String propId = propInfo.getId();
        Object obj = registeredProperties == null ? null : registeredProperties.get(propId);

        if (obj == null) {
            obj = new PropertyProxy(propInfo, value);
            registerProperties(obj, propId);
        } else if (obj instanceof PropertyProxy) {
            ((PropertyProxy) obj).setValue(value);
        } else {
            propInfo.setPropertyValue(obj, value, obj == this);
        }
    }

    @Override
    public boolean isDisabled() {
        return !isEnabled();
    }

    @Override
    public void setDisabled(boolean disabled) {
        setEnabled(!disabled);
        disableActions(disabled);
    }

    /**
     * Sets the visibility of the contained resource and any registered components.
     *
     * @param visible Visibility state to set
     */
    @Override
    protected void updateVisibility(boolean visible, boolean activated) {
        super.updateVisibility(visible, activated);

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

    /**
     * Sets the plugin definition the container will use to instantiate the plugin. If there is a
     * status bean associated with the plugin, it is registered with the container at this time. If
     * there are style sheet resources associated with the plugin, they will be added to the
     * container at this time.
     *
     * @param definition The plugin definition.
     */
    @Override
    public void setDefinition(PluginDefinition definition) {
        super.setDefinition(definition);

        if (definition != null) {
            container.addClass("cwf-plugin-" + definition.getId());
            shell.registerPlugin(this);
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
     * Enables/disables registered actions elements. Note that if the container is disabled, the
     * action elements will not be enabled by this call. It may be used, however, to temporarily
     * disable action elements that would otherwise be enabled.
     *
     * @param disable The disable status.
     */
    public void disableActions(boolean disable) {
        if (registeredActions != null) {
            for (IDisable registeredAction : registeredActions) {
                registeredAction.setDisabled(disable || isDisabled());
            }
        }
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
            ClientUtil.busy(container, message);
            busyPending = !isVisible();
        } else {
            disableActions(false);
            ClientUtil.busy(container, null);
            busyPending = false;
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
    @EventHandler("action")
    private void onAction(PluginEvent event) {
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
     * Forward command events to first level children of the container.
     *
     * @param event The command event.
     */
    @EventHandler("command")
    private void onCommand(CommandEvent event) {
        if (isEnabled()) {
            for (BaseComponent child : container.getChildren()) {
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
        PluginDefinition definition = getDefinition();

        if (!initialized && definition != null) {
            BaseComponent top;

            try {
                initialized = true;
                top = container.getFirstChild();

                if (top == null) {
                    top = PageUtil.createPage(definition.getUrl(), container).get(0);
                }
            } catch (Throwable e) {
                container.destroyChildren();
                throw createChainedException("Initialize", e, null);
            }

            if (pluginControllers != null) {
                for (Object controller : pluginControllers) {
                    top.wireController(controller);
                }
            }

            findListeners(container);
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
        if (!StringUtils.isEmpty(id) && !container.hasAttribute(id)) {
            container.setAttribute(id, component);
        }
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
     * Registers an object as a controller if it implements the IPluginController interface.
     *
     * @param object Object to register.
     */
    public void tryRegisterController(Object object) {
        if (object instanceof IPluginController) {
            if (pluginControllers == null) {
                pluginControllers = new ArrayList<>();
            }

            pluginControllers.add((IPluginController) object);
        }
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
                    proxy.getPropertyInfo().setPropertyValue(instance, proxy.getValue());
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
            tryRegisterController(bean);
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
     * Returns the shell instance that hosts this container.
     *
     * @return The shell instance.
     */
    public CareWebShell getShell() {
        return shell;
    }

}
