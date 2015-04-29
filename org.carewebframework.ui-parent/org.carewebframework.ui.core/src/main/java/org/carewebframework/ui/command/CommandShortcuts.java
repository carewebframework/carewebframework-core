/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.ui.command;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.core.io.Resource;

/**
 * Shortcut mappings for commands as loaded from a property resource.
 */
public class CommandShortcuts extends Properties {
    
    private static final Log log = LogFactory.getLog(CommandShortcuts.class);
    
    private static final long serialVersionUID = 1L;
    
    public CommandShortcuts(Resource mappings) {
        put("help", "#f1");
        String filename = " '" + mappings.getFilename() + "'.";
        InputStream is;
        
        try {
            if (!mappings.exists()) {
                log.info("No shortcut mappings found at" + filename);
                return;
            }
            
            is = mappings.getInputStream();
            
            if (is != null) {
                load(is);
                is.close();
                log.info("Shortcut mappings loaded from" + filename);
            }
        } catch (IOException e) {
            log.error("Error loading shortcut mappings from" + filename, e);
        }
    }
}
