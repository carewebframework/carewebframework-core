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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.help.GlossaryView;
import javax.help.HelpSet;
import javax.help.HelpSetException;
import javax.help.IndexView;
import javax.help.Map.ID;
import javax.help.NavigatorView;
import javax.help.TOCView;

import org.carewebframework.help.HelpModule;
import org.carewebframework.help.HelpSetBase;
import org.carewebframework.help.HelpTopic;
import org.carewebframework.help.HelpViewType;
import org.carewebframework.help.IHelpView;

public class HelpSet_JavaHelp extends HelpSetBase {
    
    private static final Map<Class<? extends NavigatorView>, HelpViewType> viewMap = new HashMap<Class<? extends NavigatorView>, HelpViewType>();
    
    static {
        viewMap.put(TOCView.class, HelpViewType.TOC);
        viewMap.put(GlossaryView.class, HelpViewType.Glossary);
        viewMap.put(IndexView.class, HelpViewType.Index);
    }
    
    private final HelpSet helpSet;
    
    private final List<IHelpView> helpViews = new ArrayList<IHelpView>();
    
    public HelpSet_JavaHelp(HelpModule descriptor) throws MalformedURLException, HelpSetException {
        super(descriptor);
        String url = descriptor.getUrl();
        helpSet = new HelpSet(HelpSet_JavaHelp.class.getClassLoader(),
                url.startsWith("/web/") ? getClass().getResource(url) : new URL(url));
        initViews();
    }
    
    private void initViews() {
        for (NavigatorView view : helpSet.getNavigatorViews()) {
            HelpViewType viewType = viewMap.get(view.getClass());
            
            if (viewType != null) {
                helpViews.add(new HelpView(view, viewType));
            }
            
        }
    }
    
    @Override
    public String getHomeID() {
        return helpSet.getHomeID().id;
    }
    
    @Override
    public HelpTopic getTopic(String topicId) {
        HelpTopic topic = (HelpTopic) helpSet.getKeyData("topics", topicId);
        
        if (topic != null) {
            return topic;
        }
        
        ID id = ID.create(topicId, helpSet);
        
        try {
            URL url = helpSet.getCombinedMap().getURLFromID(id);
            return new HelpTopic(url, topicId, helpSet.getTitle());
        } catch (Exception e) {
            return null;
        }
    }
    
    @Override
    public Collection<IHelpView> getAllViews() {
        return helpViews;
    }
    
    @Override
    public String getName() {
        return helpSet.getTitle();
    }
    
}
