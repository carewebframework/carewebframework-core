/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.ui.sharedforms;

public class TestController extends ListViewForm {
    
    private static final long serialVersionUID = 1L;
    
    @Override
    protected void init() {
        setup("Test Title", -2, "Header1", "Header2", "Header3");
        super.init();
    }
    
    @Override
    protected void asyncAbort() {
    }
    
    @Override
    protected void fetchList() {
        for (int i = 1; i <= 10; i++) {
            StringBuilder sb = new StringBuilder();
            
            for (int j = 1; j <= 3; j++) {
                sb.append(j == 1 ? "" : "^").append("Item #" + i + "." + j);
            }
            
            itemList.add(sb.toString());
        }
        
        renderList();
    }
    
}
