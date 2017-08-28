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
package org.carewebframework.plugin.filetailer;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.carewebframework.api.logging.ILogManager;
import org.carewebframework.api.logging.LogFileTailer;
import org.carewebframework.api.logging.LogFileTailerListener;
import org.fujion.common.StrUtil;
import org.carewebframework.shell.plugins.PluginController;
import org.fujion.annotation.EventHandler;
import org.fujion.annotation.WiredComponent;
import org.fujion.component.BaseComponent;
import org.fujion.component.Button;
import org.fujion.component.Combobox;
import org.fujion.component.Comboitem;
import org.fujion.component.Label;
import org.fujion.component.Memobox;
import org.fujion.component.Timer;

/**
 * Controller class for log file tailer.
 */
public class MainController extends PluginController {
    
    private static final Log log = LogFactory.getLog(MainController.class);
    
    private static final int TAIL_INTERVAL = 2000;//1000 = 1 second
    
    private static final int TIMER_INTERVAL = 2000;//1000 = 1 second
    
    //members
    
    @WiredComponent
    private Timer timer;
    
    @WiredComponent
    private Memobox txtOutput;
    
    @WiredComponent
    private Combobox cboLogFiles;
    
    @WiredComponent
    private Button btnToggle;
    
    @WiredComponent
    private Label lblMessage;
    
    private final Deque<String> logFileBuffer = new ArrayDeque<>();
    
    private LogFileTailer tailer;
    
    private final Map<String, File> logFiles = new HashMap<>();
    
    private boolean isTailerStarted;
    
    private boolean isTailerTerminated;
    
    private ILogManager logManager;
    
    /**
     * FileTailListener callback interface handles processing when notified that the file being
     * tailed has new lines
     */
    private final LogFileTailerListener tailListener = new LogFileTailerListener() {
        
        @Override
        public void newFileLine(String line) {
            logFileBuffer.add(line.concat("\n"));
        }
        
        @Override
        public void tailerTerminated() {
            log.trace("TailerTerminated event");
            isTailerTerminated = true;
        }
    };
    
    /**
     * Initializes Controller. Loads user preferences and properties. If
     * <code>{@link ILogManager} != null</code> then current logging appenders are added to member
     * Map
     */
    @Override
    public void afterInitialized(BaseComponent comp) {
        super.afterInitialized(comp);
        log.trace("Initializing Controller");
        
        //Populate LogFilePaths Combobox
        if (logManager != null) {
            List<String> logFilePaths = logManager.getAllPathsToLogFiles();
            
            for (String pathToLogFile : logFilePaths) {
                logFiles.put(pathToLogFile, new File(pathToLogFile));
                cboLogFiles.addChild(new Comboitem(pathToLogFile));
            }
            
            if (cboLogFiles.getChildCount() > 0) {
                cboLogFiles.setSelectedItem((Comboitem) cboLogFiles.getChildAt(0));
                onSelect$cboLogFiles();
            }
        }
    }
    
    /**
     * Event handler for log file tailer
     */
    @EventHandler(value = "timer", target = "@timer")
    private void onTimer$timer() {
        log.trace("onTimer event");
        
        StringBuffer lines = new StringBuffer();
        
        synchronized (logFileBuffer) {
            for (String line : logFileBuffer) {
                lines.append(line);
            }
        }
        
        txtOutput.setValue(txtOutput.getValue().concat(lines.toString()));
        logFileBuffer.clear();
        
        //check for state change of Tailer
        if (isTailerTerminated) {
            isTailerTerminated = false;
            String msg = "Tailer was terminated, stopping client timer";
            txtOutput.setValue(txtOutput.getValue().concat(msg).concat("\n"));
            log.trace(msg);
            stopTailer();
            showMessage(msg);
        }
    }
    
    /**
     * Handles the Button onClick event for changing the state of the
     * {@link org.carewebframework.api.logging.LogFileTailer}
     */
    @EventHandler(value = "click", target = "@btnToggle")
    private void onClick$btnToggle() {
        if (isTailerStarted) {
            stopTailer();
        } else {
            startTailer();
        }
    }
    
    /**
     * Stop the tailer if not already running.
     */
    private void stopTailer() {
        if (isTailerStarted) {
            log.trace("Stopping Tailer/Timer");
            tailer.stopTailing();
            log.trace("Tailer stopped");
            timer.stop();
            log.trace("Timer stopped");
            isTailerStarted = false;
            btnToggle.setLabel(StrUtil.formatMessage("@cwf.filetailer.btn.toggle.on.label"));
            showMessage("@cwf.filetailer.msg.stopped");
        }
    }
    
    /**
     * Start the tailer if it is running.
     */
    private void startTailer() {
        if (!isTailerStarted) {
            log.trace("Starting file tailer");
            Comboitem selectedItem = cboLogFiles.getSelectedItem();
            String logFilePath = selectedItem == null ? null : selectedItem.getLabel();
            
            if (tailer == null) {
                log.trace("Creating LogFileTailer with " + logFilePath);
                try {
                    tailer = new LogFileTailer(logFiles.get(logFilePath), TAIL_INTERVAL, false);
                    tailer.addFileTailerListener(tailListener);
                } catch (FileNotFoundException e) {
                    log.error(e.getMessage(), e);
                    showMessage("@cwf.filetailer.error.notfound", logFilePath);
                    return;
                }
            } else {
                try {
                    tailer.changeFile(logFiles.get(logFilePath));
                } catch (IllegalStateException ise) {
                    log.error(ise.getMessage(), ise);
                    showMessage("@cwf.filetailer.error.illegalstate", logFilePath);
                    return;
                } catch (FileNotFoundException e) {
                    log.error(e.getMessage(), e);
                    showMessage("@cwf.filetailer.error.notfound", logFilePath);
                    return;
                }
            }
            showMessage("@cwf.filetailer.msg.duration", tailer.getMaxActiveInterval() / 1000);
            timer.setInterval(TIMER_INTERVAL);
            timer.start();
            log.trace("Timer started");
            new Thread(tailer).start();
            log.trace("Tailer started");
            btnToggle.setLabel(StrUtil.formatMessage("@cwf.filetailer.btn.toggle.off.label"));
            isTailerStarted = true;
        }
    }
    
    @EventHandler(value = "select", target = "@cboLogFiles")
    private void onSelect$cboLogFiles() {
        stopTailer();
        btnToggle.setDisabled(cboLogFiles.getSelectedItem() == null);
    }
    
    /**
     * Handles the Button onClick event to clear the log file tailer output.
     */
    @EventHandler(value = "click", target = "btnClear")
    private void onClick$btnClear() {
        log.trace("Clearing LogFileTailer output and LogFileBuffer");
        logFileBuffer.clear();
        txtOutput.setValue(null);
    }
    
    /**
     * Displays message to client
     * 
     * @param message Message to display to client.
     * @param params Message parameters.
     */
    private void showMessage(String message, Object... params) {
        if (message == null) {
            lblMessage.setVisible(false);
        } else {
            lblMessage.setVisible(true);
            lblMessage.setLabel(StrUtil.formatMessage(message, params));
        }
    }
    
    public void setLogManager(ILogManager logManager) {
        this.logManager = logManager;
    }
    
}
