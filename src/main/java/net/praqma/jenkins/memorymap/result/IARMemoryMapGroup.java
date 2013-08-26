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
package net.praqma.jenkins.memorymap.result;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Praqma
 */
public class IARMemoryMapGroup extends LinkedList<String> {
    
    private String groupName;
    private int threshold;
    private List<String> accepts;
    private List<MemoryMapParsingResult> results;
    
    @Deprecated
    public IARMemoryMapGroup(String groupName, int threshold) {
        this.groupName = groupName;
        this.threshold = threshold;
    }
    
    public IARMemoryMapGroup(String groupName, List<MemoryMapParsingResult> results, String... accepts) {
        this.results = results;
        this.groupName = groupName;
        this.accepts = Arrays.asList(accepts);
    }
    
    public IARMemoryMapGroup() { }

    
    /**
     * Factory default flash group
     * @return 
     */
//    public static IARMemoryMapGroup defaultFlashGroup() {
//        IARMemoryMapGroup group = new IARMemoryMapGroup("Flash", Integer.MAX_VALUE);
//        group.addAll(Arrays.asList(".econst",".const",".text",".cinit",".switch",".pinit"));
//        return group;
//    }
    
    /**
     * Factory default ram group
     * @return 
     */
//    public static IARMemoryMapGroup defaultRamGroup() {
//        IARMemoryMapGroup group = new IARMemoryMapGroup("Ram", Integer.MAX_VALUE);
//        group.addAll(Arrays.asList(".stack",".ebss",".bss",".sysmem",".esysmem",".cio",".data"));
//        return group;  
//    }

    /**
     * @return the groupName
     */
    public String getGroupName() {
        return groupName;
    }

    /**
     * @param groupName the groupName to set
     */
    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    /**
     * @return the threshold
     */
    public int getThreshold() {
        return threshold;
    }

    /**
     * @param threshold the threshold to set
     */
    public void setThreshold(int threshold) {
        this.threshold = threshold;
    }
}

    

