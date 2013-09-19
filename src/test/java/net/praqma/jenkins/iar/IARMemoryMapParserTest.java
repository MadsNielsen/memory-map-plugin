/*
 * The MIT License
 *
 * Copyright 2013 Martin.
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
package net.praqma.jenkins.iar;

import org.junit.Test;
import static org.junit.Assert.*;
import net.praqma.jenkins.memorymap.parser.iar.*;

/**
 *
 * @author Martin
 */
public class IARMemoryMapParserTest {

    public IARMemoryMapParserTest() {
    }

    @Test
    public void testGetPatternForMemoryTypeDividedConfig() {
        assertEquals("(-[P+Z]).(CODE).[\\w+,]+=(.)([A-f,0-9]{4,})\\-([A-f,0-9]{4,})(]/10000)$",
                IARMemoryMapParser.getPatternForMemoryTypeDividedConfig("CODE").pattern());
    }

    @Test
    public void testGetPatternForConstMemoryConfig() {
        assertEquals("(-[P+Z]).(CONST).(\\w..)+=([A-f,0-9]{4,})\\-([A-f,0-9]{4,})$",
                IARMemoryMapParser.getPatternForConstMemoryConfig("CONST").pattern());
    }

    @Test
    public void testGetPatternForDataAndCodeMemoryConfig() {
        assertEquals("(-[P+Z]).(DATA).[\\w+,]+=([A-f,0-9]{4,})\\-([A-f,0-9]{4,})$",
                IARMemoryMapParser.getPatternForDataAndCodeMemoryConfig("DATA").pattern());
    }
    
    @Test
    public void testGetPatternForConstMemoryConfigSharp() {
        assertEquals("(-[P+Z]).(CONST).(\\w*)+#([A-f,0-9]{4,})$",
                IARMemoryMapParser.getPatternForConstMemoryConfigSharp("CONST").pattern());
    }
    
    @Test
    public void testGetPatternForMemoryType() {
        assertEquals("([\\d|\\s]*)\\sbytes of (DATA)",
                IARMemoryMapParser.getPatternForMemoryType("DATA").pattern());
    }

    @Test
    public void testGetDefaultWordSize() {
        IARMemoryMapParser parser = new IARMemoryMapParser();
        assertEquals("8 equals parser.getDefaultWordSize()", 8, parser.getDefaultWordSize());
        assertTrue("8 is not equal to parser.getDefaultWordSize()", 16 != parser.getDefaultWordSize());
    }
}
