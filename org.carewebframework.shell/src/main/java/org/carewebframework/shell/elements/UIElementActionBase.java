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
package org.carewebframework.shell.elements;

import org.apache.commons.lang.StringUtils;
import org.carewebframework.ui.action.ActionListener;
import org.carewebframework.ui.action.ActionUtil;

/**
 * Base class for UI elements that support an associated action.
 */
public class UIElementActionBase extends UIElementBase {
    
    private String action;
    
    private ActionListener listener;
    
    public UIElementActionBase() {
        setMaskMode(null);
    }
    
    public void setAction(String action) {
        if (!StringUtils.equals(action, this.action)) {
            this.action = action;
            listener = ActionUtil.addAction(getOuterComponent(), action);
            updateListener();
        }
    }
    
    public String getAction() {
        return action;
    }
    
    /**
     * Disable action in design mode.
     */
    @Override
    public void setDesignMode(boolean designMode) {
        super.setDesignMode(designMode);
        updateListener();
    }
    
    /**
     * Disables the event listener when in design mode.
     */
    protected void updateListener() {
        if (listener != null) {
            listener.setDisabled(isDesignMode());
        }
    }
}
