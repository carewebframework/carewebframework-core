/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.help.chm;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.carewebframework.common.StrUtil;
import org.carewebframework.common.XMLUtil;
import org.carewebframework.help.HelpModule;
import org.carewebframework.help.HelpSetBase;
import org.carewebframework.help.HelpTopic;
import org.carewebframework.help.HelpTopicNode;
import org.carewebframework.help.HelpUtil;
import org.carewebframework.help.HelpViewType;
import org.carewebframework.help.IHelpView;

import org.zkoss.zk.ui.Executions;

import org.w3c.dom.Node;

public class HelpSet_CHMHelp extends HelpSetBase {
    
    /*
    private static final Map<Class<? extends NavigatorView>, HelpViewType> viewMap = new HashMap<Class<? extends NavigatorView>, HelpViewType>();
    
    static {
        viewMap.put(TOCView.class, HelpViewType.TOC);
        viewMap.put(GlossaryView.class, HelpViewType.Glossary);
        viewMap.put(IndexView.class, HelpViewType.Index);
        viewMap.put(SearchView.class, HelpViewType.Search);
    }
    */
    
    private final String baseURL;
    
    private final Properties properties = new Properties();
    
    private final Map<String, HelpTopic> topics = new HashMap<>();
    
    private final List<IHelpView> helpViews = new ArrayList<>();
    
    private final boolean jar;
    
    private String defaultTopic;
    
    public HelpSet_CHMHelp(HelpModule module) throws Exception {
        super(module);
        String url = module.getUrl();
        int i = url.lastIndexOf('/');
        baseURL = url.substring(0, i + 1);
        jar = baseURL.startsWith("/web/");
        loadSystemInfo();
        loadTopics();
        initViews();
    }
    
    private void loadTopics() throws Exception {
        HelpTopicNode rootNode = new HelpTopicTree(this, "topics.xml").getRootNode();
        URL defaultURL = defaultTopic == null ? null : getURL(defaultTopic);
        int topicIndex = 0;
        
        for (HelpTopicNode node : rootNode.getChildren()) {
            String topicId = node.getNodeId();
            HelpTopic topic = node.getTopic();
            registerTopic(topicId, topic);
            registerTopic("i_" + topicIndex++, topic);
            
            if (defaultURL != null && defaultURL.equals(topic.getURL())) {
                defaultTopic = topicId;
                defaultURL = null;
            }
        }
    }
    
    protected void registerTopic(String id, HelpTopic topic) {
        topics.put(id, topic);
    }
    
    private void loadSystemInfo() throws Exception {
        try (InputStream is = openStream("helpset.xml");) {
            Node root = XMLUtil.parseXMLFromStream(is).getFirstChild();
            
            for (int i = 0; i < root.getChildNodes().getLength(); i++) {
                Node child = root.getChildNodes().item(i);
                
                if (child.getNodeType() == Node.ELEMENT_NODE) {
                    properties.setProperty(child.getNodeName(), child.getTextContent());
                }
            }
            
            defaultTopic = properties.getProperty("defaultTopic");
        }
    }
    
    protected URL getURL(String file) throws Exception {
        String root = jar && Executions.getCurrent() != null ? HelpUtil.getBaseUrl() + "/zkau" : "file:";
        return new URL(root + baseURL + file);
    }
    
    protected InputStream openStream(String file) throws Exception {
        return getClass().getResourceAsStream(baseURL + file);
    }
    
    private void initViews() throws Exception {
        initView("toc.xml", HelpViewType.TOC);
        initView("index.xml", HelpViewType.Index);
    }
    
    private void initView(String file, HelpViewType type) throws Exception {
        HelpTopicTree topicTree = new HelpTopicTree(this, file);
        
        if (!topicTree.isEmpty()) {
            HelpView view = new HelpView(topicTree, type);
            helpViews.add(view);
        }
    }
    
    @Override
    public String getHomeID() {
        return defaultTopic;
    }
    
    @Override
    public HelpTopic getTopic(String topicId) {
        HelpTopic topic = topics.get(topicId);
        
        if (topic == null && ("/" + topicId).startsWith(baseURL)) {
            int i = topicId.lastIndexOf('/');
            topicId = StrUtil.piece(topicId.substring(i + 1).replace('_', ' '), ".htm");
            return getTopic(topicId);
        }
        
        return topic;
    }
    
    @Override
    public Collection<IHelpView> getAllViews() {
        return helpViews;
    }
    
}
