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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.carewebframework.ui.zk.ZKUtil;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.event.Event;

/**
 * Emulates a clipboard on the server. Clipboard is shared at the session level.
 */
public class Clipboard {
    
    private static final String CLIPBOARD = DesignConstants.RESOURCE_PREFIX + "Clipboard";
    
    public static final String ON_CLIPBOARD_CHANGE = "onClipboardChange";
    
    private Object data;
    
    private final List<Component> listeners = Collections.synchronizedList(new ArrayList<Component>());
    
    public static Clipboard getInstance() {
        Clipboard clipboard = (Clipboard) Sessions.getCurrent().getAttribute(CLIPBOARD);
        
        if (clipboard == null) {
            clipboard = new Clipboard();
            Sessions.getCurrent().setAttribute(CLIPBOARD, clipboard);
        }
        
        return clipboard;
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
    
    public void view() throws Exception {
        ClipboardViewer.execute(this);
    }
    
    public boolean isEmpty() {
        return data == null;
    }
    
    public void addListener(Component listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }
    
    public void removeListener(Component listener) {
        listeners.remove(listener);
    }
    
    private void fireChange() {
        for (Component comp : new ArrayList<Component>(listeners)) {
            ZKUtil.fireEvent(new Event(ON_CLIPBOARD_CHANGE, comp, data));
        }
    }
}
