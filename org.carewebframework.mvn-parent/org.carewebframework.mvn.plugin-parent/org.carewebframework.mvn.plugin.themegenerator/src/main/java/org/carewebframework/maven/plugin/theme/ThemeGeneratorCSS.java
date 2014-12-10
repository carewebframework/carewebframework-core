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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.io.filefilter.WildcardFileFilter;

import org.codehaus.plexus.util.FileUtils;

/**
 * Generates a new theme directly from a CSS file.
 */
class ThemeGeneratorCSS extends ThemeGeneratorBase {
    
    /**
     * Applies a map template to the source CSS to generate additional CSS.
     */
    protected class CSSProcessor extends ResourceProcessor {
        
        private final Map<String, String> map = new HashMap<String, String>();
        
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
            
            while (lines.hasNext()) {
                String[] pcs = lines.nextLine().split("\\=", 2);
                
                if (pcs.length == 2) {
                    map.put(pcs[0], pcs[1]);
                }
            }
            
            IOUtils.closeQuietly(in);
            super.process(resource);
        }
        
        @Override
        protected void process() throws Exception {
            int c = 0;
            int state = 0;
            List<String> list = new ArrayList<String>();
            StringBuilder sb = new StringBuilder();
            List<String> templates = new ArrayList<String>();
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
                        for (String template : templates) {
                            for (Entry<String, String> entry : styles.entrySet()) {
                                String replace = '|' + entry.getKey() + '|';
                                template = template.replace(replace, entry.getValue());
                            }
                            
                            list.add(template);
                        }
                        
                        templates.clear();
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
                        
                        if (map.containsKey(sel)) {
                            templates.add(map.get(sel));
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
            
            for (String line : list) {
                outputStream.write(line.getBytes());
            }
        }
    }
    
    /**
     * @param theme The theme.
     * @param buildDirectory - Scratch build directory
     * @param exclusionFilters - WildcardFileFilter (i.e. exclude certain files)
     * @throws Exception if error occurs initializing generator
     */
    public ThemeGeneratorCSS(Theme theme, File buildDirectory, WildcardFileFilter exclusionFilters) throws Exception {
        
        super(theme, buildDirectory, exclusionFilters);
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
