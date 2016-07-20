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
package org.carewebframework.ui.wonderbar.test;

import java.util.ArrayList;
import java.util.List;

import org.carewebframework.ui.wonderbar.IWonderbarClientSearchProvider;
import org.carewebframework.ui.wonderbar.IWonderbarServerSearchProvider;
import org.carewebframework.ui.wonderbar.Wonderbar.MatchMode;
import org.carewebframework.ui.wonderbar.WonderbarUtil;

/**
 * Simple test provider
 */
public class TestSearchProvider implements IWonderbarServerSearchProvider<TestSearchItem>, IWonderbarClientSearchProvider<TestSearchItem> {
    
    private final List<TestSearchItem> items = new ArrayList<>();
    
    private final List<TestSearchItem> defaults = new ArrayList<>();
    
    private final MatchMode matchMode;
    
    public TestSearchProvider(int defaultCount, int itemCount, MatchMode matchMode) {
        this.matchMode = matchMode;
        initDefaults(defaultCount);
        initItems(itemCount);
    }
    
    @Override
    public boolean getSearchResults(String search, int maxItems, List<TestSearchItem> ret) {
        List<String> patterns = WonderbarUtil.tokenize(search);
        boolean tooMany = false;
        
        for (TestSearchItem item : items) {
            if (WonderbarUtil.matches(patterns, item.label, matchMode)) {
                if (ret.size() == maxItems) {
                    tooMany = true;
                    break;
                }
                ret.add(item);
            }
        }
        return !tooMany;
    }
    
    @Override
    public List<TestSearchItem> getAllItems() {
        return items;
    }
    
    @Override
    public List<TestSearchItem> getDefaultItems() {
        return defaults;
    }
    
    private void initItems(int count) {
        for (int i = 1; i <= count; i++) {
            String label = "Test item #" + i;
            TestSearchItem hit = new TestSearchItem(label, label, "category " + (i / 3), 0);
            items.add(hit);
        }
    }
    
    private void initDefaults(int count) {
        for (int i = 1; i <= count; i++) {
            String label = "Default item #" + i;
            TestSearchItem item = new TestSearchItem(label, null, null, i);
            defaults.add(item);
        }
    }
    
}
