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

import org.carewebframework.api.security.SecurityUtil;
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
    protected void init(Object target, PropertyInfo propInfo, PropertyGrid propGrid) {
        propInfo.getConfig().setProperty("readonly", Boolean.toString(!SecurityUtil.hasDebugRole()));
        super.init(target, propInfo, propGrid);
        List<ActionEntry> actions = new ArrayList<>(ActionRegistry.getRegisteredActions(ActionScope.BOTH));
        Collections.sort(actions);

        for (ActionEntry action : actions) {
            appendItem(action.toString(), action.getId());
        }
    }
}
