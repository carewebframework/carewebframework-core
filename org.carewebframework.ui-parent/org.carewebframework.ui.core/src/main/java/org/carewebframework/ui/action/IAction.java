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
package org.carewebframework.ui.action;

/**
 * Interface implemented by actions that may be associated with certain UI elements (e.g.,
 * UIElementButton).
 */
public interface IAction {
    
    /**
     * Returns the label associated with the action. This may be a label reference prefixed by an
     * '@' character, or the label text itself.
     * 
     * @return Associated label.
     */
    String getLabel();
    
    /**
     * Returns the action script. See {@link ActionType#getType(String)} for supported formats.
     * 
     * @return The action script.
     */
    String getScript();
    
    /**
     * Returns true if the action is disabled.
     * 
     * @return Disabled status of action.
     */
    boolean isDisabled();
    
    /**
     * Returns the display text for the action.
     * 
     * @return The display text.
     */
    @Override
    String toString();
}
