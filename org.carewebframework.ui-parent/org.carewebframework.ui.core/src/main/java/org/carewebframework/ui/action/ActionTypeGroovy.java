package org.carewebframework.ui.action;

import org.carewebframework.common.MiscUtil;

import groovy.lang.GroovyShell;
import groovy.lang.Script;

/**
 * Action type is a Groovy script.
 */
public class ActionTypeGroovy extends ActionTypeBase {
    
    private static volatile boolean initialized;
    
    private static GroovyShell shell;
    
    private static synchronized void initialize() {
        if (!initialized) {
            try {
                shell = new GroovyShell();
                initialized = true;
            } catch (Exception e) {
                throw MiscUtil.toUnchecked(e);
            }
        }
    }
    
    public ActionTypeGroovy() {
        super("groovy", "^groovy:.*");
    }
    
    @Override
    public Script parse(String script) {
        init();
        
        try {
            return shell.parse(stripPrefix(script));
        } catch (Exception e) {
            throw MiscUtil.toUnchecked(e);
        }
    }
    
    @Override
    public void execute(Object script) {
        init();
        ((Script) script).run();
    }
    
    private void init() {
        if (!initialized) {
            initialize();
        }
    }
}
