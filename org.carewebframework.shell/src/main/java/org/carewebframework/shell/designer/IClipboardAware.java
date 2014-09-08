/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.shell.designer;

/**
 * Provides methods for interacting with the clipboard.
 * 
 * @param <T> Type of stored object.
 */
public interface IClipboardAware<T> {
    
    /**
     * Converts to string format suitable for displaying in clipboard viewer.
     * 
     * @return String format
     */
    String toClipboard();
    
    /**
     * Converts from clipboard string format to instance of original class.
     * 
     * @param data String data from clipboard.
     * @return Instance of original class.
     * @throws Exception Unspecified exception.
     */
    T fromClipboard(String data) throws Exception;
}
