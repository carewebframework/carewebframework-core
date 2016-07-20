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

/**
 * Enum for representing known view types.
 */
public enum HelpViewType {
    Unknown, TOC, Keyword, Index, Search, History, Glossary;
    
    /**
     * Returns the help tab class that services this view type. For unsupported view types, returns
     * null.
     * 
     * @return A help tab class.
     */
    public Class<? extends HelpTab> getTabClass() {
        switch (this) {
            case TOC:
                return HelpContentsTab.class;
                
            case Keyword:
                return HelpIndexTab.class;
                
            case Index:
                return HelpIndexTab.class;
                
            case Search:
                return HelpSearchTab.class;
                
            case History:
                return HelpHistoryTab.class;
                
            case Glossary:
                return HelpIndexTab.class;
                
            default:
                return null;
        }
    }
    
};
