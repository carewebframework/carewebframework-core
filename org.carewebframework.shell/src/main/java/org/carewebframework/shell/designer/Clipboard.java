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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.carewebframework.web.component.BaseComponent;
import org.carewebframework.web.event.Event;
import org.carewebframework.web.event.EventUtil;

/**
 * Emulates a clipboard on the server.
 */
public class Clipboard {
    
    public static final String ON_CLIPBOARD_CHANGE = "clipboardChange";
    
    private static final Clipboard instance = new Clipboard();
    
    private Object data;
    
    private final List<BaseComponent> listeners = Collections.synchronizedList(new ArrayList<>());
    
    public static Clipboard getInstance() {
        return instance;
    }
    
    private Clipboard() {
    }
    
    public void clear() {
        copy(null);
    }
    
    public Object getData() {
        return data;
    }
    
    public void copy(Object data) {
        if (this.data != data) {
            this.data = data;
            fireChange();
        }
    }
    
    public void view() {
        ClipboardViewer.execute(this);
    }
    
    public boolean isEmpty() {
        return data == null;
    }
    
    public void addListener(BaseComponent listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }
    
    public void removeListener(BaseComponent listener) {
        listeners.remove(listener);
    }
    
    private void fireChange() {
        for (BaseComponent comp : new ArrayList<>(listeners)) {
            EventUtil.post(new Event(ON_CLIPBOARD_CHANGE, comp, data));
        }
    }
}
