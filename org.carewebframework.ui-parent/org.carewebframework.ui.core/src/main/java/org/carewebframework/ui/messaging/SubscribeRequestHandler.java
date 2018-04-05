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
package org.carewebframework.ui.messaging;

import java.util.HashMap;
import java.util.Map;

import org.carewebframework.api.messaging.ConsumerService;
import org.carewebframework.api.messaging.IMessageConsumer.IMessageCallback;
import org.fujion.client.ClientInvocation;
import org.fujion.client.ClientRequest;
import org.fujion.client.ExecutionContext;
import org.fujion.websocket.IRequestHandler;
import org.fujion.websocket.WebSocketHandler;
import org.springframework.web.socket.WebSocketSession;

/**
 * Handler for servicing subscription requests from the browser client.
 */
public class SubscribeRequestHandler implements IRequestHandler {

    private enum RequestType {
        SUBSCRIBE, UNSUBSCRIBE
    }
    
    private static final String ATTR_SUBSCRIBERS = "messaging-subscribers";

    private final ConsumerService consumerService;
    
    public SubscribeRequestHandler(ConsumerService consumerService) {
        this.consumerService = consumerService;
    }
    
    @Override
    public void handleRequest(ClientRequest request) {
        WebSocketSession socket = request.getPage().getSession().getSocket();
        String id = request.getParam("id", String.class);
        String channel = request.getParam("channel", String.class);
        RequestType type = request.getParam("type", RequestType.class);
        Map<String, IMessageCallback> subscribers = getSubscribers();
        String subid = id + "@" + channel;
        IMessageCallback callback = subscribers.get(subid);
        
        switch (type) {
            case SUBSCRIBE:
                if (callback == null) {
                    subscribers.put(subid, callback = (chan, message) -> {
                        ClientInvocation invocation = new ClientInvocation("cwf-shell", "messageCallback", null, id, chan,
                                message);
                        WebSocketHandler.send(socket, invocation);
                    });
                    
                    consumerService.subscribe(channel, callback);
                }
                
                break;

            case UNSUBSCRIBE:
                if (callback != null) {
                    subscribers.remove(subid);
                    consumerService.unsubscribe(channel, callback);
                }
                
                break;
        }
    }

    @Override
    public String getRequestType() {
        return "subscribe";
    }

    private Map<String, IMessageCallback> getSubscribers() {
        @SuppressWarnings("unchecked")
        Map<String, IMessageCallback> subscribers = (Map<String, IMessageCallback>) ExecutionContext.get(ATTR_SUBSCRIBERS);
        
        if (subscribers == null) {
            ExecutionContext.put(ATTR_SUBSCRIBERS, subscribers = new HashMap<>());
        }
        
        return subscribers;
    }
}
