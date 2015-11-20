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
