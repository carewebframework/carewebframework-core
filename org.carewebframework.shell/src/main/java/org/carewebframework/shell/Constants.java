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
package org.carewebframework.shell;

import org.carewebframework.ui.util.CWFUtil;

/**
 * Package-wide constants.
 */
public class Constants {

    public static final String EVENT_PREFIX = "CAREWEB";

    private static final String EVENT_INFO_ROOT = EVENT_PREFIX + ".INFO";

    public static final String EVENT_INFO_SHOW = EVENT_INFO_ROOT + ".SHOW";

    public static final String EVENT_INFO_HIDE = EVENT_INFO_ROOT + ".HIDE";

    public static final String EVENT_RESOURCE_PREFIX = EVENT_PREFIX + ".RESOURCE";

    public static final String EVENT_RESOURCE_PROPGROUP_PREFIX = EVENT_RESOURCE_PREFIX + ".PROPGROUP";

    public static final String EVENT_RESOURCE_PROPGROUP_ADD = EVENT_RESOURCE_PROPGROUP_PREFIX + ".ADD";

    public static final String EVENT_RESOURCE_PROPGROUP_REMOVE = EVENT_RESOURCE_PROPGROUP_PREFIX + ".REMOVE";

    public static final String RESOURCE_PREFIX = CWFUtil.getResourcePath(Constants.class);

    public static final String ICON_PATH = "webjars/cwf-shell/icons/";

    public static final String SHELL_INSTANCE = "CAREWEB.SHELL";

    public static final String ATTR_CONTAINER = "CAREWEB.CONTAINER";

    public static final String ATTR_VISIBLE = "CAREWEB.VISIBLE";

    /**
     * Enforce static class.
     */
    private Constants() {
    }
}
