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
package org.carewebframework.help;

import java.util.Collection;

/**
 * Interface to be implemented by a help set provider.
 */
public interface IHelpSet {
    
    /**
     * Return the unique id of the help set.
     * 
     * @return The help set unique id.
     */
    String getId();
    
    /**
     * Returns the name (title) of the help set.
     * 
     * @return The help set name.
     */
    String getName();
    
    /**
     * Returns the id of the home topic, if any.
     * 
     * @return The home topic id (may be null).
     */
    String getHomeID();
    
    /**
     * Returns the help topic associated with the topic id.
     * 
     * @param topicId A topic id.
     * @return The associated help topic (may be null).
     */
    HelpTopic getTopic(String topicId);
    
    /**
     * Returns a collection of all help views contained within the help set.
     * 
     * @return Help views in the help set.
     */
    Collection<IHelpView> getAllViews();
    
}
