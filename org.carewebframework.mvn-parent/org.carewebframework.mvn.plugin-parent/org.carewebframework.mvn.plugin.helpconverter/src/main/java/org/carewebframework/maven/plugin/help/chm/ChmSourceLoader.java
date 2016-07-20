/*
 * #%L
 * carewebframework
 * %%
 * Copyright (C) 2008 - 2016 Regenstrief Institute, Inc.
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related
 * Additional Disclaimer of Warranty and Limitation of Liability available at
 *
 *      http://www.carewebframework.org/licensing/disclaimer.
 *
 * #L%
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
