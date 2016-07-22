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
 * Recognized action types.
 */
public enum ActionType {
    UNKNOWN, URL, GROOVY, JSCRIPT;
    
    private static final String[] PATTERN = { null, "^https?:.*", "^\\~\\.\\/.*", "^groovy:.*", "^j(ava)?script:.*" };
    
    /**
     * Returns the action type from the action. Formats for supported actions are:
     * <table summary="">
     * <tr>
     * <td><b>URL</b></td>
     * <td><i>http[s]:&lt;path&gt;</i></td>
     * <td>Url for external web page.</td>
     * </tr>
     * <tr>
     * <td><b>GROOVY</b></td>
     * <td><i>groovy:&lt;script&gt;</i></td>
     * <td>Executable groovy code.</td>
     * </tr>
     * <tr>
     * <td><b>JSCRIPT</b></td>
     * <td><i>j[ava]script:&lt;script&gt;</i></td>
     * <td>Executable JavaScript code.</td>
     * </tr>
     * <tr>
     * <td><b>ZUL</b></td>
     * <td><i>~./&lt;path&gt;</i></td>
     * <td>Zul page containing zscript.</td>
     * </tr>
     * </table>
     * <p>
     * 
     * @param script The action script.
     * @return The action type.
     */
    public static ActionType getType(String script) {
        for (int i = 1; i < PATTERN.length; i++) {
            if (script.matches(PATTERN[i])) {
                return ActionType.values()[i];
            }
        }
        
        return UNKNOWN;
    }
    
}
