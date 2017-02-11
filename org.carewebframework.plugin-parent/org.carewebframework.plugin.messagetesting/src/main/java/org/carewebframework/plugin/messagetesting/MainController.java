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
import org.carewebframework.shell.elements.UIElementPlugin;
import org.carewebframework.shell.plugins.PluginController;
import org.carewebframework.ui.dialog.DialogUtil;
import org.carewebframework.ui.dialog.InputDialog.IInputCallback;
import org.carewebframework.ui.util.CWFUtil;
import org.carewebframework.web.component.Button;
import org.carewebframework.web.component.Checkbox;
import org.carewebframework.web.component.Combobox;
import org.carewebframework.web.component.Comboitem;
import org.carewebframework.web.component.Listbox;
import org.carewebframework.web.component.Listitem;
import org.carewebframework.web.component.Textbox;
import org.carewebframework.web.event.ChangeEvent;
import org.carewebframework.web.event.Event;
import org.carewebframework.web.event.IEventListener;
import org.carewebframework.web.model.ListModel;
import org.carewebframework.web.model.ModelAndView;

/**
 * Controller class for ActiveMQ Tester.
 */
public class MainController extends PluginController {
    
    private final IMessageCallback messageCallback = new IMessageCallback() {
        
        private final IEventListener eventListener = new IEventListener() {
            
            @Override
            public void onEvent(Event event) {
                received.add((Message) event.getData());
                
                if (!chkScrollLock.isChecked()) {
                    org.carewebframework.web.event.EventUtil.post("onScrollToBottom", root, null);
                }
            }
            
        };
        
        @Override
        public void onMessage(String channel, Message message) {
            CWFUtil.fireEvent(new Event(channel, root, message), eventListener);
        }
        
    };
    
    private Listbox lboxProviders;
    
    private Listbox lboxSubscriptions;
    
    private Listbox lboxReceived;
    
    private Combobox cboxChannels;
    
    private Textbox tboxMessage;
    
    private Button btnSendMessage;
    
    private Checkbox chkAsEvent;
    
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
    public void onLoad(UIElementPlugin plugin) {
        super.onLoad(plugin);
        ListModel<IMessageProducer> providers = new ListModel<>(getProviders());
        //providers.setMultiple(true);
        new ModelAndView<Listitem, IMessageProducer>(lboxProviders, providers, new MessageProviderRenderer());
        new ModelAndView<Listitem, Message>(lboxReceived, received, new ReceivedMessageRenderer());
        //channels.setMultiple(true);
        new ModelAndView<Listitem, String>(lboxSubscriptions, channels, new SubscriptionRenderer());
        //TODO: cboxChannels.setModel(channels2);
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
    
    public void onClick$btnAddSubscription() {
        DialogUtil.input("Enter the name of the channel to subscribe to:", "Subscribe to Channel", new IInputCallback() {
            
            @Override
            public void onComplete(String channel) {
                if (channel != null && !channels.contains(channel)) {
                    channels.add(channel);
                    channels2.add(channel);
                    subscribe(channel, true);
                }
            }
            
        });
    }
    
    public void onClick$btnRemoveSubscription() {
        Listitem item = lboxSubscriptions.getSelectedItem();
        
        if (item != null) {
            if (item.isSelected()) {
                subscribe(item.getLabel(), false);
            }
            
            lboxSubscriptions.removeChild(item);
        }
    }
    
    public void onClick$btnClearMessage() {
        tboxMessage.setValue(null);
    }
    
    public void onClick$btnSendMessage() {
        Comboitem item = cboxChannels.getSelectedItem();
        
        if (item != null) {
            String type = item.getLabel();
            String channel = chkAsEvent.isChecked() ? EventUtil.getChannelName(type) : type;
            Message message = chkAsEvent.isChecked() ? new EventMessage(type, tboxMessage.getValue())
                    : new Message(channel, tboxMessage.getValue());
            producerService.publish(channel, message);
        }
    }
    
    public void onClick$btnClearReceived() {
        received.clear();
    }
    
    public void onChange$cboxChannels(ChangeEvent event) {
        btnSendMessage.setDisabled(false);
    }
    
    public void onChange$lboxSubscriptions(ChangeEvent event) {
        Listitem item = (Listitem) event.getTarget();
        subscribe(item.getLabel(), item.isSelected());
    }
    
    public void onChange$lboxProviders(ChangeEvent event) {
        Listitem item = (Listitem) event.getTarget();
        IMessageProducer producer = (IMessageProducer) item.getData();
        
        if (item.isSelected()) {
            producerService.registerProducer(producer);
        } else {
            producerService.unregisterProducer(producer);
        }
    }
    
    public void onScrollToBottom() {
        Listitem item = (Listitem) lboxReceived.getChildAt(lboxReceived.getChildCount() - 1);
        item.scrollIntoView(false);
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
