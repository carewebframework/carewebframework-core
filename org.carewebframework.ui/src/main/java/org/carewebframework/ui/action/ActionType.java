/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.ui.action;

/**
 * Recognized action types.
 */
public enum ActionType {
    UNKNOWN, URL, ZUL, ZSCRIPT, JSCRIPT;
    
    private static final String[] PATTERN = { null, "^https?:.*", "^\\~\\.\\/.*", "^zscript:.*", "^j(ava)?script:.*" };
    
    /**
     * Returns the action type from the action. Formats for supported actions are:
     * <p>
     * <table>
     * <tr>
     * <td><b>URL</b></td>
     * <td><i>http[s]:&lt;path&gt;</i></td>
     * <td>Url for external web page.</td>
     * </tr>
     * <tr>
     * <td><b>ZSCRIPT</b></td>
     * <td><i>zscript:&lt;script&gt;</i></td>
     * <td>Executable zscript code.</td>
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
