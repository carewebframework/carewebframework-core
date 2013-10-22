/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.shell.help;

import org.carewebframework.help.HelpViewType;
import org.carewebframework.help.IHelpSet;
import org.carewebframework.help.IHelpViewer;
import org.carewebframework.shell.CareWebUtil;
import org.carewebframework.ui.command.CommandUtil;

import org.zkoss.zk.ui.Component;
import org.zkoss.zul.impl.XulElement;

/**
 * Utility methods for help subsystem.
 */
public class HelpUtil {
    
    private static final String HELP_PREFIX = HelpUtil.class.getPackage().getName() + ".";
    
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
        show(new HelpTarget(module, topic, label));
    }
    
    /**
     * Show help for the given help target.
     * 
     * @param target The help target.
     */
    public static void show(HelpTarget target) {
        HelpDefinition def = HelpRegistry.getInstance().get(target.module);
        IHelpSet hs = def == null ? null : def.getHelpSet();
        
        if (hs != null) {
            String label = target.label == null && target.topic == null ? def.getTitle() : target.label;
            IHelpViewer viewer = getViewer();
            viewer.mergeHelpSet(hs);
            viewer.show(hs, target.topic, label);
        }
    }
    
    /**
     * Returns a reference to the help viewer or its proxy.
     * 
     * @return The viewer or its proxy.
     */
    public static IHelpViewer getViewer() {
        return org.carewebframework.help.HelpUtil.getViewer();
    }
    
    /**
     * Displays the help viewer's table of contents.
     */
    public static void showTOC() {
        getViewer().show(HelpViewType.TOC);
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
            HelpTarget target = (HelpTarget) component.getAttribute(CSH_TARGET);
            
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
     * @param module Module id of the help set.
     * @param topic Topic id of the topic.
     * @param label Label to be associated with the topic (may be null).
     */
    public static void associateCSH(XulElement component, String module, String topic, String label) {
        associateCSH(component, new HelpTarget(module, topic, label));
    }
    
    /**
     * Associates context-sensitive help topic with a component. Any existing association is
     * replaced.
     * 
     * @param component Component to be associated.
     * @param target The help target.
     */
    public static void associateCSH(XulElement component, HelpTarget target) {
        if (component != null) {
            component.setAttribute(CSH_TARGET, target);
            CommandUtil.associateCommand("help", component, CareWebUtil.getShell());
        }
    }
    
    /**
     * Dissociates context-sensitive help from a component.
     * 
     * @param component
     */
    public static void dissociate(XulElement component) {
        if (component != null && component.hasAttribute(CSH_TARGET)) {
            CommandUtil.disassociateCommand("help", component);
            component.removeAttribute(CSH_TARGET);
        }
    }
    
    /**
     * Enforces static class.
     */
    private HelpUtil() {
    };
}
