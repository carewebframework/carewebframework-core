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
package org.carewebframework.ui.render;

import java.util.Collection;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.carewebframework.common.DateUtil;
import org.carewebframework.common.StrUtil;
import org.carewebframework.web.component.BaseComponent;
import org.carewebframework.web.component.BaseUIComponent;
import org.carewebframework.web.component.Image;
import org.carewebframework.web.component.Label;
import org.carewebframework.web.model.IComponentRenderer;

/**
 * Base for renderers.
 *
 * @param <T> The component type to be rendered.
 * @param <M> The model type.
 */
public abstract class AbstractRenderer<T extends BaseComponent, M> implements IComponentRenderer<T, M> {

    protected final String compStyle;

    protected final String cellStyle;

    /**
     * No args Constructor
     */
    public AbstractRenderer() {
        this(null, null);
    }

    /**
     * @param compStyle Style to be applied to each rendered component.
     * @param cellStyle Style to be applied to each cell.
     */
    public AbstractRenderer(String compStyle, String cellStyle) {
        this.compStyle = compStyle;
        this.cellStyle = cellStyle;
    }

    /**
     * Creates a label for a string value.
     *
     * @param parent BaseComponent that will be the parent of the label.
     * @param value Value to be used as label text.
     * @return The newly created label.
     */
    public Label createLabel(BaseComponent parent, Object value) {
        return createLabel(parent, value, null, null);
    }

    /**
     * Creates a label for a string value.
     *
     * @param parent BaseComponent that will be the parent of the label.
     * @param value Value to be used as label text.
     * @param prefix Value to be used as a prefix for the label text.
     * @return The newly created label.
     */
    public Label createLabel(BaseComponent parent, Object value, String prefix) {
        return createLabel(parent, value, prefix, null);
    }

    /**
     * Creates a label for a string value.
     *
     * @param parent BaseComponent that will be the parent of the label.
     * @param value Value to be used as label text.
     * @param prefix Value to be used as a prefix for the label text.
     * @param style Style to be applied to the label.
     * @return The newly created label.
     */
    public Label createLabel(BaseComponent parent, Object value, String prefix, String style) {
        return createLabel(parent, value, prefix, style, false);
    }

    /**
     * Creates a label for a string value.
     *
     * @param parent BaseComponent that will be the parent of the label.
     * @param value Value to be used as label text.
     * @param prefix Value to be used as a prefix for the label text.
     * @param style Style to be applied to the label.
     * @param asFirst If true, the label is prepended to the parent. If false, it is appended.
     * @return The newly created label.
     */
    public Label createLabel(BaseComponent parent, Object value, String prefix, String style, boolean asFirst) {
        Label label = new Label(createLabelText(value, prefix));
        label.setStyles(style);
        parent.addChild(label, asFirst ? 0 : -1);
        return label;
    }

    /**
     * Creates a component containing a label with the specified parameters.
     *
     * @param <C> Class of created component.
     * @param parent BaseComponent that will be the parent of the created component.
     * @param value Value to be used as label text.
     * @param prefix Value to be used as a prefix for the label text.
     * @param style Style to be applied to the label.
     * @param width Width of the created component.
     * @param clazz The class of the component to be created.
     * @return The newly created component.
     */
    protected <C extends BaseUIComponent> C createCell(BaseComponent parent, Object value, String prefix, String style,
                                                       String width, Class<C> clazz) {
        C container = null;

        try {
            container = clazz.newInstance();
            container.setParent(parent);
            container.setStyles(cellStyle);

            if (width != null) {
                container.setWidth(width);
            }

            if (value instanceof BaseComponent) {
                ((BaseComponent) value).setParent(container);
            } else if (value != null) {
                createLabel(container, value, prefix, style);
            }

        } catch (Exception e) {}
        ;

        return container;
    }

    public String createLabelText(Object value, String prefix) {
        String text = StringUtils.trimToEmpty(value == null ? null
                : value instanceof Collection ? createLabelText((Collection<?>) value)
                        : value instanceof Date ? DateUtil.formatDate((Date) value)
                                : value instanceof String ? StrUtil.formatMessage((String) value) : value.toString());
        return text.isEmpty() ? "" : StrUtil.formatMessage(StringUtils.defaultString(prefix)) + text;
    }

    private String createLabelText(Collection<?> list) {
        StringBuilder sb = new StringBuilder();

        for (Object object : list) {
            String s = createLabelText(object, null);

            if (!s.isEmpty() && sb.length() > 0) {
                sb.append(", ");
            }

            sb.append(s);
        }

        return sb.toString();
    }

    public Image createImage(BaseComponent parent, String src) {
        Image img = new Image(src);
        parent.addChild(img);
        return img;
    }

}
