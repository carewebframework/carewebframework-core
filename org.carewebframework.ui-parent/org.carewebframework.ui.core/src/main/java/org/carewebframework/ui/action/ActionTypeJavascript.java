package org.carewebframework.ui.action;

import org.carewebframework.web.client.ClientUtil;

public class ActionTypeJavascript extends ActionTypeBase {
    
    public ActionTypeJavascript() {
        super("javascript", "^j(ava)?script:.*");
    }
    
    @Override
    public Object parse(String script) {
        return stripPrefix(script);
    }
    
    @Override
    public void execute(Object script) {
        ClientUtil.eval((String) script);
    }
    
}
