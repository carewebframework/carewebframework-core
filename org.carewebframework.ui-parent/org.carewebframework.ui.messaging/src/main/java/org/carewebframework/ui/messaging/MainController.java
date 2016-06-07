/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.ui.messaging;

import java.util.Collection;

import org.carewebframework.api.event.EventMessage;
import org.carewebframework.api.messaging.ConsumerService;
import org.carewebframework.api.messaging.IMessageConsumer.IMessageCallback;
import org.carewebframework.api.messaging.IMessageProducer;
import org.carewebframework.api.messaging.Message;
import org.carewebframework.api.messaging.ProducerService;
import org.carewebframework.shell.plugins.PluginContainer;
import org.carewebframework.shell.plugins.PluginController;
import org.carewebframework.ui.zk.PromptDialog;
import org.carewebframework.ui.zk.ZKUtil;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.SelectEvent;
import org.zkoss.zul.Button;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Textbox;

/**
 * Controller class for ActiveMQ Tester.
 */
public class MainController extends PluginController {
    
    private static final long serialVersionUID = 1L;
    
    private final IMessageCallback messageCallback = new IMessageCallback() {
        
        private final EventListener<Event> eventListener = new EventListener<Event>() {
            
            @Override
            public void onEvent(Event event) throws Exception {
                received.add((Message) event.getData());
            }
            
        };
        
        @Override
        public void onMessage(Message message) {
            ZKUtil.fireEvent(new Event("onMessage", root, message), eventListener);
        }
        
    };
    
    private Listbox lboxProviders;
    
    private Listbox lboxSubscriptions;
    
    private Listbox lboxReceived;
    
    private Combobox cboxChannels;
    
    private Textbox tboxMessage;
    
    private Button btnSendMessage;
    
    private Checkbox chkAsEvent;
    
    private final ConsumerService consumerService;
    
    private final ProducerService producerService;
    
    private final ListModelList<String> channels = new ListModelList<>();
    
    private final ListModelList<String> channels2 = new ListModelList<>();
    
    private final ListModelList<Message> received = new ListModelList<>();
    
    public MainController(ConsumerService consumerService, ProducerService producerService) {
        this.consumerService = consumerService;
        this.producerService = producerService;
    }
    
    @Override
    public void onLoad(PluginContainer container) {
        super.onLoad(container);
        lboxProviders.setItemRenderer(new MessageProviderRenderer());
        ListModelList<IMessageProducer> providers = new ListModelList<>(getProviders());
        providers.setMultiple(true);
        lboxProviders.setModel(providers);
        lboxReceived.setItemRenderer(new ReceivedMessageRenderer());
        lboxReceived.setModel(received);
        channels.setMultiple(true);
        lboxSubscriptions.setModel(channels);
        lboxSubscriptions.setItemRenderer(new SubscriptionRenderer());
        cboxChannels.setModel(channels2);
    }
    
    private Collection<IMessageProducer> getProviders() {
        return producerService.getRegisteredProducers();
    }
    
    @Override
    public void onUnload() {
        super.onUnload();
        
        for (String channel : channels) {
            if (channels.isSelected(channel)) {
                subscribe(channel, false);
            }
        }
    }
    
    public void onClick$btnAddSubscription() {
        String channel = PromptDialog.input("Enter the name of the channel to subscribe to:", "Subscribe to Channel");
        
        if (channel != null && !channels.contains(channel)) {
            channels.add(channel);
            channels2.add(channel);
            subscribe(channel, true);
        }
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
        tboxMessage.setText(null);
    }
    
    public void onClick$btnSendMessage() {
        Comboitem item = cboxChannels.getSelectedItem();
        
        if (item != null) {
            Message message = chkAsEvent.isChecked() ? new EventMessage(item.getLabel(), tboxMessage.getText())
                    : new Message(item.getLabel(), tboxMessage.getText());
            producerService.publish(message);
        }
    }
    
    public void onClick$btnClearReceived() {
        received.clear();
    }
    
    public void onSelect$cboxChannels(SelectEvent<Comboitem, ?> event) {
        btnSendMessage.setDisabled(false);
    }
    
    public void onSelect$lboxSubscriptions(SelectEvent<Listitem, ?> event) {
        Listitem item = event.getReference();
        subscribe(item.getLabel(), item.isSelected());
    }
    
    public void onSelect$lboxProviders(SelectEvent<Listitem, ?> event) {
        Listitem item = event.getReference();
        IMessageProducer producer = (IMessageProducer) item.getValue();
        
        if (item.isSelected()) {
            producerService.registerProducer(producer);
        } else {
            producerService.unregisterProducer(producer);
        }
    }
    
    public void onNewMessage(Event event) {
        received.add((Message) event.getData());
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
