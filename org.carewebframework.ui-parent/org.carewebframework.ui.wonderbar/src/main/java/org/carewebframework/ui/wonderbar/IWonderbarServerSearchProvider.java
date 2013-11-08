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

import java.util.List;

/**
 * Interface for server-based search providers.
 * 
 * @param <T> Type returned by search provider.
 */
public interface IWonderbarServerSearchProvider<T> extends IWonderbarSearchProvider<T> {
    
    /**
     * Return the list of items that match the given search string. maxItems is given as
     * informational and can be used for performance reasons. The returned list will be truncated to
     * maxItems regardless of the # of items returned.
     * 
     * @param search The search term.
     * @param maxItems The max # of hits that will be returned in the search.
     * @param hits List to receive matched items.
     * @return True if all matching items were returned, or false if maxItems was exceeded.
     */
    boolean getSearchResults(String search, int maxItems, List<T> hits);
}
