/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.maven.plugin.help.chm;

import org.carewebframework.maven.plugin.core.BaseMojo;
import org.carewebframework.maven.plugin.help.HelpProcessor;
import org.carewebframework.maven.plugin.help.SourceLoader;

/**
 * Used where source archive is a chm file format.
 */
public class ChmSourceLoader extends SourceLoader {
    
    public ChmSourceLoader() {
        super("chm", "#SYSTEM", ChmSource.class);
    }
    
    @Override
    public void registerTransforms(HelpProcessor processor) {
        BaseMojo mojo = processor.getMojo();
        processor.registerTransform("*.htm,*.html", new HtmlTransform(mojo));
        processor.registerTransform("*.hhc", new ViewTransform(mojo, "toc"));
        processor.registerTransform("*.hhk", new ViewTransform(mojo, "index"));
        processor.registerTransform("#TOPICS", new TopicTransform(mojo));
        processor.registerTransform("#SYSTEM", new SystemTransform(mojo));
        processor.registerTransform("#*,*.lng,*.brs,*.glo,$*,Property", null);
        super.registerTransforms(processor);
    }
    
}
