/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.help.ohj;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.carewebframework.help.HelpModule;
import org.carewebframework.help.HelpSetBase;
import org.carewebframework.help.HelpTopic;
import org.carewebframework.help.viewer.HelpViewType;
import org.carewebframework.help.viewer.IHelpView;

import oracle.help.common.View;
import oracle.help.library.helpset.HelpSet;
import oracle.help.library.helpset.HelpSetParseException;

/**
 * Oracle Help for Java implementation.
 */
public class HelpSet_OHJ extends HelpSetBase {
    
    private static final Map<String, HelpViewType> viewMap = new HashMap<String, HelpViewType>();
    
    static {
        viewMap.put("oracle.help.navigator.tocNavigator.TOCNavigator", HelpViewType.TOC);
        viewMap.put("oracle.help.navigator.keywordNavigator.KeywordNavigator", HelpViewType.Index);
        viewMap.put("oracle.help.navigator.searchNavigator.SearchNavigator", HelpViewType.Search);
    }
    
    private final HelpSet helpSet;
    
    private final List<IHelpView> helpViews = new ArrayList<IHelpView>();
    
    private final String rootURL;
    
    public HelpSet_OHJ(HelpModule descriptor) throws MalformedURLException, HelpSetParseException {
        super(descriptor);
        rootURL = descriptor.getUrl();
        helpSet = rootURL.startsWith("/web/") ? new HelpSet(this.getClass(), rootURL) : new HelpSet(new URL(rootURL));
        initViews();
    }
    
    private void initViews() {
        for (View view : helpSet.getAllViews()) {
            HelpViewType viewType = viewMap.get(view.getType());
            
            if (viewType != null && viewType != HelpViewType.Search) {
                helpViews.add(new HelpView(this, view, viewType));
            }
        }
    }
    
    @Override
    public String getHomeID() {
        return helpSet.getHomeID();
    }
    
    @Override
    public HelpTopic getTopic(String topicId) {
        URL url = helpSet.mapIDToURL(topicId);
        
        if (url == null && topicId.contains("/")) {
            int i = topicId.lastIndexOf("/");
            
            if (rootURL.contains(topicId.substring(0, i - 1))) {
                topicId = topicId.substring(i + 1);
                i = topicId.lastIndexOf('.');
                return getTopic(i == -1 ? topicId : topicId.substring(0, i));
            }
        }
        
        return url == null ? null : new HelpTopic(url, topicId.replace("_", " "), getName());
    }
    
    @Override
    public Collection<IHelpView> getAllViews() {
        return helpViews;
    }
    
    @Override
    public String getName() {
        return helpSet.getBookTitle();
    }
    
}
