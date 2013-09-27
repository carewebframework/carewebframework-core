/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.shell;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.carewebframework.api.IRegisterEvent;

/**
 * This class maintains a list of startup routines (those that implement the ICareWebStartup
 * interface) and executes those routines upon startup of the CareWeb framework.
 */
public class CareWebStartup implements IRegisterEvent {
    
    private static final Log log = LogFactory.getLog(CareWebStartup.class);
    
    private final List<ICareWebStartup> startupRoutines = new ArrayList<ICareWebStartup>();
    
    /**
     * Register a startup routine.
     */
    @Override
    public void registerObject(Object object) {
        if (object instanceof ICareWebStartup) {
            startupRoutines.add((ICareWebStartup) object);
        }
    }
    
    /**
     * Unregister a startup routine.
     */
    @Override
    public void unregisterObject(Object object) {
        if (object instanceof ICareWebStartup) {
            startupRoutines.remove(object);
        }
    }
    
    /**
     * Execute registered startup routines.
     */
    public void execute() {
        List<ICareWebStartup> temp = new ArrayList<ICareWebStartup>(startupRoutines);
        
        for (ICareWebStartup startupRoutine : temp) {
            try {
                if (startupRoutine.execute()) {
                    unregisterObject(startupRoutine);
                }
            } catch (Throwable t) {
                log.error("Error executing startup routine.", t);
                unregisterObject(startupRoutine);
            }
        }
    }
}
