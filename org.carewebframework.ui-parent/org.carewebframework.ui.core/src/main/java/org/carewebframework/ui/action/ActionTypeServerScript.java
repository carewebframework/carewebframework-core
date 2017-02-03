package org.carewebframework.ui.action;

import org.carewebframework.web.script.IScriptLanguage;
import org.carewebframework.web.script.IScriptLanguage.IParsedScript;
import org.carewebframework.web.script.ScriptRegistry;
import org.springframework.util.Assert;

/**
 * Action type is a server-side script.
 */
public class ActionTypeServerScript extends ActionTypeBase {
    
    public ActionTypeServerScript() {
        super("sscript", "^sscript\\-.*:.*");
    }
    
    @Override
    public IParsedScript parse(String script) {
        String lang = getType(script);
        lang = lang.substring(lang.indexOf('-') + 1);
        IScriptLanguage language = ScriptRegistry.getInstance().get(lang);
        Assert.notNull(language, "Unknown script language: " + lang);
        script = stripPrefix(script);
        return language.parse(script);
    }
    
    @Override
    public void execute(Object script) {
        ((IParsedScript) script).run();
    }
    
}
