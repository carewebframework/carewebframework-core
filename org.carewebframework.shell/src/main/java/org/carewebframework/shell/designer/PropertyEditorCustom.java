/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.shell.designer;

import org.carewebframework.common.MiscUtil;
import org.carewebframework.common.StrUtil;
import org.carewebframework.shell.layout.UIElementBase;
import org.carewebframework.shell.property.PropertyInfo;
import org.carewebframework.ui.zk.ZKUtil;

import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.OpenEvent;
import org.zkoss.zul.Bandbox;
import org.zkoss.zul.Bandpopup;

/**
 * Allows registration of custom editors for complex property types.
 */
public class PropertyEditorCustom extends PropertyEditorBase implements EventListener<Event> {
    
    protected final Bandbox bandbox;
    
    protected final Bandpopup bandpopup;
    
    protected PropertyEditorCustom() {
        super(new Bandbox());
        bandbox = (Bandbox) component;
        bandbox.setAutodrop(false);
        bandbox.setReadonly(true);
        bandbox.setText(StrUtil.getLabel("cwf.shell.designer.propedit.custom.bandbox.prompt"));
        bandbox.addEventListener(Events.ON_OPEN, this);
        bandpopup = new Bandpopup();
        bandbox.appendChild(bandpopup);
    }
    
    protected PropertyEditorCustom(String template) throws Exception {
        this();
        ZKUtil.wireController(ZKUtil.loadZulPage(template, bandpopup), this);
    }
    
    /**
     * Invoked when the associated bandbox is opened.
     */
    protected void doOpen() {
    }
    
    /**
     * Invoked when the associated bandbox is closed;
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
        bandbox.open();
        doOpen();
    }
    
    @Override
    protected Object getValue() {
        return null;
    }
    
    @Override
    protected void setValue(Object value) {
    }
    
    /**
     * Capture the bandbox onOpen event and invokes doOpen or doClose depending on the bandbox
     * state.
     */
    @Override
    public void onEvent(Event event) throws Exception {
        event = ZKUtil.getEventOrigin(event);
        
        if (event instanceof OpenEvent) {
            OpenEvent openEvent = (OpenEvent) event;
            
            if (openEvent.isOpen()) {
                doOpen();
            } else {
                doClose();
            }
        }
    }
    
}
