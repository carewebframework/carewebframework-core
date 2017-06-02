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
package org.carewebframework.plugin.currentdatetime;

import java.util.Calendar;

import org.apache.commons.lang.time.FastDateFormat;
import org.carewebframework.common.DateUtil;
import org.carewebframework.shell.elements.ElementPlugin;
import org.carewebframework.shell.plugins.PluginController;
import org.carewebframework.web.annotation.EventHandler;
import org.carewebframework.web.annotation.WiredComponent;
import org.carewebframework.web.component.BaseComponent;
import org.carewebframework.web.component.Label;
import org.carewebframework.web.component.Timer;

/**
 * Simple component to display the current date and time.
 */
public class MainController extends PluginController {
    
    @WiredComponent
    private Label lblCurrentTime;
    
    @WiredComponent
    private Timer timer;
    
    private String format;
    
    private String color;
    
    private FastDateFormat formatter;
    
    @Override
    public void afterInitialized(BaseComponent comp) {
        super.afterInitialized(comp);
        timer.start();
    }
    
    @EventHandler(value = "timer", target = "@timer")
    public void onTimer() {
        updateTime();
    }
    
    private void updateTime() {
        Calendar cal = Calendar.getInstance(DateUtil.getLocalTimeZone());
        lblCurrentTime.setLabel(formatter == null ? "" : formatter.format(cal));
    }
    
    public String getFormat() {
        return format;
    }
    
    public void setFormat(String format) {
        formatter = FastDateFormat.getInstance(format);
        this.format = format;
        updateTime();
    }
    
    public String getColor() {
        return color;
    }
    
    public void setColor(String color) {
        this.color = color;
        lblCurrentTime.addStyle("color", color);
    }
    
    @Override
    public void onLoad(ElementPlugin plugin) {
        super.onLoad(plugin);
        plugin.registerProperties(this, "format", "color");
    }
    
}
