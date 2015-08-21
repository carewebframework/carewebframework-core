/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
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
        
        protected Graphics2D g;
        
        protected Image resultImg;
        
        protected BufferedImage result;
        
        protected int width;
        
        protected int height;
        
        @Override
        public void transform(InputStream inputStream, OutputStream outputStream) throws Exception {
            final BufferedImage orig = ImageIO.read(inputStream);
            this.width = orig.getWidth();
            this.height = orig.getHeight();
            
            this.resultImg = Toolkit.getDefaultToolkit()
                    .createImage(new FilteredImageSource(orig.getSource(), ZKThemeProcessor.this.hueFilter));
                    
            this.result = new BufferedImage(this.width, this.height, BufferedImage.TYPE_INT_ARGB);
            this.g = this.result.createGraphics();
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
            g.drawImage(resultImg, 0, 0, null);
            ImageIO.write(result, "png", outputStream);
            g.dispose();
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
            g.setColor(java.awt.Color.white);
            g.setComposite(AlphaComposite.Clear);
            g.fillRect(0, 0, width, height);
            g.setComposite(AlphaComposite.SrcOver);
            g.drawImage(this.resultImg, 0, 0, null);
            Sanselan.writeImage(result, outputStream, ImageFormat.IMAGE_FORMAT_GIF, null);
            g.dispose();
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
    
    private final HueFilter hueFilter;
    
    private final Color color;
    
    /**
     * @param theme The theme.
     * @param mojo The theme generator mojo.
     * @throws Exception if error occurs initializing generator
     */
    public ZKThemeProcessor(Theme theme, ThemeGeneratorMojo mojo) throws Exception {
        
        super(theme, mojo);
        color = toColor(theme.getBaseColor());
        hueFilter = new HueFilter(color);
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
            int rgb = hueFilter.filterRGB(0, 0, Integer.parseInt(hexColor, 16));
            String transfHexColor = String.format("%06x", rgb);
            matcher.appendReplacement(sb, "#" + transfHexColor);
        }
        
        matcher.appendTail(sb);
        return sb.toString();
    }
    
}
