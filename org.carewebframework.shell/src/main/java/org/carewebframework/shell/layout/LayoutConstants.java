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
package org.carewebframework.shell.layout;

import org.carewebframework.shell.Constants;
import org.carewebframework.ui.util.CWFUtil;

/**
 * Package-wide constants.
 */
public class LayoutConstants {
    
    public static final String RESOURCE_PREFIX = CWFUtil.getResourcePath(LayoutConstants.class);
    
    public static final String PATH_DELIMITER = "\\\\";
    
    protected static final String PROPERTY_LAYOUT_SHARED = "CAREWEB.LAYOUT.SHARED";
    
    protected static final String PROPERTY_LAYOUT_PRIVATE = "CAREWEB.LAYOUT.PRIVATE";
    
    protected static final String PROPERTY_LAYOUT_ASSOCIATION = "CAREWEB.LAYOUT.ASSOCIATION";
    
    public static final String EVENT_ELEMENT_ACTIVATE = Constants.EVENT_PREFIX + ".ELEMENT.ACTIVATE";
    
    public static final String EVENT_ELEMENT_INACTIVATE = Constants.EVENT_PREFIX + ".ELEMENT.INACTIVATE";
    
    /**
     * Enforce static class.
     */
    private LayoutConstants() {
    }
}
