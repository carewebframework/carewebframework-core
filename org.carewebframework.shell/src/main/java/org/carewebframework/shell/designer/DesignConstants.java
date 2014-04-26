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

import org.carewebframework.shell.Constants;
import org.carewebframework.ui.zk.ZKUtil;

public class DesignConstants {
    
    public static final String RESOURCE_PREFIX = ZKUtil.getResourcePath(DesignConstants.class);
    
    public static final String MSG_LAYOUT_DUP = "@cwf.shell.designer.layout.duplicate.message";
    
    public static final String MSG_LAYOUT_BADNAME = "@cwf.shell.designer.layout.badname.message";
    
    public static final String MSG_LAYOUT_OVERWRITE = "@cwf.shell.designer.layout.overwrite.message";
    
    public static final String CAP_LAYOUT_OVERWRITE = "@cwf.shell.designer.layout.overwrite.caption";
    
    public static final String MSG_LAYOUT_CLONE = "@cwf.shell.designer.layout.clone.message";
    
    public static final String CAP_LAYOUT_CLONE = "@cwf.shell.designer.layout.clone.caption";
    
    public static final String MSG_LAYOUT_RENAME = "@cwf.shell.designer.layout.rename.message";
    
    public static final String CAP_LAYOUT_RENAME = "@cwf.shell.designer.layout.rename.caption";
    
    public static final String MSG_DESKTOP_CLEAR = "@cwf.shell.designer.desktop.clear.message";
    
    public static final String CAP_DESKTOP_CLEAR = "@cwf.shell.designer.desktop.clear.caption";
    
    public static final String MSG_LAYOUT_SAVE = "@cwf.shell.designer.layout.save.message";
    
    public static final String CAP_LAYOUT_SAVE = "@cwf.shell.designer.layout.save.caption";
    
    public static final String MSG_LAYOUT_LOAD = "@cwf.shell.designer.layout.load.message";
    
    public static final String CAP_LAYOUT_LOAD = "@cwf.shell.designer.layout.load.caption";
    
    public static final String MSG_LAYOUT_MANAGE = "@cwf.shell.designer.layout.manage.message";
    
    public static final String CAP_LAYOUT_MANAGE = "@cwf.shell.designer.layout.manage.caption";
    
    public static final String MSG_LAYOUT_DELETE = "@cwf.shell.designer.layout.delete.message";
    
    public static final String MSG_LAYOUT_IMPORT = "@cwf.shell.designer.layout.import_.message";
    
    public static final String CAP_LAYOUT_IMPORT = "@cwf.shell.designer.layout.import_.caption";
    
    public static final String ERR_LAYOUT_IMPORT = "@cwf.shell.designer.layout.import_.error.bad";
    
    public static final String DESIGN_HINT_ACTIVE = "@cwf.shell.designer.designmode.active.hint";
    
    public static final String DESIGN_HINT_INACTIVE = "@cwf.shell.designer.designmode.inactive.hint";
    
    public static final String ATTR_DESIGN_MENU = RESOURCE_PREFIX + "DesignMenu";
    
    public static final String DESIGN_ICON_INACTIVE = Constants.IMAGE_PATH + "designOff.png";
    
    public static final String DESIGN_ICON_ACTIVE = Constants.IMAGE_PATH + "designOn.png";;
    
    public static final String DESIGN_MODE_PRIVS = "PRIV_CAREWEB_DESIGNER";
    
    /**
     * Enforce static class.
     */
    private DesignConstants() {
    };
}
