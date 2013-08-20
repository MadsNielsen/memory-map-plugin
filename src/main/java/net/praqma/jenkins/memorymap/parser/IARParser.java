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

import java.util.regex.Pattern;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 *
 * @author Unknown
 */
public class IARParser extends AbstractMemoryMapParser {
    
    
/*
 * Ram
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
    public IARParser(String mapFile, String configurationFile, Integer wordSize, Boolean bytesOnGraph, Pattern... pattern) {
        super(mapFile, configurationFile, wordSize, bytesOnGraph, NEAR_I,NEAR_Z,NEAR_N,CSTACK,SADDR_I,SADDR_Z);
    }

    public IARParser() {
        super();        
    }
 
    
}
