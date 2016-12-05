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

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.carewebframework.web.ancillary.IAutoWired;
import org.carewebframework.web.annotation.EventHandler;
import org.carewebframework.web.annotation.WiredComponent;
import org.carewebframework.web.client.ExecutionContext;
import org.carewebframework.web.component.BaseComponent;
import org.carewebframework.web.component.Button;
import org.carewebframework.web.component.Cell;
import org.carewebframework.web.component.Textbox;
import org.carewebframework.web.component.Window;
import org.carewebframework.web.page.PageUtil;
import org.springframework.util.StringUtils;

/**
 * Implements a simple dialog for prompting for user input.
 */
public class InputDialog implements IAutoWired {
    
    protected static final Log log = LogFactory.getLog(InputDialog.class.getClass());
    
    public interface IInputCallback {
        
        void onComplete(String value);
    }
    
    /**
     * Prompt user for input.
     * 
     * @param args The argument map.
     * @param callback The callback to receive the text input. If the dialog was cancelled, the text
     *            input will be returned as null.
     */
    public static void show(Map<String, Object> args, IInputCallback callback) {
        Window dialog = (Window) PageUtil
                .createPage(DialogConstants.RESOURCE_PREFIX + "inputDialog.cwf", ExecutionContext.getPage(), args).get(0);
        
        dialog.modal(callback == null ? null : (event) -> {
            callback.onComplete(dialog.getAttribute("result", String.class));
        });
    }
    
    /******************** Controller *******************/
    
    @WiredComponent
    private Textbox textbox;
    
    @WiredComponent
    private Cell prompt;
    
    @WiredComponent
    private Button btnOK;
    
    private Window root;
    
    @Override
    public void afterInitialized(BaseComponent comp) {
        this.root = (Window) comp;
        root.setAttribute("controller", this);
        root.setTitle(root.getAttribute("title", ""));
        root.addClass("flavor:" + root.getAttribute("panelClass", "panel-primary"));
        prompt.setLabel(root.getAttribute("prompt", ""));
        textbox.setValue(root.getAttribute("oldValue", null));
        textbox.selectAll();
        updateState();
    }
    
    private void updateState() {
        btnOK.setDisabled(StringUtils.isEmpty(textbox.getValue()));
    }
    
    @EventHandler(value = "change", target = "@textbox")
    private void onChange() {
        updateState();
    }
    
    @EventHandler(value = "click", target = "@btnOK")
    private void onCommit() {
        root.setAttribute("result", textbox.getValue());
        root.close();
    }
    
    @EventHandler(value = "click", target = "btnCancel")
    private void onCancel() {
        root.setAttribute("result", null);
        root.close();
    }
}
