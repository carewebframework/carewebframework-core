/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.help;

/**
 * Defines a specific help context target.
 */
public class HelpContext {
    
    public final String module;
    
    public final String topic;
    
    public final String label;
    
    public HelpContext(String module, String topic, String label) {
        this.module = module;
        this.topic = topic;
        this.label = label;
    }
}
