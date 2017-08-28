package org.carewebframework.ui.action;

import org.fujion.client.ClientUtil;

/**
 * URL action type causes navigation to URL in a separate viewport.
 */
public class ActionTypeUrl extends ActionTypeBase<String> {

    public ActionTypeUrl() {
        super("url", "^https?:.*");
    }

    @Override
    public String parse(String script) {
        return script;
    }

    @Override
    public void execute(String script) {
        ClientUtil.redirect(script, "_blank");
    }

}
