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

/**
 * A wonder bar group item. A group item provides a non-selectable header for a related group of
 * wonder bar items.
 */
public class WonderbarGroup extends WonderbarAbstractItem {
    
    private static final long serialVersionUID = 1L;
    
    public WonderbarGroup() {
        super();
    }
    
    public WonderbarGroup(String label) {
        super(label);
    }
    
    @Override
    public String getWidgetClass() {
        return "wonderbar.ext.WonderbarGroup";
    }
    
    @Override
    public String getZclass() {
        return _zclass == null ? "cwf-wonderbar-group" : _zclass;
    }
    
    @Override
    public String getLabel() {
        return super.getLabel();
    }
    
    @Override
    public void setLabel(String label) {
        super.setLabel(label);
    }
    
}
