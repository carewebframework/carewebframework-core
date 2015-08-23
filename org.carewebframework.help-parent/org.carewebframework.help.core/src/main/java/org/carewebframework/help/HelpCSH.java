/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.help;

import org.carewebframework.ui.command.CommandUtil;

import org.zkoss.zk.ui.Component;
import org.zkoss.zul.impl.XulElement;

/**
 * Utility methods supporting context sensitive help.
 */
public class HelpCSH {
    
    private static final String HELP_PREFIX = HelpCSH.class.getPackage().getName() + ".";
    
    private static final String CSH_TARGET = HELP_PREFIX + "target";
    
    /**
     * Show help for the given module and optional topic.
     * 
     * @param module The id of the help module.
     * @param topic The id of the desired topic. If null, the home topic is shown.
     * @param label The label to display for the topic. If null, the topic id is displayed as the
     *            label.
     */
    public static void show(String module, String topic, String label) {
        show(new HelpContext(module, topic, label));
    }
    
    /**
     * Show help for the given help target.
     * 
     * @param target The help target.
     */
    public static void show(HelpContext target) {
        HelpModule dx = HelpModuleRegistry.getInstance().get(target.module);
        IHelpSet hs = dx == null ? null : HelpSetCache.getInstance().get(dx);
        
        if (hs != null) {
            String label = target.label == null && target.topic == null ? dx.getTitle() : target.label;
            IHelpViewer viewer = HelpUtil.getViewer();
            viewer.mergeHelpSet(hs);
            viewer.show(hs, target.topic, label);
        }
    }
    
    /**
     * Displays the help viewer's table of contents.
     */
    public static void showTOC() {
        HelpUtil.getViewer().show(HelpViewType.TOC);
    }
    
    /**
     * Display context-sensitive help associated with the specified component. If none is associated
     * with the component, its parent is examined, and so on. If no help association is found, no
     * action is taken.
     * 
     * @param component Component whose CSH is to be displayed.
     */
    public static void showCSH(Component component) {
        while (component != null) {
            HelpContext target = (HelpContext) component.getAttribute(CSH_TARGET);
            
            if (target != null) {
                show(target);
                break;
            }
            component = component.getParent();
        }
    }
    
    /**
     * Associates context-sensitive help topic with a component. Any existing association is
     * replaced.
     * 
     * @param component Component to be associated.
     * @param helpContext The help target.
     * @param commandTarget The command target.
     */
    public static void associateCSH(XulElement component, HelpContext helpContext, Component commandTarget) {
        if (component != null) {
            component.setAttribute(CSH_TARGET, helpContext);
            CommandUtil.associateCommand("help", component, commandTarget);
        }
    }
    
    /**
     * Dissociates context-sensitive help from a component.
     * 
     * @param component Component to dissociate.
     */
    public static void dissociate(XulElement component) {
        if (component != null && component.hasAttribute(CSH_TARGET)) {
            CommandUtil.dissociateCommand("help", component);
            component.removeAttribute(CSH_TARGET);
        }
    }
    
    /**
     * Enforces static class.
     */
    private HelpCSH() {
    };
}
