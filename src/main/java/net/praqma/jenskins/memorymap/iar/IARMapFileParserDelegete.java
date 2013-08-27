/*
 * The MIT License
 *
 * Copyright 2013 Praqma.
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
package net.praqma.jenskins.memorymap.iar;

import hudson.remoting.VirtualChannel;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import net.praqma.jenkins.memorymap.parser.AbstractMemoryMapParser;
import net.praqma.jenkins.memorymap.result.MemoryMapConfigMemory;
import net.praqma.jenkins.memorymap.util.FileFoundable;

/**
 * Class to wrap the FileCallable method. Serves as a proxy to the parser method. 
 * @author Praqma
 */
public class IARMapFileParserDelegete extends FileFoundable<MemoryMapConfigMemory>{
    private static final Logger log = Logger.getLogger(IARMapFileParserDelegete.class.getName());
    private AbstractMemoryMapParser parser;
    private MemoryMapConfigMemory config;
    private static HashMap<String,Pattern> patternRegistry;
    
    //Empty constructor. For serialization purposes.
    public IARMapFileParserDelegete() { }

    public IARMapFileParserDelegete(AbstractMemoryMapParser parser) {
        this.parser = parser;
    }
    
    public IARMapFileParserDelegete(AbstractMemoryMapParser parser, MemoryMapConfigMemory config) {
        this.parser = parser;
        this.config = config;        
    }

    @Override
    public MemoryMapConfigMemory invoke(File file, VirtualChannel vc) throws IOException, InterruptedException {        

        try {
            return getParser().parseMapFile(findFile(file, parser.getMapFile()), config); 
        } catch (FileNotFoundException fnfex) {
            log.logp(Level.WARNING, "invoke", IARConfigFileParserDelegate.class.getName(), "invoke caught file not found exception", fnfex);
            throw new IOException(fnfex.getMessage());
        } catch (IOException ex) {
            log.logp(Level.WARNING, "invoke", IARConfigFileParserDelegate.class.getName(), "invoke caught IOException", ex);
            throw new IOException(ex.getMessage());                    
        }
 
    }

    /**
     * @return the parser
     */
    public AbstractMemoryMapParser getParser() {
        return parser;
    }

    /**
     * @param parser the parser to set
     */
    public void setParser(AbstractMemoryMapParser parser) {
        this.parser = parser;
    }
    
    public static Pattern getPatternForMemorySection(String sectionName) {
        if(patternRegistry == null) {
            patternRegistry = new HashMap<String, Pattern>();
        }
        if(patternRegistry.containsKey(sectionName)) {
            return patternRegistry.get(sectionName);
        } else {
            String regex = String.format("^(\\s+)(\\d+)(\\s+)(\\d*)(\\s+)(\\S+)(\\s+)(\\S+)(\\s+)(\\S+)(\\s+)(\\S+)$", sectionName);
            Pattern memsection = Pattern.compile(regex,Pattern.MULTILINE);
            patternRegistry.put(sectionName, memsection);
            return memsection;
        }
    }
}
