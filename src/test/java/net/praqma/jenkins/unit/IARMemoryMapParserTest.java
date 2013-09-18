/*
 * The MIT License
 *
 * Copyright 2013 hugo.
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
package net.praqma.jenkins.unit;

import java.io.File;
import java.io.IOException;
import net.praqma.jenkins.memorymap.parser.MemoryMapMapParserDelegate;
import net.praqma.jenkins.memorymap.parser.iar.IARMemoryMapParser;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Rule;
import org.junit.Test;

/**
 *
 * @author hugo
 */
public class IARMemoryMapParserTest {
    
    
//   @Rule
//    public ResourceFile res = new ResourceFile("/res.txt");
//
//    @Test
//    public void test() throws Exception
//    {
//        assertTrue(res.getContent().length() > 0);
//        assertTrue(res.getFile().exists());
//    }
//
//    private void assertTrue(boolean b) {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//    }
    
    @Test
    public void findFile_test() throws IOException {

        File f = File.createTempFile("testfile", ".test");
                
        System.out.println(f.getAbsolutePath());
        
        MemoryMapMapParserDelegate delegate = new MemoryMapMapParserDelegate();
        
        IARMemoryMapParser parser = new IARMemoryMapParser("*.config","*.test",8,true);
        delegate.setParser(parser);
        
        assertNotNull(delegate.getParser());
        assertNotNull(parser.getMapFile());
        assertNotNull(parser.getConfigurationFile());
        
        File test = new File(f.getAbsolutePath().substring(0,f.getAbsolutePath().lastIndexOf(File.separator)));
        assertTrue(test.isDirectory());
        
        
        try {
            delegate.findFile(test,"*.test");
            
        } catch(Exception ex) {
            
            fail("Parser did not find the file"+ex);
        } finally {
            f.deleteOnExit();
        }
        
    }
    
}
