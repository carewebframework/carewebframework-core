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

import org.carewebframework.api.FrameworkUtil;
import org.carewebframework.api.ManifestIterator;
import org.carewebframework.help.HelpContext;
import org.carewebframework.help.viewer.HelpUtil;
import org.carewebframework.ui.action.ActionRegistry;
import org.carewebframework.web.component.BaseUIComponent;

/**
 * Static utility methods.
 */
public class CareWebUtil {
    
    private static final String ACTION_BASE = "groovy:" + CareWebUtil.class.getName() + ".getShell().";
    
    static {
        ActionRegistry.register(true, "cwf.shell.lock", "@cwf.shell.action.lock.label", ACTION_BASE + "lock();");
        ActionRegistry.register(true, "cwf.shell.logout", "@cwf.shell.action.logout.label", ACTION_BASE + "logout();");
    }
    
    /**
     * Returns the active instance of the CareWeb shell.
     * 
     * @return The shell instance.
     */
    public static CareWebShell getShell() {
        return (CareWebShell) FrameworkUtil.getAttribute(Constants.SHELL_INSTANCE);
    }
    
    /**
     * Sets the active instance of the CareWeb shell. An exception is raised if an active instance
     * has already been set.
     * 
     * @param shell Shell to become the active instance.
     */
    protected static void setShell(CareWebShell shell) {
        if (getShell() != null) {
            throw new RuntimeException("A CareWeb shell instance has already been registered.");
        }
        
        FrameworkUtil.setAttribute(Constants.SHELL_INSTANCE, shell);
    }
    
    /**
     * Displays the About dialog.
     */
    public static void about() {
        AboutDialog.execute(ManifestIterator.getInstance().getPrimaryManifest());
    }
    
    /**
     * Sends an informational message for display by desktop.
     * 
     * @param message Text of the message.
     * @param caption Optional caption text.
     */
    public static void showMessage(String message, String caption) {
        getShell().getMessageWindow().showMessage(message, caption);
    }
    
    /**
     * Sends an informational message for display by desktop.
     * 
     * @param message Text of the message.
     */
    public static void showMessage(String message) {
        showMessage(message, null);
    }
    
    /**
     * Associates help context with a component.
     * 
     * @param component The component.
     * @param module The help module identifier.
     * @param topic The topic id.
     * @param label The topic label.
     */
    public static void associateCSH(BaseUIComponent component, String module, String topic, String label) {
        HelpContext context = new HelpContext(module, topic, label);
        HelpUtil.associateCSH(component, context, getShell());
    }
    
    /**
     * Associates help context with a component.
     * 
     * @param component The component.
     * @param context The help context.
     */
    public static void associateCSH(BaseUIComponent component, HelpContext context) {
        HelpUtil.associateCSH(component, context, getShell());
    }
    
    /**
     * Enforce static class.
     */
    private CareWebUtil() {
    }
}
