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

import java.util.HashMap;

import org.carewebframework.api.event.EventManager;
import org.carewebframework.api.event.IEventManager;
import org.carewebframework.api.event.IGenericEvent;
import org.carewebframework.common.StrUtil;
import org.carewebframework.web.component.BaseUIComponent;

/**
 * Supports slide-down style message window.
 */
public class MessageWindow extends BaseUIComponent {
    
    private static final String EVENT_ROOT = "CAREWEB.INFO";
    
    public static final String EVENT_SHOW = EVENT_ROOT + ".SHOW";
    
    public static final String EVENT_HIDE = EVENT_ROOT + ".HIDE";
    
    private final IGenericEvent<Object> messageWindowListener = new IGenericEvent<Object>() {
        
        @Override
        public void eventCallback(String eventName, Object eventData) {
            if (eventName.startsWith(EVENT_SHOW)) {
                if (eventData instanceof MessageInfo) {
                    show((MessageInfo) eventData);
                } else {
                    show(eventData.toString());
                }
            } else if (eventName.startsWith(EVENT_HIDE)) {
                clear((String) eventData);
            }
        }
        
    };
    
    /**
     * Packages parameters for delivery to widget.
     */
    public static class MessageInfo {
        
        private final HashMap<String, Object> map = new HashMap<>();
        
        public MessageInfo(String message, String caption, String color, Integer duration, String tag, String action) {
            map.put("message", StrUtil.formatMessage(message));
            map.put("caption", StrUtil.formatMessage(caption));
            map.put("color", color);
            map.put("duration", duration);
            map.put("tag", tag);
            map.put("action", ZKUtil.toJavaScriptValue(action));
        }
    }
    
    /**
     * Default duration to show message in ms.
     */
    private int _duration = 8000;
    
    public MessageWindow() {
        super();
        subscribe(true);
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        subscribe(false);
    }
    
    /**
     * Subscribe to/unsubscribe from {@value #EVENT_ROOT} events if event manager is available.
     * 
     * @param doSubscribe If true, subscribe. If false, unsubscribe.
     */
    private void subscribe(boolean doSubscribe) {
        IEventManager eventManager = EventManager.getInstance();
        
        if (eventManager != null) {
            if (doSubscribe) {
                eventManager.subscribe(EVENT_ROOT, messageWindowListener);
            } else {
                eventManager.unsubscribe(EVENT_ROOT, messageWindowListener);
            }
        }
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
            sync("duration", _duration);
        }
    }
    
    /**
     * Displays a message with default caption, color and duration.
     * 
     * @param message Message text. If begins with &lt;html&gt; tag, is interpreted as html.
     */
    public void show(String message) {
        show(message, null, null, null, null);
    }
    
    /**
     * Displays a message with default color and duration.
     * 
     * @param message Message text. If begins with &lt;html&gt; tag, is interpreted as html.
     * @param caption Optional caption text.
     */
    public void show(String message, String caption) {
        show(message, caption, null, null, null);
    }
    
    /**
     * Displays a message with default duration.
     * 
     * @param message Message text. If begins with &lt;html&gt; tag, is interpreted as html.
     * @param caption Optional caption text.
     * @param color Background color (html format).
     */
    public void show(String message, String caption, String color) {
        show(message, caption, color, null, null);
    }
    
    /**
     * Displays a message.
     * 
     * @param message Message text. If begins with &lt;html&gt; tag, is interpreted as html.
     * @param caption Optional caption text.
     * @param color Background color (html format). Null means default color.
     * @param duration Message duration (in milliseconds). A nonpositive value means default
     *            duration.
     */
    public void show(String message, String caption, String color, int duration) {
        show(message, caption, color, duration <= 0 ? null : new Integer(duration), null, null);
    }
    
    /**
     * Displays a message.
     * 
     * @param message Message text. If begins with &lt;html&gt; tag, is interpreted as html.
     * @param caption Optional caption text.
     * @param color Background color (html format) or Bootstrap alert style (warning, danger,
     *            success, info). Null means default color.
     * @param duration Message duration (in milliseconds). Null means default duration.
     * @param tag Tag to classify message for selective deletion. May be null.
     */
    public void show(String message, String caption, String color, Integer duration, String tag) {
        show(new MessageInfo(message, caption, color, duration, tag, null));
    }
    
    /**
     * Displays a message.
     * 
     * @param message Message text. If begins with &lt;html&gt; tag, is interpreted as html.
     * @param caption Optional caption text.
     * @param color Background color (html format) or Bootstrap alert style (warning, danger,
     *            success, info). Null means default color.
     * @param duration Message duration (in milliseconds). Null means default duration.
     * @param tag Tag to classify message for selective deletion. May be null.
     * @param action Javascript action to associate with message. Null means no action.
     */
    public void show(String message, String caption, String color, Integer duration, String tag, String action) {
        show(new MessageInfo(message, caption, color, duration, tag, action));
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
    
}
