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
package org.carewebframework.shell.elements;

import java.util.HashMap;

import org.carewebframework.shell.plugins.PluginDefinition;
import org.carewebframework.shell.property.IPropertyAccessor;
import org.carewebframework.shell.property.PropertyInfo;

/**
 * Proxy for an arbitrary UI element that can store and return property values. This is used by the
 * designer to create placeholders for actual UI elements without creating the element itself and
 * for deferring property changes to existing UI elements.
 */
public class ElementProxy extends ElementBase implements IPropertyAccessor {

    private final HashMap<String, Object> properties = new HashMap<>();

    private ElementBase target;

    private boolean deleted;

    public ElementProxy(PluginDefinition def) {
        super();
        setDefinition(def);
        revert();
    }

    public ElementProxy(ElementBase target) {
        super();
        this.target = target;

        if (target != null) {
            setDefinition(target.getDefinition());
        }

        revert();
    }

    /**
     * Override to get property value from proxy's property cache.
     *
     * @see org.carewebframework.shell.property.IPropertyAccessor#getPropertyValue
     */
    @Override
    public Object getPropertyValue(PropertyInfo propInfo) throws Exception {
        return getPropertyValue(propInfo.getId());
    }

    public Object getPropertyValue(String propName) {
        return properties.get(propName);
    }

    /**
     * Overridden to set property value in proxy's property cache.
     *
     * @see org.carewebframework.shell.property.IPropertyAccessor#setPropertyValue
     */
    @Override
    public void setPropertyValue(PropertyInfo propInfo, Object value) {
        setPropertyValue(propInfo.getId(), value);
    }

    public Object setPropertyValue(String propName, Object value) {
        return properties.put(propName, value);
    }

    public ElementBase getTarget() {
        return target;
    }

    protected void revert() {
        properties.clear();
        syncProperties(true);
    }

    public void commit() {
        syncProperties(false);
    }

    /**
     * Realizes the creation or destruction of the proxied target. In other words, if this is a
     * deletion operation and a target exists, the target is removed from its parent. If this is not
     * a deletion and the target does not exist, a new target is instantiated as a child to the
     * specified parent.
     *
     * @param parent The parent UI element.
     * @throws Exception Unspecified exception.
     */
    public void realize(ElementBase parent) throws Exception {
        if (!deleted && target == null) {
            target = getDefinition().createElement(parent, null, false);
        } else if (deleted && target != null) {
            target.remove(true);
            target = null;
        }
    }

    /**
     * Synchronizes property values between the proxy and its target.
     *
     * @param fromTarget If true, property values are copied from the target to the proxy. If false,
     *            property values are copied from the proxy to the target.
     */
    private void syncProperties(boolean fromTarget) {
        PluginDefinition def = getDefinition();

        for (PropertyInfo propInfo : def.getProperties()) {
            if (fromTarget) {
                syncProperty(propInfo, target, this);
            } else {
                syncProperty(propInfo, this, target);
            }
        }
    }

    private void syncProperty(PropertyInfo propInfo, Object from, Object to) {
        if (to != null) {
            propInfo.setPropertyValue(to, propInfo.getPropertyValue(from));
        }
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public boolean isDeleted() {
        return deleted;
    }
}
