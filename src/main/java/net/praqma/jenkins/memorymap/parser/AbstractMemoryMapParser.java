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
package net.praqma.jenkins.memorymap.parser;

import hudson.DescriptorExtensionList;
import hudson.ExtensionPoint;
import hudson.model.Describable;
import hudson.model.Descriptor;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import jenkins.model.Jenkins;
import net.praqma.jenkins.memorymap.graph.MemoryMapGraphConfiguration;
import net.praqma.jenkins.memorymap.graph.MemoryMapGraphConfigurationDescriptor;
import net.praqma.jenkins.memorymap.result.MemoryMapConfigMemory;
import org.apache.commons.collections.ListUtils;

/**
 *
 * @author Praqma
 */
public abstract class AbstractMemoryMapParser implements Describable<AbstractMemoryMapParser>, ExtensionPoint, Serializable {

    private static final String UTF_8_CHARSET = "UTF8";
   
    protected static final Logger logger = Logger.getLogger(AbstractMemoryMapParser.class.toString());
    
    protected List<Pattern> patterns;
    public final List<MemoryMapGraphConfiguration> gConf;
    public final String parserUniqueName;
    protected String mapFile;
    private String configurationFile;
    private Integer wordSize;
    private Boolean bytesOnGraph;

    /**
     * 
     * @return  The default word size. If the map files contains usages in decimal value of bytes (say 1 200 bytes used). Use a word size of 8.
     * else use what your compiler prefers. 
     */
    public abstract int getDefaultWordSize();
    
    public AbstractMemoryMapParser () {  
        this.patterns = ListUtils.EMPTY_LIST;
        this.gConf = new ArrayList<MemoryMapGraphConfiguration>();
        this.parserUniqueName = "Unspecified";
    }
    
    public AbstractMemoryMapParser(String parserUniqueName, String mapFile, String configurationFile, Integer wordSize, Boolean bytesOnGraph, List<MemoryMapGraphConfiguration> graphConfiguration, Pattern... pattern) {
        this.patterns = Arrays.asList(pattern);
        this.mapFile = mapFile;
        this.configurationFile = configurationFile;
        this.wordSize = wordSize;
        this.bytesOnGraph = bytesOnGraph;
        this.gConf = graphConfiguration;
        this.parserUniqueName = parserUniqueName;
    }
    
    /**
     * Implemented in order to get a unique name for the chosen parser
     * @return 
     */
    public String getUniqueName() {
        return String.format("%s_%s_%s", this.getClass().getSimpleName().replace(".class", ""), mapFile, configurationFile);
    }
     
    protected CharSequence createCharSequenceFromFile(File f) throws IOException {
        return createCharSequenceFromFile(UTF_8_CHARSET, f);
    }
     
    protected CharSequence createCharSequenceFromFile(String charset, File f) throws IOException {
        String chosenCharset = charset;
        
        CharBuffer cbuf = null;
        FileInputStream fis = null;
        try 
        {
            fis = new FileInputStream(f.getAbsolutePath());
            FileChannel fc = fis.getChannel();
            ByteBuffer bbuf = fc.map(FileChannel.MapMode.READ_ONLY, 0, (int)fc.size());
            
            if(!Charset.isSupported(chosenCharset)) {
                logger.warning(String.format("The charset %s is not supported", charset));
                cbuf = Charset.defaultCharset().newDecoder().decode(bbuf);
            } else {
                cbuf = Charset.forName(charset).newDecoder().decode(bbuf);
            }
            
        } catch (IOException ex) {
            throw ex;
        } finally {
            if(fis != null) {
                fis.close();
            }
        }
        return cbuf;
    }    
    
    public abstract MemoryMapConfigMemory parseConfigFile(File f) throws IOException;
    public abstract MemoryMapConfigMemory parseMapFile(File f, MemoryMapConfigMemory configuration) throws IOException;


    /**
     * @return the includeFilePattern
     */
    public String getMapFile() {
        return mapFile;
    }

    /**
     * @param includeFilePattern the includeFilePattern to set
     */
    public void setMapFile(String mapFile) {
        this.mapFile = mapFile;
    }
    
    @Override
    public Descriptor<AbstractMemoryMapParser> getDescriptor() {
            return (Descriptor<AbstractMemoryMapParser>) Jenkins.getInstance().getDescriptorOrDie( getClass() );
    }
    
    /**
    * All registered {@link AbstractConfigurationRotatorSCM}s.
    */
   public static DescriptorExtensionList<AbstractMemoryMapParser, MemoryMapParserDescriptor<AbstractMemoryMapParser>> all() {
           return Jenkins.getInstance().<AbstractMemoryMapParser, MemoryMapParserDescriptor<AbstractMemoryMapParser>> getDescriptorList( AbstractMemoryMapParser.class );
   }

   public static List<MemoryMapParserDescriptor<?>> getDescriptors() {
        List<MemoryMapParserDescriptor<?>> list = new ArrayList<MemoryMapParserDescriptor<?>>();
        for( MemoryMapParserDescriptor<?> d : all() ) {
                list.add( d );
        }
        return list;
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

    /**
     * @return the bytesOnGraph
     */
    public Boolean getBytesOnGraph() {
        return bytesOnGraph;
    }

    /**
     * @param bytesOnGraph the bytesOnGraph to set
     */
    public void setBytesOnGraph(Boolean bytesOnGraph) {
        this.bytesOnGraph = bytesOnGraph;
    }

    /**
     * @return the configurationFile
     */
    public String getConfigurationFile() {
        return configurationFile;
    }

    /**
     * @param configurationFile the configurationFile to set
     */
    public void setConfigurationFile(String configurationFile) {
        this.configurationFile = configurationFile;
    } 
   
    public List<MemoryMapGraphConfigurationDescriptor<?>> getGraphOptions() {
        return MemoryMapGraphConfiguration.getDescriptors();
    }

    @Override
    public String toString() {
        return getUniqueName();
    }
}
