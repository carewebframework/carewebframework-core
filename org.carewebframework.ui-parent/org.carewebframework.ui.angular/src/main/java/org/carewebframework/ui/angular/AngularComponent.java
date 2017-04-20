package org.carewebframework.ui.angular;

import java.io.IOException;

import org.apache.commons.lang.StringUtils;
import org.zkoss.zk.ui.HtmlBasedComponent;
import org.zkoss.zk.ui.sys.ContentRenderer;

public class AngularComponent extends HtmlBasedComponent {
    
    private static final long serialVersionUID = 1L;

    private String src;

    @Override
    public void renderProperties(ContentRenderer renderer) throws IOException {
        super.renderProperties(renderer);
        renderer.render("src", src);
    }

    @Override
    public String getZclass() {
        return _zclass == null ? "cwf-angular" : _zclass;
    }

    public String getSrc() {
        return src;
    }

    public void setSrc(String src) {
        src = StringUtils.trimToNull(src);
        
        if (!StringUtils.equals(src, this.src)) {
            smartUpdate("src", this.src = src);
        }
    }

}
