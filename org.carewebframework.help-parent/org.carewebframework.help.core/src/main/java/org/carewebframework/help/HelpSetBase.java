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
 * Abstract base class for help sets.
 */
public abstract class HelpSetBase implements IHelpSet {
    
    protected final HelpModule descriptor;
    
    protected HelpSetBase(HelpModule descriptor) {
        this.descriptor = descriptor;
    }
    
    @Override
    public String getId() {
        return descriptor.getId();
    }
    
    @Override
    public String getName() {
        return descriptor.getTitle();
    }
    
    @Override
    public String toString() {
        return getId();
    }
}
