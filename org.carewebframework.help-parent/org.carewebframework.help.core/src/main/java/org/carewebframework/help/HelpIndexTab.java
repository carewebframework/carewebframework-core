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
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;

import org.carewebframework.ui.zk.ZKUtil;

import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.InputEvent;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.ListitemRenderer;
import org.zkoss.zul.Textbox;

/**
 * Tab for displaying the keyword index. This tab consists of two list boxes: one that displays the
 * keywords and one the displays the topics associated with the selected keyword. There is also a
 * quick find feature that allows locating a keyword by entering the first letters of the keyword.
 */
public class HelpIndexTab extends HelpTab implements ListitemRenderer<HelpTopic> {
    
    private static final long serialVersionUID = 1L;
    
    private Listbox lstKeywords;
    
    private Listbox lstTopics;
    
    private Textbox txtFind;
    
    private final Map<String, List<HelpTopic>> topicIndex = new TreeMap<String, List<HelpTopic>>(
            String.CASE_INSENSITIVE_ORDER);
    
    private ListModelList<String> keywordList;
    
    /**
     * Create the help tab for the specified viewer and viewType.
     * 
     * @param viewer
     * @param viewType
     */
    public HelpIndexTab(HelpViewer viewer, HelpViewType viewType) {
        super(viewer, viewType, "helpIndexTab.zul");
        lstTopics.setItemRenderer(this);
    }
    
    /**
     * Initialize the topic list when a keyword is selected. The last topic selected for the keyword
     * is automatically selected.
     */
    public void onSelect$lstKeywords() {
        Listitem item = lstKeywords.getSelectedItem();
        
        if (item == null) {
            lstTopics.getItems().clear();
            setTopic(null);
            return;
        }
        
        List<HelpTopic> topics = getTopics(item.getLabel());
        
        if (!item.hasAttribute("sorted")) {
            item.setAttribute("sorted", true);
            Collections.sort(topics);
        }
        
        lstTopics.setModel(new ListModelList<HelpTopic>(topics));
        lstTopics.renderAll();
        onSelect$lstTopics();
    }
    
    /**
     * Set the topic view when a topic selection is made.
     */
    public void onSelect$lstTopics() {
        Listitem item = lstTopics.getSelectedItem();
        Listitem keywordItem = lstKeywords.getSelectedItem();
        
        if (item == null) {
            int i = keywordItem.hasAttribute("last") ? (Integer) keywordItem.getAttribute("last") : 0;
            item = lstTopics.getItemAtIndex(i);
            lstTopics.selectItem(item);
        }
        
        keywordItem.setAttribute("last", lstTopics.getSelectedIndex());
        setTopic((HelpTopic) item.getValue());
    }
    
    /**
     * Locates and selects a matching keyword as the user types in the quick find text box.
     * 
     * @param event
     */
    public void onChanging$txtFind(Event event) {
        String find = ((InputEvent) ZKUtil.getEventOrigin(event)).getValue().toLowerCase();
        
        if (StringUtils.isEmpty(find)) {
            return;
        }
        
        int match = -1;
        
        for (int i = 0; i < keywordList.size(); i++) {
            if (keywordList.get(i).toLowerCase().startsWith(find)) {
                match = i;
                break;
            }
        }
        
        if (match != lstKeywords.getSelectedIndex()) {
            Listitem item = match == -1 ? null : lstKeywords.getItemAtIndex(match);
            lstKeywords.renderItem(item);
            lstKeywords.setSelectedItem(item);
            onSelect$lstKeywords();
        }
    }
    
    /**
     * Initialize the tab the first time it is selected. This method creates a model list from the
     * current keyword list and assigns it to the keyword list box.
     * 
     * @see org.carewebframework.help.HelpTab#init()
     */
    @Override
    protected void init() {
        super.init();
        keywordList = new ListModelList<String>(topicIndex.keySet());
        lstKeywords.setModel(keywordList);
    }
    
    /**
     * Sets the focus to the quick find text box when the tab is selected.
     * 
     * @see org.carewebframework.help.HelpTab#onSelect()
     */
    @Override
    public void onSelect() {
        super.onSelect();
        txtFind.setFocus(true);
        txtFind.select();
    }
    
    /**
     * Add keywords and topics from the specified view to the tab.
     * 
     * @see org.carewebframework.help.HelpTab#addView(IHelpView)
     */
    @Override
    public void addView(IHelpView view) {
        super.addView(view);
        HelpTopicNode topNode = view.getTopicTree();
        
        for (HelpTopicNode child : topNode.getChildren()) {
            addKeywordTopic(child, null);
        }
    }
    
    /**
     * Traverse the keyword topic tree, adding keywords and topics to the list.
     * 
     * @param node
     * @param topics
     */
    private void addKeywordTopic(HelpTopicNode node, List<HelpTopic> topics) {
        HelpTopic topic = node.getTopic();
        
        if (topics == null) {
            topics = getTopics(topic.getLabel());
        }
        
        if (topic.getURL() != null) {
            topics.add(topic);
        }
        
        for (HelpTopicNode child : node.getChildren()) {
            addKeywordTopic(child, topics);
        }
    }
    
    /**
     * Returns the topic list for the specified keyword, creating one if it does not already exist.
     * 
     * @param keyword
     * @return
     */
    private List<HelpTopic> getTopics(String keyword) {
        List<HelpTopic> topics = topicIndex.get(keyword);
        
        if (topics == null) {
            topics = new ArrayList<HelpTopic>();
            topicIndex.put(keyword, topics);
        }
        
        return topics;
    }
    
    /**
     * This is the renderer for the topic list.
     * 
     * @see org.zkoss.zul.ListitemRenderer#render
     */
    @Override
    public void render(Listitem item, HelpTopic topic, int index) throws Exception {
        item.setLabel(topic.getLabel());
        item.setValue(topic);
    }
}
