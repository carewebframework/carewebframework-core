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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.carewebframework.api.event.IEventManager;
import org.carewebframework.api.event.IGenericEvent;
import org.carewebframework.api.event.ILocalEventDispatcher;
import org.carewebframework.api.event.IPublisherInfo;
import org.carewebframework.api.spring.SpringUtil;
import org.carewebframework.common.StrUtil;
import org.carewebframework.ui.action.ActionRegistry;
import org.carewebframework.ui.chat.ParticipantListener.IParticipantUpdate;
import org.carewebframework.ui.zk.MessageWindow;
import org.carewebframework.ui.zk.MessageWindow.MessageInfo;

/**
 * Chat service.
 */
public class ChatService implements IGenericEvent<String>, IParticipantUpdate {
    
    private static final String EVENT_PREFIX = "CHAT.SERVICE.";
    
    private static final String EVENT_ACTIVE = EVENT_PREFIX + "ACTIVE";
    
    private static final String EVENT_INACTIVE = EVENT_PREFIX + "INACTIVE";
    
    private static final String EVENT_INVITE = EVENT_PREFIX + "INVITE";
    
    private static final String EVENT_ACCEPT = EVENT_PREFIX + "ACCEPT";
    
    private static final String[] EVENTS = { EVENT_INVITE, EVENT_ACCEPT };
    
    private static final AtomicInteger lastId = new AtomicInteger();
    
    private final IEventManager eventManager;
    
    private final List<SessionController> sessions = new ArrayList<SessionController>();
    
    private final Set<IPublisherInfo> participants = new HashSet<IPublisherInfo>();
    
    private boolean active;
    
    private final IPublisherInfo self;
    
    private ParticipantListener participantListener;
    
    /**
     * Returns an instance of the chat service.
     * 
     * @return The chat service.
     */
    public static ChatService getInstance() {
        return SpringUtil.getBean("chatService", ChatService.class);
    }
    
    /**
     * Creates the chat service, supplying event manager instance.
     * 
     * @param eventManager
     */
    public ChatService(IEventManager eventManager) {
        this.eventManager = eventManager;
        self = ((ILocalEventDispatcher) eventManager).getGlobalEventDispatcher().getPublisherInfo();
    }
    
    /**
     * Initialization of service.
     */
    public void init() {
        ActionRegistry.addLocalAction("@chat.action.create.session", "zscript:" + ChatService.class.getName()
                + ".getInstance().createSession();");
        participantListener = new ParticipantListener(self, EVENT_INVITE, EVENT_ACTIVE, EVENT_INACTIVE, eventManager, this);
        setActive(true);
    }
    
    /**
     * Tear-down of service. Closes any open sessions.
     */
    public void destroy() {
        setActive(false);
        participantListener.setActive(false);
        
        for (SessionController session : new ArrayList<SessionController>(sessions)) {
            session.close();
        }
    }
    
    /**
     * Creates a participant listener.
     * 
     * @param sentinelEvent The sentinel event that will be used to filter participants.
     * @param addEvent The event that signals a new participant has been added.
     * @param removeEvent The event that signals a participant has been removed.
     * @param callback The callback interface to invoke when a participant event has been received.
     * @return The newly created participant listener.
     */
    public ParticipantListener createListener(String sentinelEvent, String addEvent, String removeEvent,
                                              IParticipantUpdate callback) {
        return new ParticipantListener(self, sentinelEvent, addEvent, removeEvent, eventManager, callback);
    }
    
    /**
     * Returns the root identifier for sessions created by this service.
     * 
     * @return Session root.
     */
    public String getSessionRoot() {
        String id = self.getNodeId();
        return id == null ? "" : id + "-";
    }
    
    /**
     * Creates a new session id.
     * 
     * @return New session id.
     */
    private String newSessionId() {
        return getSessionRoot() + lastId.incrementAndGet();
    }
    
    /**
     * Creates a new session with a new session id.
     * 
     * @return The newly created session.
     */
    public SessionController createSession() {
        return createSession(newSessionId());
    }
    
