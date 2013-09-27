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

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import org.carewebframework.common.StrUtil;
import org.carewebframework.help.HelpSearcher.IHelpSearchListener;

import org.zkoss.image.AImage;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.ListitemRenderer;
import org.zkoss.zul.Textbox;

/**
 * Tab supporting the help system search function. Consists of a text box into which the user may
 * enter a search expression (including boolean operators) and a list box to display the results of
 * the search.
 */
public class HelpSearchTab extends HelpTab implements ListitemRenderer<HelpSearchHit>, IHelpSearchListener {
    
    private static final long serialVersionUID = 1L;
    
    private Textbox txtSearch;
    
    private Listbox lstSrchResults;
    
    private Label lblNoResultsFound;
    
    private final HelpSearcher searcher = new HelpSearcher();
    
    private final AImage[] icons = new AImage[3];
    
    private double tertile1;
    
    private double tertile2;
    
    private final EventListener<Event> searchListener = new EventListener<Event>() {
        
        @Override
        public void onEvent(Event event) throws Exception {
            @SuppressWarnings("unchecked")
            List<HelpSearchHit> searchResults = (List<HelpSearchHit>) event.getData();
            Collections.sort(searchResults);
            
            if (searchResults.isEmpty()) {
                showMessage("cwf.help.tab.search.noresults");
                return;
            }
            
            double highscore = searchResults.get(0).getConfidence();
            double lowscore = searchResults.get(searchResults.size() - 1).getConfidence();
            double interval = (highscore - lowscore) / 3;
            tertile1 = lowscore + interval;
            tertile2 = tertile1 + interval;
            lstSrchResults.setModel(new ListModelList<HelpSearchHit>(searchResults));
        }
        
    };
    
    /**
     * Create the help tab for the specified viewer and viewType.
     * 
     * @param viewer
     * @param viewType
     */
    public HelpSearchTab(HelpViewer viewer, HelpViewType viewType) {
        super(viewer, viewType, "helpSearchTab.zul");
        lstSrchResults.setItemRenderer(this);
    }
    
    /**
     * Sets the focus to the search text box when the tab is selected.
     * 
     * @see org.carewebframework.help.HelpTab#onSelect()
     */
    @Override
    public void onSelect() {
        super.onSelect();
        txtSearch.select();
        txtSearch.setFocus(true);
    }
    
    /**
     * Sets the currently viewed topic when a search result is selected.
     */
    public void onSelect$lstSrchResults() {
        Listitem item = lstSrchResults.getSelectedItem();
        lstSrchResults.renderItem(item);
        setTopic((HelpTopic) item.getValue());
    }
    
    /**
     * Perform search when user presses enter button.
     */
    public void onOK$txtSearch() {
        onClick$btnSearch();
    }
    
    /**
     * Perform the search and display the results.
     */
    public void onClick$btnSearch() {
        lstSrchResults.setModel((ListModelList<?>) null);
        lstSrchResults.getItems().clear();
        String query = txtSearch.getValue();
        showMessage(null);
        
        if (query != null && query.trim().length() > 0) {
            searcher.search(query, this);
        } else {
            showMessage("cwf.help.tab.search.noentry");
        }
    }
    
    /**
     * Returns the icon that represents the specified score. There are three icons available based
     * on within which tertile the score falls.
     * 
     * @param score
     * @return
     */
    private AImage toImage(double score) {
        int tertile = score >= tertile2 ? 2 : score >= tertile1 ? 1 : 0;
        AImage aimage = icons[tertile];
        
        if (aimage == null) {
            String img = tertile <= 0 ? "empty" : tertile == 1 ? "half" : "full";
            aimage = HelpUtil.getImageContent(img + ".png");
            icons[tertile] = aimage;
        }
        
        return aimage;
    }
    
    /**
     * Displays the specified message. The list box is hidden if the message is not empty.
     * 
     * @param message
     */
    private void showMessage(String message) {
        message = message == null ? null : Labels.getLabel(message);
        lblNoResultsFound.setValue(message);
        lblNoResultsFound.setVisible(!StringUtils.isEmpty(message));
        lstSrchResults.setVisible(!lblNoResultsFound.isVisible());
    }
    
    /**
     * Adds the query handler from the specified view to the list of registered handlers.
     * 
     * @see org.carewebframework.help.HelpTab#addView(IHelpView)
     */
    @Override
    public void addView(IHelpView view) {
        super.addView(view);
        searcher.addView(view);
    }
    
    /**
     * Renders the list box contents.
     * 
     * @see org.zkoss.zul.ListitemRenderer#render
     */
    @Override
    public void render(Listitem item, HelpSearchHit qr, int index) throws Exception {
        double score = qr.getConfidence();
        item.setValue(qr.getTopic());
        Listcell lc = new Listcell();
        lc.setImageContent(toImage(score));
        String tt = StrUtil.formatMessage("@cwf.help.tab.search.score", score);
        lc.setTooltiptext(tt);
        item.appendChild(lc);
        lc = new Listcell(qr.getTopic().getLabel());
        item.appendChild(lc);
        lc = new Listcell(qr.getTopic().getSource());
        item.appendChild(lc);
    }
    
    @Override
    public void onSearchComplete(List<HelpSearchHit> results) {
        Executions.schedule(getDesktop(), searchListener, new Event("onSearchComplete", this, results));
    }
}
