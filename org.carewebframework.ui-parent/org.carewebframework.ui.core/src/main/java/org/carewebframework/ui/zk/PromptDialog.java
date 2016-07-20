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
package org.carewebframework.ui.zk;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.carewebframework.api.property.PropertyUtil;
import org.carewebframework.common.StrUtil;

import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zk.ui.util.ConventionWires;
import org.zkoss.zul.Button;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

/**
 * Implements a simple, generic dialog for prompting for arbitrary responses.
 */
public class PromptDialog extends Window {
    
    
    private static final long serialVersionUID = 1L;
    
    protected static final Log log = LogFactory.getLog(PromptDialog.class.getClass());
    
    private static final String ATTR_RESPONSE = "response";
    
    private static final String SAVED_RESPONSE_PROP_NAME = "CAREWEB.SAVED.RESPONSES";
    
    private static final String RESOURCE_PREFIX = ZKUtil.getResourcePath(PromptDialog.class);
    
    private static final String STYLE_FIXED_FONT = "|cwf-fixed-font";
    
    private static final String LABEL_ID_OK = "@cwf.btn.ok.label";
    
    private static final String LABEL_ID_CANCEL = "@cwf.btn.cancel.label";
    
    private static final String LABEL_IDS_OK_CANCEL = LABEL_ID_OK + "|" + LABEL_ID_CANCEL;
    
    private static final String STYLES_INFO = Messagebox.INFORMATION + "||panel-info";
    
    private static final String STYLES_WARNING = Messagebox.EXCLAMATION + "||panel-warning";
    
    private static final String STYLES_ERROR = Messagebox.ERROR + "||panel-danger";
    
    private static final String STYLES_QUESTION = Messagebox.QUESTION + "||panel-primary";
    
    /**
     * A response object that will be associated the button that, when clicked, will generate the
     * response.
     */
    public static class Response {
        
        
        private final int index;
        
        private final String label;
        
        private final boolean excluded;
        
        private final boolean ok;
        
        private final boolean cancel;
        
        private final boolean dflt;
        
        Response(int index, String label, boolean excluded, boolean dflt) {
            this.index = index;
            this.ok = PromptDialog.isOK(label);
            this.cancel = PromptDialog.isCancel(label);
            this.label = StrUtil.formatMessage(label);
            this.excluded = excluded;
            this.dflt = dflt;
        }
        
        /**
         * Returns the response index.
         * 
         * @return The response index.
         */
        public int getIndex() {
            return index;
        }
        
        /**
         * Returns the response label.
         * 
         * @return The response label.
         */
        public String getLabel() {
            return label;
        }
        
        /**
         * Returns true if the response is never to be saved.
         * 
         * @return True to suppress saving of response.
         */
        public boolean isExcluded() {
            return excluded;
        }
        
        /**
         * True if this response corresponds to an "OK" response.
         * 
         * @return True if OK response.
         */
        public boolean isOk() {
            return ok;
        }
        
        /**
         * True if this response corresponds to a "Cancel" response.
         * 
         * @return True if cancel response.
         */
        public boolean isCancel() {
            return cancel;
        }
        
        /**
         * True if this is the default response.
         * 
         * @return True if default response.
         */
        public boolean isDflt() {
            return dflt;
        }
        
    }
    
    private Response _response;
    
    private EventListener<Event> _listener;
    
    private Object _input;
    
    private boolean _remember;
    
    private Textbox textBox;
    
    private Listbox listBox;
    
    private Checkbox chkRemember;
    
    private Button btnOK;
    
