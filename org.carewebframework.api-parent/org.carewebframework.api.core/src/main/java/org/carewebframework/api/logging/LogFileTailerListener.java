/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.api.logging;

/**
 * Provides listener notification methods when a tailed file is updated
 * 
 * @author Steven Haines
 *         <a href="http://www.informit.com/guides/content.aspx?g=java&seqNum=226">project</a>
 */
public interface LogFileTailerListener {
    
    
    /**
     * A new line has been added to the tailed file
     * 
     * @param line The new line that has been added to the tailed file
     */
    public void newFileLine(String line);
    
    /**
     * FileTailer exceeded {@link LogFileTailer#getMaxActiveInterval()} Note that this means that
     * {@link LogFileTailer#stopTailing()} was called
     */
    public void tailerTerminated();
}
