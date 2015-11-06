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

import org.carewebframework.api.security.SecurityUtil;
import org.carewebframework.shell.layout.UIElementBase;
import org.carewebframework.shell.property.PropertyInfo;
import org.carewebframework.ui.action.ActionEntry;
import org.carewebframework.ui.action.ActionRegistry;
import org.carewebframework.ui.action.ActionRegistry.ActionScope;

/**
 * Editor for actions.
 */
public class PropertyEditorAction extends PropertyEditorList {
    
    /**
     * Initialize the list from the action registry.
     */
    @Override
    protected void init(UIElementBase target, PropertyInfo propInfo, PropertyGrid propGrid) {
        propInfo.getConfig().setProperty("readonly", Boolean.toString(!SecurityUtil.hasDebugRole()));
        super.init(target, propInfo, propGrid);
        List<ActionEntry> actions = new ArrayList<>(ActionRegistry.getRegisteredActions(ActionScope.BOTH));
        Collections.sort(actions);
        
        for (ActionEntry action : actions) {
            appendItem(action.toString(), action.getId());
        }
    }
}