    /**
     * Display the prompt dialog.
     * 
     * @param <T> Type of response object
     * @param message Text message
     * @param title Title of dialog
     * @param responses Array of response objects.
     * @param styles Style classes for icon, message text, and panel (pipe-delimited)
     * @param defaultResponse Default response object (associated button will have initial focus).
     * @param eventListener Optional event listener to intercept click events
     * @param saveResponseId Uniquely identifies this response for purposes of saving and retrieving
     *            the last response. If not specified (null or empty), the response is not saved.
     *            Otherwise, if a saved response exists, it is returned without displaying the
     *            dialog. If a saved response does not exist, the user is prompted in the normal
     *            manner with the addition of a check box on the dialog asking if the response is to
     *            be saved. If this box is checked, the user's response is then saved as a user
     *            preference.
     * @param excludeResponses Only applies if saveResponseId is specified. This is a list of
     *            responses that will not be saved.
     * @return Chosen response.
     */
    public static <T> T show(String message, String title, T[] responses, String styles, T defaultResponse,
                             EventListener<Event> eventListener, String saveResponseId, T[] excludeResponses) {
        int rsp = show(message, title, toResponseStr(responses), styles,
            defaultResponse == null ? null : defaultResponse.toString(), eventListener, saveResponseId,
            toResponseStr(excludeResponses));
        
        return rsp < 0 ? null : responses[rsp];
    }
    
    /**
     * Display the prompt dialog.
     * 
     * @param message Text message
     * @param title Title of dialog
     * @param buttonCaptions Button captions separated by vertical bars
     * @param styles Style classes for icon, message text, and panel (pipe-delimited)
     * @param defaultButton Caption of button to have initial focus
     * @param eventListener Optional event listener to intercept click events
     * @param saveResponseId Uniquely identifies this response for purposes of saving and retrieving
     *            the last response. If not specified (null or empty), the response is not saved.
     *            Otherwise, if a saved response exists, it is returned without displaying the
     *            dialog. If a saved response does not exist, the user is prompted in the normal
     *            manner with the addition of a check box on the dialog asking if the response is to
     *            be saved. If this box is checked, the user's response is then saved as a user
     *            preference.
     * @param excludeResponses Only applies if saveResponseId is specified. This is a list of
     *            responses that will not be saved and is specified in the same format as the
     *            buttonCaptions parameter.
     * @return Index of button that was clicked.
     */
    public static int show(String message, String title, String buttonCaptions, String styles, String defaultButton,
                           EventListener<Event> eventListener, String saveResponseId, String excludeResponses) {
        List<Response> responseList = toResponseList(buttonCaptions, excludeResponses, defaultButton);
        
        if (saveResponseId != null) {
            int i = getLastResponse(saveResponseId);
            
            if (i >= 0 && i < responseList.size() && !responseList.get(i).isExcluded()) {
                return i;
            }
        }
        
        Map<Object, Object> args = initArgs(message, title);
        String[] sclass = styles == null ? null : StrUtil.split(styles, "|", 3, false);
        args.put("iconClass", sclass == null ? null : sclass[0]);
        args.put("textClass", sclass == null ? null : sclass[1]);
        args.put("panelClass", sclass == null || sclass[2] == null ? "panel-primary" : sclass[2]);
        args.put("sclass", "panel-default");
        args.put("focus", StrUtil.formatMessage(defaultButton));
        args.put("responses", responseList);
        args.put("remember", saveResponseId != null);
        args.put("input", null);
        PromptDialog dlg = showDialog(args, eventListener, false);
        
        if (dlg != null) {
            Response response = dlg._response;
            
            if (response == null) {
                return -1;
            }
            
            if (dlg._remember && !response.isExcluded()) {
                saveLastResponse(saveResponseId, response.getIndex());
            }
            
            return response.getIndex();
            
        }
        return -1;
    }
    
    /**
     * Display the prompt dialog.
     * 
     * @param <T> Type of response object
     * @param message Text message
     * @param title Title of dialog
     * @param responses Array of response objects.
     * @param styles Style classes for icon, message text, and panel (pipe-delimited)
     * @param defaultResponse Default response object (associated button will have initial focus).
     * @param eventListener Optional event listener to intercept click events
     * @return Chosen response.
     */
    public static <T> T show(String message, String title, T[] responses, String styles, T defaultResponse,
                             EventListener<Event> eventListener) {
        return show(message, title, responses, styles, defaultResponse, eventListener, null, null);
    }
    
