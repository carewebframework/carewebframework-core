package org.carewebframework.ui.action;

import org.fujion.script.IScriptLanguage;
import org.fujion.script.IScriptLanguage.IParsedScript;
import org.fujion.script.ScriptRegistry;
import org.springframework.util.Assert;

/**
 * Action type is a server-side script.
 */
public class ActionTypeServerScript extends ActionTypeBase<IParsedScript> {
    
    public ActionTypeServerScript() {
        super("sscript", null);
    }
    
    @Override
    public boolean matches(String script) {
        return ScriptRegistry.getInstance().get(getType(script)) != null;
    }
    
    @Override
    public IParsedScript parse(String script) {
        String lang = getType(script);
        IScriptLanguage language = ScriptRegistry.getInstance().get(lang);
        Assert.notNull(language, "Unknown script language: " + lang);
        script = stripPrefix(script);
        return language.parse(script);
    }
    
    @Override
    public void execute(IParsedScript script) {
        script.run();
    }
    
}
