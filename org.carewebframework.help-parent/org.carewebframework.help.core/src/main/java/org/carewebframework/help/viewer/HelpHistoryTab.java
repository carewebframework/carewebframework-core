/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.help.viewer;

import org.carewebframework.help.HelpTopic;

import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.ListitemRenderer;

/**
 * Tab that implements a view of the current topic selection history. Unlike other help tabs, this
 * one is created directly by the viewer. The tab consists of a simple list box that corresponds to
 * the current contents of the history list. Because the list box uses the history list directly as
 * its list model, the list box contents changes dynamically as the history changes.
 */
public class HelpHistoryTab extends HelpTab implements ListitemRenderer<HelpTopic> {
    
    private static final long serialVersionUID = 1L;
    
    private Listbox lstHistory;
    
    private final HelpHistory history;
    
    /**
     * Create the help tab for the specified viewer and viewType.
     * 
     * @param viewer The help viewer.
     * @param viewType The view type.
     */
    public HelpHistoryTab(HelpViewer viewer, HelpViewType viewType) {
        super(viewer, viewType, "helpHistoryTab.zul");
        history = viewer.getHistory();
        lstHistory.setItemRenderer(this);
        lstHistory.setModel(history.getItems());
    }
    
    /**
     * Set the history position to correspond to the new list box selection.
     */
    public void onSelect$lstHistory() {
        history.setPosition(lstHistory.getSelectedIndex());
    }
    
    /**
     * Sets focus to the list box when the tab is selected.
     * 
     * @see org.carewebframework.help.viewer.HelpTab#onSelect()
     */
    @Override
    public void onSelect() {
        super.onSelect();
        lstHistory.setFocus(true);
    }
    
    /**
     * Updated the list box selection when the history selection changes.
     * 
     * @see org.carewebframework.help.viewer.HelpTab#onTopicSelected(HelpTopic)
     */
    @Override
    public void onTopicSelected(HelpTopic topic) {
        lstHistory.setSelectedIndex(history.getPosition());
        setVisible(!history.getItems().isEmpty());
    }
    
    /**
     * Render method for the history list box. The list item label is the topic label and its value
     * is the topic itself.
     * 
     * @see org.zkoss.zul.ListitemRenderer#render
     */
    @Override
    public void render(Listitem item, HelpTopic topic, int index) throws Exception {
        item.setLabel(topic.getLabel());
        item.setValue(topic);
    }
    
}
