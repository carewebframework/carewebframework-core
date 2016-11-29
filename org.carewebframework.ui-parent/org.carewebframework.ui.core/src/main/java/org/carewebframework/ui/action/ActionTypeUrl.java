package org.carewebframework.ui.action;

import org.carewebframework.web.client.ClientUtil;

/**
 * URL action type causes navigation to URL in a separate viewport.
 */
public class ActionTypeUrl extends ActionTypeBase {
    
    public ActionTypeUrl() {
        super("url", "^https?:.*");
    }
    
    @Override
    public Object parse(String script) {
        return script;
    }
    
    @Override
    public void execute(Object script) {
        ClientUtil.redirect((String) script, "_blank");
    }
    
}
