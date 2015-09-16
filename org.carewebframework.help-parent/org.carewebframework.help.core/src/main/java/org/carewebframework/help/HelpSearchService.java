/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.help;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import com.google.common.io.Files;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.QueryBuilder;
import org.apache.lucene.util.Version;
import org.apache.tika.Tika;
import org.apache.tika.parser.html.HtmlParser;

import org.carewebframework.common.MiscUtil;
import org.carewebframework.common.StrUtil;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;

/**
 * Service for managing the search index and providing search capabilities using Lucene.
 */
public class HelpSearchService implements ApplicationContextAware {
    
    private static final Log log = LogFactory.getLog(HelpSearchService.class);
    
    /**
     * Callback interface for receiving search results.
     */
    public interface IHelpSearchListener {
        
        /**
         * Called by search engine to report results.
         * 
         * @param results List of search results (may be null to indicated no results or no search
         *            capability).
         */
        void onSearchComplete(List<HelpSearchHit> results);
    }
    
    private static class IndexTracker {
        
        private final File propertyFile;
        
        private final Properties properties = new Properties();
        
        private boolean changed;
        
        /**
         * Load the index tracker from persistent storage.
         * 
         * @param indexDirectoryPath The index directory path. The tracker properties file will be
         *            located here.
         * @throws IOException IO error when attempting to delete index.
         */
        IndexTracker(File indexDirectoryPath) throws IOException {
            this.propertyFile = new File(indexDirectoryPath, "tracker.properties");
            
            try (InputStream is = new FileInputStream(propertyFile);) {
                properties.load(is);
            } catch (Exception e) {
                // Just ignore since we can recreate the property file.
            }
            
            if (!isCompatible(properties.getProperty("lucene_version"))) {
                log.info("Initializing help search index");
                changed = true;
                properties.clear();
                properties.setProperty("lucene_version", Version.LATEST.toString());
                FileUtils.cleanDirectory(indexDirectoryPath);
            }
            
        }
        
        /**
         * Returns true if the index version is compatible with the running Lucene version.
         * 
         * @param indexVersion The index version.
         * @return True if the current index is compatible with the running Lucene version.
         */
        boolean isCompatible(String indexVersion) {
            try {
                return indexVersion != null && Version.parse(indexVersion).equals(Version.LATEST);
            } catch (ParseException e) {
                return false;
            }
        }
        
        /**
         * Save the index tracker state if it has changed.
         */
        void save() {
            if (changed) {
                try (OutputStream os = new FileOutputStream(propertyFile)) {
                    properties.store(os, "Indexed help modules.");
                } catch (Exception e) {
                    log.error("Failed to save index tracking information", e);
                }
                
                changed = false;
            }
            
        }
        
        /**
         * Add a help module to the tracker data.
         * 
         * @param module Help module to add.
         */
        void add(HelpModule module) {
            properties.setProperty(module.getId(), module.getVersion());
            changed = true;
        }
        
        /**
         * Remove a help module from the tracker data.
         * 
         * @param module Help module to remove.
         */
        void remove(HelpModule module) {
            properties.remove(module.getId());
            changed = true;
        }
        
        /**
         * Returns true if the indexed module is the same as the loaded one.
         * 
         * @param module The loaded help module.
         * @return True if the indexed module version is the same as the loaded one.
         */
        boolean isSame(HelpModule module) {
            String v = properties.getProperty(module.getId());
            
            if (v == null) {
                return false;
            }
            
            return v.equals(module.getVersion());
        }
    }
    
    private static final HelpSearchService instance = new HelpSearchService();
    
    private IndexWriter writer;
    
    private String indexDirectoryPath;
    
    private Directory indexDirectory;
    
    private IndexTracker indexTracker;
    
    private IndexSearcher indexSearcher;
    
    private IndexReader indexReader;
    
    private QueryBuilder queryBuilder;
    
    private Tika tika;
    
    private ApplicationContext appContext;
    
    public static HelpSearchService getInstance() {
        return instance;
    }
    
    /**
     * Enforce singleton instance.
     */
    private HelpSearchService() {
    }
    
    /**
     * Setter for index directory path (injected by IOC container).
     * 
     * @param path The index directory path (may be null or empty).
     */
    public void setIndexDirectoryPath(String path) {
        indexDirectoryPath = path;
    }
    
    /**
     * Resolves the index directory path. If a path is not specified, one is created within
     * temporary storage.
     * 
     * @return The resolved index directory path.
     * @throws IOException Unspecified IO exception.
     */
    private File resolveIndexDirectoryPath() throws IOException {
        if (StringUtils.isEmpty(indexDirectoryPath)) {
            indexDirectoryPath = System.getProperty("java.io.tmpdir") + appContext.getApplicationName();
        }
        
        File dir = new File(indexDirectoryPath, HelpUtil.class.getPackage().getName());
        Files.createParentDirs(dir);
        log.info("Help search index located at " + dir);
        return dir;
    }
    
    /**
     * Index all HTML files within the content of the help module.
     * 
     * @param helpModule Help module to be indexed.
     */
    public void indexHelpModule(HelpModule helpModule) {
        try {
            if (indexTracker.isSame(helpModule)) {
                return;
            }
            
            unindexHelpModule(helpModule);
            log.info("Indexing help module " + helpModule.getId());
            int i = helpModule.getUrl().lastIndexOf('/');
            String pattern = "classpath:" + helpModule.getUrl().substring(0, i + 1) + "*.htm";
            
            for (Resource resource : appContext.getResources(pattern)) {
                indexDocument(helpModule, resource);
            }
            
            writer.commit();
            indexTracker.add(helpModule);
        } catch (Exception e) {
            throw MiscUtil.toUnchecked(e);
        }
    }
    
