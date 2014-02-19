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

import java.util.List;

import org.carewebframework.ui.sharedforms.TestController.TestItem;

public class TestController extends ListViewForm<TestItem> {
    
    private static final long serialVersionUID = 1L;
    
    public class TestItem {
        
        String item1, item2, item3;
        
        public TestItem(String data) {
            String[] pcs = data.split("\\^", 3);
            this.item1 = pcs[0];
            this.item2 = pcs[1];
            this.item3 = pcs[2];
        }
    }
    
    @Override
    protected void init() {
        setup("Test Title", -2, "Header1", "Header2", "Header3");
        super.init();
    }
    
    @Override
    protected void asyncAbort() {
    }
    
    @Override
    protected void requestData() {
        for (int i = 1; i <= 10; i++) {
            StringBuilder sb = new StringBuilder();
            
            for (int j = 1; j <= 3; j++) {
                sb.append(j == 1 ? "" : "^").append("Item #" + i + "." + j);
            }
            
            model.add(new TestItem(sb.toString()));
        }
        
        renderData();
    }
    
    @Override
    protected void render(TestItem dao, List<Object> columns) {
        columns.add(dao.item1);
        columns.add(dao.item2);
        columns.add(dao.item3);
    }
    
}
