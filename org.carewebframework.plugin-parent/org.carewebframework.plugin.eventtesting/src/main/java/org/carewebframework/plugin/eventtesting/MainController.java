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
package org.carewebframework.plugin.eventtesting;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.carewebframework.api.event.EventManager;
import org.carewebframework.api.event.EventUtil;
import org.carewebframework.api.event.IEventManager;
import org.carewebframework.api.event.IGenericEvent;
import org.carewebframework.api.messaging.Recipient;
import org.carewebframework.api.messaging.Recipient.RecipientType;
import org.fujion.common.JSONUtil;
import org.fujion.common.StrUtil;
import org.carewebframework.shell.plugins.PluginController;
import org.fujion.annotation.EventHandler;
import org.fujion.annotation.WiredComponent;
import org.fujion.client.ClientUtil;
import org.fujion.component.Checkbox;
import org.fujion.component.Label;
import org.fujion.component.Listbox;
import org.fujion.component.Listitem;
import org.fujion.component.Memobox;
import org.fujion.component.Textbox;
import org.fujion.event.ChangeEvent;

/**
 * Plug-in to test remote events.
 */
public class MainController extends PluginController implements IGenericEvent<Object> {

    @WiredComponent
    private Textbox tboxEventName;

    @WiredComponent
    private Textbox tboxEventRecipients;

    @WiredComponent
    private Memobox tboxEventData;

    @WiredComponent
    private Memobox tboxEventResults;

    @WiredComponent
    private Textbox tboxNewEvent;

    @WiredComponent
    private Listbox lboxEventList;

    @WiredComponent
    private Checkbox chkAutoGenerate;

    @WiredComponent
    private Checkbox chkScrollLock;

    @WiredComponent
    private Label lblInfo;

    private final IEventManager eventManager = EventManager.getInstance();

    private int messageCount;

    @EventHandler(value = "click", target = "btnSend")
    private void onClick$btnSend() {
        messageCount++;

        if (chkAutoGenerate.isChecked()) {
            tboxEventData.setValue("Sending test event #" + messageCount);
        }

        eventManager.fireRemoteEvent(tboxEventName.getValue(), tboxEventData.getValue(),
            parseRecipients(tboxEventRecipients.getValue()));
        info("Fired", tboxEventName.getValue());
    }

    @EventHandler(value = "click", target = "btnReset")
    private void onClick$btnReset() {
        tboxEventName.setValue("");
        tboxEventRecipients.setValue("");
        tboxEventData.setValue("");
    }

    @EventHandler(value = "click", target = "btnPing")
    private void onClick$btnPing() {
        EventUtil.ping("PING.RESPONSE", null);
    }

    @EventHandler(value = "click", target = "btnClear")
    private void onClick$btnClear() {
        tboxEventResults.setValue("");
    }

    @EventHandler(value = "click", target = "btnNewEvent")
    private void onClick$btnNewEvent() {
        String eventName = StringUtils.trimToNull(tboxNewEvent.getValue());

        if (eventName != null && !containsEvent(eventName)) {
            Listitem item = new Listitem();
            item.setLabel(eventName);
            lboxEventList.addChild(item);
        }

        tboxNewEvent.setValue("");
    }

    private Recipient[] parseRecipients(String text) {
        if (text == null || text.isEmpty()) {
            return null;
        }

        List<Recipient> recipients = new ArrayList<>();

        for (String recip : text.split("\\,")) {
            String[] pcs = recip.split("\\:", 2);

            if (pcs.length == 2) {
                RecipientType type = RecipientType.valueOf(pcs[0].trim().toUpperCase());
                recipients.add(new Recipient(type, pcs[1]));
            }
        }

        return recipients.isEmpty() ? null : (Recipient[]) recipients.toArray();
    }

    private boolean containsEvent(String eventName) {
        for (Object object : lboxEventList.getChildren()) {
            if (((Listitem) object).getLabel().equals(eventName)) {
                return true;
            }
        }

        return false;
    }

    @EventHandler(value = "change", target = "lboxEventList")
    private void onChange$lboxEventList(ChangeEvent event) {
        Listitem item = (Listitem) event.getValue();
        String eventName = item.getLabel();

        if (item.isSelected()) {
            eventManager.subscribe(eventName, this);
            info("Subscribed to", eventName);
        } else {
            eventManager.unsubscribe(eventName, this);
            info("Unsubscribed from", eventName);
        }
    }

    private void info(String action, String eventName) {
        lblInfo.setLabel(action + " '" + eventName + " ' event.");
    }

    @Override
    public void eventCallback(String eventName, Object eventData) {
        String s = tboxEventResults.getValue();

        if (!(eventData instanceof String)) {
            try {
                eventData = JSONUtil.serialize(eventData, true);
            } catch (Exception e) {}
        }

        s += "\n\n" + eventName + ":\n" + eventData;
        tboxEventResults.setValue(s);
        info("Received", eventName);

        if (!chkScrollLock.isChecked()) {
            String js = StrUtil.formatMessage("$('#%1$s').scrollTop($('#%1$s')[0].scrollHeight);", tboxEventResults.getId());
            ClientUtil.eval(js);
        }
    }

}
