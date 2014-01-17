/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.ui.jmstesting;

import org.apache.commons.lang.StringUtils;

import org.carewebframework.api.event.EventUtil;
import org.carewebframework.api.event.IEventManager;
import org.carewebframework.api.event.IGenericEvent;
import org.carewebframework.api.event.ILocalEventDispatcher;
import org.carewebframework.api.event.IPublisherInfo;
import org.carewebframework.api.event.PingEventHandler;
import org.carewebframework.common.StrUtil;
import org.carewebframework.jms.MessagingSupport;
import org.carewebframework.shell.plugins.PluginContainer;
import org.carewebframework.shell.plugins.PluginController;

import org.zkoss.zk.ui.event.Event;
import org.zkoss.zul.Label;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Textbox;

/**
 * Controller class for ActiveMQ Tester.
 */
public class MainController extends PluginController {
    
    private static final long serialVersionUID = 1L;
    
    private static String[] EVENTS = { "CONNECT", "DISCONNECT", PingEventHandler.EVENT_PING_RESPONSE };
    
    private Label lblMessage;
    
    private Textbox txtMessageData;
    
    private Listbox lstTopicRecipients;
    
    private Textbox txtDestinationName;
    
    private Textbox txtEndpointId;
    
    private MessagingSupport messagingSupport;
    
    private IEventManager eventManager;
    
    private final IGenericEvent<IPublisherInfo> eventListener = new IGenericEvent<IPublisherInfo>() {
        
        @Override
        public void eventCallback(String eventName, IPublisherInfo publisherInfo) {
            if ("DISCONNECT".equals(eventName)) {
                
            } else {
                
            }
        }
        
    };
    
    private void subscribe(boolean doSubscribe) {
        for (String eventName : EVENTS) {
            if (doSubscribe) {
                eventManager.subscribe(eventName, eventListener);
            } else {
                eventManager.unsubscribe(eventName, eventListener);
            }
        }
    }
    
    public void onClick$btnProduceLocalMessage(final Event event) {
        //TODO clear local vars, constraints
        final String destName = StringUtils.trimToNull(this.txtDestinationName.getValue());
        final String messageData = StringUtils.trimToNull(this.txtMessageData.getValue());
        this.messagingSupport.produceLocalMessage(destName, messageData);
        showMessage("@cwf.jmstesting.msg.local.complete");
    }
    
    public void onClick$btnProduceTopicMessage(final Event event) {
        final String destName = StringUtils.trimToNull(this.txtDestinationName.getValue());
        final String messageData = StringUtils.trimToNull(this.txtMessageData.getValue());
        final String recipients = StringUtils.trimToNull(getSelectedRecipients());
        this.messagingSupport.produceTopicMessage(destName, messageData, recipients);
        showMessage("@cwf.jmstesting.msg.topic.complete");
    }
    
    private String getSelectedRecipients() {
        StringBuilder sb = new StringBuilder();
        
        for (Listitem item : lstTopicRecipients.getSelectedItems()) {
            if (sb.length() > 0) {
                sb.append(',');
            }
            
            sb.append(item.getValue());
        }
        
        return sb.toString();
    }
    
    public void onClick$btnProduceQueueMessage(final Event event) {
        final String destName = StringUtils.trimToNull(this.txtDestinationName.getValue());
        final String messageData = StringUtils.trimToNull(this.txtMessageData.getValue());
        this.messagingSupport.produceQueueMessage(destName, messageData);
        showMessage("@cwf.jmstesting.msg.queue.complete");
    }
    
    /**
     * Displays message to client
     * 
     * @param message Message to display to client.
     * @param params Message parameters.
     */
    private void showMessage(final String message, Object... params) {
        if (message == null) {
            lblMessage.setVisible(false);
        } else {
            lblMessage.setVisible(true);
            lblMessage.setValue(StrUtil.formatMessage(message, params));
        }
    }
    
    /**
     * @param messagingSupport The messaging support service.
     */
    public void setMessagingSupport(final MessagingSupport messagingSupport) {
        this.messagingSupport = messagingSupport;
    }
    
    /**
     * @param eventManager The event manager.
     */
    public void setEventManager(final IEventManager eventManager) {
        this.eventManager = eventManager;
    }
    
    @Override
    public void onLoad(PluginContainer container) {
        super.onLoad(container);
        txtEndpointId.setValue(((ILocalEventDispatcher) eventManager).getGlobalEventDispatcher().getEndpointId());
        subscribe(true);
    }
    
    @Override
    public void onUnload() {
        subscribe(false);
        super.onUnload();
        EventUtil.ping(null, null);
    }
    
}