    /**
     * Creates a new session with the specified session id.
     * 
     * @param sessionId The session id to associate with the new session.
     * @return The newly created session.
     */
    public SessionController createSession(String sessionId) {
        SessionController controller = SessionController.create(sessionId);
        sessions.add(controller);
        return controller;
    }
    
    /**
     * Returns this user's publisher info.
     * 
     * @return The user's publisher info.
     */
    public IPublisherInfo getSelf() {
        return self;
    }
    
    /**
     * Called by a session controller when it closes.
     * 
     * @param session Session being closed.
     */
    protected void onSessionClosed(SessionController session) {
        sessions.remove(session);
    }
    
    /**
     * Respond to events:
     * <p>
     * CHAT.SERVICE.INVITE - An invitation has been received to join a dialog. Event stub format is:
     * Chat Session ID^Requester name
     * <p>
     * CHAT.SERVICE.ACCEPT - This client has accepted the invitation to join. Event stub format is:
     * Chat Session ID
     */
    @Override
    public void eventCallback(String eventName, String eventData) {
        String action = StrUtil.piece(eventName, ".", 3);
        
        if ("INVITE".equals(action)) {
            String[] pcs = StrUtil.split(eventData, StrUtil.U);
            MessageInfo mi = new MessageInfo(StrUtil.formatMessage("@chat.invitation.message", pcs[1]),
                    StrUtil.formatMessage("@chat.invitation.caption"), null, 999999, null,
                    "cwf.fireLocalEvent('CHAT.SERVICE.ACCEPT', '" + pcs[0] + "'); return true;");
            eventManager.fireLocalEvent(MessageWindow.EVENT, mi);
            ;
        } else if ("ACCEPT".equals(action)) {
            createSession(eventData);
        }
    }
    
    /**
     * Returns true if the service is actively listening for events.
     * 
     * @return
     */
    public boolean isActive() {
        return active;
    }
    
    /**
     * Sets the listening state of the service. When set to false, the service stops listening to
     * all events.
     * 
     * @param active
     */
    public void setActive(boolean active) {
        this.active = active;
        
        for (String eventName : EVENTS) {
            if (active) {
                eventManager.subscribe(eventName, this);
            } else {
                eventManager.unsubscribe(eventName, this);
            }
        }
        
        participantListener.setActive(active);
    }
    
    /**
     * Returns a list of candidate participants.
     * 
     * @return
     */
    public Collection<IPublisherInfo> getChatCandidates() {
        return participants;
    }
    
    /**
     * Sends a message via the specified event.
     * 
     * @param eventName Event to use to deliver the message.
     * @param text The message text.
     * @return The message that was sent (may be null if no text).
     */
    public ChatMessage sendMessage(String eventName, String text) {
        if (text != null && !text.isEmpty()) {
            ChatMessage message = new ChatMessage(self, text);
            eventManager.fireRemoteEvent(eventName, message);
            return message;
        }
        
        return null;
    }
    
    /**
     * Sends an invitation request to the specified invitees.
     * 
     * @param sessionId The id of the chat session making the invitation.
     * @param invitees The list of invitees. This will be used to constraint delivery of the
     *            invitation event to only those subscribers.
     */
    public void invite(String sessionId, Collection<IPublisherInfo> invitees) {
        if (invitees == null || invitees.isEmpty()) {
            return;
        }
        
        StringBuilder sb = new StringBuilder();
        
        for (IPublisherInfo invitee : invitees) {
            if (sb.length() > 0) {
                sb.append(",");
            }
            
            sb.append(invitee.getEndpointId());
        }
        
        eventManager.fireRemoteEvent("CHAT.SERVICE.INVITE", sessionId + StrUtil.U + self.getUserName(), sb.toString());
    }
    
    /**
     * Callback for adding a chat candidate to the list.
     */
    @Override
    public void onParticipantAdded(IPublisherInfo participant, boolean fromRefresh) {
        participants.add(participant);
    }
    
    /**
     * Callback for removing a chat candidate from the list.
     */
    @Override
    public void onParticipantRemoved(IPublisherInfo participant) {
        participants.remove(participant);
    }
    
}
