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
package org.carewebframework.ui.angular;

import java.io.IOException;

import org.apache.commons.lang.StringUtils;
import org.zkoss.zk.ui.HtmlBasedComponent;
import org.zkoss.zk.ui.sys.ContentRenderer;

/**
 * Container for hosting an Angular 2 component.
 */
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

    /**
     * Returns the source module containing the Angular 2 component.
     *
     * @return The source module containing the Angular 2 component.
     */
    public String getSrc() {
        return src;
    }

    /**
     * Sets the source module containing the Angular 2 component.
     *
     * @param src The source module containing the Angular 2 component.
     */
    public void setSrc(String src) {
        src = StringUtils.trimToNull(src);
        
        if (!StringUtils.equals(src, this.src)) {
            smartUpdate("src", this.src = src);
        }
    }

}
