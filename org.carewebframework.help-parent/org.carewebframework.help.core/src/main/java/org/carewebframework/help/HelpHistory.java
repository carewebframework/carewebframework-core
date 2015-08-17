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
import java.util.List;

import org.zkoss.zul.ListModelList;

/**
 * This class manages the history of selected topics. There are methods for adding new topics,
 * navigating the history list, and for registering topic change listeners.
 */
public class HelpHistory {
    
    /**
     * Interface for issuing callbacks when the topic selection changes.
     */
    public interface ITopicListener {
        
        void onTopicSelected(HelpTopic topic);
    }
    
    /**
     * Class for storing the topic history. It extends ZK's ListModelList (and can be used as a live
     * model list for a list box) mainly to introduce some convenience methods without any
     * significant new functionality.
     */
    public class HistoryList extends ListModelList<HelpTopic> {
        
        private static final long serialVersionUID = 1L;
        
        /**
         * Overrides the removeRange method to suppress unwanted exceptions.
         * 
         * @see org.zkoss.zul.ListModelList#removeRange(int, int)
         */
        @Override
        public void removeRange(int fromIndex, int toIndex) {
            if (fromIndex > toIndex || toIndex <= 0) {
                return;
            }
            
            super.removeRange(fromIndex, toIndex);
        }
    }
    
    private final HistoryList history = new HistoryList();
    
    private final List<ITopicListener> topicListeners = new ArrayList<ITopicListener>();
    
    private final int maxsize = 50;
    
    private int position = -1;
    
    /**
     * Clears the history list.
     */
    public void clear() {
        history.clear();
        position = -1;
    }
    
    /**
     * Adds a topic to the history list at the current position. All topics following the current
     * position are removed so that the added topic is always the last topic in the history. The
     * list is also truncated from the beginning if it exceeds the maximum history length. The added
     * topic becomes the current selection and all listeners are notified of the topic change.
     * <p>
     * Note that if the topic being added is the same as the topic at the current position, the
     * request is ignored.
     * 
     * @param topic HelpTopic to add.
     */
    public void add(HelpTopic topic) {
        if (topic != null && topic.getURL() != null && !sameTopic(topic, getCurrentItem())) {
            history.removeRange(position + 1, history.size());
            history.removeRange(0, history.size() - maxsize);
            position = history.size();
            history.add(topic);
        }
        
        notifyListeners(topic);
    }
    
    /**
     * Because the HelpTopic class does not implement its own equals method, have to implement
     * equality test here. Two topics are considered equal if the are the same instance or if their
     * targets are equal.
     * 
     * @param topic1 First topic to compare.
     * @param topic2 Second topic to compare.
     * @return True if topics are equal.
     */
    private boolean sameTopic(HelpTopic topic1, HelpTopic topic2) {
        return topic1 == topic2 || (topic1 != null && topic2 != null && topic1.equals(topic2));
    }
    
    /**
     * Adds a topic change listener.
     * 
     * @param topicListener Topic listener to add.
     */
    public void addTopicListener(ITopicListener topicListener) {
        topicListeners.add(topicListener);
    }
    
    /**
     * Returns true if the current position can be moved backward.
     * 
     * @return True if the current position can be moved backward.
     */
    public boolean hasPrevious() {
        return position > 0;
    }
    
    /**
     * Moves the current topic selection to the previous entry in the history if one exists.
     */
    public void previous() {
        if (hasPrevious()) {
            setPosition(position - 1);
        }
    }
    
    /**
     * Returns true if the current position can be moved forward.
     * 
     * @return True if the current position can be moved forward.
     */
    public boolean hasNext() {
        return position < history.size() - 1;
    }
    
    /**
     * Moves the current topic selection to the next entry in the history if one exists.
     */
    public void next() {
        if (hasNext()) {
            setPosition(position + 1);
        }
    }
    
    /**
     * Returns the current position in the history list.
     * 
     * @return The current position.
     */
    public int getPosition() {
        return position;
    }
    
    /**
     * Sets the current position in the history list. The topic at that position becomes the
     * currently selected topic and all topic change listeners are notified.
     * 
     * @param newPosition Position to set.
     */
    public void setPosition(int newPosition) {
        if (newPosition != position) {
            position = newPosition;
            notifyListeners(getCurrentItem());
        }
    }
    
    /**
     * Returns the history item at the specified index.
     * 
     * @param index The index of the history item. If negative, null is returned.
     * @return The history item at the specified index.
     */
    private HelpTopic getItem(int index) {
        return index < 0 ? null : history.get(index);
    }
    
    /**
     * Returns the currently selected topic, or null if one is not selected.
     * 
     * @return The current topic.
     */
    public HelpTopic getCurrentItem() {
        return position < 0 ? null : getItem(position);
    }
    
    /**
     * Notifies all registered listeners of a topic selection change.
     */
    private void notifyListeners(HelpTopic topic) {
        for (ITopicListener listener : topicListeners) {
            listener.onTopicSelected(topic);
        }
    }
    
    /**
     * Returns a reference to the underlying history list for use as a live list.
     * 
     * @return The history list.
     */
    public HistoryList getItems() {
        return history;
    }
}