    /**
     * Display the prompt dialog.
     * 
     * @param message Text message
     * @param title Title of dialog
     * @param buttonCaptions Button captions separated by vertical bars
     * @param styles Style classes for icon, message text, and panel (pipe-delimited)
     * @param defaultButton Caption of button to have initial focus
     * @param eventListener Optional event listener to intercept click events
     * @return Index of button that was clicked.
     */
    public static int show(String message, String title, String buttonCaptions, String styles, String defaultButton,
                           EventListener<Event> eventListener) {
        return show(message, title, buttonCaptions, styles, defaultButton, eventListener, null, null);
    }
    
    /**
     * Display the prompt dialog.
     * 
     * @param <T> Type of response object
     * @param message Text message
     * @param title Title of dialog
     * @param responses Array of response objects.
     * @param styles Style classes for icon, message text, and panel (pipe-delimited)
     * @param defaultResponse Default response object (associated button will have initial focus).
     * @return Chosen response.
     */
    public static <T> T show(String message, String title, T[] responses, String styles, T defaultResponse) {
        return show(message, title, responses, styles, defaultResponse, null);
    }
    
    /**
     * Display the prompt dialog.
     * 
     * @param message Text message
     * @param title Title of dialog
     * @param buttonCaptions Button captions separated by vertical bars
     * @param styles Style classes for icon, message text, and panel (pipe-delimited)
     * @param defaultButton Caption of button to have initial focus
     * @return Index of button that was clicked.
     */
    public static int show(String message, String title, String buttonCaptions, String styles, String defaultButton) {
        return show(message, title, buttonCaptions, styles, defaultButton, null);
    }
    
    /**
     * Display the prompt dialog.
     * 
     * @param <T> Type of response object
     * @param message Text message
     * @param title Title of dialog
     * @param responses Array of response objects.
     * @param styles Style classes for icon, message text, and panel (pipe-delimited)
     * @return Chosen response.
     */
    public static <T> T show(String message, String title, T[] responses, String styles) {
        return show(message, title, responses, styles, null);
    }
    
    /**
     * Display the prompt dialog.
     * 
     * @param message Text message
     * @param title Title of dialog
     * @param buttonCaptions Button captions separated by vertical bars
     * @param styles Style classes for icon, message text, and panel (pipe-delimited)
     * @return Index of button that was clicked.
     */
    public static int show(String message, String title, String buttonCaptions, String styles) {
        return show(message, title, buttonCaptions, styles, null);
    }
    
    /**
     * Display the prompt dialog.
     * 
     * @param <T> Type of response object
     * @param message Text message
     * @param title Title of dialog
     * @param responses Array of response objects.
     * @return Chosen response.
     */
    public static <T> T show(String message, String title, T[] responses) {
        return show(message, title, responses, STYLES_INFO);
    }
    
    /**
     * Display the prompt dialog.
     * 
     * @param message Text message
     * @param title Title of dialog
     * @param buttonCaptions Button captions separated by vertical bars
     * @return Index of button that was clicked.
     */
    public static int show(String message, String title, String buttonCaptions) {
        return show(message, title, buttonCaptions, STYLES_INFO);
    }
    
    /**
     * Initialize dialog control parameters.
     * 
     * @param message Text message (if starts with "@", assumed to be a label name).
     * @param title Title of dialog (if starts with "@", assumed to be a label name)
     * @return Initialized map of control parameters.
     */
    private static Map<Object, Object> initArgs(String message, String title) {
        Map<Object, Object> args = new HashMap<>();
        args.put("message", StrUtil.formatMessage(message));
        args.put("title", StrUtil.formatMessage(title));
        return args;
    }
    
    /**
     * Create and modally display the dialog.
     * 
     * @param args Control parameter map.
     * @param eventListener An event listener.
     * @param useInputListener If true, use the generic input listener.
     * @return The prompt dialog.
     */
    private static PromptDialog showDialog(Map<Object, Object> args, EventListener<Event> eventListener,
                                           boolean useInputListener) {
        PromptDialog dlg = null;
        
        try {
            dlg = (PromptDialog) ZKUtil.loadZulPage(RESOURCE_PREFIX + "promptDialog.zul", null, args);
            dlg.addEventListener(Events.ON_MOVE, new MoveEventListener());
            dlg._listener = useInputListener ? dlg.new InputListener() : eventListener;
            ConventionWires.wireVariables(dlg, dlg);
            ZKUtil.suppressContextMenu(dlg, true);
            dlg.doModal();
            return dlg;
        } catch (Exception e) {
            log.error("Error Displaying Dialog", e);
            
            if (dlg != null) {
                dlg.detach();
            }
            
            return null;
        }
    }
    
