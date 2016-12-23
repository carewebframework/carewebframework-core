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
package org.carewebframework.ui.dialog;

import static org.carewebframework.ui.dialog.DialogConstants.LABEL_ID_CANCEL;
import static org.carewebframework.ui.dialog.DialogConstants.LABEL_ID_OK;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.carewebframework.common.MiscUtil;
import org.carewebframework.common.StrUtil;
import org.carewebframework.ui.dialog.DialogControl.ChoiceFormat;
import org.carewebframework.ui.dialog.DialogControl.IPromptCallback;
import org.carewebframework.web.ancillary.IAutoWired;
import org.carewebframework.web.annotation.WiredComponent;
import org.carewebframework.web.client.ExecutionContext;
import org.carewebframework.web.component.BaseComponent;
import org.carewebframework.web.component.BaseUIComponent;
import org.carewebframework.web.component.Button;
import org.carewebframework.web.component.Cell;
import org.carewebframework.web.component.Checkbox;
import org.carewebframework.web.component.Listbox;
import org.carewebframework.web.component.Listitem;
import org.carewebframework.web.component.Toolbar;
import org.carewebframework.web.component.Window;
import org.carewebframework.web.event.ClickEvent;
import org.carewebframework.web.event.DblclickEvent;
import org.carewebframework.web.event.Event;
import org.carewebframework.web.event.IEventListener;
import org.carewebframework.web.page.PageUtil;

/**
 * Implements a simple, generic dialog to prompt for specified responses.
 */
public class PromptDialog implements IAutoWired {
    
    protected static final Log log = LogFactory.getLog(PromptDialog.class.getClass());
    
    /**
     * Display the prompt dialog.
     * 
     * @param <T> The type of response object.
     * @param message Text message
     * @param title Title of dialog
     * @param styles Style classes for icon, message text, and panel (pipe-delimited)
     * @param responses Responses for the dialog.
     * @param excludeResponses Only applies if saveResponseId is specified. This is a list of
     *            responses that will not be saved.
     * @param defaultResponse Default response for the dialog.
     * @param saveResponseId Uniquely identifies this response for purposes of saving and retrieving
     *            the last response. If not specified (null or empty), the response is not saved.
     *            Otherwise, if a saved response exists, it is returned without displaying the
     *            dialog. If a saved response does not exist, the user is prompted in the normal
     *            manner with the addition of a check box on the dialog asking if the response is to
     *            be saved. If this box is checked, the user's response is then saved as a user
     *            preference.
     * @param callback Callback to receive response.
     */
    public static <T> void show(String message, String title, String styles, T[] responses, T[] excludeResponses,
                                T defaultResponse, String saveResponseId, IPromptCallback<T> callback) {
        show(new DialogControl<T>(message, title, styles, responses, excludeResponses, defaultResponse, saveResponseId,
                callback));
    }
    
    /**
     * Display the prompt dialog.
     * 
     * @param message Text message
     * @param title Title of dialog
     * @param styles Style classes for icon, message text, and panel (pipe-delimited)
     * @param responses Response text (pipe-delimited)
     * @param excludeResponses Only applies if saveResponseId is specified. This is a list of
     *            responses that will not be saved and is specified in the same format as the
     *            buttonCaptions parameter.
     * @param defaultResponse Default response text
     * @param saveResponseId Uniquely identifies this response for purposes of saving and retrieving
     *            the last response. If not specified (null or empty), the response is not saved.
     *            Otherwise, if a saved response exists, it is returned without displaying the
     *            dialog. If a saved response does not exist, the user is prompted in the normal
     *            manner with the addition of a check box on the dialog asking if the response is to
     *            be saved. If this box is checked, the user's response is then saved as a user
     *            preference.
     * @param callback Callback to receive response.
     */
    public static void show(String message, String title, String styles, String responses, String excludeResponses,
                            String defaultResponse, String saveResponseId, IPromptCallback<String> callback) {
        show(DialogControl.toDialogParameters(message, title, styles, responses, excludeResponses, defaultResponse,
            saveResponseId, callback));
    }
    
