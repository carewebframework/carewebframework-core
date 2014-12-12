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

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.maven.plugin.logging.Log;

import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.StringUtils;

/**
 * Generates a new theme directly from a CSS file.
 */
class ThemeGeneratorCSS extends ThemeGeneratorBase {
    
    /**
     * Applies a map template to the source CSS to generate additional CSS.
     */
    protected class CSSProcessor extends ResourceProcessor {
        
        private static final String DELIM = "|";
        
        private final Map<String, String> srcMap = new HashMap<String, String>();
        
        @Override
        protected void process(IThemeResource resource) throws Exception {
            ThemeResourceCSS css = (ThemeResourceCSS) resource;
            File mapper = css.getMapper();
            
            if (mapper == null) {
                super.process(resource);
                return;
            }
            InputStream in = new FileInputStream(mapper);
            LineIterator lines = IOUtils.lineIterator(in, "UTF-8");
            String line = "";
            
            while (lines.hasNext()) {
                line += lines.nextLine();
                
                if (line.endsWith("\\")) {
                    line = StringUtils.left(line, line.length() - 1);
                } else {
                    addMapEntry(line);
                    line = "";
                }
            }
            
            addMapEntry(line);
            IOUtils.closeQuietly(in);
            super.process(resource);
        }
        
        /**
         * Parses and adds the entry to the map.
         * 
         * @param s String entry.
         */
        private void addMapEntry(String s) {
            String[] pcs = s.split("\\=", 2);
            
            if (pcs.length == 2) {
                if (srcMap.containsKey(pcs[0])) {
                    srcMap.put(pcs[0], srcMap.get(pcs[0]) + pcs[1]);
                } else {
                    srcMap.put(pcs[0], pcs[1]);
                }
            }
        }
        
        @Override
        protected void process() throws Exception {
            int c = 0;
            int state = 0;
            StringBuilder sb = new StringBuilder();
            Set<String> matches = new HashSet<String>();
            Map<String, String> refMap = new LinkedHashMap<String, String>();
            Map<String, String> styles = new HashMap<String, String>();
            String prop = null;
            
            while (c != -1) {
                c = inputStream.read();
                
                if (c == -1) {
                    state = -1;
                } else {
                    outputStream.write(c);
                }
                
                switch (state) {
                    case -1: // Process template
                        for (String match : matches) {
                            String template = refMap.get(match);
                            
                            if (!template.contains(DELIM)) {
                                continue;
                            }
                            
                            for (Entry<String, String> entry : styles.entrySet()) {
                                String replace = DELIM + entry.getKey() + DELIM;
                                template = template.replace(replace, entry.getValue());
                            }
                            
                            refMap.put(match, template);
                        }
                        
                        matches.clear();
                        styles.clear();
                        
                        if (c == -1) {
                            continue;
                        }
                        
                        state = 0;
                        // Fall through intended
                        
                    case 0: // Baseline
                        switch (c) {
                            case '/': // Possible comment start
                                state = 1;
                                break;
                            
                            case '<': // Directive start
                                state = 10;
                                break;
                            
                            case '{': // Declaration block
                                state = 20;
                                break;
                            
                            case ',':
                                state = 30;
                                break;
                            
                            default:
                                sb.append((char) c);
                                continue;
                        }
                        
                        String sel = sb.toString().trim();
                        sb.setLength(0);
                        
                        if (srcMap.containsKey(sel)) {
                            matches.add(sel);
                            refMap.put(sel, srcMap.get(sel));
                            srcMap.remove(sel);
                        } else if (refMap.containsKey(sel)) {
                            matches.add(sel);
                        }
                        
                        break;
                    
                    case 1: // Possible comment
                        state = c == '*' ? 2 : 0;
                        break;
                    
                    case 2: // Possible comment end
                        state = c == '*' ? 3 : state;
                        break;
                    
                    case 3: // Comment end
                        state = c == '/' ? 0 : c == '*' ? state : 2;
                        break;
                    
                    case 10: // Directive end
                        state = c == '>' ? 0 : state;
                        break;
                    
                    case 20: // Declaration block
                        switch (c) {
                            case '}': // End block
                                state = -1;
                                break;
                            
                            case ':': // Start of property value
                                prop = sb.toString().trim();
                                sb.setLength(0);
                                state = 30;
                                break;
                            
                            default: // Build property name
                                sb.append((char) c);
                                break;
                        }
                        break;
                    
                    case 30: // Property value
                        switch (c) {
                            case ';': // Property separator
                            case '}': // Block terminator
                                styles.put(prop, sb.toString());
                                sb.setLength(0);
                                state = c == ';' ? 20 : -1;
                                break;
                            
                            default: // Build property value
                                sb.append((char) c);
                                break;
                        }
                        break;
                }
                
            }
            
            for (String line : refMap.values()) {
                if (line.contains(DELIM)) {
                    log.warn("Output contains unresolved reference: " + line);
                }
                
                outputStream.write(line.getBytes());
            }
        }
    }
    
    /**
     * @param theme The theme.
     * @param buildDirectory - Scratch build directory
     * @param exclusionFilters - WildcardFileFilter (i.e. exclude certain files)
     * @param log The logger
     * @throws Exception if error occurs initializing generator
     */
    public ThemeGeneratorCSS(Theme theme, File buildDirectory, WildcardFileFilter exclusionFilters, Log log)
        throws Exception {
        
        super(theme, buildDirectory, exclusionFilters, log);
    }
    
    @Override
    protected void registerProcessors(Map<String, ResourceProcessor> processors) {
        String mapper = getTheme().getCSSMapper();
        ResourceProcessor processor = mapper == null ? new CopyProcessor() : new CSSProcessor();
        processors.put(".css", processor);
    }
    
    @Override
    protected String getConfigTemplate() {
        return "/theme-config-css.xml";
    }
    
    @Override
    protected String getRootPath() {
        return "org/carewebframework/themes/css/";
    }
    
    @Override
    protected String relocateResource(String resourceName, String rootPath) {
        return "web/" + rootPath + "/" + FileUtils.filename(resourceName);
    }
    
}
