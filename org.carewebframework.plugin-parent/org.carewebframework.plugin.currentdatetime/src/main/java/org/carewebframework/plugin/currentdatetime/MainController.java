/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.plugin.currentdatetime;

import java.util.Calendar;

import org.apache.commons.lang.time.FastDateFormat;

import org.carewebframework.common.DateUtil;
import org.carewebframework.shell.plugins.PluginContainer;
import org.carewebframework.shell.plugins.PluginController;
import org.carewebframework.ui.zk.ZKUtil;

import org.zkoss.zk.ui.Component;
import org.zkoss.zul.Label;
import org.zkoss.zul.Timer;

/**
 * Simple component to display the current date and time.
 */
public class MainController extends PluginController {
    
    private static final long serialVersionUID = 1L;
    
    private Label lblCurrentTime;
    
    private Timer timer;
    
    private String format;
    
    private String color;
    
    private FastDateFormat formatter;
    
    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        timer.start();
    }
    
    public void onTimer$timer() {
        updateTime();
    }
    
    private void updateTime() {
        Calendar cal = Calendar.getInstance(DateUtil.getLocalTimeZone());
        lblCurrentTime.setValue(formatter == null ? "" : formatter.format(cal));
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
        ZKUtil.updateStyle(lblCurrentTime, "color", color);
    }
    
    @Override
    public void onLoad(PluginContainer container) {
        super.onLoad(container);
        container.registerProperties(this, "format", "color");
    }
    
}