    /**
     * Removes the index for a help module.
     * 
     * @param helpModule Help module whose index is to be removed.
     */
    public void unindexHelpModule(HelpModule helpModule) {
        try {
            log.info("Removing index for help module " + helpModule.getId());
            Term term = new Term("module", helpModule.getId());
            writer.deleteDocuments(term);
            writer.commit();
            indexTracker.remove(helpModule);
        } catch (IOException e) {
            MiscUtil.toUnchecked(e);
        }
    }
    
    /**
     * Index an HTML text file resource.
     * 
     * @param helpModule The help module owning the resource.
     * @param resource The HTML text file resource.
     * @throws Exception Unspecified exception.
     */
    private void indexDocument(HelpModule helpModule, Resource resource) throws Exception {
        String title = getTitle(resource);
        
        try (InputStream is = resource.getInputStream()) {
            Document document = new Document();
            document.add(new TextField("module", helpModule.getId(), Store.YES));
            document.add(new TextField("source", helpModule.getTitle(), Store.YES));
            document.add(new TextField("title", title, Store.YES));
            document.add(new TextField("url", resource.getURL().toString(), Store.YES));
            document.add(new TextField("content", tika.parseToString(is), Store.NO));
            writer.addDocument(document);
        }
    }
    
    /**
     * Extract the title of the document, if any.
     * 
     * @param resource The document resource.
     * @return The document title, or null if not found.
     */
    private String getTitle(Resource resource) {
        String title = null;
        
        try (InputStream is = resource.getInputStream()) {
            Iterator<String> iter = IOUtils.lineIterator(is, "UTF-8");
            
            while (iter.hasNext()) {
                String line = iter.next().trim();
                String lower = line.toLowerCase();
                int i = lower.indexOf("<title>");
                
                if (i > -1) {
                    i += 7;
                    int j = lower.indexOf("</title>", i);
                    title = line.substring(i, j == -1 ? line.length() : j).trim();
                    title = title.replace("_no", "").replace('_', ' ');
                    break;
                }
            }
        } catch (Exception e) {
            throw MiscUtil.toUnchecked(e);
        }
        
        return title;
    }
    
    /**
     * Performs a search query using the specified string on each registered query handler, calling
     * the listener for each set of results.
     * 
     * @param words List of words to be located.
     * @param helpSets Help sets to be searched
     * @param listener Listener for search results.
     */
    public void search(String words, Collection<IHelpSet> helpSets, IHelpSearchListener listener) {
        try {
            if (queryBuilder == null) {
                initQueryBuilder();
            }
            
            Query searchForWords = queryBuilder.createBooleanQuery("content", words, Occur.MUST);
            Query searchForModules = queryBuilder.createBooleanQuery("module", StrUtil.fromList(helpSets, " "));
            BooleanQuery query = new BooleanQuery();
            query.add(searchForModules, Occur.MUST);
            query.add(searchForWords, Occur.MUST);
            TopDocs docs = indexSearcher.search(query, 9999);
            List<HelpSearchHit> hits = new ArrayList<>(docs.totalHits);
            
            for (ScoreDoc sdoc : docs.scoreDocs) {
                Document doc = indexSearcher.doc(sdoc.doc);
                String source = doc.get("source");
                String title = doc.get("title");
                String url = doc.get("url");
                HelpTopic topic = new HelpTopic(new URL(url), title, source);
                HelpSearchHit hit = new HelpSearchHit(topic, sdoc.score);
                hits.add(hit);
            }
            
            listener.onSearchComplete(hits);
        } catch (Exception e) {
            MiscUtil.toUnchecked(e);
        }
    }
    
    /**
     * Initialize the index writer.
     * 
     * @throws IOException Unspecified IO exception.
     */
    public void init() throws IOException {
        File path = resolveIndexDirectoryPath();
        indexTracker = new IndexTracker(path);
        indexDirectory = FSDirectory.open(path);
        tika = new Tika(null, new HtmlParser());
        Analyzer analyzer = new StandardAnalyzer();
        IndexWriterConfig config = new IndexWriterConfig(Version.LATEST, analyzer);
        writer = new IndexWriter(indexDirectory, config);
    }
    
    /**
     * Performs a delayed initialization of the query builder to ensure that the index has been
     * fully created.
     * 
     * @throws IOException Unspecified IO exception.
     */
    private synchronized void initQueryBuilder() throws IOException {
        if (queryBuilder == null) {
            indexReader = DirectoryReader.open(indexDirectory);
            indexSearcher = new IndexSearcher(indexReader);
            queryBuilder = new QueryBuilder(writer.getAnalyzer());
        }
    }
    
    /**
     * Release/update resources upon destruction.
     */
    public void destroy() {
        try {
            if (indexReader != null) {
                indexReader.close();
            }
            
            writer.close();
            indexTracker.save();
        } catch (IOException e) {
            throw MiscUtil.toUnchecked(e);
        }
    }
    
    /**
     * Application context is needed to iterate over help content resources.
     */
    @Override
    public void setApplicationContext(ApplicationContext appContext) throws BeansException {
        this.appContext = appContext;
    }
}
