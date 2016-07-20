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
package org.carewebframework.ui.zk;

import java.io.Serializable;
import java.util.Date;

import org.carewebframework.common.DateUtil;

import org.zkoss.zk.ui.Component;
import org.zkoss.zul.Image;
import org.zkoss.zul.Label;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;

/**
 * This is the base class for a row renderer for the selection grid. It has some convenience methods
 * for adding cell content. It also forwards click events from each row to the parent grid as an
 * onCheck event to allow selection without clicking directly on the checkbox.
 * 
 * @param <T> Class of rendered object.
 */
public class SelectionGridRenderer<T> implements RowRenderer<T>, Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * Subclasses should override this to perform row rendering. This super method should be called
     * as the final operation in the overriding method.
     */
    @Override
    public void render(Row row, T data, int index) throws Exception {
        ((SelectionGrid) row.getGrid()).addCheckbox(row);
    }
    
    /**
     * Add the specified component to the next available cell.
     * 
     * @param row Row to receive the component.
     * @param component Component to add.
     */
    public void addCell(Row row, Component component) {
        component.setParent(row);
    }
    
    /**
     * Add a formatted date value to the next available cell.
     * 
     * @param row Row to receive the formatted date.
     * @param value Date to add.
     */
    public void addCell(Row row, Date value) {
        addCell(row, DateUtil.formatDate(value));
    }
    
    /**
     * Add a string value to the next available cell.
     * 
     * @param row Row to receive the string value.
     * @param value String value to add.
     */
    public void addCell(Row row, String value) {
        addCell(row, new Label(value));
    }
    
    /**
     * Add an image to the next available cell.
     * 
     * @param row Row to receive the image.
     * @param url Url for the image.
     */
    public void addImage(Row row, String url) {
        if (url == null) {
            addCell(row, "");
        } else {
            addCell(row, new Image(url));
        }
    }
}
