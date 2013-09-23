package net.praqma.jenkins.memorymap.parser.gcc;

import hudson.Extension;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.praqma.jenkins.memorymap.graph.MemoryMapGraphConfiguration;
import net.praqma.jenkins.memorymap.parser.AbstractMemoryMapParser;
import net.praqma.jenkins.memorymap.parser.MemoryMapParserDescriptor;
import net.praqma.jenkins.memorymap.result.MemoryMapConfigMemory;
import net.praqma.jenkins.memorymap.result.MemoryMapConfigMemoryItem;
import net.praqma.jenkins.memorymap.util.HexUtils;
import net.praqma.jenkins.memorymap.util.MemoryMapMemorySelectionError;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

/**
 * @author Praqma
 */
public class GccMemoryMapParser extends AbstractMemoryMapParser implements Serializable {    
    private static final Pattern MEM_SECTIONS = Pattern.compile("(\\S+)( : \\{[^\\}]\\n)+");    
    
    @DataBoundConstructor
    public GccMemoryMapParser(String mapFile, String configurationFile, Integer wordSize, Boolean bytesOnGraph) {
        super(mapFile, configurationFile, wordSize, bytesOnGraph);
    }
    
    /**
     * @return  a list of the defined MEMORY in the map file
     */
    public MemoryMapConfigMemory getMemory(File f) throws IOException {
        Pattern allMemory = Pattern.compile(String.format("\\s+(%s)(.*?)(?=ORIGIN)(ORIGIN)(.*?)(?=\\=)(\\=)(.*?)(?=,)(,)(\\s+)(LENGTH)(.*?)(?=\\=)(\\=)(.*)", "\\S+"));
        CharSequence seq = createCharSequenceFromFile(f);
        Matcher match = allMemory.matcher(seq);
        MemoryMapConfigMemory memory = new MemoryMapConfigMemory();
        while(match.find()) {
            
            String hexLength = new HexUtils.HexifiableString(match.group(12)).toValidHexString().rawString;
            
            MemoryMapConfigMemoryItem item = new MemoryMapConfigMemoryItem(match.group(1), match.group(6), hexLength);
            memory.add(item);            
        }
        return memory;
    }
    
    /*
     * SECTIONS {
     * ---
     * secname start BLOCK(align) (NOLOAD) : AT ( ldadr )
        { contents } >region =fill
        ...
        }   
     * 
     */
    
    /**
     * For now hardcoded sections this pattern: (.text : {[^}]*}) matches the entire contents of the .text section. 
     * @return 
     */
    public List<MemoryMapConfigMemoryItem> getSections(File f) throws IOException {
        List<MemoryMapConfigMemoryItem> items = new ArrayList<MemoryMapConfigMemoryItem>();
        CharSequence m = createCharSequenceFromFile(f);
        Matcher match = MEM_SECTIONS.matcher(m);
        while(match.find()) {
            MemoryMapConfigMemoryItem it = new MemoryMapConfigMemoryItem(match.group(1), "0");
            items.add(it);
        }
  
        return items;
        
    }
    
    public GccMemoryMapParser() { 
        super();
    }
    
    public Pattern getLinePatternForMapFile(String sectionName) {
        Pattern p = Pattern.compile(String.format ( "^(%s)(\\s+)(\\S+)(\\s+)(\\S+)(\\S*)", sectionName), Pattern.MULTILINE );
        return p;
    }
    /**
     * Given an item with length == null. 
     * Look down in the list. If we find an item whoose length is not null, set the items length to that
     * @param memory
     * @return a more complete configuration, where i have better values  
     */
    public MemoryMapConfigMemory guessLengthOfSections(MemoryMapConfigMemory memory) {
        Collections.sort(memory);
        for(MemoryMapConfigMemoryItem item : memory) {
            if (item.getLength() == null ) {
                int itemIndex = memory.indexOf(item);
                
                for(int i = itemIndex; i < memory.size(); i++) {
                    if(memory.get(i).getLength() != null) {
                        item.setLength(memory.get(i).getLength());
                        break;
                    }
                } 
                
            }
        }
        return memory;
    }
    
    @Override
    public MemoryMapConfigMemory parseMapFile(File f, MemoryMapConfigMemory configuration) throws IOException {
        
        for(MemoryMapConfigMemoryItem item : configuration) {
            Matcher m = getLinePatternForMapFile(item.getName()).matcher(createCharSequenceFromFile(f));            
            while(m.find()) {
                item.setOrigin(m.group(3));
                item.setUsed(m.group(5));
            }
        }
        return guessLengthOfSections(configuration);
    }
    
    @Override
    public MemoryMapConfigMemory parseConfigFile(List<MemoryMapGraphConfiguration> graphConfig, File f) throws IOException {
        
        //Collect sections from both the MEMORY and the SECTIONS areas from the command file.
        MemoryMapConfigMemory memconfig = getMemory(f);
        memconfig.addAll(getSections(f));
        
        for(MemoryMapGraphConfiguration g : graphConfig) {
            for(String gItem : g.itemizeGraphDataList()) {
                for (String gSplitItem : gItem.split("\\+") ) {
                    //We will fail if the name of the data section does not match any of the named items in the map file.
                    if(!memconfig.containsSectionWithName(gSplitItem)) {
                        throw new MemoryMapMemorySelectionError(String.format( "The memory section named %s not found in map file", gSplitItem)); 
                    }
                }
            }
        }
        
        return memconfig;  
    }

    @Override
    public int getDefaultWordSize() {
        return 8;
    }

    @Override
    public MemoryMapConfigMemory parseConfigFile(List<MemoryMapGraphConfiguration> graphConfig, File f) throws IOException {
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

