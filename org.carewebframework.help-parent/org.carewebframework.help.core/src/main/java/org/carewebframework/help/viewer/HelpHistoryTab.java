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
package org.carewebframework.help.viewer;

import org.carewebframework.help.HelpTopic;
import org.carewebframework.help.HelpViewType;
import org.carewebframework.web.annotation.EventHandler;
import org.carewebframework.web.annotation.WiredComponent;
import org.carewebframework.web.component.Listbox;
import org.carewebframework.web.model.ModelAndView;

/**
 * Tab that implements a view of the current topic selection history. Unlike other help tabs, this
 * one is created directly by the viewer. The tab consists of a simple list box that corresponds to
 * the current contents of the history list. Because the list box uses the history list directly as
 * its list model, the list box contents changes dynamically as the history changes.
 */
public class HelpHistoryTab extends HelpTab {
    
    @WiredComponent
    private Listbox lstHistory;
    
    private final HelpHistory history;
    
    /**
     * Create the help tab for the specified viewer and viewType.
     * 
     * @param viewer The help viewer.
     * @param viewType The view type.
     */
    public HelpHistoryTab(HelpViewer viewer, HelpViewType viewType) {
        super(viewer, viewType, "helpHistoryTab.cwf");
        history = viewer.getHistory();
        new ModelAndView<>(lstHistory, history.getItems(), new HelpTopicRenderer());
    }
    
    /**
     * Set the history position to correspond to the new list box selection.
     */
    @EventHandler(value = "change", target = "@lstHistory")
    private void onChange$lstHistory() {
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
    
}
