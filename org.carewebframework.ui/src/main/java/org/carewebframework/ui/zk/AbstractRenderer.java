/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.ui.zk;

import java.util.Date;

import org.apache.commons.lang.StringUtils;

import org.carewebframework.common.DateUtil;
import org.carewebframework.common.StrUtil;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.HtmlBasedComponent;
import org.zkoss.zul.Label;

/**
 * Base for renderers.
 */
public abstract class AbstractRenderer {
    
    private static final String STYLE_COMP_DEFAULT = "background:white";
    
    private static final String STYLE_CELL_DEFAULT = "border:none;background:transparent";
    
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
        this.compStyle = compStyle == null ? STYLE_COMP_DEFAULT : compStyle;
        this.cellStyle = cellStyle == null ? STYLE_CELL_DEFAULT : cellStyle;
    }
    
    /**
     * Creates a label for a string value.
     * 
     * @param parent Component that will be the parent of the label.
     * @param value Value to be used as label text.
     * @return The newly created label.
     */
    protected Label createLabel(Component parent, Object value) {
        return createLabel(parent, value, null, null);
    }
    
    /**
     * Creates a label for a string value.
     * 
     * @param parent Component that will be the parent of the label.
     * @param value Value to be used as label text.
     * @param prefix Value to be used as a prefix for the label text.
     * @return The newly created label.
     */
    protected Label createLabel(Component parent, Object value, String prefix) {
        return createLabel(parent, value, prefix, null);
    }
    
    /**
     * Creates a label for a string value.
     * 
     * @param parent Component that will be the parent of the label.
     * @param value Value to be used as label text.
     * @param prefix Value to be used as a prefix for the label text.
     * @param style Style to be applied to the label.
     * @return The newly created label.
     */
    protected Label createLabel(Component parent, Object value, String prefix, String style) {
        return createLabel(parent, value, prefix, style, false);
    }
    
    /**
     * Creates a label for a string value.
     * 
     * @param parent Component that will be the parent of the label.
     * @param value Value to be used as label text.
     * @param prefix Value to be used as a prefix for the label text.
     * @param style Style to be applied to the label.
     * @param asFirst If true, the label is prepended to the parent. If false, it is appended.
     * @return The newly created label.
     */
    protected Label createLabel(Component parent, Object value, String prefix, String style, boolean asFirst) {
        Label label = new Label(createLabelText(value, prefix));
        label.setStyle(style);
        parent.insertBefore(label, asFirst ? parent.getFirstChild() : null);
        return label;
    }
    
    /**
     * Creates a component containing a label with the specified parameters.
     * 
     * @param parent Component that will be the parent of the created component.
     * @param value Value to be used as label text.
     * @param prefix Value to be used as a prefix for the label text.
     * @param style Style to be applied to the label.
     * @param width Width of the created component.
     * @param clazz The class of the component to be created.
     * @return The newly created component.
     */
    protected <C extends HtmlBasedComponent> C createCell(Component parent, Object value, String prefix, String style,
                                                          String width, Class<C> clazz) {
        C container = null;
        
        try {
            container = clazz.newInstance();
            container.setParent(parent);
            container.setStyle(cellStyle);
            
            if (width != null) {
                container.setWidth(width);
            }
            
            if (value instanceof Component) {
                ((Component) value).setParent(container);
            } else if (value != null) {
                createLabel(container, value, prefix, style);
            }
            
        } catch (Exception e) {}
        ;
        
        return container;
    }
    
    protected String createLabelText(Object value, String prefix) {
        String text = StringUtils.trimToEmpty(value == null ? null : value instanceof Date ? DateUtil
                .formatDate((Date) value) : value instanceof String ? StrUtil.formatMessage((String) value) : value
                .toString());
        return text.isEmpty() ? "" : StrUtil.formatMessage(StringUtils.defaultString(prefix)) + text;
    }
    
}
