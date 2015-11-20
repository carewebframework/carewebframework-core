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

import java.util.Collection;
import java.util.List;

/**
 * Service for managing the search index and providing search capabilities using Lucene.
 */
public interface IHelpSearch {
    
    /**
     * Callback interface for receiving search results.
     */
    public interface IHelpSearchListener {
        
        /**
         * Called by search engine to report results.
         * 
         * @param results List of search results (may be null to indicated no results or no search
         *            capability).
         */
        void onSearchComplete(List<HelpSearchHit> results);
    }
    
    /**
     * Performs a search query using the specified string on each registered query handler, calling
     * the listener for each set of results.
     * 
     * @param words List of words to be located.
     * @param helpSets Help sets to be searched
     * @param listener Listener for search results.
     */
    void search(String words, Collection<IHelpSet> helpSets, IHelpSearchListener listener);
    
    /**
     * Index all HTML files within the content of the help module.
     * 
     * @param helpModule Help module to be indexed.
     */
    void indexHelpModule(HelpModule helpModule);
    
    /**
     * Removes the index for a help module.
     * 
     * @param helpModule Help module whose index is to be removed.
     */
    void unindexHelpModule(HelpModule helpModule);
    
}
