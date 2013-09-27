/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.ui.zk;

import java.io.IOException;
import java.util.HashMap;

import org.carewebframework.api.event.EventManager;
import org.carewebframework.api.event.IGenericEvent;
import org.carewebframework.common.StrUtil;

import org.zkoss.zk.au.out.AuInvoke;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.sys.ContentRenderer;
import org.zkoss.zul.impl.XulElement;

/**
 * Supports slide-down style message window.
 */
public class MessageWindow extends XulElement {
    
    private static final long serialVersionUID = 1L;
    
    public static final String EVENT = "CAREWEB.INFO";
    
    private final IGenericEvent<Object> messageWindowListener = new IGenericEvent<Object>() {
        
        @Override
        public void eventCallback(String eventName, Object eventData) {
            if (eventData instanceof MessageInfo) {
                show((MessageInfo) eventData);
            } else {
                show(eventData.toString());
            }
        }
        
    };
    
    /**
     * Packages parameters for delivery to widget.
     */
    public static class MessageInfo {
        
        private final HashMap<String, Object> map = new HashMap<String, Object>();
        
        public MessageInfo(String message, String color, Integer duration, String tag) {
            map.put("message", StrUtil.formatMessage(message));
            map.put("color", color);
            map.put("duration", duration);
            map.put("tag", tag);
        }
    }
    
    /**
     * Default duration to show message in ms.
     */
    private int _duration = 8000;
    
    public MessageWindow() {
        super();
    }
    
    @Override
    public void onPageAttached(Page newpage, Page oldpage) {
        super.onPageAttached(newpage, oldpage);
        EventManager.getInstance().subscribe(EVENT, messageWindowListener);
    }
    
    @Override
    public void onPageDetached(Page page) {
        super.onPageDetached(page);
        EventManager.getInstance().unsubscribe(EVENT, messageWindowListener);
    }
    
    /**
     * Returns default message duration.
     * 
     * @return Message duration (in milliseconds).
     */
    public int getDuration() {
        return _duration;
    }
    
    /**
     * Sets default message duration.
     * 
     * @param duration Message duration (in milliseconds).
     */
    public void setDuration(int duration) {
        if (duration <= 0) {
            throw new IllegalArgumentException();
        }
        
        if (duration != _duration) {
            _duration = duration;
            smartUpdate("duration", _duration);
        }
    }
    
    /**
     * Displays a message with default color and duration.
     * 
     * @param message Message text. If begins with &lt;html&gt; tag, is interpreted as html.
     */
    public void show(String message) {
        show(message, null, null, null);
    }
    
    /**
     * Displays a message with default duration.
     * 
     * @param message Message text. If begins with &lt;html&gt; tag, is interpreted as html.
     * @param color Background color (html format).
     */
    public void show(String message, String color) {
        show(message, color, null, null);
    }
    
    /**
     * Displays a message.
     * 
     * @param message Message text. If begins with &lt;html&gt; tag, is interpreted as html.
     * @param color Background color (html format). Null means default color.
     * @param duration Message duration (in milliseconds). A nonpositive value means default
     *            duration.
     */
    public void show(String message, String color, int duration) {
        show(message, color, duration <= 0 ? null : new Integer(duration), null);
    }
    
    /**
     * Displays a message.
     * 
     * @param message Message text. If begins with &lt;html&gt; tag, is interpreted as html.
     * @param color Background color (html format). Null means default color.
     * @param duration Message duration (in milliseconds). Null means default duration.
     * @param tag Tag to classify message for selective deletion. May be null.
     */
    public void show(String message, String color, Integer duration, String tag) {
        show(new MessageInfo(message, color, duration, tag));
    }
    
    /**
     * Displays a message.
     * 
     * @param info A MessageInfo object.
     */
    public void show(MessageInfo info) {
        invoke("_show", info.map);
    }
    
    /**
     * Clears all messages.
     */
    public void clear() {
        clear(null);
    }
    
    /**
     * Clears messages with the specified tag.
     * 
     * @param tag Messages with this tag will be cleared. If null, all messages are cleared.
     */
    public void clear(String tag) {
        invoke("_clear", tag);
    }
    
    /**
     * Invokes the specified widget function with the provided argument.
     * 
     * @param func Widget function name.
     * @param arg Function argument (may be null);
     */
    private void invoke(String func, Object arg) {
        response(new AuInvoke(this, func, arg));
    }
    
    @Override
    public String getWidgetClass() {
        return "cwf.ext.MessageWindow";
    }
    
    @Override
    public String getZclass() {
        String zclass = super.getZclass();
        return zclass == null ? "cwf-messagewindow" : zclass;
    }
    
    @Override
    protected void renderProperties(ContentRenderer renderer) throws IOException {
        super.renderProperties(renderer);
        renderer.render("duration", _duration);
    }
}
