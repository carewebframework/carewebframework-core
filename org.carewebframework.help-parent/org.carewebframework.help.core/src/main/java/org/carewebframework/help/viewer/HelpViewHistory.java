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
import org.fujion.annotation.EventHandler;
import org.fujion.annotation.WiredComponent;
import org.fujion.component.BaseComponent;
import org.fujion.component.Listbox;
import org.fujion.component.Listitem;
import org.fujion.component.Tab;
import org.fujion.event.ChangeEvent;
import org.fujion.model.IListModel;
import org.fujion.model.IModelAndView;
import org.fujion.model.ListModel;

/**
 * Tab that implements a view of the current topic selection history. Unlike other help tabs, this
 * one is created directly by the viewer. The tab consists of a simple list box that corresponds to
 * the current contents of the history list. Because the list box uses the history list directly as
 * its list model, the list box contents changes dynamically as the history changes.
 */
public class HelpViewHistory extends HelpViewBase {
    
    @WiredComponent
    private Listbox lstHistory;
    
    private HelpHistory history;
    
    /**
     * Create the help tab for the specified viewer and viewType.
     *
     * @param viewer The help viewer.
     * @param viewType The view type.
     */
    public HelpViewHistory(HelpViewer viewer, HelpViewType viewType) {
        super(viewer, viewType, "helpHistoryTab.fsp");
    }
    
    @Override
    public void afterInitialized(BaseComponent comp) {
        super.afterInitialized(comp);
        IModelAndView<Listitem, HelpTopic> mv = lstHistory.getModelAndView(HelpTopic.class);
        IListModel<HelpTopic> model = new ListModel<>();
        history = new HelpHistory(model);
        mv.setModel(model);
        mv.setRenderer(new HelpTopicRenderer());
        getViewer().setHelpHistory(history);
    }
    
    /**
     * Set the history position to correspond to the new list box selection.
     *
     * @param event The change event.
     */
    @EventHandler(value = "change", target = "@lstHistory")
    private void onChange$lstHistory(ChangeEvent event) {
        
        if (event.getValue() == Boolean.TRUE) {
            history.setPosition(lstHistory.getSelectedIndex());
        }
    }
    
    /**
     * Sets focus to the list box when the tab is selected.
     *
     * @see org.carewebframework.help.viewer.HelpViewBase#onSelect()
     */
    @Override
    public void onSelect() {
        super.onSelect();
        lstHistory.setFocus(true);
    }
    
    /**
     * Updated the list box selection when the history selection changes.
     *
     * @see org.carewebframework.help.viewer.HelpViewBase#onTopicSelected(HelpTopic)
     */
    @Override
    public void onTopicSelected(HelpTopic topic) {
        Listitem item = (Listitem) lstHistory.getChildAt(history.getPosition());
        lstHistory.setSelectedItem(item);
        
        if (item != null) {
            getContainer().getAncestor(Tab.class).setVisible(true);
            item.scrollIntoView();
        }
    }
    
}
