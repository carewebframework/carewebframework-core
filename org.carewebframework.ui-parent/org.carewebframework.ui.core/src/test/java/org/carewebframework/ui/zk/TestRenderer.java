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
package org.carewebframework.ui.zk;

import static org.junit.Assert.assertNotNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.zkoss.zul.Grid;
import org.zkoss.zul.Hbox;
import org.zkoss.zul.Panel;
import org.zkoss.zul.Row;

import org.junit.Test;

public class TestRenderer {
    
    /**
     * Test method for
     * {@link org.carewebframework.ui.zk.IRenderer#render(org.zkoss.zk.ui.Component, java.lang.Object, java.lang.Object[])}
     * .
     */
    @Test
    public void testRenderNullGrid() {
        TestGridRowRendererImpl renderer = new TestGridRowRendererImpl();
        Row row = renderer.render((Grid) null, (Long) null);
        assertNotNull(row);
    }
    
    /**
	 * 
	 * 
	 */
    @Test
    public void testRenderRowValue() {
        TestGridRowRendererImpl renderer = new TestGridRowRendererImpl();
        List<String> list = Arrays.asList(new String[] { "item1", "item2" });
        Row row = renderer.render(null, 23L, list);
        assertNotNull(row);
    }
    
    /**
	 * 
	 * 
	 */
    @Test
    public void testRenderRowListMap() {
        TestGridRowRendererImpl renderer = new TestGridRowRendererImpl();
        List<String> list = Arrays.asList(new String[] { "item1", "item2" });
        Map<String, Object> map = new HashMap<>();
        map.put("value1", "value");
        Row row = renderer.render(null, 54L, list, map);
        assertNotNull(row);
    }
    
    /**
	 *
	 */
    public static class TestGridRowRendererImpl implements IRenderer<Row, Grid, Long> {
        
        /**
         * @see org.carewebframework.ui.zk.IRenderer#render(org.zkoss.zk.ui.Component,
         *      java.lang.Object, java.lang.Object[])
         */
        @Override
        public Row render(Grid optionalParentorExistingComponent, Long data, Object... supportData) {
            return new Row();
        }
    }
    
    /**
	 *
	 */
    public static class TestFormInitializingRendererImpl implements IRenderer<Panel, Panel, Long> {
        
        /**
         * @see org.carewebframework.ui.zk.IRenderer#render(org.zkoss.zk.ui.Component,
         *      java.lang.Object, java.lang.Object[])
         */
        @Override
        public Panel render(Panel optionalParentorExistingComponent, Long data, Object... supportData) {
            return optionalParentorExistingComponent;
        }
    }
    
    /**
	 *
	 */
    public static class TestNewFormRendererImpl implements IRenderer<Panel, Hbox, Long> {
        
        /**
         * @see org.carewebframework.ui.zk.IRenderer#render(org.zkoss.zk.ui.Component,
         *      java.lang.Object, java.lang.Object[])
         */
        @Override
        public Panel render(Hbox optionalParent, Long data, Object... supportData) {
            Panel panel = new Panel();
            panel.setParent(optionalParent);
            return panel;
        }
    }
}
