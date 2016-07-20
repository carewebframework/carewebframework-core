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

import org.carewebframework.help.IHelpSet;

/**
 * Interface implemented by a help viewer.
 */
public interface IHelpViewer {
    
    /**
     * Loads the specified helpsets.
     * 
     * @param helpSets An iterable of helpsets to load. May be null.
     */
    void load(Iterable<IHelpSet> helpSets);
    
    /**
     * Merges a help set into the viewer. If the help set has already been merged, no action is
     * taken.
     * 
     * @param helpSet The help set to merge.
     */
    void mergeHelpSet(IHelpSet helpSet);
    
    /**
     * Displays the help viewer. Any current selections remain unchanged.
     */
    void show();
    
    /**
     * Displays the home page of the specified help set.
     * 
     * @param helpSet Help set to display.
     */
    void show(IHelpSet helpSet);
    
    /**
     * Displays the topic associated with the specified topic id in the specified help set.
     * 
     * @param helpSet Help set to display.
     * @param topicId Id of the topic to display. If null or empty, uses the homeId of the help set.
     */
    void show(IHelpSet helpSet, String topicId);
    
    /**
     * Displays the topic associated with the specified topic id in the specified help set.
     * 
     * @param helpSet Help set to display.
     * @param topicId Id of the topic to display. If null or empty, uses the homeId of the help set.
     * @param topicLabel The text label used to identify the topic. If null or empty, uses the
     *            topicId as the label.
     */
    void show(IHelpSet helpSet, String topicId, String topicLabel);
    
    /**
     * Selects the tab associated with the specified view type and displays the viewer.
     * 
     * @param viewType The view type.
     */
    void show(HelpViewType viewType);
    
    /**
     * Displays the default topic associated with the help set corresponding to the specified home
     * id.
     * 
     * @param homeId Home id of the help set to display.
     */
    void show(String homeId);
    
    /**
     * Displays the specified topic associated with the help set corresponding to the specified home
     * id.
     * 
     * @param homeId Home id of the help set to display.
     * @param topicId The id of the topic to display.
     */
    void show(String homeId, String topicId);
    
    /**
     * Closes the viewer.
     */
    void close();
}
