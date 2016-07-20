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
