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
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.PrintStream;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.sanselan.ImageFormat;
import org.apache.sanselan.Sanselan;

/**
 * Generates a new theme from a base theme by using specialized processors to transform individual
 * theme elements.
 */
class ThemeGeneratorZK extends ThemeGeneratorBase {
    
    /**
     * Base class for processing gif- and png-formatted images.
     */
    protected class ImageProcessor extends ResourceProcessor {
        
        protected Graphics2D g;
        
        protected Image resultImg;
        
        protected BufferedImage result;
        
        protected int width;
        
        protected int height;
        
        @Override
        protected void process() throws Exception {
            final BufferedImage orig = ImageIO.read(this.inputStream);
            this.width = orig.getWidth();
            this.height = orig.getHeight();
            
            this.resultImg = Toolkit.getDefaultToolkit().createImage(
                new FilteredImageSource(orig.getSource(), ThemeGeneratorZK.this.hueFilter));
            
            this.result = new BufferedImage(this.width, this.height, BufferedImage.TYPE_INT_ARGB);
            this.g = this.result.createGraphics();
        }
    }
    
    /**
     * Processes a png image.
     */
    protected class PngProcessor extends ImageProcessor {
        
        @Override
        protected void process() throws Exception {
            super.process();
            g.drawImage(resultImg, 0, 0, null);
            ImageIO.write(result, "png", outputStream);
            g.dispose();
        }
    }
    
    /**
     * Process a gif image.
     */
    protected class GifProcessor extends ImageProcessor {
        
        @Override
        protected void process() throws Exception {
            super.process();
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
    protected class CssProcessor extends ResourceProcessor {
        
        @Override
        protected void process() throws Exception {
            List<String> readLines = IOUtils.readLines(new BufferedInputStream(inputStream));
            PrintStream ps = new PrintStream(outputStream);
            
            for (final String line : readLines) {
                ps.println(replaceURLs(replaceColor(line)));
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
    public ThemeGeneratorZK(Theme theme, ThemeGeneratorMojo mojo) throws Exception {
        
        super(theme, mojo);
        color = toColor(theme.getBaseColor());
        hueFilter = new HueFilter(color);
        addConfigEntry("zk");
    }
    
    @Override
    protected void registerProcessors(Map<String, ResourceProcessor> processors) {
        processors.put(".gif", new GifProcessor());
        processors.put(".png", new PngProcessor());
        CssProcessor processCss = new CssProcessor();
        processors.put(".css", processCss);
        processors.put(".css.dsp", processCss);
        processors.put(".wcs", processCss);
    }
    
    /**
     * Modifies the path of a jar entry to use the new root path.
     * 
     * @param resourceName Path to modify.
     * @return The modified path.
     */
    @Override
    protected String relocateResource(String resourceName) {
        return resourceName.replaceFirst("^web", "web/" + getResourceBase());
    }
    
    @Override
    protected String getResourceBase() {
        return mojo.getThemeBase() + "zk/" + getTheme().getThemeName();
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
    protected void process() throws Exception {
        FileFilter filter = new WildcardFileFilter("*.jar");
        mojo.getLog().info("Processing ZK theme sources.");
        
        for (File file : mojo.getSourceDirectory().listFiles(filter)) {
            try {
                processJarFile(file);
            } catch (final Exception e) {
                throw new Exception("Exception occurred processing source jar:" + file.getName(), e);
            }
        }
    }
    
    /**
     * Process one jar file from the source folder.
     * 
     * @param file A jar file from the source folder.
     * @throws Exception Unspecified exception.
     */
    private void processJarFile(File file) throws Exception {
        JarFile sourceJar = new JarFile(file);
        Enumeration<JarEntry> entries = sourceJar.entries();
        boolean wasProcessed = false;
        
        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            
            if (entry.isDirectory()) {
                continue;
            }
            
            IThemeResource resource = new ThemeResourceJarEntry(sourceJar, entry);
            process(resource);
        }
        sourceJar.close();
        
        if (!wasProcessed) {
            mojo.getLog().info("Source jar contained no themeable resources: " + file.getName());
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
