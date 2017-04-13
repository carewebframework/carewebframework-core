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

import java.util.HashMap;
import java.util.Map;

import org.carewebframework.web.ancillary.IAutoWired;
import org.carewebframework.web.annotation.EventHandler;
import org.carewebframework.web.annotation.WiredComponent;
import org.carewebframework.web.component.BaseComponent;
import org.carewebframework.web.component.Button;
import org.carewebframework.web.component.Cell;
import org.carewebframework.web.component.Html;
import org.carewebframework.web.component.Window;
import org.carewebframework.web.event.IEventListener;

/**
 * A simple dialog for displaying text information modally or amodally.
 */
public class ReportDialog implements IAutoWired {

    /**
     * Displays the dialog.
     *
     * @param text The text or HTML content. HTML content is indicated by prefixing with the html
     *            tag.
     * @param title Dialog title.
     * @param allowPrint If true, a print button is provided.
     * @param asModal If true, open as modal; otherwise, as popup.
     * @param callback Callback when dialog is closed.
     * @return The created dialog.
     */
    public static Window show(String text, String title, boolean allowPrint, boolean asModal, IEventListener callback) {
        Map<String, Object> args = new HashMap<>();
        args.put("text", text);
        args.put("title", title);
        args.put("allowPrint", allowPrint);
        Window dialog = PopupDialog.show(DialogConstants.RESOURCE_PREFIX + "reportDialog.cwf", args, true, true, false,
            null);

        if (asModal) {
            dialog.modal(callback);
        } else {
            dialog.popup(callback);
        }

        return dialog;
    }

    private Window window;

    @WiredComponent
    private Cell cmpText;

    @WiredComponent
    private Html cmpHtml;

    @WiredComponent
    private Button btnPrint;

    @Override
    public void afterInitialized(BaseComponent root) {
        window = (Window) root;
        window.setTitle(root.getAttribute("title", ""));
        btnPrint.setVisible(root.getAttribute("allowPrint", false));
        String text = root.getAttribute("text", "");

        if (text.startsWith("<html>")) {
            cmpHtml.setContent(text);
        } else {
            cmpText.setLabel(text);
        }
    }

    @EventHandler(value = "click", target = "btnClose")
    private void btnClose$click() {
        window.close();
    }

    @EventHandler(value = "click", target = "@btnPrint")
    private void btnPrint$click() {

    }
}
