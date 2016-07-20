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

import java.util.Map;

import org.carewebframework.ui.wonderbar.WonderbarItem;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.A;
import org.zkoss.zul.Hlayout;
import org.zkoss.zul.Vlayout;

/**
 * FROM JDC... super basic, use just for testing
 */
class TestMenu extends Vlayout {
    
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    
    private static final String OBJECT_FOR_SELECT = "objectForSelect";
    
    private static final String STRING_FOR_SELECT = "stringUsedForSelect";
    
    //private static final String KEYBOARD_NUMBER = "keyboardNumber";
    
    private final Map<String, WonderbarItem> listboxItems;
    
    private final boolean useNumbers;
    
    private String currentSelectedText;
    
    private WonderbarItem currentSelectedItem;
    
    public TestMenu(Map<String, WonderbarItem> items, boolean useNumbers) {
        this.listboxItems = items;
        this.useNumbers = false;
        
        init();
    }
    
    private void init() {
        if (this.listboxItems != null && !this.listboxItems.isEmpty()) {
            /*int keyNum = 0;
            if (useNumbers) {
                keyboardList = new HashMap<Integer, Listitem>();
            }*/
            
            //this.addForward(Events.ON_SELECT, this, WonderbarBandboxComponent.ON_SELECT_FROM_BANDBOX_EVENT);
            //this.addEventListener(Events.ON_SELECT, wb.createBandboxSelectionEventListener());
            
            for (Map.Entry<String, ?> entry : this.listboxItems.entrySet()) {
                if (entry.getKey().startsWith("!") || entry.getValue() == null) {
                    /*if (!StringUtils.isBlank(entry.getKey())) {
                        String groupLabel = (entry.getKey().startsWith("!")) ?
                                entry.getKey().substring(1) : entry.getKey();
                        Listgroup group = new Listgroup(groupLabel);
                        group.setParent(this);
                        group.setStyle("background: #E9F2FB");
                    }*/
                } else {
                    
                    EventListener<Event> chandler = new EventListener<Event>() {
                        
                        @Override
                        public void onEvent(Event event) throws Exception {
                            // TODO Auto-generated method stub
                            Component c = event.getTarget();
                            currentSelectedItem = (WonderbarItem) c.getAttribute(OBJECT_FOR_SELECT);
                            currentSelectedText = (String) c.getAttribute(STRING_FOR_SELECT);
                            
                            Event fevent = new Event("onSelectFromBandbox", TestMenu.this);
                            Executions.getCurrent().postEvent(fevent);
                        }
                        
                    };
                    
                    Hlayout row = new Hlayout();
                    
                    A item = new A(entry.getKey());
                    item.setAttribute(OBJECT_FOR_SELECT, entry.getValue());
                    item.setAttribute(STRING_FOR_SELECT, entry.getKey());
                    item.addEventListener(Events.ON_CLICK, chandler);
                    
                    row.appendChild(item);
                    appendChild(row);
                    
                    //item.setAttribute(KEYBOARD_NUMBER, ++keyNum);
                    
                    //item.addForward(Events.ON_CLICK, this, WonderbarBandboxComponent.ON_SELECT_FROM_BANDBOX_EVENT);
                    
                    /*Listcell cell = new Listcell();
                    cell.setLabel((useNumbers ? (keyNum + ") ") : "") + entry.getKey());

                    cell.setParent(item);
                    item.setParent(this);

                    if (useNumbers) {
                        keyboardList.put(keyNum, item);
                    }*/
                }
            }
        }
    }
    
    public void clearSelection() {
        currentSelectedItem = null;
        currentSelectedText = null;
    }
    
    public WonderbarItem getSelectedItem() {
        return currentSelectedItem;
    }
    
    public String getSelectedText() {
        return currentSelectedText;
    }
    
    public String getMaxHeight() {
        return "150px";
    }
    
    public boolean useKeyboardNumbers() {
        return useNumbers;
    }
    
}
