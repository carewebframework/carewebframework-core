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
package org.carewebframework.plugin.messagetesting;

import java.util.Collection;

import org.carewebframework.api.event.EventMessage;
import org.carewebframework.api.event.EventUtil;
import org.carewebframework.api.messaging.ConsumerService;
import org.carewebframework.api.messaging.IMessageConsumer.IMessageCallback;
import org.carewebframework.api.messaging.IMessageProducer;
import org.carewebframework.api.messaging.Message;
import org.carewebframework.api.messaging.ProducerService;
import org.carewebframework.shell.elements.ElementPlugin;
import org.carewebframework.shell.plugins.PluginController;
import org.carewebframework.ui.dialog.DialogUtil;
import org.carewebframework.ui.util.CWFUtil;
import org.fujion.annotation.EventHandler;
import org.fujion.annotation.WiredComponent;
import org.fujion.component.Button;
import org.fujion.component.Checkbox;
import org.fujion.component.Combobox;
import org.fujion.component.Comboitem;
import org.fujion.component.Grid;
import org.fujion.component.Listbox;
import org.fujion.component.Listitem;
import org.fujion.component.Memobox;
import org.fujion.event.ChangeEvent;
import org.fujion.event.Event;
import org.fujion.event.IEventListener;
import org.fujion.model.ListModel;

/**
 * Controller class for ActiveMQ Tester.
 */
public class MainController extends PluginController {
    
    private final IMessageCallback messageCallback = new IMessageCallback() {
        
        private final IEventListener eventListener = (event) -> {
            received.add((Message) event.getData());
            
            if (!chkScrollLock.isChecked()) {
                org.fujion.event.EventUtil.post("scrollToBottom", root, null);
            }
        };
        
        @Override
        public void onMessage(String channel, Message message) {
            CWFUtil.fireEvent(new Event(channel, root, message), eventListener);
        }
        
    };
    
    @WiredComponent
    private Listbox lboxProviders;
    
    @WiredComponent
    private Listbox lboxSubscriptions;
    
    @WiredComponent
    private Grid gridReceived;
    
    @WiredComponent
    private Combobox cboxChannels;
    
    @WiredComponent
    private Memobox memoMessage;
    
    @WiredComponent
    private Button btnSendMessage;
    
    @WiredComponent
    private Checkbox chkAsEvent;
    
    @WiredComponent
    private Checkbox chkScrollLock;
    
    private final ConsumerService consumerService;
    
    private final ProducerService producerService;
    
    private final ListModel<String> channels = new ListModel<>();
    
    private final ListModel<String> channels2 = new ListModel<>();
    
    private final ListModel<Message> received = new ListModel<>();
    
    public MainController(ConsumerService consumerService, ProducerService producerService) {
        this.consumerService = consumerService;
        this.producerService = producerService;
    }
    
    @Override
    public void onLoad(ElementPlugin plugin) {
        super.onLoad(plugin);
        ListModel<IMessageProducer> providers = new ListModel<>(getProviders());
        //providers.setMultiple(true);
        lboxProviders.setModel(providers);
        lboxProviders.setRenderer(new MessageProviderRenderer());
        gridReceived.getRows().setModel(received);
        gridReceived.getRows().setRenderer(new ReceivedMessageRenderer(gridReceived));
        //channels.setMultiple(true);
        lboxSubscriptions.setModel(channels);
        lboxSubscriptions.setRenderer((String channel) -> {
            return new Listitem(channel);
        });
        cboxChannels.setModel(channels2);
        cboxChannels.setRenderer((String channel) -> {
            return new Comboitem(channel);
        });
    }
    
    private Collection<IMessageProducer> getProviders() {
        return producerService.getRegisteredProducers();
    }
    
    @Override
    public void onUnload() {
        super.onUnload();
        
        for (Listitem item : lboxSubscriptions.getChildren(Listitem.class)) {
            if (item.isSelected()) {
                subscribe(item.getLabel(), false);
            }
        }
    }
    
    @EventHandler(value = "click", target = "btnAddSubscription")
    private void onClick$btnAddSubscription() {
        DialogUtil.input("Enter the name of the channel to subscribe to:", "Subscribe to Channel", (channel) -> {
            if (channel != null && !channels.contains(channel)) {
                channels.add(channel);
                channels2.add(channel);
                subscribe(channel, true);
            }
        });
    }
    
    @EventHandler(value = "click", target = "btnRemoveSubscription")
    private void onClick$btnRemoveSubscription() {
        Listitem item = lboxSubscriptions.getSelectedItem();
        
        if (item != null) {
            if (item.isSelected()) {
                subscribe(item.getLabel(), false);
            }
            
            lboxSubscriptions.removeChild(item);
        }
    }
    
    @EventHandler(value = "click", target = "btnClearMessage")
    private void onClick$btnClearMessage() {
        memoMessage.setValue(null);
    }
    
    @EventHandler(value = "click", target = "btnSendMessage")
    private void onClick$btnSendMessage() {
        Comboitem item = cboxChannels.getSelectedItem();
        
        if (item != null) {
            String type = item.getLabel();
            String channel = chkAsEvent.isChecked() ? EventUtil.getChannelName(type) : type;
            Message message = chkAsEvent.isChecked() ? new EventMessage(type, memoMessage.getValue())
                    : new Message(channel, memoMessage.getValue());
            producerService.publish(channel, message);
        }
    }
    
    @EventHandler(value = "click", target = "btnClearReceived")
    private void onClick$btnClearReceived() {
        received.clear();
    }
    
    @EventHandler(value = "change", target = "cboxChannels")
    private void onChange$cboxChannels(ChangeEvent event) {
        btnSendMessage.setDisabled(false);
    }
    
    @EventHandler(value = "change", target = "lboxSubscriptions")
    private void onChange$lboxSubscriptions(ChangeEvent event) {
        Listitem item = (Listitem) event.getTarget();
        subscribe(item.getLabel(), item.isSelected());
    }
    
    @EventHandler(value = "change", target = "lboxProviders")
    private void onChange$lboxProviders(ChangeEvent event) {
        Listitem item = (Listitem) event.getValue();
        IMessageProducer producer = (IMessageProducer) item.getData();
        
        if (item.isSelected()) {
            producerService.registerProducer(producer);
        } else {
            producerService.unregisterProducer(producer);
        }
    }
    
    public void onScrollToBottom() {
        Listitem item = (Listitem) gridReceived.getChildAt(gridReceived.getChildCount() - 1);
        item.scrollIntoView();
    }
    
    private void subscribe(String channel, boolean subscribe) {
        if (subscribe) {
            consumerService.subscribe(channel, messageCallback);
        } else {
            consumerService.unsubscribe(channel, messageCallback);
        }
        
        if (!channel.startsWith("cwf-event-")) {
            subscribe("cwf-event-" + channel, subscribe);
        }
    }
}
