package org.carewebframework.ui.action;

import org.carewebframework.common.MiscUtil;
import org.carewebframework.web.script.GroovyScript;

import groovy.lang.Script;

/**
 * Action type is a Groovy script.
 */
public class ActionTypeGroovy extends ActionTypeBase {
    
    public ActionTypeGroovy() {
        super("groovy", "^groovy:.*");
    }
    
    @Override
    public Script parse(String script) {
        try {
            return GroovyScript.getGroovyShell().parse(stripPrefix(script));
        } catch (Exception e) {
            throw MiscUtil.toUnchecked(e);
        }
    }
    
    @Override
    public void execute(Object script) {
        ((Script) script).run();
    }
    
}
