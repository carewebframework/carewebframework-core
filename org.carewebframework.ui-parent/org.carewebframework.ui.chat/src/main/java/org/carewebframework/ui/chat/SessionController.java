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

import java.util.HashMap;
import java.util.Map;

import org.carewebframework.api.event.IGenericEvent;
import org.carewebframework.api.event.IPublisherInfo;
import org.carewebframework.common.DateUtil;
import org.carewebframework.common.StrUtil;
import org.carewebframework.ui.FrameworkController;
import org.carewebframework.ui.chat.ParticipantListener.IParticipantUpdate;
import org.carewebframework.ui.zk.PopupDialog;
import org.carewebframework.ui.zk.ZKUtil;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.InputEvent;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Button;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModelSet;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

/**
 * Controller for an individual chat session.
 */
public class SessionController extends FrameworkController implements IGenericEvent<ChatMessage>, IParticipantUpdate {
    
    private static final long serialVersionUID = 1L;
    
    private static final String DIALOG = ZKUtil.getResourcePath(SessionController.class) + "session.zul";
    
    private String sessionId;
    
    private ChatService chatService;
    
    private Listbox lstParticipants;
    
    private Component pnlDialog;
    
    private Textbox txtMessage;
    
    private Button btnSendMessage;
    
    private String messageEvent;
    
    private final ListModelSet<IPublisherInfo> model = new ListModelSet<IPublisherInfo>();
    
    private ParticipantListener participantListener;
    
    /**
     * Creates a chat session bound to the specified session id.
     * 
     * @param sessionId The chat session id.
     * @param originator If true, this user is originating the chat session.
     * @return The controller for the chat session.
     */
    protected static SessionController create(String sessionId, boolean originator) {
        Map<Object, Object> args = new HashMap<Object, Object>();
        args.put("id", sessionId);
        args.put("title", StrUtil.formatMessage("@chat.session.title", sessionId));
        args.put("originator", originator ? true : null);
        Window dlg = PopupDialog.popup(DIALOG, args, true, true, false);
        return (SessionController) FrameworkController.getController(dlg);
    }
    
    /**
     * Initialize the dialog.
     */
    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        sessionId = (String) arg.get("id");
        String eventRoot = "CHAT.SESSION." + sessionId + ".";
        messageEvent = eventRoot + "MESSAGE";
        lstParticipants.setItemRenderer(new ParticipantRenderer(chatService.getSelf(), null));
        model.add(chatService.getSelf());
        lstParticipants.setModel(model);
        participantListener = chatService.createListener(messageEvent, eventRoot + "JOIN", eventRoot + "LEAVE", this);
        doSubscribe(true);
        clearMessage();
        
