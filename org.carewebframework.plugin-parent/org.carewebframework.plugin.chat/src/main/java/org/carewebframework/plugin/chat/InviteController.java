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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.carewebframework.api.messaging.IPublisherInfo;
import org.carewebframework.ui.controller.FrameworkController;
import org.carewebframework.ui.dialog.PopupDialog;
import org.carewebframework.ui.util.CWFUtil;
import org.carewebframework.web.ancillary.IResponseCallback;
import org.carewebframework.web.annotation.EventHandler;
import org.carewebframework.web.annotation.WiredComponent;
import org.carewebframework.web.component.BaseComponent;
import org.carewebframework.web.component.Button;
import org.carewebframework.web.component.Checkbox;
import org.carewebframework.web.component.Listbox;
import org.carewebframework.web.component.Listitem;
import org.carewebframework.web.model.ListModel;

/**
 * Controller for inviting participants to a chat session.
 */
public class InviteController extends FrameworkController {
    
    private static final String DIALOG = CWFUtil.getResourcePath(InviteController.class) + "invite.cwf";
    
    private static final String ATTR_HIDE = InviteController.class.getName() + ".HIDE_ACTIVE";
    
    private static final Comparator<IPublisherInfo> sessionComparator = (s1, s2) -> {
        return s1.getUserName().compareToIgnoreCase(s2.getUserName());
    };

    @WiredComponent
    private Listbox lstSessions;
    
    @WiredComponent
    private Button btnInvite;
    
    @WiredComponent
    private Checkbox chkHideActive;
    
    private final ListModel<IPublisherInfo> model = new ListModel<>();
    
    private String sessionId;
    
    private ChatService chatService;
    
    private ParticipantRenderer renderer;
    
    /**
     * Displays the participant invitation dialog.
     *
     * @param sessionId The id of the chat session making the invitation request.
     * @param exclusions List of participants that should be excluded from user selection.
     * @param callback Reports the list of participants that were sent invitations, or null if the
     *            dialog was cancelled.
     */
    @SuppressWarnings("unchecked")
    public static void show(String sessionId, Collection<IPublisherInfo> exclusions,
                            IResponseCallback<Collection<IPublisherInfo>> callback) {
        Map<String, Object> args = new HashMap<>();
        args.put("sessionId", sessionId);
        args.put("exclusions", exclusions);
        PopupDialog.show(DIALOG, args, true, true, true, (event) -> {
            Collection<IPublisherInfo> invitees = (Collection<IPublisherInfo>) event.getTarget().getAttribute("invitees");
            IResponseCallback.invoke(callback, invitees);
        });
    }
    
    /**
     * Initialize the dialog.
     */
    @SuppressWarnings("unchecked")
    @Override
    public void afterInitialized(BaseComponent comp) {
        super.afterInitialized(comp);
        sessionId = (String) comp.findAttribute("sessionId");
        Collection<IPublisherInfo> exclusions = (Collection<IPublisherInfo>) comp.findAttribute("exclusions");
        renderer = new ParticipantRenderer(chatService.getSelf(), exclusions);
        lstSessions.setRenderer(renderer);
        chkHideActive.setChecked(getAppFramework().getAttribute(ATTR_HIDE) != null);
        refresh();
    }
    
    /**
     * Refresh the participant list.
     */
    @Override
    public void refresh() {
        lstSessions.setModel(null);
        model.clear();
        model.addAll(chatService.getChatCandidates());
        renderer.setHideExclusions(chkHideActive.isChecked());
        model.sort(sessionComparator);
        lstSessions.setModel(model);
        updateControls();
    }
    
    /**
     * Updates controls to reflect the current selection state.
     */
    private void updateControls() {
        btnInvite.setDisabled(lstSessions.getSelectedCount() == 0);
    }
    
    /**
     * Update control states when the selection state changes.
     */
    @EventHandler(value = "change", target = "@lstSessions")
    private void onChange$lstSessions() {
        updateControls();
    }
    
    /**
     * Send invitations to the selected participants, then close the dialog.
     */
    @EventHandler(value = "click", target = "@btnInvite")
    private void onClick$btnInvite() {
        List<IPublisherInfo> invitees = getInvitees();
        chatService.invite(sessionId, invitees, false);
        root.setAttribute("invitees", invitees);
        root.detach();
    }
    
    private List<IPublisherInfo> getInvitees() {
        List<IPublisherInfo> invitees = new ArrayList<>();
        
        for (Listitem item : lstSessions.getSelected()) {
            invitees.add(item.getData(IPublisherInfo.class));
        }
        
        return invitees;
    }
    
    /**
     * Update display when hide active participants setting changes.
     */
    @EventHandler(value = "change", target = "@chkHideActive")
    private void onCheck$chkHideActive() {
        getAppFramework().setAttribute(ATTR_HIDE, chkHideActive.isChecked() ? true : null);
        refresh();
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
