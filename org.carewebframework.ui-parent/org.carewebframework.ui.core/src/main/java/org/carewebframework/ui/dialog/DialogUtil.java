package org.carewebframework.ui.dialog;

import static org.carewebframework.ui.dialog.DialogConstants.LABEL_IDS_OK_CANCEL;
import static org.carewebframework.ui.dialog.DialogConstants.LABEL_ID_CANCEL;
import static org.carewebframework.ui.dialog.DialogConstants.LABEL_ID_OK;
import static org.carewebframework.ui.dialog.DialogConstants.STYLES_ERROR;
import static org.carewebframework.ui.dialog.DialogConstants.STYLES_INFO;
import static org.carewebframework.ui.dialog.DialogConstants.STYLES_QUESTION;
import static org.carewebframework.ui.dialog.DialogConstants.STYLES_WARNING;
import static org.carewebframework.ui.dialog.DialogConstants.STYLE_FIXED_FONT;

import java.util.HashMap;
import java.util.Map;

import org.carewebframework.ui.core.CWFUtil;
import org.carewebframework.ui.dialog.DialogControl.IPromptCallback;
import org.carewebframework.ui.dialog.InputDialog.IInputCallback;
import org.carewebframework.web.component.Window;
import org.carewebframework.web.event.IEventListener;

public class DialogUtil {
    
    /********************* Confirmation Dialogs *********************/
    
    public interface IConfirmCallback {
        
        void onComplete(boolean confirm);
    }
    
    /**
     * Present a confirmation (OK/Cancel) dialog with the specified prompt and return the user
     * response.
     * 
     * @param message Prompt to present to the user.
     * @param callback Callback to receive dialog response.
     */
    public static void confirm(String message, IConfirmCallback callback) {
        confirm(message, "@cwf.prompt.confirm.title", callback);
    }
    
    /**
     * Present a confirmation (OK/Cancel) dialog with the specified prompt and return the user
     * response.
     * 
     * @param message Prompt to present to the user.
     * @param title Caption of prompt dialog.
     * @param callback Callback to receive dialog response.
     */
    public static void confirm(String message, String title, IConfirmCallback callback) {
        confirm(message, title, null, callback);
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
     * @param callback Callback to receive dialog response.
     */
    public static void confirm(String message, String title, String responseId, IConfirmCallback callback) {
        PromptDialog.show(message, title, STYLES_QUESTION, LABEL_IDS_OK_CANCEL, LABEL_ID_CANCEL, null, responseId,
            callback == null ? null : new IPromptCallback<String>() {
                
                @Override
                public void onComplete(DialogResponse<String> response) {
                    callback.onComplete(response != null && response.isOk());
                }
                
            });
    }
    
    /********************* Informational Dialogs *********************/
    
    /**
     * Show an informational message with the specified title.
     * 
     * @param message Text message
     * @param title Title of dialog
     */
    public static void showInfo(String message, String title) {
        showDialog(message, title, STYLES_INFO);
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
        showDialog(message, title, STYLES_WARNING);
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
        showDialog(message, title, STYLES_ERROR);
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
        showError(CWFUtil.formatExceptionForDisplay(e));
    }
    
    /**
     * Show a text message with the specified title and using a fixed pitch font.
     * 
     * @param message Text message
     * @param title Title of dialog
     */
    public static void showText(String message, String title) {
        showDialog(message, title, STYLE_FIXED_FONT);
    }
    
    private static void showDialog(String message, String title, String style) {
        PromptDialog.show(message, title, style, LABEL_ID_OK, null, null, null, null);
    }
    
    /********************* Report Dialogs *********************/
    
    /**
     * Displays the dialog amodally.
     * 
     * @param text The text or HTML content. HTML content is indicated by prefixing with the html
     *            tag.
     * @param title Dialog title.
     * @param allowPrint If true, a print button is provided.
     * @return The created window.
     */
    public static Window showReport(String text, String title, boolean allowPrint) {
        return ReportDialog.show(text, title, allowPrint, false, null);
    }
    
    /**
     * Displays the dialog modally.
     * 
     * @param text The text or HTML content. HTML content is indicated by prefixing with the html
     *            tag.
     * @param title Dialog title.
     * @param allowPrint If true, a print button is provided.
     * @param callback Callback when dialog is closed.
     * @return The created window.
     */
    public static Window showReport(String text, String title, boolean allowPrint, IEventListener callback) {
        return ReportDialog.show(text, title, allowPrint, true, callback);
    }
    
    /********************* Input Dialogs *********************/
    
    /**
     * Prompt user for input.
     * 
     * @param prompt Prompt message to display.
     * @param title Text to display.
     * @param callback The callback to receive the text input. If the dialog was cancelled, the text
     *            input will be returned as null.
     */
    public static void input(String prompt, String title, IInputCallback callback) {
        input(prompt, title, null, callback);
    }
    
    /**
     * Prompt user for input.
     * 
     * @param prompt Prompt message to display.
     * @param title Text to display.
     * @param oldValue Old value of input.
     * @param callback The callback to receive the text input. If the dialog was cancelled, the text
     *            input will be returned as null.
     */
    public static void input(String prompt, String title, String oldValue, IInputCallback callback) {
        Map<String, Object> args = new HashMap<>();
        args.put("prompt", prompt);
        args.put("title", title);
        args.put("oldValue", oldValue);
        InputDialog.show(args, callback);
    }
    
    /********************* Popup Dialogs *********************/
    
    /**
     * Opens any arbitrary page in a modal window.
     * 
     * @param cwfPage Url of page.
     * @return Reference to the opened window, if successful.
     */
    public static Window popup(String cwfPage) {
        return popup(cwfPage, true, true, true);
    }
    
    /**
     * Can be used to popup any page as a modal dialog.
     * 
     * @param cwfPage Url of page.
     * @param closable If true, window closure button appears.
     * @param sizable If true, window sizing grips appear.
     * @return Reference to the opened window, if successful.
     */
    public static Window popup(String cwfPage, boolean closable, boolean sizable) {
        return popup(cwfPage, closable, sizable, true);
    }
    
    /**
     * Can be used to popup any page as a modal dialog.
     * 
     * @param cwfPage Url of page.
     * @param closable If true, window closure button appears.
     * @param sizable If true, window sizing grips appear.
     * @param show If true, the window is displayed modally. If false, the window is created but not
     *            displayed.
     * @return Reference to the opened window, if successful.
     */
    public static Window popup(String cwfPage, boolean closable, boolean sizable, boolean show) {
        return PopupDialog.show(cwfPage, null, closable, sizable, show, null);
    }
    
    private DialogUtil() {
    }
}
