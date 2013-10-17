/*
 * The MIT License
 *
 * Copyright 2012 Praqma.
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
package net.praqma.jenkins.memorymap;

import hudson.AbortException;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.BuildListener;
import hudson.model.Hudson;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.praqma.jenkins.memorymap.graph.MemoryMapGraphConfiguration;
import net.praqma.jenkins.memorymap.parser.AbstractMemoryMapParser;
import net.praqma.jenkins.memorymap.parser.MemoryMapConfigFileParserDelegate;
import net.praqma.jenkins.memorymap.parser.MemoryMapMapParserDelegate;
import net.praqma.jenkins.memorymap.parser.MemoryMapParserDescriptor;
import net.praqma.jenkins.memorymap.result.MemoryMapConfigMemory;
import net.praqma.jenkins.memorymap.util.MemoryMapError;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import net.praqma.util.ExceptionUtils.*;
import org.kohsuke.stapler.QueryParameter;
/**
 *
 * @author Praqma
 */
public class MemoryMapRecorder extends Recorder {

    private String mapFile;
    private Integer wordSize;
    private boolean showBytesOnGraph;
    
    @Deprecated
    public final String chosenParser;
    
    @Deprecated
    public final String configurationFile;
    
    public final String scale;
    public final List<AbstractMemoryMapParser> chosenParsers;
    public final List<MemoryMapGraphConfiguration> graphConfiguration;
    private static final Logger log = Logger.getLogger(MemoryMapRecorder.class.getName());
        
    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.BUILD;
    }
    
    @DataBoundConstructor
    public MemoryMapRecorder(String chosenParser, List<AbstractMemoryMapParser> chosenParsers, String configurationFile, boolean showBytesOnGraph, String wordSize, final String scale , final List<MemoryMapGraphConfiguration> graphConfiguration) {
        this.chosenParsers = chosenParsers;
        this.showBytesOnGraph = showBytesOnGraph;
        //TODO: This should be chose at parse-time. The 8 that is...
        this.wordSize = StringUtils.isBlank(wordSize) ? 8 : Integer.parseInt(wordSize);   
        this.scale = scale;
        this.graphConfiguration = graphConfiguration;
        
        //Backwards compatability
        this.chosenParser = chosenParser;
        this.configurationFile = configurationFile;
    }
    
    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
        
        boolean failed = false;
        PrintStream out = listener.getLogger();
                
        HashMap<String, MemoryMapConfigMemory> config = null;
        
        String version = Hudson.getInstance().getPlugin( "memory-map" ).getWrapper().getVersion();
        out.println( "Memory Map Plugin version " + version );
        
        try {
            config = build.getWorkspace().act(new MemoryMapConfigFileParserDelegate(chosenParsers));
            config = build.getWorkspace().act(new MemoryMapMapParserDelegate(chosenParsers, config));            
        } catch(IOException ex) {
            //Catch all known errors (By using a marker interface)
            if (ex instanceof MemoryMapError) {
                out.println(ex.getMessage());
            } else {
                out.println("Unspecified error. Writing trace to log");
                log.log(Level.SEVERE, "Abnormal plugin execution, trace written to log", ex);
                throw new AbortException( String.format("Unspecified error. Please review error message.%nPlease install the logging plugin to record the standard java logger output stream."
                        + "%nThe plugin is described here: https://wiki.jenkins-ci.org/display/JENKINS/Logging+plugin and requires core 1.483  "));
            }       
            return false;
        }

        out.println("Printing configuration");
        if(config != null) {
            out.println();
            out.println(config.toString());
        }

        MemoryMapBuildAction mmba = new MemoryMapBuildAction(build, config);
        mmba.setRecorder(this);
        mmba.setMemoryMapConfig(config);                
        build.getActions().add(mmba);
        
        return true;        
    } 

    /**
     * @return the mapFile
     */
    public String getMapFile() {
        return mapFile;
    }

    /**
     * @param mapFile the mapFile to set
     */
    public void setMapFile(String mapFile) {
        this.mapFile = mapFile;
    }
    
    /**
     * @return the showBytesOnGraph
     */
    public Boolean getShowBytesOnGraph() {
        return showBytesOnGraph;
    }

    /**
     * @param showBytesOnGraph the showBytesOnGraph to set
     */
    public void setShowBytesOnGraph(Boolean showBytesOnGraph) {
        this.showBytesOnGraph = showBytesOnGraph;
    }

    /**
     * @return the wordSize
     */
    public Integer getWordSize() {
        return wordSize;
    }

    /**
     * @param wordSize the wordSize to set
     */
    public void setWordSize(Integer wordSize) {
        this.wordSize = wordSize;
    }
    
    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {

        
        @Override
        public boolean isApplicable(Class<? extends AbstractProject> type) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return "Memory Map Publisher";
        }
        
        public List<MemoryMapParserDescriptor<?>> getParsers() {
            return AbstractMemoryMapParser.getDescriptors();
        }
        


        @Override
        public Publisher newInstance(StaplerRequest req, JSONObject formData) throws FormException {
            MemoryMapRecorder instance = req.bindJSON(MemoryMapRecorder.class, formData);   
            save();            
            return instance;
        }
        
        public DescriptorImpl() {
            super(MemoryMapRecorder.class);
            load();
        }
        
        private List<String> getScales(){
            List<String> scales = new ArrayList<String>();
            scales.add("default");
            scales.add("kilo");
            scales.add("Mega");
            scales.add("Giga");
            return scales;
        }
        
         public ListBoxModel doFillScaleItems() {
            ListBoxModel items = new ListBoxModel();
            for (String scale : getScales()) {
                items.add(scale);
            }
            return items;
        }
         public FormValidation doCheckConfigurationFile(@QueryParameter String configurationFile) {
             return FormValidation.validateRequired(configurationFile);
         }
                 
    }
    
    @Override
    public Action getProjectAction(AbstractProject<?, ?> project) {
        return new MemoryMapProjectAction(project);
    }    
}
