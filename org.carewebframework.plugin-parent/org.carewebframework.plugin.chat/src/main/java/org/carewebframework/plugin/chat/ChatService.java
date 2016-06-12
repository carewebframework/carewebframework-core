/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.plugin.chat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.carewebframework.api.event.IEventManager;
import org.carewebframework.api.event.ILocalEventDispatcher;
import org.carewebframework.api.messaging.IPublisherInfo;
import org.carewebframework.api.messaging.Recipient;
import org.carewebframework.api.messaging.Recipient.RecipientType;
import org.carewebframework.api.spring.SpringUtil;
import org.carewebframework.common.StrUtil;
import org.carewebframework.plugin.chat.ParticipantListener.IParticipantUpdate;
import org.carewebframework.plugin.chat.SessionService.ISessionUpdate;
import org.carewebframework.ui.action.ActionRegistry;
import org.carewebframework.ui.zk.MessageWindow;
import org.carewebframework.ui.zk.MessageWindow.MessageInfo;

/**
 * Chat service.
 */
public class ChatService implements IParticipantUpdate {
    
    protected static final String EVENT_SERVICE = "CHAT.SERVICE.";
    
    protected static final String EVENT_ACTIVE = EVENT_SERVICE + "ACTIVE";
    
    protected static final String EVENT_INACTIVE = EVENT_SERVICE + "INACTIVE";
    
    protected static final String EVENT_INVITE = EVENT_SERVICE + "INVITE";
    
    protected static final String EVENT_ACCEPT = EVENT_SERVICE + "ACCEPT";
    
    protected static final String EVENT_PING = EVENT_SERVICE + "PING";
    
    private static final AtomicInteger lastId = new AtomicInteger();
    
    private final IEventManager eventManager;
    
    private final Map<String, SessionController> sessions = new HashMap<>();
    
    private final Set<IPublisherInfo> participants = new HashSet<>();
    
    private boolean active;
    
    private final IPublisherInfo self;
    
    private ParticipantListener participantListener;
    
    private ServiceListener<String> inviteListener;
    
    private ServiceListener<String> acceptListener;
    
    /**
     * Returns an instance of the chat service.
     * 
     * @return The chat service.
     */
    public static ChatService getInstance() {
        return SpringUtil.getBean("cwfChatService", ChatService.class);
    }
    
    /**
     * Creates the chat service, supplying event manager instance.
     * 
     * @param eventManager The event manager.
     */
    public ChatService(IEventManager eventManager) {
        this.eventManager = eventManager;
        self = ((ILocalEventDispatcher) eventManager).getGlobalEventDispatcher().getPublisherInfo();
        ActionRegistry.register(false, "chat.create.session", "@chat.action.create.session",
            "zscript:" + ChatService.class.getName() + ".getInstance().createSession();");
    }
    
    /**
     * Initialization of service.
     */
    public void init() {
        participantListener = new ParticipantListener(self, EVENT_INVITE, EVENT_ACTIVE, EVENT_INACTIVE, eventManager, this);
        inviteListener = new ServiceListener<String>(EVENT_INVITE, eventManager) {
            
            /**
             * If event data is of the format [session id]^[requester], this is an invitation
             * request. If event data is of the format [session id], it is a cancellation of a
             * previous request.
             */
            @Override
            public void eventCallback(String eventName, String eventData) {
                String[] pcs = eventData.split("\\^", 2);
                String tag = EVENT_INVITE + "_" + pcs[0];
                
                if (pcs.length == 2) {
                    String message = StrUtil.formatMessage("@chat.invitation.message", pcs[1]);
                    String caption = StrUtil.formatMessage("@chat.invitation.caption");
                    String action = "cwf.fireLocalEvent('" + EVENT_ACCEPT + "', '" + pcs[0] + "'); return true;";
                    MessageInfo mi = new MessageInfo(message, caption, null, 999999, tag, action);
                    eventManager.fireLocalEvent(MessageWindow.EVENT_SHOW, mi);
                } else {
                    eventManager.fireLocalEvent(MessageWindow.EVENT_HIDE, tag);
                }
            }
        };
        acceptListener = new ServiceListener<String>(EVENT_ACCEPT, eventManager) {
            
            @Override
            public void eventCallback(String eventName, String eventData) {
                createSession(eventData);
            }
            
        };
        setActive(true);
    }
    
    /**
     * Tear-down of service. Closes any open sessions.
     */
    public void destroy() {
        setActive(false);
        
        for (SessionController session : new ArrayList<>(sessions.values())) {
            session.close();
        }
    }
    
    /**
     * Creates a session listener.
     * 
     * @param sessionId Chat session identifier.
     * @param callback The callback interface to invoke when a session update event has been
     *            received.
     * @return The newly created session listener.
     */
    public ParticipantListener createSessionListener(String sessionId, ISessionUpdate callback) {
        return SessionService.create(self, sessionId, eventManager, callback);
    }
    
    /**
     * Returns the root identifier for sessions created by this service.
     * 
     * @return Session root.
     */
    public String getSessionRoot() {
        String id = self.getSessionId();
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
        return createSession(null);
    }
    
    /**
     * Creates a new session with the specified session id.
     * 
     * @param sessionId The session id to associate with the new session.
     * @return The newly created session.
     */
    public SessionController createSession(String sessionId) {
        boolean newSession = sessionId == null;
        sessionId = newSession ? newSessionId() : sessionId;
        SessionController controller = sessions.get(sessionId);
        
        if (controller != null) {
            return controller;
        }
        
        controller = SessionController.create(sessionId, newSession);
        
        if (controller != null) {
            controller.setSessionService(SessionService.create(self, sessionId, eventManager, controller));
            sessions.put(sessionId, controller);
        }
        
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
        sessions.remove(session.getSessionId());
    }
    
    /**
     * Returns true if the service is actively listening for events.
     * 
     * @return The active state.
     */
    public boolean isActive() {
        return active;
    }
    
    /**
     * Sets the listening state of the service. When set to false, the service stops listening to
     * all events.
     * 
     * @param active The active state.
     */
    public void setActive(boolean active) {
        if (this.active != active) {
            this.active = active;
            inviteListener.setActive(active);
            acceptListener.setActive(active);
            participants.clear();
            participants.add(self);
            participantListener.setActive(active);
        }
    }
    
    /**
     * Returns a list of candidate participants.
     * 
     * @return Candidate participant list.
     */
    public Collection<IPublisherInfo> getChatCandidates() {
        return participants;
    }
    
    /**
     * Sends an invitation request to the specified invitees.
     * 
     * @param sessionId The id of the chat session making the invitation.
     * @param invitees The list of invitees. This will be used to constraint delivery of the
     *            invitation event to only those subscribers.
     * @param cancel If true, invitees are sent a cancellation instead.
     */
    public void invite(String sessionId, Collection<IPublisherInfo> invitees, boolean cancel) {
        if (invitees == null || invitees.isEmpty()) {
            return;
        }
        
        List<Recipient> recipients = new ArrayList<>();
        
        for (IPublisherInfo invitee : invitees) {
            recipients.add(new Recipient(RecipientType.SESSION, invitee.getSessionId()));
        }
        
        String eventData = sessionId + (cancel ? "" : StrUtil.U + self.getUserName());
        eventManager.fireRemoteEvent(EVENT_INVITE, eventData, (Recipient[]) recipients.toArray());
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