    /**
     * Show an informational message with the specified title.
     * 
     * @param message Text message
     * @param title Title of dialog
     */
    public static void showInfo(String message, String title) {
        show(message, title, LABEL_ID_OK, STYLES_INFO);
    }
    
    /**
     * Show an informational message with the default title.
     * 
     * @param message Text message
     */
    public static void showInfo(String message) {
        showInfo(message, "@cwf.prompt.info.title");
    }
    
    /**
     * Show a warning message with the specified title.
     * 
     * @param message Text message
     * @param title Title of dialog
     */
    public static void showWarning(String message, String title) {
        show(message, title, LABEL_ID_OK, STYLES_WARNING);
    }
    
    /**
     * Show a warning message with the default title.
     * 
     * @param message Text message
     */
    public static void showWarning(String message) {
        showWarning(message, "@cwf.prompt.warning.title");
    }
    
    /**
     * Show an error message with the specified title.
     * 
     * @param message Text message
     * @param title Title of dialog
     */
    public static void showError(String message, String title) {
        Clients.clearBusy();
        show(message, title, LABEL_ID_OK, STYLES_ERROR);
    }
    
    /**
     * Show an error message with the default title.
     * 
     * @param message Text message
     */
    public static void showError(String message) {
        showError(message, "@cwf.prompt.error.title");
    }
    
    /**
     * Show an exception message with the default title.
     * 
     * @param e Exception to display.
     */
    public static void showError(Throwable e) {
        showError(ZKUtil.formatExceptionForDisplay(e));
    }
    
    /**
     * Show a text message with the specified title and using a fixed pitch font.
     * 
     * @param message Text message
     * @param title Title of dialog
     */
    public static void showText(String message, String title) {
        show(message, title, LABEL_ID_OK, STYLE_FIXED_FONT);
    }
    
    /**
     * Present a confirmation (OK/Cancel) dialog with the specified prompt and return the user
     * response.
     * 
     * @param message Prompt to present to the user.
     * @return True if user clicked OK, false otherwise.
     */
    public static boolean confirm(String message) {
        return confirm(message, "@cwf.prompt.confirm.title");
    }
    
    /**
     * Present a confirmation (OK/Cancel) dialog with the specified prompt and return the user
     * response.
     * 
     * @param message Prompt to present to the user.
     * @param title Caption of prompt dialog.
     * @return True if user clicked OK, false otherwise.
     */
    public static boolean confirm(String message, String title) {
        return confirm(message, title, null);
    }
    
    /**
     * Present a confirmation (OK/Cancel) dialog with the specified prompt and return the user
     * response.
     * 
     * @param message Prompt to present to the user.
     * @param title Caption of prompt dialog.
     * @param responseId Optional response id if user response is to be cached. If null, the
     *            response will not be cached. If specified, the response is cached and the user is
     *            not prompted again.
     * @return True if user clicked OK, false otherwise.
     */
    public static boolean confirm(String message, String title, String responseId) {
        return 0 == show(message, title, LABEL_IDS_OK_CANCEL, STYLES_QUESTION, null, null, responseId, LABEL_ID_CANCEL);
    }
    
    /**
     * Returns true if the text corresponds to an OK response.
     * 
     * @param text Text to test.
     * @return True if represents an OK result.
     */
    private static boolean isOK(String text) {
        return isResponseType(text, LABEL_ID_OK);
    }
    
    /**
     * Returns true if the text corresponds to cancel response.
     * 
     * @param text Text to test.
     * @return True if represents an OK result.
     */
    private static boolean isCancel(String text) {
        return isResponseType(text, LABEL_ID_CANCEL);
    }
    
