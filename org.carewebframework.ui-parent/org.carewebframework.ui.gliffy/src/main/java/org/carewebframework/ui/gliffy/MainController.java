/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.ui.gliffy;

import org.apache.commons.lang.StringUtils;

import org.carewebframework.shell.plugins.PluginContainer;
import org.carewebframework.shell.plugins.PluginController;

import org.zkoss.util.media.AMedia;
import org.zkoss.util.media.Media;
import org.zkoss.zk.ui.Component;
import org.zkoss.zul.Iframe;

/**
 * Simple component to display gliffy-based wireframes.
 */
public class MainController extends PluginController {
    
    private static final long serialVersionUID = 1L;
    
    private static final String GLIFFY_DATA = "<script src='http://www.gliffy.com/diagramEmbed.js' "
            + "type='text/javascript'> </script><script type='text/javascript'> gliffy_did = '%s'; "
            + "embedGliffy(); </script>";
    
    private Iframe iframe;
    
    private String gliffyId;
    
    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
    }
    
    @Override
    public void onLoad(PluginContainer container) {
        super.onLoad(container);
        container.registerProperties(this, "gliffyId");
    }
    
    @Override
    public void refresh() {
        if (gliffyId == null) {
            iframe.setContent(null);
            return;
        }
        
        String data = String.format(GLIFFY_DATA, gliffyId);
        Media media = new AMedia(null, null, "text/html", data);
        iframe.setContent(media);
    }
    
    public String getGliffyId() {
        return gliffyId;
    }
    
    public void setGliffyId(String gliffyId) {
        this.gliffyId = StringUtils.trimToNull(gliffyId);
        refresh();
    }
    
}
