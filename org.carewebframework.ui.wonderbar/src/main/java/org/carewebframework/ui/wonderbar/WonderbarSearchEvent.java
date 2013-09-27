/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.ui.wonderbar;

import java.util.Map;

import org.zkoss.zk.au.AuRequest;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;

/**
 * Fired when a search request is received from the client.
 */
public class WonderbarSearchEvent extends Event {
    
    public static final String ON_WONDERBAR_SEARCH = "onWonderbarSearch";
    
    private static final long serialVersionUID = 1L;
    
    private final String term;
    
    public static final WonderbarSearchEvent getSearchEvent(AuRequest request) {
        final Map<String, Object> data = request.getData();
        return new WonderbarSearchEvent(request.getComponent(), (String) data.get("term"));
    }
    
    public WonderbarSearchEvent(Component target, String term) {
        super(ON_WONDERBAR_SEARCH, target);
        this.term = term;
    }
    
    /**
     * Returns the search term.
     * 
     * @return
     */
    public String getTerm() {
        return term;
    }
    
}
