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

import java.util.List;

import org.zkoss.zul.Image;

/**
 * Presents an icon picker component.
 */
public class IconPicker extends AbstractPicker<Image> {
    
    private static final long serialVersionUID = 1L;
    
    public IconPicker() {
        super("cwf-iconpicker", new Image(NO_CHOICE_URL));
    }
    
    /**
     * Adds an icon with the specified URL.
     * 
     * @param url The URL.
     */
    public void addIconByUrl(String url) {
        addItem(new Image(url));
    }
    
    /**
     * Adds multiple icons, given a list of URLs.
     * 
     * @param urls List of URLs.
     */
    public void addIconsByUrl(List<String> urls) {
        for (String url : urls) {
            _addItem(new Image(url));
        }
        
        updateModel();
    }
    
    /**
     * Sets the tooltip text to the file name extracted from the URL.
     */
    @Override
    protected Image prepItem(Image icon) {
        String url = icon.getSrc();
        int i = url.lastIndexOf("/") + 1;
        icon.setTooltiptext(url.substring(i));
        return super.prepItem(icon);
    }
    
    /**
     * Two images are equal if their URLs are the same.
     */
    @Override
    protected boolean itemsAreEqual(Image image1, Image image2) {
        return image1.getSrc().equals(image2.getSrc());
    }
    
    /**
     * Use the tooltip text for the display text.
     */
    @Override
    protected String getItemText(Image item) {
        return item.getTooltiptext();
    }
    
    /**
     * Locates an icon by its url.
     * 
     * @param url URL whose associated icon is sought.
     * @return The icon associated with the specified URL, or null if none found.
     */
    public Image findIcon(String url) {
        int i = findItem(new Image(url), false);
        return i == -1 ? null : getItems().get(i);
    }
    
}
