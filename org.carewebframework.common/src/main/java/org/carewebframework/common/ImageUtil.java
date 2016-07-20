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
package org.carewebframework.common;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * Image manipulation utilities.
 */
public class ImageUtil {
    
    private static final Font defaultFont = new Font("Arial", Font.PLAIN, 12);
    
    private static final Color defaultBackColor = Color.WHITE;
    
    private static final Color defaultFontColor = Color.BLACK;
    
    /**
     * Creates an image of the specified text using default font and color.
     * 
     * @param text Text to be rendered in the image.
     * @return A buffered image.
     * @throws IOException IO exception.
     */
    public static BufferedImage toImage(String text) throws IOException {
        return toImage(text, null, null, null);
    }
    
    /**
     * Creates an image of the specified text.
     * 
     * @param text Text to be rendered in the image.
     * @param font Font to be used to render the text. If null, Tahoma, plain, 11pt is used.
     * @param backColor Background color for the image. If null, a transparent background is used.
     * @param fontColor Font color. If null, black is used.
     * @return A buffered image.
     * @throws IOException IO exception.
     */
    public static BufferedImage toImage(String text, Font font, Color backColor, Color fontColor) throws IOException {
        text = text == null ? "" : text;
        font = font == null ? defaultFont : font;
        backColor = backColor == null ? defaultBackColor : backColor;
        fontColor = fontColor == null ? defaultFontColor : fontColor;
        
        // Create the FontRenderContext object which helps us to measure the text
        FontRenderContext frc = new FontRenderContext(null, true, true);
        
        // Get the height and width of the text
        Rectangle2D bounds = font.getStringBounds(text + "XX", frc);
        int w = (int) bounds.getWidth();
        int h = (int) bounds.getHeight();
        
        // Create a BufferedImage object
        BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        
        // Get the drawing canvas
        Graphics2D g = image.createGraphics();
        
        // Draw the graphic
        g.setColor(backColor);
        g.fillRect(0, 0, w, h);
        g.setColor(fontColor);
        g.setFont(font);
        g.drawString(text, (float) bounds.getX(), (float) -bounds.getY());
        g.dispose();
        
        return image;
    }
    
    /**
     * Enforce static class.
     */
    private ImageUtil() {
    }
}
