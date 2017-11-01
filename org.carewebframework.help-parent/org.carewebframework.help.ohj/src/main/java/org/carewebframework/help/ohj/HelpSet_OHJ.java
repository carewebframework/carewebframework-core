/*
 * #%L
 * carewebframework
 * %%
 * Copyright (C) 2008 - 2016 Regenstrief Institute, Inc.
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related
 * Additional Disclaimer of Warranty and Limitation of Liability available at
 *
 *      http://www.carewebframework.org/licensing/disclaimer.
 *
 * #L%
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
import org.carewebframework.help.HelpViewType;
import org.carewebframework.help.IHelpView;

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
        viewMap.put("oracle.help.navigator.keywordNavigator.KeywordNavigator", HelpViewType.INDEX);
        viewMap.put("oracle.help.navigator.searchNavigator.SearchNavigator", HelpViewType.SEARCH);
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
            
            if (viewType != null && viewType != HelpViewType.SEARCH) {
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
