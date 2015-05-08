/*
 * The MIT License
 *
 * Copyright 2015 Mads.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package net.praqma.jenkins.integration;

import hudson.FilePath;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Result;
import java.io.BufferedReader;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipInputStream;
import net.praqma.jenkins.memorymap.MemoryMapRecorder;
import net.praqma.jenkins.memorymap.graph.MemoryMapGraphConfiguration;
import net.praqma.jenkins.memorymap.parser.AbstractMemoryMapParser;
import net.praqma.jenkins.memorymap.parser.gcc.GccMemoryMapParser;
import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

/**
 *
 * @author Mads
 */
public class MemoryMapGccFailureWithGcc483IT {
    @Rule
    public JenkinsRule jenkins = new JenkinsRule();
    
    @Test
    public void testParseGcc483LDfile() throws Exception {
        FreeStyleProject fsp = jenkins.createFreeStyleProject("ggc_483_IT");
        MemoryMapGraphConfiguration config = new MemoryMapGraphConfiguration(".prom_text+.ram_data", "Rom usage", Boolean.TRUE);
        List<MemoryMapGraphConfiguration> graphs = Arrays.asList(config);
        
        AbstractMemoryMapParser parser = new GccMemoryMapParser("myParser", "**/gcc482.map", "**/prom482.ld", 8, true, graphs);
        MemoryMapRecorder recorder = new MemoryMapRecorder(Arrays.asList(parser), true, null, null, graphs);
        fsp.getPublishersList().add(recorder);
        
        //Schedule a build that should fail. We need to create the workspace.
        FreeStyleBuild b = fsp.scheduleBuild2(0).get();
        jenkins.assertBuildStatus(Result.FAILURE, b);
  
        File zipfile = new File(this.getClass().getResource("gcc482.zip").getFile());
        System.out.println(zipfile.getAbsolutePath());
        
        //Copy the zip file to the workspace
        FileUtils.copyFileToDirectory(zipfile, new File(b.getWorkspace().absolutize().getRemote()), true);
        
        //Unzip the contents of our zip file into the workspace.
        FilePath zipInWorkspace = new FilePath(b.getWorkspace(), "gcc482.zip");
        zipInWorkspace.unzip(b.getWorkspace());
        
        //Run build again. We should detect these two memory sections
        FreeStyleBuild b2 = fsp.scheduleBuild2(0).get();
        
        BufferedReader reader = new BufferedReader(b2.getLogReader());
        while(reader.readLine() != null) {
            System.out.println(reader.readLine());
        }
        reader.close();
        
        jenkins.assertBuildStatus(Result.SUCCESS, b2);
       
    }
}
