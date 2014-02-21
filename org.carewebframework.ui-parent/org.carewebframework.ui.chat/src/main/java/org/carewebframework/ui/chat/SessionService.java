/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.ui.chat;

import org.carewebframework.api.event.IEventManager;
import org.carewebframework.api.event.IPublisherInfo;
import org.carewebframework.common.StrUtil;

/**
 * Services for a single chat session.
 */
public class SessionService extends ParticipantListener {
    
    protected static final String EVENT_SESSION = "CHAT.SESSION.%s.";
    
    protected static final String EVENT_JOIN = EVENT_SESSION + "JOIN";
    
    protected static final String EVENT_LEAVE = EVENT_SESSION + "LEAVE";
    
    protected static final String EVENT_SEND = EVENT_SESSION + "SEND";
    
    public interface ISessionUpdate extends IParticipantUpdate {
        
        /**
         * Called when a chat message has been received.
         * 
         * @param message The received chat message.
         */
        void onMessageReceived(ChatMessage message);
    }
    
    private final ServiceListener<ChatMessage> messageListener;
    
    private final String sendEvent;
    
    /**
     * Creates a service handler for a chat session.
     * 
     * @param self Creator of the chat session.
     * @param sessionId Unique id of the chat session.
     * @param eventManager Event manager instance.
     * @param callback Call back method for session-related events.
     * @return The newly created service.
     */
    protected static SessionService create(IPublisherInfo self, String sessionId, IEventManager eventManager,
                                           ISessionUpdate callback) {
        String sendEvent = StrUtil.formatMessage(EVENT_SEND, sessionId);
        String joinEvent = StrUtil.formatMessage(EVENT_JOIN, sessionId);
        String leaveEvent = StrUtil.formatMessage(EVENT_LEAVE, sessionId);
        return new SessionService(self, sendEvent, joinEvent, leaveEvent, eventManager, callback);
    }
    
    private SessionService(IPublisherInfo self, String sendEvent, String addEvent, String removeEvent,
        IEventManager eventManager, final ISessionUpdate callback) {
        super(self, sendEvent, addEvent, removeEvent, eventManager, callback);
        this.sendEvent = sendEvent;
        this.messageListener = new ServiceListener<ChatMessage>(
                                                                sendEvent, eventManager) {
            
            @Override
            public void eventCallback(String eventName, ChatMessage chatMessage) {
                callback.onMessageReceived(chatMessage);
            }
            
        };
    }
    
    /**
     * Sets the active state of all listeners.
     */
    @Override
    public void setActive(boolean active) {
        super.setActive(active);
        messageListener.setActive(active);
    }
    
    /**
     * Sends a message to a chat session.
     * 
     * @param text The message text.
     * @return The message that was sent (may be null if no text).
     */
    public ChatMessage sendMessage(String text) {
        if (text != null && !text.isEmpty()) {
            ChatMessage message = new ChatMessage(self, text);
            eventManager.fireRemoteEvent(sendEvent, message);
            return message;
        }
        
        return null;
    }
    
}
