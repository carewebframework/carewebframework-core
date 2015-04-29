/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.ui.zk;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.carewebframework.common.StrUtil;

import org.zkoss.zul.Window;

/**
 * A simple dialog for displaying text information modally or amodally.
 */
public class ReportBox {
    
    private static final String DIALOG = ZKUtil.getResourcePath(ReportBox.class) + "reportBox.zul";
    
    /**
     * Displays the dialog amodally.
     * 
     * @param text The text or HTML content. HTML content is indicated by prefixing with the html
     *            tag.
     * @param title Dialog title.
     * @param allowPrint If true, a print button is provided.
     * @return The created window.
     */
    public static Window amodal(String text, String title, boolean allowPrint) {
        return show(text, title, allowPrint, Window.OVERLAPPED);
    }
    
    /**
     * Displays the dialog modally.
     * 
     * @param text The text or HTML content. HTML content is indicated by prefixing with the html
     *            tag.
     * @param title Dialog title.
     * @param allowPrint If true, a print button is provided.
     * @return The created window.
     */
    public static Window modal(String text, String title, boolean allowPrint) {
        return show(text, title, allowPrint, Window.MODAL);
    }
    
    /**
     * Displays the dialog amodally.
     * 
     * @param text The text or HTML content. HTML content is indicated by prefixing with the html
     *            tag.
     * @param title Dialog title.
     * @param allowPrint If true, a print button is provided.
     * @return The created window.
     */
    public static Window amodal(List<String> text, String title, boolean allowPrint) {
        return show(text, title, allowPrint, Window.OVERLAPPED);
    }
    
    /**
     * Displays the dialog modally.
     * 
     * @param text The text or HTML content. HTML content is indicated by prefixing with the html
     *            tag.
     * @param title Dialog title.
     * @param allowPrint If true, a print button is provided.
     * @return The created window.
     */
    public static Window modal(List<String> text, String title, boolean allowPrint) {
        return show(text, title, allowPrint, Window.MODAL);
    }
    
    /**
     * Displays the dialog.
     * 
     * @param text The text or HTML content. HTML content is indicated by prefixing with the html
     *            tag.
     * @param title Dialog title.
     * @param allowPrint If true, a print button is provided.
     * @param mode The window mode.
     * @return The created window.
     */
    private static Window show(List<String> text, String title, boolean allowPrint, int mode) {
        return show(StrUtil.fromList(text), title, allowPrint, mode);
    }
    
    /**
     * Displays the dialog.
     * 
     * @param text The text or HTML content. HTML content is indicated by prefixing with the html
     *            tag.
     * @param title Dialog title.
     * @param allowPrint If true, a print button is provided.
     * @param mode The window mode.
     * @return The created window.
     */
    private static Window show(String text, String title, boolean allowPrint, int mode) {
        Map<Object, Object> args = new HashMap<Object, Object>();
        
        if (text.startsWith("<html>")) {
            args.put("html", text);
        } else {
            args.put("text", text);
        }
        args.put("title", title);
        args.put("allowPrint", allowPrint);
        Window window = PopupDialog.popup(DIALOG, args, true, true, false);
        window.setPosition("center");
        window.setMode(mode);
        return window;
    }
    
    /**
     * Enforces static class.
     */
    private ReportBox() {
    };
}
