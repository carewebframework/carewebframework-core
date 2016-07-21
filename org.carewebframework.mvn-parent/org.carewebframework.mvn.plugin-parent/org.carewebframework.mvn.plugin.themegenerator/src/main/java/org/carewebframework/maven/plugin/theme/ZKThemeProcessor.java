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
package org.carewebframework.maven.plugin.theme;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.io.File;
import java.io.FileFilter;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.sanselan.ImageFormat;
import org.apache.sanselan.Sanselan;
import org.carewebframework.maven.plugin.core.BaseMojo;
import org.carewebframework.maven.plugin.iterator.ZipIterator;
import org.carewebframework.maven.plugin.transform.AbstractTransform;

/**
 * Generates a new theme from a base theme by using specialized processors to transform individual
 * theme elements.
 */
public class ZKThemeProcessor extends AbstractThemeProcessor {
    
    /**
     * Base class for processing gif- and png-formatted images.
     */
    protected class ImageTransform extends AbstractTransform {
        
        public ImageTransform(BaseMojo mojo) {
            super(mojo);
        }
        
        protected Graphics2D graphics2D;
        
        protected Image image;
        
        protected BufferedImage bufferedImage;
        
        protected int width;
        
        protected int height;
        
        @Override
        public void transform(InputStream inputStream, OutputStream outputStream) throws Exception {
            BufferedImage orig = ImageIO.read(inputStream);
            width = orig.getWidth();
            height = orig.getHeight();
            image = Toolkit.getDefaultToolkit().createImage(new FilteredImageSource(orig.getSource(), colorTransform));
            bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            graphics2D = bufferedImage.createGraphics();
        }
    }
    
    /**
     * Processes a png image.
     */
    protected class PngTransform extends ImageTransform {
        
        public PngTransform(BaseMojo mojo) {
            super(mojo);
        }
        
        @Override
        public void transform(InputStream inputStream, OutputStream outputStream) throws Exception {
            super.transform(inputStream, outputStream);
            graphics2D.drawImage(image, 0, 0, null);
            ImageIO.write(bufferedImage, "png", outputStream);
            graphics2D.dispose();
        }
    }
    
    /**
     * Process a gif image.
     */
    protected class GifTransform extends ImageTransform {
        
        public GifTransform(BaseMojo mojo) {
            super(mojo);
        }
        
        @Override
        public void transform(InputStream inputStream, OutputStream outputStream) throws Exception {
            super.transform(inputStream, outputStream);
            graphics2D.setColor(java.awt.Color.white);
            graphics2D.setComposite(AlphaComposite.Clear);
            graphics2D.fillRect(0, 0, width, height);
            graphics2D.setComposite(AlphaComposite.SrcOver);
            graphics2D.drawImage(this.image, 0, 0, null);
            Sanselan.writeImage(bufferedImage, outputStream, ImageFormat.IMAGE_FORMAT_GIF, null);
            graphics2D.dispose();
        }
    }
    
    /**
     * Processes style sheets and related resources. Applies color morphing to any color references
     * and adjusts url references to use new path.
     */
    protected class CssTransform extends AbstractTransform {
        
        public CssTransform(BaseMojo mojo) {
            super(mojo);
        }
        
        @Override
        public void transform(InputStream inputStream, OutputStream outputStream) throws Exception {
            LineIterator iter = IOUtils.lineIterator(inputStream, "UTF-8");
            PrintStream ps = new PrintStream(outputStream);
            
            while (iter.hasNext()) {
                ps.println(replaceURLs(replaceColor(iter.next())));
            }
        }
    }
    
    private static final Pattern COLOR_PATTERN = Pattern.compile("#([A-Fa-f0-9]{6,6})");
    
    private final ColorTransform colorTransform;
    
    private final Color color;
    
    /**
     * @param theme The theme.
     * @param mojo The theme generator mojo.
     * @throws Exception if error occurs initializing generator
     */
    public ZKThemeProcessor(Theme theme, ThemeGeneratorMojo mojo) throws Exception {
        
        super(theme, mojo);
        color = toColor(theme.getBaseColor());
        colorTransform = new ColorTransform(color);
        addConfigEntry("zk");
        registerTransform("*.gif", new GifTransform(mojo));
        registerTransform("*.png", new PngTransform(mojo));
        registerTransform("*.css,*.css.dsp,*.wcs", new CssTransform(mojo));
    }
    
    /**
     * Modifies the path of a jar entry to use the new root path.
     * 
     * @param resourceName Path to modify.
     * @return The modified path.
     */
    @Override
    public String relocateResource(String resourceName) {
        return resourceName.replaceFirst("^web", "web/" + getResourceBase());
    }
    
    @Override
    public String getResourceBase() {
        return getThemeBase() + "zk/" + getTheme().getThemeName();
    }
    
    private Color toColor(String colorString) {
        if (colorString.startsWith("#")) {
            colorString = colorString.substring(1);
        }
        
        if (!colorString.matches("^[a-fA-F0-9]{6,6}$")) {
            throw new IllegalArgumentException("Color should be in 6 hex digit format, for example: A4FFC0");
        }
        
        return new Color(Integer.parseInt(colorString, 16));
    }
    
    @Override
    public void transform() throws Exception {
        FileFilter filter = new WildcardFileFilter("*.jar");
        mojo.getLog().info("Processing ZK theme sources.");
        
        for (File jarFile : mojo.getSourceDirectory().listFiles(filter)) {
            transform(new ZipIterator(jarFile));
        }
    }
    
    /**
     * Adjust any color references using the active hue filter.
     * 
     * @param line The string to modify
     * @return the modified string
     */
    public String replaceColor(String line) {
        StringBuffer sb = new StringBuffer();
        Matcher matcher = COLOR_PATTERN.matcher(line);
        
        while (matcher.find()) {
            String hexColor = matcher.group(1);
            int rgb = colorTransform.filterRGB(0, 0, Integer.parseInt(hexColor, 16));
            String transfHexColor = String.format("%06x", rgb);
            matcher.appendReplacement(sb, "#" + transfHexColor);
        }
        
        matcher.appendTail(sb);
        return sb.toString();
    }
    
}
