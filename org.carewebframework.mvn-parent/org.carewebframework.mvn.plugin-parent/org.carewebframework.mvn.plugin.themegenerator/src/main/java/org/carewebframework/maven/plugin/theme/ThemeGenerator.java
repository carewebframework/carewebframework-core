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
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.lang.StringUtils;
import org.apache.sanselan.ImageFormat;
import org.apache.sanselan.Sanselan;

/**
 *
 */
class ThemeGenerator {
    
    /**
     * Abstract base class for processing jar file entries. Override the abstract process method to
     * implement the logic for processing a jar entry.
     */
    private abstract class ProcessEntry {
        
        protected InputStream inputStream;
        
        protected OutputStream outputStream;
        
        protected void process(final JarFile jarFile, final JarEntry entry) throws Exception {
            this.inputStream = jarFile.getInputStream(entry);
            this.outputStream = new FileOutputStream(newFile(entry));
            process();
            this.inputStream.close();
            this.outputStream.close();
        }
        
        protected abstract void process() throws Exception;
    }
    
    /**
     * Performs a simple copy of a jar entry from the source to the destination.
     */
    protected class ProcessCopy extends ProcessEntry {
        
        @Override
        protected void process() throws Exception {
            IOUtils.copy(this.inputStream, this.outputStream);
        }
    }
    
    /**
     * Base class for processing gif- and png-formatted images.
     */
    protected class ProcessImage extends ProcessEntry {
        
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
                new FilteredImageSource(orig.getSource(), ThemeGenerator.this.hueFilter));
            
