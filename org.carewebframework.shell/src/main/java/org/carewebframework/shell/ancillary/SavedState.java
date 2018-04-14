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
package org.carewebframework.shell.ancillary;

import org.carewebframework.shell.elements.ElementBase;
import org.fujion.component.BaseUIComponent;
import org.fujion.component.Popup;

/**
 * Saves various states of a component prior to configuring it for design mode. The restore method
 * then restores to the saved state.
 */
public class SavedState {

    private static final String SAVED_STATE = ElementBase.class.getName() + ".STATE";

    final BaseUIComponent component;

    final String hint;

    final Popup contextMenu;
    
    public static void restore(BaseUIComponent comp) {
        SavedState ss = (SavedState) comp.getAttribute(SAVED_STATE);

        if (ss != null) {
            ss.restore();
        }
    }

    public SavedState(BaseUIComponent component) {
        this.component = component;
        hint = component.getHint();
        contextMenu = component.getContext();
        component.setAttribute(SAVED_STATE, this);
        component.addClass("cwf-designmode-active");
    }

    private void restore() {
        component.setHint(hint);
        component.setContext(contextMenu);
        component.removeAttribute(SAVED_STATE);
        component.removeClass("cwf-designmode-active");
    }
}
