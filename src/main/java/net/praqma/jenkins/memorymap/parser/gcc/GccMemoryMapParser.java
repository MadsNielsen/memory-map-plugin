package net.praqma.jenkins.memorymap.parser.gcc;

import hudson.Extension;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.regex.Pattern;
import net.praqma.jenkins.memorymap.parser.AbstractMemoryMapParser;
import net.praqma.jenkins.memorymap.parser.MemoryMapParserDescriptor;
import net.praqma.jenkins.memorymap.result.MemoryMapConfigMemory;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

/**
 * @author Praqma
 */
public class GccMemoryMapParser extends AbstractMemoryMapParser implements Serializable {

    private static final Pattern TEXT_DOT = Pattern.compile("^\\.text\\s+\\S+\\s+\\S+\\s+(\\S+)", Pattern.MULTILINE);
    private static final Pattern CONST_DOT = Pattern.compile("^\\.econst\\s+\\S+\\s+\\S+\\s+(\\S+)", Pattern.MULTILINE);
    
    private static final Pattern CINIT_DOT = Pattern.compile("^\\.cinit\\s+\\S+\\s+\\S+\\s+(\\S+)", Pattern.MULTILINE);
    private static final Pattern STACK_DOT = Pattern.compile("^\\.stack\\s+\\S+\\s+\\S+\\s+(\\S+)", Pattern.MULTILINE);
    private static final Pattern BSS_DOT = Pattern.compile("^\\.ebss\\s+\\S+\\s+\\S+\\s+(\\S+)", Pattern.MULTILINE);
    
    @DataBoundConstructor
    public GccMemoryMapParser(String mapFile, String configurationFile, Integer wordSize, Boolean bytesOnGraph) {
        super(mapFile, configurationFile, wordSize, bytesOnGraph, TEXT_DOT, CONST_DOT, CINIT_DOT, STACK_DOT, BSS_DOT);
    }
    
    public GccMemoryMapParser() { 
        super();
    }

    @Override
    public MemoryMapConfigMemory parseMapFile(File f, MemoryMapConfigMemory configuration) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Extension
    public static final class DescriptorImpl extends MemoryMapParserDescriptor<GccMemoryMapParser> {

        @Override
        public String getDisplayName() {
            return "Gcc";
        }

        @Override
        public AbstractMemoryMapParser newInstance(StaplerRequest req, JSONObject formData, AbstractMemoryMapParser instance) throws FormException {
            GccMemoryMapParser parser = (GccMemoryMapParser)instance;
            save();
            return parser;
        } 
    }    
}

