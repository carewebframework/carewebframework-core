package org.carewebframework.ui.action;

import org.fujion.client.ClientUtil;

public class ActionTypeJavascript extends ActionTypeBase<String> {

    public ActionTypeJavascript() {
        super("javascript", "^j(ava)?script:.*");
    }

    @Override
    public String parse(String script) {
        return stripPrefix(script);
    }

    @Override
    public void execute(String script) {
        ClientUtil.eval(script);
    }

}
