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
package org.carewebframework.ui.wonderbar;

import org.fujion.annotation.EventType;
import org.fujion.annotation.EventType.EventParameter;
import org.fujion.annotation.OnFailure;
import org.fujion.component.BaseComponent;
import org.fujion.event.Event;

/**
 * Fired when a search request is received from the client.
 */
@EventType(WonderbarSearchEvent.TYPE)
public class WonderbarSearchEvent extends Event {
    
    public static final String TYPE = "wonderbarSearch";
    
    @EventParameter(onFailure = OnFailure.IGNORE)
    private String term;
    
    public WonderbarSearchEvent() {
    }
    
    public WonderbarSearchEvent(BaseComponent target, String term) {
        super(TYPE, target);
        this.term = term;
    }
    
    /**
     * Returns the search term.
     * 
     * @return The search term.
     */
    public String getTerm() {
        return term;
    }
    
}
