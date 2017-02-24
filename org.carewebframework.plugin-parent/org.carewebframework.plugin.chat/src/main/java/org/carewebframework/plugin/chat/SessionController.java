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
package org.carewebframework.plugin.chat;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.carewebframework.api.messaging.IPublisherInfo;
import org.carewebframework.common.DateUtil;
import org.carewebframework.common.StrUtil;
import org.carewebframework.plugin.chat.SessionService.ISessionUpdate;
import org.carewebframework.ui.FrameworkController;
import org.carewebframework.ui.dialog.PopupDialog;
import org.carewebframework.ui.util.CWFUtil;
import org.carewebframework.web.ancillary.IResponseCallback;
import org.carewebframework.web.annotation.EventHandler;
import org.carewebframework.web.annotation.WiredComponent;
import org.carewebframework.web.component.BaseComponent;
import org.carewebframework.web.component.Button;
import org.carewebframework.web.component.Label;
import org.carewebframework.web.component.Listbox;
import org.carewebframework.web.component.Memobox;
import org.carewebframework.web.component.Window;
import org.carewebframework.web.event.ChangeEvent;
import org.carewebframework.web.model.ListModel;

/**
 * Controller for an individual chat session.
 */
public class SessionController extends FrameworkController implements ISessionUpdate {

    private static final String DIALOG = CWFUtil.getResourcePath(SessionController.class) + "session.cwf";

    private String sessionId;

    private ChatService chatService;

    private Window window;
    
    @WiredComponent
    private Listbox lstParticipants;

    @WiredComponent
    private BaseComponent pnlDialog;

    @WiredComponent
    private Memobox txtMessage;

    @WiredComponent
    private Button btnSendMessage;

    private final ListModel<IPublisherInfo> model = new ListModel<>();

    private final Set<IPublisherInfo> outstandingInvitations = new HashSet<>();

    private SessionService sessionService;

    /**
     * Creates a chat session bound to the specified session id.
     *
     * @param sessionId The chat session id.
     * @param originator If true, this user is originating the chat session.
     * @return The controller for the chat session.
     */
    protected static SessionController create(String sessionId, boolean originator) {
        Map<String, Object> args = new HashMap<>();
        args.put("id", sessionId);
        args.put("title", StrUtil.formatMessage("@cwf.chat.session.title"));
        args.put("originator", originator ? true : null);
        Window dlg = PopupDialog.show(DIALOG, args, true, true, false, null);
        return (SessionController) FrameworkController.getController(dlg);
    }

    /**
     * Initialize the dialog.
     */
    @Override
    public void afterInitialized(BaseComponent comp) {
        super.afterInitialized(comp);
        window = (Window) comp;
        sessionId = (String) comp.getAttribute("id");
        lstParticipants.setRenderer(new ParticipantRenderer(chatService.getSelf(), null));
        model.add(chatService.getSelf());
        lstParticipants.setModel(model);
        clearMessage();

        if (comp.getAttribute("originator") != null) {
            invite((result) -> {
                if (!result) {
                    close();
                } else {
                    initSession();
                }
            });
        } else {
            initSession();
        }
    }

    private void initSession() {
        sessionService = SessionService.create(chatService.getSelf(), sessionId, getEventManager(), this);
        sessionService.setActive(true);
        window.popup(null);
    }

    /**
     * Extend chat invitation.
     *
     * @param callback Reports true if invitations were sent.
     */
    private void invite(IResponseCallback<Boolean> callback) {
        InviteController.show(sessionId, model, (invitees) -> {
            if (invitees != null) {
                outstandingInvitations.addAll(invitees);
                IResponseCallback.invoke(callback, true);
            } else {
                IResponseCallback.invoke(callback, false);
            }
        });
    }

    /**
     * Refreshes the participant list.
     */
    @Override
    public void refresh() {
        model.clear();
        model.add(chatService.getSelf());
        sessionService.refresh();
    }

    /**
     * Clear any text in the message text box.
     */
    private void clearMessage() {
        txtMessage.setValue(null);
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
    @EventHandler(value = "click", target = "btnClearMessage")
    private void onClick$btnClearMessage() {
        clearMessage();
    }

    /**
     * Send the message text.
     */
    @EventHandler(value = "click", target = "btnSendMessage")
    private void onClick$btnSendMessage() {
        addDialog(sessionService.sendMessage(txtMessage.getValue().trim()), true);
        clearMessage();
    }

    /**
     * Clear the dialog panel.
     */
    @EventHandler(value = "click", target = "btnClearDialog")
    private void onClick$btnClearDialog() {
        pnlDialog.destroyChildren();
    }

    /**
     * Refresh the display.
     */
    @EventHandler(value = "click", target = "btnRefresh")
    private void onClick$btnRefresh() {
        refresh();
    }

    /**
     * Invokes the participate invitation dialog.
     */
    @EventHandler(value = "click", target = "btnInvite")
    private void onClick$btnInvite() {
        invite(null);
    }

    /**
     * Enables the send button when text is present in the message text box.
     *
     * @param event The input event.
     */
    @EventHandler(value = "change", target = "txtMessage")
    private void onChange$txtMessage(ChangeEvent event) {
        updateControls(event.getValue(String.class).trim().isEmpty());
    }

    /**
     * Adds a message to the dialog panel.
     *
     * @param chatMessage Message to add.
     * @param self True if this user is the message author.
     */
    private void addDialog(ChatMessage chatMessage, boolean self) {
        if (chatMessage != null) {
            String header = chatMessage.sender.getUserName() + " @ " + DateUtil.formatDate(chatMessage.timestamp);
            addDialog(header, chatMessage.message, self);
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
        lbl.addClass(sclass);
        //lbl.setMultiline(true);
        //lbl.setPre(true);
        pnlDialog.addChild(lbl);
        lbl.scrollIntoView(true);
    }

    /**
     * Adds a newly joined participant to the active participant list.
     *
     * @param participant Participant to add.
     */
    @Override
    public void onParticipantAdded(IPublisherInfo participant, boolean fromRefresh) {
        if (model.add(participant) && !fromRefresh && !participant.equals(chatService.getSelf())) {
            addDialog(StrUtil.formatMessage("@cwf.chat.session.event.join", participant.getUserName()), null, false);
        }

        outstandingInvitations.remove(participant);
    }

    /**
     * Remove a participant from the list;
     *
     * @param participant Participant to remove.
     */
    @Override
    public void onParticipantRemoved(IPublisherInfo participant) {
        if (model.remove(participant)) {
            addDialog(StrUtil.formatMessage("@cwf.chat.session.event.leave", participant.getUserName()), null, false);
        }
    }

    /**
     * Handles chat message receipt.
     */
    @Override
    public void onMessageReceived(ChatMessage chatMessage) {
        addDialog(chatMessage, false);
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
    @EventHandler("close")
    private void onClose() {
        if (sessionService != null) {
            sessionService.setActive(false);
        }

        chatService.invite(sessionId, outstandingInvitations, true);
        chatService.onSessionClosed(this);
    }

    /**
     * Closes the chat dialog. Unsubscribes from all events and notifies the chat service.
     */
    public void close() {
        window.fireEvent("close");
    }

    /**
     * Allows IOC container to inject chat service.
     *
     * @param chatService The chat service.
     */
    public void setChatService(ChatService chatService) {
        this.chatService = chatService;
    }

}
