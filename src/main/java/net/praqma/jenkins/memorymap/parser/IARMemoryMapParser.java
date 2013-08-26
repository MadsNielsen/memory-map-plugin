/*
 * The MIT License
 *
 * Copyright 2013 Unknown.
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
package net.praqma.jenkins.memorymap.parser;

import hudson.Extension;
import hudson.model.Descriptor;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.praqma.jenkins.memorymap.graph.MemoryMapGraphConfiguration;
import net.praqma.jenkins.memorymap.result.MemoryMapConfigMemory;
import net.praqma.jenkins.memorymap.result.MemoryMapConfigMemoryItem;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

/**
 *
 * @author Praqma
 */
public class IARMemoryMapParser extends AbstractMemoryMapParser {
    
/*
 * ROM/Flash 
 */
private static final Pattern INTVEC = Pattern.compile(null, Pattern.MULTILINE);
private static final Pattern OPTBYTE = Pattern.compile(null, Pattern.MULTILINE);
private static final Pattern SECUID = Pattern.compile(null, Pattern.MULTILINE);
private static final Pattern aseg = Pattern.compile(null, Pattern.MULTILINE);
private static final Pattern RCODE = Pattern.compile(null, Pattern.MULTILINE);
private static final Pattern CODE = Pattern.compile(null, Pattern.MULTILINE);
private static final Pattern NEAR_ID = Pattern.compile(null, Pattern.MULTILINE);
private static final Pattern NEAR_CONST = Pattern.compile(null, Pattern.MULTILINE);
private static final Pattern SWITCH = Pattern.compile(null, Pattern.MULTILINE);
private static final Pattern CHECKSUM = Pattern.compile(null, Pattern.MULTILINE);
private static final Pattern NEAR_A = Pattern.compile(null, Pattern.MULTILINE);
private static final Pattern SADDR_A = Pattern.compile(null, Pattern.MULTILINE);
    
    
/*
 * RAM
 */
 private static final Pattern FAR_HEAP_SIZE = Pattern.compile(null, Pattern.MULTILINE);
 private static final Pattern NEAR_CONST_LOCATION_START = Pattern.compile(null, Pattern.MULTILINE);
 private static final Pattern NEAR_CONST_LOCATION_END = Pattern.compile(null, Pattern.MULTILINE);
 private static final Pattern NEAR_CONST_LOCATION = Pattern.compile(null, Pattern.MULTILINE);
 private static final Pattern NEAR_HEAP_SIZE = Pattern.compile(null, Pattern.MULTILINE);
 private static final Pattern CSTACK_SIZE = Pattern.compile(null, Pattern.MULTILINE);
 private static final Pattern NEAR_I = Pattern.compile(null, Pattern.MULTILINE);
 private static final Pattern NEAR_Z = Pattern.compile(null, Pattern.MULTILINE);                         
 private static final Pattern NEAR_N = Pattern.compile(null, Pattern.MULTILINE);                         
 private static final Pattern CSTACK = Pattern.compile(null, Pattern.MULTILINE);                            
 private static final Pattern SADDR_I = Pattern.compile(null, Pattern.MULTILINE);                                
 private static final Pattern SADDR_Z = Pattern.compile(null, Pattern.MULTILINE);  
 
 
    @DataBoundConstructor
    public IARMemoryMapParser(String mapFile, String configurationFile, Integer wordSize, Boolean bytesOnGraph, Pattern... pattern) {
        super(mapFile, configurationFile, wordSize, bytesOnGraph, INTVEC,OPTBYTE,SECUID,aseg,RCODE,CODE,NEAR_ID,NEAR_CONST,SWITCH,CHECKSUM,NEAR_A,SADDR_A,FAR_HEAP_SIZE,
                                                                    NEAR_CONST_LOCATION_START,NEAR_CONST_LOCATION_END,NEAR_CONST_LOCATION,NEAR_HEAP_SIZE,
                                                                    CSTACK_SIZE,NEAR_I,NEAR_Z,NEAR_N,CSTACK,SADDR_I,SADDR_Z);
    }

    public IARMemoryMapParser() {
        super();        
    }
    
    @Override
    public MemoryMapConfigMemory parseConfigFile(List<MemoryMapGraphConfiguration> graphConfig, File f) throws IOException {
        MemoryMapConfigMemory config = new MemoryMapConfigMemory();
        CharSequence sequence = createCharSequenceFromFile(f);
        for (MemoryMapGraphConfiguration graph : graphConfig) {
            String[] split = graph.getGraphDataList().split(",");
            for (String s : split) {
                s.trim();
                String[] multiSections = s.split("\\+");
                for (String ms : multiSections) {
                    Matcher m = MemoryMapConfigFileParserDelegate.getPatternForMemoryLayout(ms.replace(" ", "")).matcher(sequence);
                    MemoryMapConfigMemoryItem item = null;
                    while (m.find()) {
                        item = new MemoryMapConfigMemoryItem(m.group(1), m.group(3), m.group(5));
                        config.add(item);
                    }

                    if (item == null) {
                        logger.logp(Level.WARNING, "parseConfigFile", AbstractMemoryMapParser.class.getName(), String.format("parseConfigFile(List<MemoryMapGraphConfiguration> graphConfig, File f) non existing item: %s", s));
                        throw new IOException(String.format("No match found for program memory named %s", s));
                    }
                }

            }
        }
        return config;
    }

    @Override
    public MemoryMapConfigMemory parseMapFile(File f, MemoryMapConfigMemory config) throws IOException {
        return super.parseMapFile(f, config);
    }

    @Extension
    public static final class DescriptorImpl extends MemoryMapParserDescriptor<IARMemoryMapParser> {

        @Override
        public String getDisplayName() {
            return "IAR";
        }

        @Override
        public AbstractMemoryMapParser newInstance(StaplerRequest req, JSONObject formData, AbstractMemoryMapParser instance) throws Descriptor.FormException {
            IARMemoryMapParser parser = (IARMemoryMapParser) instance;
            save();
            return parser;
        }
    }
}
 
    

