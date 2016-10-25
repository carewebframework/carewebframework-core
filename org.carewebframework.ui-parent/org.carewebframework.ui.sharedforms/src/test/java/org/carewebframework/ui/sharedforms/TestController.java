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
package org.carewebframework.ui.sharedforms;

import java.util.List;

import org.carewebframework.ui.sharedforms.TestController.TestItem;

public class TestController extends ListViewForm<TestItem> {
    
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