    /**
     * Display the prompt dialog.
     * 
     * @param <T> The type of response object.
     * @param control The dialog control.
     */
    public static <T> void show(DialogControl<T> control) {
        DialogResponse<T> response = control.getLastResponse();
        
        if (response != null) {
            control.callback(response);
            return;
        }
        
        Window root = null;
        
        try {
            Map<String, Object> args = Collections.singletonMap("control", control);
            root = (Window) PageUtil
                    .createPage(DialogConstants.RESOURCE_PREFIX + "promptDialog.cwf", ExecutionContext.getPage(), args)
                    .get(0);
            root.modal(null);
        } catch (Exception e) {
            log.error("Error Displaying Dialog", e);
            
            if (root != null) {
                root.destroy();
            }
            
            throw MiscUtil.toUnchecked(e);
        }
    }
    
    /******************** Controller *******************/
    
    private final IEventListener clickListener = new IEventListener() {
        
        /**
         * When a response component is clicked, return its associated response and close the
         * dialog.
         * 
         * @param event Click event.
         */
        @Override
        public void onEvent(Event event) {
            close((event.getTarget()));
        }
    };
    
    DialogControl<?> control;
    
    @WiredComponent
    private Listbox listbox;
    
    @WiredComponent
    private Checkbox chkRemember;
    
    @WiredComponent
    private Cell message;
    
    @WiredComponent
    private BaseUIComponent icon;
    
    @WiredComponent
    private Toolbar toolbar;
    
    private Window root;
    
    private DialogResponse<?> response;
    
    @Override
    public void afterInitialized(BaseComponent comp) {
        this.root = (Window) comp;
        root.setAttribute("controller", this);
        control = (DialogControl<?>) root.getAttribute("control");
        root.setTitle(control.getTitle());
        icon.addClass(control.getIconClass());
        message.addClass(control.getTextClass());
        message.setLabel(control.getMessage());
        root.addClass(control.getPanelClass());
        chkRemember.setVisible(root.hasAttribute("remember"));
        root.setOnCanClose(() -> {
            control.callback(response);
            return true;
        });
        
        if (control.getFormat() == ChoiceFormat.BUTTONS) {
            processButtonResponses();
        } else {
            processListResponses();
        }
    }
    
    private void processButtonResponses() {
        List<?> responses = control.getResponses();
        
        for (Object rsp : responses) {
            DialogResponse<?> response = (DialogResponse<?>) rsp;
            Button button = addButton(response.getLabel(), response.getFlavor(), clickListener);
            button.setData(response);
            
            if (response.isDefault()) {
                button.setFocus(true);
            }
        }
    }
    
    private void processListResponses() {
        List<?> responses = control.getResponses();
        listbox.setVisible(true);
        
        for (Object rsp : responses) {
            DialogResponse<?> response = (DialogResponse<?>) rsp;
            Listitem item = new Listitem(StrUtil.formatMessage(response.getLabel()));
            item.addEventListener(DblclickEvent.TYPE, clickListener);
            item.setData(response);
            listbox.addChild(item);
            
            if (response.isDefault()) {
                item.setSelected(true);
            }
        }
        
        if (listbox.getSelectedCount() == 0) {
            listbox.setSelectedItem(listbox.getChild(Listitem.class));
        }
        
        addButton(LABEL_ID_CANCEL, "danger", (event) -> {
            close(null);
        });
        
        addButton(LABEL_ID_OK, "success", (event) -> {
            close(listbox.getSelectedItem());
        });
    }
    
    private Button addButton(String label, String flavor, IEventListener listener) {
        Button button = new Button(StrUtil.formatMessage(label));
        button.addClass("flavor:btn-" + flavor);
        button.addEventListener(ClickEvent.TYPE, listener);
        toolbar.addChild(button);
        return button;
    }
    
    /**
     * Retrieve the response and close the dialog.
     * 
     * @param component The component associated with the response.
     */
    private void close(BaseComponent component) {
        response = component == null ? null : (DialogResponse<?>) component.getData();
        root.close();
    }
    
}
