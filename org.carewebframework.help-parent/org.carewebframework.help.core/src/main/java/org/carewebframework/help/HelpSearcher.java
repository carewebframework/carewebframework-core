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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Integrates the help search function across multiple help sets.
 */
public class HelpSearcher {
    
    /**
     * Interface implemented by search engines.
     */
    public interface IHelpSearch {
        
        /**
         * Search for occurrences of a search string.
         * 
         * @param searchString A search string.
         * @param listener Listener to receive results. Depending on the search engine, this may be
         *            called synchronously or asynchronously.
         */
        void search(String searchString, IHelpSearchListener listener);
    }
    
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
     * Used to sequence search results to facilitate identification of duplicates.
     */
    private static final Comparator<HelpSearchHit> duplicateComparator = new Comparator<HelpSearchHit>() {
        
        @Override
        public int compare(HelpSearchHit hit1, HelpSearchHit hit2) {
            HelpTopic topic1 = hit1.getTopic();
            HelpTopic topic2 = hit2.getTopic();
            String url1 = ObjectUtils.toString(topic1.getURL());
            String url2 = ObjectUtils.toString(topic2.getURL());
            int result = ObjectUtils.compare(url1, url2);
            
            if (result == 0) {
                result = topic1.compareTo(topic2);
            }
            
            if (result == 0) {
                result = Double.compare(hit2.getConfidence(), hit1.getConfidence());
            }
            
            return result;
        }
        
    };
    
    /**
     * A single instance of this is created for each search operation. It receives and merges search
     * results from each search engine consulted and reports the merged set of results to the search
     * listener of the original requester.
     */
    private static class MergingSearchListener implements IHelpSearchListener {
        
        private final IHelpSearchListener mergedSearchListener;
        
        private List<HelpSearchHit> mergedResults = new ArrayList<HelpSearchHit>();
        
        private int handlerCount;
        
        private boolean aborted;
        
        /**
         * Creates the merging search listener.
         * 
         * @param mergedSearchListener This listener will receive the merged set of search results.
         * @param handlerCount The count of search engines being consulted for the search operation.
         */
        public MergingSearchListener(IHelpSearchListener mergedSearchListener, int handlerCount) {
            this.mergedSearchListener = mergedSearchListener;
            this.handlerCount = handlerCount;
        }
        
        /**
         * Called by each search engine to report the results of its search operation.
         */
        @Override
        public synchronized void onSearchComplete(List<HelpSearchHit> results) {
            if (!aborted) {
                if (results != null) {
                    mergedResults.addAll(results);
                }
                
                handled();
            }
        }
        
        /**
         * Called as each search engine completes its search task. When all search engines have
         * completed, removes duplicates within the merged results, sorts results by relevance, and
         * reports the merged result set to the original requester.
         */
        private void handled() {
            if (--handlerCount == 0) {
                removeDups();
                Collections.sort(mergedResults);
                mergedSearchListener.onSearchComplete(mergedResults);
                mergedResults = null;
            }
        }
        
        /**
         * Removes duplicate entries from the merged results.
         */
        private void removeDups() {
            Collections.sort(mergedResults, duplicateComparator);
            Iterator<HelpSearchHit> iter = mergedResults.iterator();
            HelpSearchHit previous = null;
            
            while (iter.hasNext()) {
                HelpSearchHit hit = iter.next();
                
                if (previous != null && hit.getTopic().isDuplicate(previous.getTopic())) {
                    iter.remove();
                } else {
                    previous = hit;
                }
            }
        }
        
    }
    
    private static final Log log = LogFactory.getLog(HelpSearcher.class);
    
    private final Set<IHelpSearch> handlers = new HashSet<IHelpSearch>();
    
    private MergingSearchListener mergingSearchListener;
    
    /**
     * Removes all search handlers.
     */
    public void reset() {
        abort();
        handlers.clear();
    }
    
    /**
     * Aborts any search operation in progress.
     */
    private synchronized void abort() {
        if (mergingSearchListener != null) {
            mergingSearchListener.aborted = true;
            mergingSearchListener = null;
        }
    }
    
    /**
     * Adds the search handler for the specified view to the list of registered handlers.
     * 
     * @param view The view to add.
     */
    public void addView(IHelpView view) {
        if (view instanceof IHelpSearch) {
            handlers.add((IHelpSearch) view);
        }
    }
    
    /**
     * Performs a search query using the specified string on each registered query handler, calling
     * the listener for each set of results.
     * 
     * @param words List of words to be located.
     * @param listener Listener for search results.
     */
    public void search(String words, IHelpSearchListener listener) {
        abort();
        mergingSearchListener = new MergingSearchListener(listener, handlers.size());
        
        for (IHelpSearch handler : handlers) {
            try {
                handler.search(words, mergingSearchListener);
            } catch (Exception e) {
                mergingSearchListener.onSearchComplete(null);
                ;
                log.error("Error during search execution.", e);
            }
        }
    }
    
}
