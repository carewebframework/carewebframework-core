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

import java.util.List;

/**
 * Interface that defines management type methods for a logging implementation to fulfill.
 */
public interface ILogManager {
    
    
    /**
     * Enable ERROR level logging
     * 
     * @param target Logging target.
     */
    void enableError(String target);
    
    /**
     * Enable WARN level logging
     * 
     * @param target Logging target.
     */
    void enableWarn(String target);
    
    /**
     * Enable INFO level logging
     * 
     * @param target Logging target.
     */
    void enableInfo(String target);
    
    /**
     * Enable DEBUG level logging
     * 
     * @param target Logging target.
     */
    void enableDebug(String target);
    
    /**
     * Enable TRACE level logging
     * 
     * @param target Logging target.
     */
    void enableTrace(String target);
    
    /**
     * Find all loggers (i.e. appenders, etc.) and return all paths to files being written to. Based
     * on configuration.
     * 
     * @return List of log file paths.
     */
    List<String> getAllPathsToLogFiles();
}