    /**
     * Returns true if the text corresponds to a specific response type.
     * 
     * @param text Text to test.
     * @return True if represents the specified response type.
     */
    private static boolean isResponseType(String text, String label) {
        return label.equals(text) || StrUtil.formatMessage(label).equals(text);
    }
    
    /**
     * Removes a saved response.
     * 
     * @param responseId Unique response id.
     */
    public static void removeSavedResponse(String responseId) {
        saveLastResponse(responseId, -1);
    }
    
    /**
     * Returns the index of the last response for this dialog.
     * 
     * @param responseId Unique id of response.
     * @return Index of last response or -1 if none saved.
     */
    private static int getLastResponse(String responseId) {
        String saved = PropertyUtil.getValue(SAVED_RESPONSE_PROP_NAME, responseId);
        return NumberUtils.toInt(saved, -1);
    }
    
    /**
     * Saves the last response under the named responseId.
     * 
     * @param responseId Unique id of response.
     * @param index Index of response to save. A value less than zero will delete any saved
     *            response.
     */
    private static void saveLastResponse(String responseId, int index) {
        PropertyUtil.saveValue(SAVED_RESPONSE_PROP_NAME, responseId, false, index < 0 ? null : Integer.toString(index));
    }
    
    /**
     * Returns list of response objects created from a string of vertical bar delimited captions.
     * 
     * @param responses Response list separated by vertical bars.
     * @param exclusions Exclusion list separated by vertical bars (may be null).
     * @param dflt Default response (may be null).
     * @return List of response objects corresponding to response list.
     */
    private static List<Response> toResponseList(String responses, String exclusions, String dflt) {
        List<String> excList = StrUtil.toList(exclusions, "|");
        List<String> rspList = StrUtil.toList(responses, "|");
        List<Response> list = new ArrayList<>();
        boolean forceDefault = dflt == null && rspList.size() == 1;
        
        for (int i = 0; i < rspList.size(); i++) {
            String label = rspList.get(i);
            Response rsp = new Response(i, label, excList.contains(label), forceDefault || label.equals(dflt));
            list.add(rsp);
        }
        
        return list;
    }
    
    /**
     * Returns an array of response objects as a vertical bar-delimited string.
     * 
     * @param responses Array of response objects.
     * @return Vertical bar delimited list.
     */
    private static <T> String toResponseStr(T[] responses) {
        return StringUtils.join(responses, "|");
    }
    
    /**
     * Prompt user for input.
     * 
     * @param message Message to display.
     * @param title Text to display.
     * @return User input
     */
    public static String input(String message, String title) {
        return input(message, title, null);
    }
    
    /**
     * Prompt user for input.
     * 
     * @param message Message to display.
     * @param title Text to display.
     * @param oldValue Old value of input.
     * @return User input
     */
    public static String input(String message, String title, String oldValue) {
        Map<Object, Object> args = initArgs(message, title);
        args.put("input", oldValue == null ? "" : oldValue);
        return (String) input(args);
    }
    
    /**
     * Prompts user for input from a list.
     * 
     * @param message Message to display.
     * @param title Text to display.
     * @param oldValue Old value of input.
     * @param itemNames List of item names. If null or fewer entries than items, names taken from
     *            items using toString().
     * @param items List of items. If null or fewer entries than names, names are used as items.
     * @return Object representing input result(s)
     */
    public static Object input(String message, String title, String oldValue, List<String> itemNames, List<Object> items) {
        int nameCount = itemNames == null ? 0 : itemNames.size();
        int itemCount = items == null ? 0 : items.size();
        int count = itemCount > nameCount ? itemCount : nameCount;
        
        if (count == 0) {
            return null;
        }
        
        List<InputItem> inputItems = new ArrayList<>(count);
        
        for (int i = 0; i < count; i++) {
            inputItems.add(new InputItem(i >= nameCount ? null : itemNames.get(i), i >= itemCount ? null : items.get(i)));
        }
        
        Map<Object, Object> args = initArgs(message, title);
        args.put("selected", oldValue == null ? inputItems.get(0).getName() : oldValue);
        args.put("list", inputItems);
        args.put("size", count > 20 ? 20 : count);
        return input(args);
    }
    
