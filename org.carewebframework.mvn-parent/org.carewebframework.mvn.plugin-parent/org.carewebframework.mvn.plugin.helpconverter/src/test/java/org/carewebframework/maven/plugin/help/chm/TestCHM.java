package org.carewebframework.maven.plugin.help.chm;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import org.carewebframework.maven.plugin.help.HelpConverterMojo;
import org.carewebframework.maven.plugin.help.HelpProcessor;
import org.carewebframework.maven.plugin.help.chm.ChmSourceLoader;

import org.junit.Test;

public class TestCHM {
    
    class TestMojo extends HelpConverterMojo {
        
        TestMojo() throws Exception {
            this.stagingDirectory = getTargetDir();
        }
        
        public File getStagingDirectory() {
            return stagingDirectory;
        }
        
        @Override
        public String getModuleId() {
            return "id";
        }
        
        @Override
        public String getModuleBase() {
            return "base/";
        }
        
        @Override
        public String getModuleName() {
            return "Help Set Name";
        }
    }
    
    @Test
    public void test() throws Exception {
        ChmSourceLoader loader = new ChmSourceLoader();
        TestMojo mojo = new TestMojo();
        HelpProcessor processor = new HelpProcessor(mojo, "./src/test/resources/test.chm", loader);
        processor.transform();
        File refBase = new File("./src/test/resources/reference/");
        File outBase = new File(mojo.getStagingDirectory(), "/web/base/id/");
        compareFiles(outBase, refBase, "Extraneous ");
        compareFiles(refBase, outBase, "Missing ");
        FileUtils.deleteQuietly(mojo.getStagingDirectory());
    }
    
    /**
     * Recursively compares files between the staging directory and the reference directory.
     * 
     * @param base1 The first base directory.
     * @param base2 The second base directory.
     * @param msg The message to display if a file appears in base1 but not base2.
     * @throws IOException IO exception.
     */
    private void compareFiles(File base1, File base2, String msg) throws IOException {
        for (File file : base1.listFiles()) {
            String name = file.getName();
            File refFile = new File(base2, name);
            boolean isDirectory = file.isDirectory();
            assertTrue(msg + (isDirectory ? "folder" : "file") + ": " + name, refFile.exists());
            
            if (isDirectory) {
                compareFiles(file, refFile, msg);
            } else {
                assertTrue("File contents is not valid: " + name, FileUtils.contentEquals(file, refFile));
            }
        }
    }
    
    /**
     * Returns a temporary directory to receive extracted output.
     * 
     * @return The directory.
     */
    private File getTargetDir() {
        File file = new File("./target/chm-test/");
        FileUtils.deleteQuietly(file);
        return file;
    }
}
