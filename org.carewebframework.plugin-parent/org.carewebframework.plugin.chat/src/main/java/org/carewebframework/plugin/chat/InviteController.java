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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.carewebframework.api.messaging.IPublisherInfo;
import org.carewebframework.ui.FrameworkController;
import org.carewebframework.ui.dialog.PopupDialog;
import org.carewebframework.ui.util.CWFUtil;
import org.carewebframework.web.component.BaseComponent;
import org.carewebframework.web.component.Button;
import org.carewebframework.web.component.Checkbox;
import org.carewebframework.web.component.Column;
import org.carewebframework.web.component.Row;
import org.carewebframework.web.component.Table;
import org.carewebframework.web.model.ListModel;

/**
 * Controller for inviting participants to a chat session.
 */
public class InviteController extends FrameworkController {
    
    private static final String DIALOG = CWFUtil.getResourcePath(InviteController.class) + "invite.cwf";
    
    private static final String ATTR_HIDE = InviteController.class.getName() + ".HIDE_ACTIVE";
    
    private Table sessions;
    
    private Column getUserName;
    
    private Button btnInvite;
    
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
     * @return List of participants that were sent invitations, or null if the dialog was cancelled.
     */
    @SuppressWarnings("unchecked")
    public static Collection<IPublisherInfo> show(String sessionId, Collection<IPublisherInfo> exclusions) {
        Map<String, Object> args = new HashMap<>();
        args.put("sessionId", sessionId);
        args.put("exclusions", exclusions);
        return (Collection<IPublisherInfo>) PopupDialog.show(DIALOG, args, true, true, true, null).getAttribute("invitees");
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
        sessions.getRows().getModelAndView(IPublisherInfo.class).setRenderer(renderer);
        chkHideActive.setChecked(getAppFramework().getAttribute(ATTR_HIDE) != null);
        refresh();
    }
    
    /**
     * Refresh the participant list.
     */
    @Override
    public void refresh() {
        sessions.getRows().getModelAndView(IPublisherInfo.class).setModel(null);
        model.clear();
        model.addAll(chatService.getChatCandidates());
        renderer.setHideExclusions(chkHideActive.isChecked());
        sessions.getRows().getModelAndView(IPublisherInfo.class).setModel(model);
        getUserName.sort();
        updateControls();
    }
    
    /**
     * Updates controls to reflect the current selection state.
     */
    private void updateControls() {
        btnInvite.setDisabled(sessions.getRows().getSelectedCount() == 0);
    }
    
    /**
     * Update control states when the selection state changes.
     */
    public void onSelect$sessions() {
        updateControls();
    }
    
    /**
     * Send invitations to the selected participants, then close the dialog.
     */
    public void onClick$btnInvite() {
        List<IPublisherInfo> invitees = getInvitees();
        chatService.invite(sessionId, invitees, false);
        root.setAttribute("invitees", invitees);
        root.detach();
    }
    
    private List<IPublisherInfo> getInvitees() {
        List<IPublisherInfo> invitees = new ArrayList<>();
        
        for (Row row : sessions.getRows().getSelected()) {
            invitees.add(row.getData(IPublisherInfo.class));
        }
        
        return invitees;
    }
    
    /**
     * Update display when hide active participants setting changes.
     */
    public void onCheck$chkHideActive() {
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
