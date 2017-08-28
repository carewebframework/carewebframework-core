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

import org.carewebframework.api.messaging.Message;
import org.carewebframework.api.messaging.ProducerService;
import org.fujion.client.ClientRequest;
import org.fujion.websocket.IRequestHandler;

/**
 * Handler for servicing publication requests from the browser client.
 */
public class PublishRequestHandler implements IRequestHandler {
    
    private final ProducerService producerService;
    
    public PublishRequestHandler(ProducerService producerService) {
        this.producerService = producerService;
    }
    
    @Override
    public void handleRequest(ClientRequest request) {
        String channel = request.getParam("channel", String.class);
        String type = request.getParam("type", String.class);
        Object payload = request.getParam("payload", Object.class);
        Message message = new Message(type, payload);
        producerService.publish(channel, message);
    }
    
    @Override
    public String getRequestType() {
        return "publish";
    }
    
}