    /**
     * Displays input dialog and returns result.
     * 
     * @param args Control parameters.
     * @return Result of input, or null if canceled.
     */
    private static Object input(Map<Object, Object> args) {
        List<Response> responseList = toResponseList(LABEL_IDS_OK_CANCEL, null, null);
        args.put("icon", null);
        args.put("focus", null);
        args.put("remember", false);
        args.put("responses", responseList);
        PromptDialog dlg = showDialog(args, null, true);
        
        if (dlg != null) {
            return dlg._response.isOk() ? dlg._input : null;
            
        }
        return null;
    }
    
    /**
     * Represents list items for input dialog.
     */
    public static class InputItem {
        
        
        private final String name;
        
        private final Object item;
        
        private InputItem(String name, Object item) {
            this.name = name;
            this.item = item;
        }
        
        /**
         * Getter
         * 
         * @return name
         */
        public String getName() {
            return name == null ? item.toString() : name;
        }
        
        /**
         * Getter
         * 
         * @return item
         */
        public Object getItem() {
            return item == null ? name : item;
        }
    }
    
    /**
     * Retrieve the response and close the dialog.
     * 
     * @param button The button that was clicked.
     */
    private void close(Button button) {
        _response = getResponse(button);
        _remember = chkRemember.isChecked();
        
        if (textBox.isVisible()) {
            _input = textBox.getValue();
        } else if (listBox.isVisible()) {
            Listitem item = listBox.getSelectedItem();
            _input = item == null ? null : item.getValue();
        }
        
        detach();
    }
    
    /**
     * Returns true if valid input is present.
     * 
     * @param button The button that was clicked.
     * @return True if input is valid.
     */
    private boolean inputCheck(Button button) {
        if (!getResponse(button).isOk()) {
            return true;
        }
        
        if (textBox.isVisible()) {
            return !StringUtils.isEmpty(textBox.getValue());
        }
        
        if (listBox.isVisible()) {
            return listBox.getSelectedItem() != null;
        }
        
        return true;
    }
    
    /**
     * Returns the response object associated with the button.
     * 
     * @param button A button.
     * @return The associated response object.
     */
    private Response getResponse(Button button) {
        return (Response) button.getAttribute(ATTR_RESPONSE);
    }
    
    /**
     * Used by input dialogs to disable closure if input is incomplete.
     */
    private class InputListener implements EventListener<Event> {
        
        
        @Override
        public void onEvent(Event event) throws Exception {
            if (!inputCheck(((Button) event.getTarget()))) {
                event.stopPropagation();
            }
        }
    }
    
    /**
     * Post the onShow event.
     */
    public void onCreate() {
        setClosable(false);
        Events.echoEvent("onShow", this, null);
    }
    
    /**
     * Select all of input text.
     */
    public void onShow() {
        if (btnOK != null) {
            btnOK.addForward(Events.ON_OK, this, null);
        }
        
        if (textBox.isVisible()) {
            textBox.select();
        }
        
        if (listBox.isVisible()) {
            if (listBox.getSelectedItem() != null) {
                listBox.getSelectedItem().setFocus(true);
            } else {
                listBox.setFocus(true);
            }
        }
    }
    
    /**
     * Accept input when enter key is pressed.
     */
    public void onOK() {
        if (btnOK != null && inputCheck(btnOK)) {
            close(btnOK);
        }
    }
    
    /**
     * When a button is clicked, save its label in _result and save the state of the remember
     * response check box to _remember. Then close the dialog.
     * 
     * @param event Click event.
     * @throws Exception Unspecified exception.
     */
    public void onButtonClick(Event event) throws Exception {
        event = ZKUtil.getEventOrigin(event);
        
        if (_listener != null) {
            _listener.onEvent(event);
            
            if (!event.isPropagatable()) {
                return;
            }
        }
        
        close(((Button) event.getTarget()));
    }
}
