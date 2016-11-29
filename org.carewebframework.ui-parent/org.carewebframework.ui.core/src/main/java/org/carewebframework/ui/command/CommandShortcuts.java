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
