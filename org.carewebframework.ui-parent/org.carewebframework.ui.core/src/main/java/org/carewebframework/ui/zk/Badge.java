/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.ui.zk;

/**
 * Helper class for displaying badges.
 */
public class Badge {
    
    private final String classes;
    
    private final String label;
    
    public Badge() {
        this(null, null);
    }
    
    public Badge(String label) {
        this(label, null);
    }
    
    public Badge(String label, String classes) {
        this.label = label;
        this.classes = classes;
    }
    
    public void apply(String selector) {
        ZKUtil.setBadge(selector, label, classes);
    }
    
}
