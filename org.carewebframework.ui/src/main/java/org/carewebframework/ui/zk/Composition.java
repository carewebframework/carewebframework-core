/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.ui.zk;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Execution;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.UiException;
import org.zkoss.zk.ui.metainfo.Annotation;
import org.zkoss.zk.ui.sys.ComponentCtrl;
import org.zkoss.zk.ui.util.Initiator;
import org.zkoss.zk.ui.util.InitiatorExt;

/**
 * This is a variation on ZK's composition-based templating. It is more efficient and gets around
 * the requirement for insertion points to be uniquely named and always globally defined.
 */
public class Composition implements Initiator, InitiatorExt {
    
    private static final String COMPOSITION = "cwf.COMPOSITION";
    
    private static final String ROOTS = "cwf.ROOTS";
    
    @Override
    public boolean doCatch(Throwable ex) throws Exception {
        return false;
    }
    
    @Override
    public void doFinally() throws Exception {
    }
    
    @Override
    public void doInit(Page page, Map<String, Object> args) throws Exception {
        //first called doInit, last called doAfterCompose
        final Execution exec = Executions.getCurrent();
        
        if (!exec.hasAttribute(COMPOSITION)) {
            exec.setAttribute(COMPOSITION, this);
            exec.setAttribute(ROOTS, new ArrayList<Component>());
        }
        
        Object arg;
        
        for (int i = 0; (arg = args.get("arg" + i)) != null; i++) {
            exec.createComponents((String) arg, null, null);
        }
    }
    
    @Override
    public void doAfterCompose(Page page, Component[] comps) throws Exception {
        final Execution exec = Executions.getCurrent();
        @SuppressWarnings("unchecked")
        List<Component> roots = (List<Component>) exec.getAttribute(ROOTS);
        roots.addAll(Arrays.asList(comps));
        
        //resolve only once in the last page
        if (exec.getAttribute(COMPOSITION) != this) {
            return;
        }
        
        exec.removeAttribute(COMPOSITION);
        exec.removeAttribute(ROOTS);
        
        // resolve insert components
        final Map<String, Component> insertMap = new HashMap<String, Component>(); //(insert name, insert component)
        resolveInsertComponents(roots, insertMap);
        
        if (!roots.isEmpty()) {
            Component comp = roots.iterator().next();
            
            // join "define" components as children of "insert" component
            do {
                final Component nextRoot = comp.getNextSibling();
                final Annotation annt = ((ComponentCtrl) comp).getAnnotation(null, "define");
                if (annt != null) {
                    final String joinId = annt.getAttribute("value");
                    final Component insertComp = insertMap.get(joinId);
                    
                    if (insertComp != null) {
                        comp.setParent(insertComp);
                    } else {
                        comp.detach(); //no where to insert
                    }
                }
                comp = nextRoot;
            } while (comp != null);
        }
    }
    
    private void resolveInsertComponents(Collection<Component> comps, Map<String, Component> map) {
        for (Component comp : comps) {
            final Annotation annt = ((ComponentCtrl) comp).getAnnotation(null, "insert");
            
            if (annt != null) {
                final String insertName = annt.getAttribute("value");
                
                if (map.containsKey(insertName)) {
                    throw new UiException("Duplicate insert name: " + insertName + " at Component " + comp);
                }
                
                map.put(insertName, comp);
            }
            resolveInsertComponents(comp.getChildren(), map); //recursive
        }
    }
}
