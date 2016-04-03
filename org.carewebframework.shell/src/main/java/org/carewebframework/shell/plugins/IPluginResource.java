/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.shell.plugins;

import org.carewebframework.shell.CareWebShell;
import org.carewebframework.shell.layout.UIElementBase;

/**
 * Interface for all plugin-associated resources.
 */
public interface IPluginResource {
    
    
    /**
     * Registers/unregisters a plugin resource.
     * 
     * @param shell The running shell.
     * @param owner Owner of the resource.
     * @param register If true, register the resource. If false, unregister it.
     */
    void register(CareWebShell shell, UIElementBase owner, boolean register);
}
