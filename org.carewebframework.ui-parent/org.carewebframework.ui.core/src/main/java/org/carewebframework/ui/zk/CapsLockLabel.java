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

import org.zkoss.zul.Label;
import org.zkoss.zul.Textbox;

/**
 * Displays a warning message when caps lock is activated when entering data in a text box. Note
 * that JavaScript cannot directly determine the state of the caps lock key. It can only infer it by
 * the receipt of an upper case key code in the absence of shift key activation. Thus, this is not a
 * perfect solution.
 */
public class CapsLockLabel extends Label {
    
    private static final long serialVersionUID = 1L;
    
    private Textbox textbox;
    
    @Override
    protected void renderProperties(org.zkoss.zk.ui.sys.ContentRenderer renderer) throws java.io.IOException {
        super.renderProperties(renderer);
        render(renderer, "textbox", textbox);
    }
    
    /**
     * Sets the text box to be monitored.
     * 
     * @param textbox The text box.
     */
    public void setTextbox(Textbox textbox) {
        this.textbox = textbox;
        smartUpdate("textbox", textbox == null ? null : textbox);
    }
    
    /**
     * Returns the text box being monitored.
     * 
     * @return A text box.
     */
    public Textbox getTextbox() {
        return textbox;
    }
}
