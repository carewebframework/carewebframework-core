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
package org.carewebframework.shell.designer;

import org.carewebframework.common.MiscUtil;
import org.carewebframework.common.StrUtil;
import org.carewebframework.shell.layout.UIElementBase;
import org.carewebframework.shell.property.PropertyInfo;
import org.carewebframework.web.component.BaseComponent;
import org.carewebframework.web.component.Popup;
import org.carewebframework.web.component.Popupbox;
import org.carewebframework.web.event.CloseEvent;
import org.carewebframework.web.event.OpenEvent;
import org.carewebframework.web.page.PageUtil;

/**
 * Allows registration of custom editors for complex property types.
 */
public class PropertyEditorCustom extends PropertyEditorBase {
    
    protected final Popupbox popupbox;
    
    protected final Popup popup;
    
    protected PropertyEditorCustom() {
        super(new Popupbox());
        popupbox = (Popupbox) component;
        //popupbox.setAutodrop(false);
        popupbox.setReadonly(true);
        popupbox.setValue(StrUtil.getLabel("cwf.shell.designer.propedit.custom.popupbox.prompt"));
        popupbox.addEventListener(OpenEvent.class, (event) -> {
            doOpen();
        });
        popupbox.addEventListener(CloseEvent.class, (event) -> {
            doClose();
        });
        popup = new Popup();
        popupbox.addChild(popup);
    }
    
    protected PropertyEditorCustom(String template) throws Exception {
        this();
        BaseComponent root = PageUtil.createPage(template, popup).get(0);
        root.wireController(this);
    }
    
    /**
     * Invoked when the associated popupbox is opened.
     */
    protected void doOpen() {
    }
    
    /**
     * Invoked when the associated popupbox is closed;
     */
    protected void doClose() {
    }
    
    /**
     * For custom property editors, if a property getter is specified, the value returned by the
     * getter is the real target.
     * 
     * @param target Target element.
     * @param propInfo The property information.
     * @param propGrid The parent property grid.
     */
    @Override
    protected void init(UIElementBase target, PropertyInfo propInfo, PropertyGrid propGrid) {
        if (propInfo.getGetter() != null) {
            try {
                target = (UIElementBase) propInfo.getPropertyValue(target);
                propInfo.setSetter(null);
            } catch (Exception e) {
                throw MiscUtil.toUnchecked(e);
            }
        }
        
        super.init(target, propInfo, propGrid);
    }
    
    @Override
    public void setFocus() {
        popupbox.open();
        doOpen();
    }
    
    @Override
    protected Object getValue() {
        return null;
    }
    
    @Override
    protected void setValue(Object value) {
    }
    
}
