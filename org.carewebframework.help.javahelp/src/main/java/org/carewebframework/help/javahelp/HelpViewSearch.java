/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.help.javahelp;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.help.NavigatorView;
import javax.help.search.MergingSearchEngine;
import javax.help.search.SearchEngine;
import javax.help.search.SearchEvent;
import javax.help.search.SearchItem;
import javax.help.search.SearchListener;
import javax.help.search.SearchQuery;

import org.carewebframework.help.HelpSearchHit;
import org.carewebframework.help.HelpSearcher.IHelpSearch;
import org.carewebframework.help.HelpSearcher.IHelpSearchListener;
import org.carewebframework.help.HelpTopic;
import org.carewebframework.help.HelpViewType;

import org.zkoss.util.Locales;

/**
 * JavaHelp search support.
 */
public class HelpViewSearch extends HelpView implements IHelpSearch {
    
    private SearchEngine searchEngine;
    
    public HelpViewSearch(NavigatorView view) {
        super(view, HelpViewType.Search);
    }
    
    @Override
    public void search(final String searchString, final IHelpSearchListener listener) {
        if (searchEngine == null) {
            searchEngine = new MergingSearchEngine(view);
        }
        
        SearchQuery query = searchEngine.createQuery();
        query.addSearchListener(new SearchListener() {
            
            @SuppressWarnings("unchecked")
            @Override
            public void itemsFound(SearchEvent e) {
                listener.onSearchComplete(processResults(e.getSearchItems()));
            }
            
            @Override
            public void searchStarted(SearchEvent e) {
            }
            
            @Override
            public void searchFinished(SearchEvent e) {
            }
            
        });
        query.start(searchString, Locales.getCurrent());
        
    }
    
    /**
     * Converts search results returned by the search engine to a list of search hits.
     * 
     * @param searchItems Search results to process.
     * @return Equivalent list of search hits.
     */
    private List<HelpSearchHit> processResults(Enumeration<SearchItem> searchItems) {
        List<HelpSearchHit> hits = new ArrayList<HelpSearchHit>();
        String source = view.getHelpSet().getTitle();
        
        while (searchItems.hasMoreElements()) {
            SearchItem si = searchItems.nextElement();
            
            try {
                URL url = new URL(si.getBase(), si.getFilename());
                HelpTopic topic = new HelpTopic(url, si.getTitle(), source);
                hits.add(new HelpSearchHit(topic, si.getConfidence()));
            } catch (MalformedURLException e) {}
        }
        
        return hits;
    }
    
}
