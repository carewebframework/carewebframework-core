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
package org.carewebframework.logging.log4j;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Appender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import org.carewebframework.api.logging.ILogManager;

import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;

/**
 * Implementation of {@link ILogManager}, specific to Log4J.
 */
@ManagedResource(description = "Manage Logging at Runtime")
public class Log4JLogManager implements ILogManager {
    
    
    private static final Log log = LogFactory.getLog(Log4JLogManager.class);
    
    private Set<String> loggerNames;
    
    /**
     * @see ILogManager#enableInfo(java.lang.String)
     */
    @ManagedOperation(description = "Enables INFO level for logger")
    @ManagedOperationParameters({ @ManagedOperationParameter(name = "target", description = "Name of logger") })
    @Override
    public void enableInfo(String target) {
        LogManager.getLogger(target).setLevel(Level.INFO);
    }
    
    /**
     * @see ILogManager#enableWarn(java.lang.String)
     */
    @ManagedOperation(description = "Enables WARN level for logger")
    @ManagedOperationParameters({ @ManagedOperationParameter(name = "target", description = "Name of logger") })
    @Override
    public void enableWarn(String target) {
        LogManager.getLogger(target).setLevel(Level.WARN);
    }
    
    /**
     * @see ILogManager#enableError(java.lang.String)
     */
    @ManagedOperation(description = "Enables ERROR level for logger")
    @ManagedOperationParameters({ @ManagedOperationParameter(name = "target", description = "Name of logger") })
    @Override
    public void enableError(String target) {
        LogManager.getLogger(target).setLevel(Level.ERROR);
    }
    
    /**
     * @see ILogManager#enableDebug(java.lang.String)
     */
    @ManagedOperation(description = "Enables DEBUG level for logger")
    @ManagedOperationParameters({ @ManagedOperationParameter(name = "target", description = "Name of logger") })
    @Override
    public void enableDebug(String target) {
        LogManager.getLogger(target).setLevel(Level.DEBUG);
    }
    
    /**
     * @see ILogManager#enableDebug(java.lang.String)
     */
    @ManagedOperation(description = "Enables TRACE level for logger")
    @ManagedOperationParameters({ @ManagedOperationParameter(name = "target", description = "Name of logger") })
    @Override
    public void enableTrace(String target) {
        LogManager.getLogger(target).setLevel(Level.TRACE);
    }
    
    /**
     * @see ILogManager#getAllPathsToLogFiles()
     */
    @ManagedAttribute(description = "Returns paths to file appenders")
    @Override
    public List<String> getAllPathsToLogFiles() {
        
        List<String> filePaths = new ArrayList<>();
        
        for (String loggerName : getLoggerNames()) {
            if (log.isTraceEnabled()) {
                log.trace("Looking up appenders for Logger:" + loggerName);
            }
            Logger logger = null;
            if ("root".equals(loggerName)) {
                logger = LogManager.getRootLogger();
            } else {
                logger = LogManager.getLogger(loggerName);
            }
            if (logger == null) {
                if (log.isWarnEnabled()) {
                    log.warn("Could not find logger with name: " + loggerName);
                }
            } else {
                Enumeration<?> appenders = logger.getAllAppenders();
                while (appenders.hasMoreElements()) {
                    Appender appender = (Appender) appenders.nextElement();
                    if (log.isTraceEnabled()) {
                        log.trace("Appender found: " + appender.getName());
                    }
                    if (appender instanceof FileAppender) {
                        filePaths.add(((FileAppender) appender).getFile());
                    }
                }
            }
        }
        
        return filePaths;
    }
    
    /**
     * Getter for set of logger names
     * 
     * @return Set of logger names.
     */
    @ManagedAttribute(description = "Returns logger names")
    public Set<String> getLoggerNames() {
        return this.loggerNames;
    }
    
    /**
     * Setter for set of logger names
     * 
     * @param loggerNames Set of logger names
     */
    public void setLoggerNames(Set<String> loggerNames) {
        this.loggerNames = loggerNames;
    }
    
}