        if (arg.get("originator") != null && !InviteController.show(sessionId, model)) {
            root.detach();
        } else {
            ((Window) root).doOverlapped();
        }
    }
    
    /**
     * Refreshes the participant list.
     */
    @Override
    public void refresh() {
        model.clear();
        model.add(chatService.getSelf());
        participantListener.refresh();
    }
    
    /**
     * Subscribe to / unsubscribe from events of interest.
     * 
     * @param subscribe If true, subscribe; false, unsubscribe.
     */
    private void doSubscribe(boolean subscribe) {
        if (subscribe) {
            getEventManager().subscribe(messageEvent, this);
        } else {
            getEventManager().unsubscribe(messageEvent, this);
        }
        
        participantListener.setActive(subscribe);
    }
    
    /**
     * Clear any text in the message text box.
     */
    private void clearMessage() {
        txtMessage.setText(null);
        updateControls(true);
    }
    
    /**
     * Updates control status.
     * 
     * @param disableSend If true, disable the send button.
     */
    private void updateControls(boolean disableSend) {
        btnSendMessage.setDisabled(disableSend);
        txtMessage.setFocus(true);
    }
    
    /**
     * Clear the message text.
     */
    public void onClick$btnClearMessage() {
        clearMessage();
    }
    
    /**
     * Send the message text.
     */
    public void onClick$btnSendMessage() {
        addDialog(chatService.sendMessage(messageEvent, txtMessage.getText().trim()));
        clearMessage();
    }
    
    /**
     * Clear the dialog panel.
     */
    public void onClick$btnClearDialog() {
        ZKUtil.detachChildren(pnlDialog);
    }
    
    /**
     * Refresh the display.
     */
    public void onClick$btnRefresh() {
        refresh();
    }
    
    /**
     * Invokes the participate invitation dialog.
     */
    public void onClick$btnInvite() {
        InviteController.show(sessionId, model);
    }
    
    /**
     * Enables the send button when text is present in the message text box.
     * 
     * @param event
     */
    public void onChanging$txtMessage(InputEvent event) {
        updateControls(event.getValue().trim().isEmpty());
    }
    
    /**
     * Adds a message to the dialog panel.
     * 
     * @param chatMessage Message to add.
     */
    private void addDialog(ChatMessage chatMessage) {
        if (chatMessage != null) {
            String header = chatMessage.sender.getUserName() + " @ " + DateUtil.formatDate(chatMessage.timestamp);
            addDialog(header, chatMessage.message, chatMessage.sender.equals(chatService.getSelf()));
        }
    }
    
    /**
     * Adds a dialog fragment to the dialog panel.
     * 
     * @param header Header for the fragment.
     * @param text Text for the fragment.
     * @param self If true, this message comes from oneself.
     */
    private void addDialog(String header, String text, boolean self) {
        String selfStyle = self ? "-self" : "";
        newLabel(header, "chat-dialog-header" + selfStyle);
        newLabel(text, "chat-dialog-text" + selfStyle);
    }
    
    /**
     * Adds a text entry to the dialog panel.
     * 
     * @param text Text to add.
     * @param sclass SClass of the added text.
     */
    private void newLabel(String text, String sclass) {
        Label lbl = new Label(text);
        lbl.setSclass(sclass);
        lbl.setMultiline(true);
        lbl.setPre(true);
        pnlDialog.appendChild(lbl);
        Clients.scrollIntoView(lbl);
    }
    
    /**
     * Adds a newly joined participant to the active participant list.
     * 
     * @param participant Participant to add.
     */
    @Override
    public void onParticipantAdded(IPublisherInfo participant, boolean fromRefresh) {
        if (model.add(participant) && !fromRefresh && !participant.equals(chatService.getSelf())) {
            addDialog(StrUtil.formatMessage("@chat.session.event.join", participant.getUserName()), null, false);
        }
    }
    
    /**
     * Remove a participant from the list;
     * 
     * @param participant Participant to remove.
     */
    @Override
    public void onParticipantRemoved(IPublisherInfo participant) {
        if (model.remove(participant)) {
            addDialog(StrUtil.formatMessage("@chat.session.event.leave", participant.getUserName()), null, false);
        }
    }
    
    /**
     * Returns the id of the session to which this controller is bound.
     * 
     * @return The session id.
     */
    public String getSessionId() {
        return sessionId;
    }
    
    /**
     * Catch the close event.
     */
    public void onClose() {
        close();
    }
    
    /**
     * Closes the chat dialog. Unsubscribes from all events and notifies the chat service.
     */
    public void close() {
        doSubscribe(false);
        root.detach();
        chatService.onSessionClosed(this);
    }
    
    /**
     * Allows IOC container to inject chat service.
     * 
     * @param chatService
     */
    public void setChatService(ChatService chatService) {
        this.chatService = chatService;
    }
    
    /**
     * Handles chat dialog.
     */
    @Override
    public void eventCallback(String eventName, ChatMessage chatMessage) {
        if (!chatMessage.sender.equals(chatService.getSelf())) {
            addDialog(chatMessage);
        }
    }
    
}