            this.result = new BufferedImage(this.width, this.height, BufferedImage.TYPE_INT_ARGB);
            this.g = this.result.createGraphics();
        }
    }
    
    /**
     * Processes a png image.
     */
    protected class ProcessPng extends ProcessImage {
        
        @Override
        protected void process() throws Exception {
            super.process();
            this.g.drawImage(this.resultImg, 0, 0, null);
            ImageIO.write(this.result, "png", this.outputStream);
            this.g.dispose();
        }
    }
    
    /**
     * Process a gif image.
     */
    protected class ProcessGif extends ProcessImage {
        
        @Override
        protected void process() throws Exception {
            super.process();
            this.g.setColor(java.awt.Color.white);
            this.g.setComposite(AlphaComposite.Clear);
            this.g.fillRect(0, 0, this.width, this.height);
            this.g.setComposite(AlphaComposite.SrcOver);
            this.g.drawImage(this.resultImg, 0, 0, null);
            Sanselan.writeImage(this.result, this.outputStream, ImageFormat.IMAGE_FORMAT_GIF, null);
            this.g.dispose();
        }
    }
    
    /**
     * Processes style sheets and related resources. Applies color morphing to any color references
     * and adjusts url references to use new path.
     */
    protected class ProcessCss extends ProcessEntry {
        
        @Override
        protected void process() throws Exception {
            final List<String> readLines = IOUtils.readLines(new BufferedInputStream(this.inputStream));
            final PrintStream ps = new PrintStream(this.outputStream);
            
            for (final String line : readLines) {
                ps.println(replaceURLs(replaceColor(line)));
            }
        }
    }
    
    private static final String THEME_NAME_REGEX = "^[\\w\\-]+$";
    
    private static final Pattern URL_PATTERN = Pattern.compile("~\\./");
    
    private static final Pattern COLOR_PATTERN = Pattern.compile("#([A-Fa-f0-9]{6,6})");
    
    private final WildcardFileFilter exclusionFilters;
    
    private final HueFilter hueFilter;
    
    private final Color color;
    
    private final String rootPath;
    
    private final String themeName;
    
    private final File buildDirectory;
    
    private final Map<String, ProcessEntry> processors = new HashMap<String, ProcessEntry>();
    
    /**
     * @param themeName - Name of theme
     * @param baseColor - Base Color (i.e. 000000)
     * @param themeVersion - Version identifier
     * @param buildDirectory - Scratch build directory
     * @param exclusionFilters - WildcardFileFilter (i.e. exclude certain files)
     * @throws Exception if error occurs initializing generator
     */
    public ThemeGenerator(final String themeName, final String baseColor, final String themeVersion, /*File sourceDirectory,*/
        final File buildDirectory, final WildcardFileFilter exclusionFilters) throws Exception {
        if ((themeName == null) || !themeName.matches(THEME_NAME_REGEX)) {
            throw new Exception(
                    "Theme names must not be null and must be alphanumeric with no blanks, conforming to regexp: "
                            + ThemeGenerator.THEME_NAME_REGEX);
        }
        this.exclusionFilters = exclusionFilters;
        this.themeName = themeName;
        this.buildDirectory = buildDirectory;
        this.color = toColor(baseColor);
        this.hueFilter = new HueFilter(this.color);
        this.rootPath = "org/carewebframework/themes/" + themeName;
        this.processors.put(".gif", new ProcessGif());
        this.processors.put(".png", new ProcessPng());
        final ProcessCss processCss = new ProcessCss();
        this.processors.put(".css", processCss);
        this.processors.put(".css.dsp", processCss);
        this.processors.put(".wcs", processCss);
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
    
    /**
     * Modifies the path of a jar entry to use the new root path.
     * 
     * @param entryName
     * @return
     */
    protected String relocateEntry(final String entryName) {
        return entryName.replaceFirst("^web", "web/" + this.rootPath);
    }
    
    /**
     * Adjust any url references in the line to use new root path.
     * 
     * @param line - String to modify
     * @return the modified string
     */
    public String replaceURLs(final String line) {
        final StringBuffer sb = new StringBuffer();
        final Matcher matcher = URL_PATTERN.matcher(line);
        final String newPath = "~./" + this.rootPath + "/";
        
        while (matcher.find()) {
            final char dlm = line.charAt(matcher.start() - 1);
            final int i = line.indexOf(dlm, matcher.end());
            final String url = i > 0 ? line.substring(matcher.start(), i) : null;
            
            if ((url == null) || !isExcluded(url)) {
                matcher.appendReplacement(sb, newPath);
            }
        }
        
        matcher.appendTail(sb);
        return sb.toString();
    }
    
    /**
     * Adjust any color references using the active hue filter.
     * 
     * @param line - The string to modify
     * @return the modified string
     */
    public String replaceColor(final String line) {
        final StringBuffer sb = new StringBuffer();
        final Matcher matcher = COLOR_PATTERN.matcher(line);
        
        while (matcher.find()) {
            final String hexColor = matcher.group(1);
            final int rgb = this.hueFilter.filterRGB(0, 0, Integer.parseInt(hexColor, 16));
            final String transfHexColor = String.format("%06x", rgb);
            matcher.appendReplacement(sb, "#" + transfHexColor);
        }
        matcher.appendTail(sb);
        return sb.toString();
    }
    
    /**
     * Creates a new file in the build directory. Ensures that all folders in the path are also
     * created.
     * 
     * @param entryName Entry name to create.
     * @param modTime Modification timestamp for the new entry. If 0, defaults to the current time.
     * @return the new file
     */
    public File newFile(final String entryName, final long modTime) {
        final File file = new File(this.buildDirectory, entryName);
        
        if (modTime != 0) {
            file.setLastModified(modTime);
        }
        
        file.getParentFile().mkdirs();
        return file;
    }
    
    /**
     * Creates a new jar file entry from an existing jar file entry.
     * 
     * @param oldEntry Jar file entry from a source jar file.
     * @return the new file
     */
    public File newFile(final JarEntry oldEntry) {
        return newFile(relocateEntry(oldEntry.getName()), oldEntry.getTime());
    }
    
    /**
     * Finds and executes the processor appropriate for the jar entry.
     * 
     * @param sourceJar The source jar.
     * @param jarEntry The entry within the source jar.
     * @return True if a processor was found for the jar entry.
     * @throws Exception
     */
    public boolean process(final JarFile sourceJar, final JarEntry jarEntry) throws Exception {
        final String entryName = StringUtils.trimToEmpty(jarEntry.getName());
        
        if (isExcluded(entryName)) {
            return false;
        }
        final String entryLower = entryName.toLowerCase();
        
        for (final String ext : this.processors.keySet()) {
            if (entryLower.endsWith(ext)) {
                this.processors.get(ext).process(sourceJar, jarEntry);
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Returns true if the specified file is in the exclusion list.
     * 
     * @param fileName
     * @return
     */
    private boolean isExcluded(final String fileName) {
        return isExcluded(new File(fileName));
    }
    
    /**
     * Returns true if the specified file is in the exclusion list.
     * 
     * @param file
     * @return
     */
    private boolean isExcluded(final File file) {
        if (this.exclusionFilters.accept(file)) {
            //getLog().info("Resource excluded: " + file.getName());
            return true;
        }
        
        return false;
    }
    
    /**
     * @return the themeName
     */
    public String getThemeName() {
        return this.themeName;
    }
    
    /**
     * @return the buildDirectory
     */
    public File getBuildDirectory() {
        return this.buildDirectory;
    }
    
}
