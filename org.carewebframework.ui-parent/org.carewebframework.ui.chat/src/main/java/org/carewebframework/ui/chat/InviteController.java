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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.carewebframework.api.event.IPublisherInfo;
import org.carewebframework.ui.FrameworkController;
import org.carewebframework.ui.zk.PopupDialog;
import org.carewebframework.ui.zk.RowComparator;
import org.carewebframework.ui.zk.ZKUtil;

import org.zkoss.zk.ui.Component;
import org.zkoss.zul.Button;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.ListModel;
import org.zkoss.zul.ListModelSet;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listheader;

/**
 * Controller for inviting participants to a chat session.
 */
public class InviteController extends FrameworkController {
    
    private static final long serialVersionUID = 1L;
    
    private static final String DIALOG = ZKUtil.getResourcePath(InviteController.class) + "invite.zul";
    
    private static final String ATTR_HIDE = InviteController.class.getName() + ".HIDE_ACTIVE";
    
    private Listbox lstSessions;
    
    private Listheader getUserName;
    
    private Button btnInvite;
    
    private Checkbox chkHideActive;
    
    private final ListModelSet<IPublisherInfo> model = new ListModelSet<>();
    
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
        Map<Object, Object> args = new HashMap<>();
        args.put("sessionId", sessionId);
        args.put("exclusions", exclusions);
        return (Collection<IPublisherInfo>) PopupDialog.popup(DIALOG, args, true, true, true).getAttribute("invitees");
    }
    
    /**
     * Initialize the dialog.
     */
    @SuppressWarnings("unchecked")
    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        sessionId = (String) arg.get("sessionId");
        Collection<IPublisherInfo> exclusions = (Collection<IPublisherInfo>) arg.get("exclusions");
        model.setMultiple(lstSessions.isMultiple());
        renderer = new ParticipantRenderer(chatService.getSelf(), exclusions);
        lstSessions.setItemRenderer(renderer);
        RowComparator.autowireColumnComparators(lstSessions.getListhead().getChildren());
        chkHideActive.setChecked(getAppFramework().getAttribute(ATTR_HIDE) != null);
        refresh();
    }
    
    /**
     * Refresh the participant list.
     */
    @Override
    public void refresh() {
        lstSessions.setModel((ListModel<?>) null);
        model.clear();
        model.addAll(chatService.getChatCandidates());
        renderer.setHideExclusions(chkHideActive.isChecked());
        lstSessions.setModel(model);
        getUserName.sort(true);
        updateControls();
    }
    
    /**
     * Updates controls to reflect the current selection state.
     */
    private void updateControls() {
        btnInvite.setDisabled(model.isSelectionEmpty());
    }
    
    /**
     * Update control states when the selection state changes.
     */
    public void onSelect$lstSessions() {
        updateControls();
    }
    
    /**
     * Send invitations to the selected participants, then close the dialog.
     */
    public void onClick$btnInvite() {
        Collection<IPublisherInfo> invitees = model.getSelection();
        chatService.invite(sessionId, invitees, false);
        root.setAttribute("invitees", invitees);
        root.detach();
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
