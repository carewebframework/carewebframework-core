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
        Execution exec = Executions.getCurrent();
        
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
        Execution exec = Executions.getCurrent();
        @SuppressWarnings("unchecked")
        List<Component> roots = (List<Component>) exec.getAttribute(ROOTS);
        roots.addAll(Arrays.asList(comps));
        
        //resolve only once in the last page
        if (exec.getAttribute(COMPOSITION) != this) {
            return;
        }
        
        exec.removeAttribute(COMPOSITION);
        exec.removeAttribute(ROOTS);
        
        if (!roots.isEmpty()) {
            // resolve insert components
            Map<String, Component> insertionPoints = new HashMap<>(); //(insert name, insert component)
            resolveInsertionPoints(roots, insertionPoints);
            Component root = roots.get(0);
            
            // join "define" components as children of "insert" component
            while (root != null) {
                Component nextRoot = root.getNextSibling();
                String insertionId = getAnnotationValue(root, "define");
                
                if (insertionId != null) {
                    Component insertionPoint = insertionPoints.get(insertionId);
                    
                    if (insertionPoint != null) {
                        root.setParent(insertionPoint);
                    } else {
                        root.detach(); //no where to insert
                        throw new UiException("Could not find insertion point named '" + insertionId
                                + "' referenced by Component " + root);
                    }
                }
                root = nextRoot;
            }
        }
    }
    
    /**
     * Returns the value of the named annotation, if any.
     * 
     * @param comp Component of interest.
     * @param name The name of the annotation whose value is sought.
     * @return The annotation value, or null if not present.
     */
    private String getAnnotationValue(Component comp, String name) {
        Annotation annt = ((ComponentCtrl) comp).getAnnotation(null, name);
        return annt == null ? null : annt.getAttribute("value");
    }
    
    /**
     * Build a map of all insertion points.
     * 
     * @param comps Components to search for insertion points.
     * @param map Map to receive discovered insertion points.
     */
    private void resolveInsertionPoints(Collection<Component> comps, Map<String, Component> map) {
        for (Component comp : comps) {
            String insertionId = getAnnotationValue(comp, "insert");
            
            if (insertionId != null && map.put(insertionId, comp) != null) {
                throw new UiException("Duplicate insertion point named '" + insertionId + "' at Component " + comp);
            }
            
            resolveInsertionPoints(comp.getChildren(), map); //recursive
        }
    }
}
