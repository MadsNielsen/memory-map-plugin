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
package net.praqma.jenkins.memorymap.parser.iar;

import hudson.Extension;
import hudson.model.Descriptor;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.praqma.jenkins.memorymap.graph.MemoryMapGraphConfiguration;
import net.praqma.jenkins.memorymap.graph.MemoryMapGraphConfigurationDescriptor;
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
 *
 * @author Praqma
 */
public class IARMemoryMapParser extends AbstractMemoryMapParser {

    @DataBoundConstructor
    public IARMemoryMapParser(String mapFile, String configurationFile, Integer wordSize, Boolean bytesOnGraph, List<MemoryMapGraphConfiguration> graphConfiguration, Pattern... pattern) {
        super(mapFile, configurationFile, wordSize, bytesOnGraph, graphConfiguration);
    }

    public IARMemoryMapParser() {
        super();
    }
    
    private static Pattern getPatternForMemoryTypeDividedConfig(String memoryTypeName) {
        String RegEx = String.format("(-[P+Z]).(%s).[\\w+,]+=(.)([A-f,0-9]{4,})\\-([A-f,0-9]{4,})(]/10000)$", memoryTypeName);
        Pattern memoryType = Pattern.compile(RegEx, Pattern.MULTILINE);
        return memoryType;
    }

    private static Pattern getPatternForConstMemoryConfig(String memoryTypeName) {
        String RegEx = String.format("(-[P+Z]).(%s).(\\w..)+=([A-f,0-9]{4,})\\-([A-f,0-9]{4,})$", memoryTypeName);
        Pattern memoryType = Pattern.compile(RegEx, Pattern.MULTILINE);
        return memoryType;
    }
    
    private static Pattern getPatternForDataAndCodeMemoryConfig(String memoryTypeName) {
        String RegEx = String.format("(-[P+Z]).(%s).[\\w+,]+=([A-f,0-9]{4,})\\-([A-f,0-9]{4,})$", memoryTypeName);
        Pattern memoryType = Pattern.compile(RegEx, Pattern.MULTILINE);
        return memoryType;
    }

    private static Pattern getPatternForConstMemoryConfigSharp(String memoryTypeName) {
        String RegEx = String.format("(-[P+Z]).(%s).(\\w*)+#([A-f,0-9]{4,})$", memoryTypeName);
        Pattern memoryType = Pattern.compile(RegEx, Pattern.MULTILINE);
        return memoryType;
    }

    private static Pattern getPatternForMemoryType(String memoryTypeName) {
        String RegEx = String.format("([\\d|\\s]*)\\sbytes of (%s)", memoryTypeName);
        Pattern memoryType = Pattern.compile(RegEx, Pattern.MULTILINE);
        return memoryType;
    }

    @Override
    public MemoryMapConfigMemory parseConfigFile(File f) throws IOException {
        MemoryMapConfigMemory config = new MemoryMapConfigMemory();
        CharSequence sequence = createCharSequenceFromFile(f);
        for (MemoryMapGraphConfiguration graph : gConf) {            
            for (String s : graph.itemizeGraphDataList()) {

                String[] multiSections = s.trim().split("\\+");
                for (String ms : multiSections) {

                    if (ms.matches("CODE")) {

                        Matcher codeMatcher1 = getPatternForMemoryTypeDividedConfig(ms.replace(" ", "")).matcher(sequence);
                        Matcher codeMatcher2 = getPatternForDataAndCodeMemoryConfig(ms.replace(" ", "")).matcher(sequence);

                        MemoryMapConfigMemoryItem codeItem1 = null;
                        MemoryMapConfigMemoryItem codeItem2 = null;
       
                        while (codeMatcher1.find()) {
                            codeItem1 = new MemoryMapConfigMemoryItem(codeMatcher1.group(2), codeMatcher1.group(4));
                            codeItem1.setEndAddress(codeMatcher1.group(5));
                            codeItem1.setCalculatedLength(codeMatcher1.group(4), codeMatcher1.group(5));
                            config.add(codeItem1);                    
                        }
                        
                        while (codeMatcher2.find()) {
                            codeItem2 = new MemoryMapConfigMemoryItem(codeMatcher2.group(2), codeMatcher2.group(3));
                            codeItem2.setEndAddress(codeMatcher2.group(4));
                            codeItem2.setCalculatedLength(codeMatcher2.group(3), codeMatcher2.group(4));
                            config.add(codeItem2);
                        }
                    }

                    if (ms.matches("DATA")) {

                        Matcher dataMatcher1 = getPatternForMemoryTypeDividedConfig(ms.replace(" ", "")).matcher(sequence);
                        Matcher dataMatcher2 = getPatternForDataAndCodeMemoryConfig(ms.replace(" ", "")).matcher(sequence);
                        MemoryMapConfigMemoryItem dataItem1 = null;
                        MemoryMapConfigMemoryItem dataItem2 = null;

                        while (dataMatcher1.find()) {
                            dataItem1 = new MemoryMapConfigMemoryItem(dataMatcher1.group(2), dataMatcher1.group(4));
                            dataItem1.setEndAddress(dataMatcher1.group(5));
                            dataItem1.setCalculatedLength(dataMatcher1.group(4), dataMatcher1.group(5));
                            config.add(dataItem1);
                        }

                        while (dataMatcher2.find()) {
                            dataItem2 = new MemoryMapConfigMemoryItem(dataMatcher2.group(2), dataMatcher2.group(3));
                            dataItem2.setEndAddress(dataMatcher2.group(4));
                            dataItem2.setCalculatedLength(dataMatcher2.group(3), dataMatcher2.group(4));
                            config.add(dataItem2);
                        }
                    }

                    if (ms.matches("CONST")) {

                        Matcher constMatcher1 = getPatternForMemoryTypeDividedConfig(ms.replace(" ", "")).matcher(sequence);
                        Matcher constMatcher2 = getPatternForConstMemoryConfig(ms.replace(" ", "")).matcher(sequence);
                        Matcher constMatcher3 = getPatternForConstMemoryConfigSharp(ms.replace(" ", "")).matcher(sequence);
                        MemoryMapConfigMemoryItem constItem1 = null;
                        MemoryMapConfigMemoryItem constItem2 = null;
                        MemoryMapConfigMemoryItem constItem3 = null;

                        while (constMatcher1.find()) {
                            constItem1 = new MemoryMapConfigMemoryItem(constMatcher1.group(2), constMatcher1.group(4));
                            constItem1.setEndAddress(constMatcher1.group(5));
                            //constItem1.setLength(getSegmentLength(constMatcher1.group(4), constMatcher1.group(5)) + "");
                            constItem1.setCalculatedLength(constMatcher1.group(4), constMatcher1.group(5));
                            config.add(constItem1);
                        }

                        while (constMatcher2.find()) {
                            constItem2 = new MemoryMapConfigMemoryItem(constMatcher2.group(2), constMatcher2.group(4));
                            constItem2.setEndAddress(constMatcher2.group(5));
                            //constItem2.setLength(getSegmentLength(constMatcher2.group(4), constMatcher2.group(5)) + "");
                            constItem2.setCalculatedLength(constMatcher2.group(4), constMatcher2.group(5));
                            config.add(constItem2);
                        }

                        while (constMatcher3.find()) {
                            constItem3 = new MemoryMapConfigMemoryItem(constMatcher3.group(2), constMatcher3.group(4));
                            constItem3.setEndAddress(constMatcher3.group(4));
                            //constItem3.setLength(getSegmentLength(constMatcher3.group(4), constMatcher3.group(4)) + "");
                            constItem3.setCalculatedLength(constMatcher3.group(4), constMatcher3.group(4));
                            config.add(constItem3);
                        }
                    }
                }
            }
        }
        return config;
    }

    @Override
    public MemoryMapConfigMemory parseMapFile(File f, MemoryMapConfigMemory config) throws IOException {
        CharSequence sequence = createCharSequenceFromFile(f);
        for (MemoryMapConfigMemoryItem codeItem : config) {
            Matcher matcher = getPatternForMemoryType(codeItem.getName()).matcher(sequence);
            boolean found = false;
            
            while (matcher.find()) {
                HexUtils.HexifiableString s = new HexUtils.HexifiableString(Integer.parseInt(matcher.group(1).replaceAll("\\s", "")));
                codeItem.setUsed(s.rawString);
                found = true;
            }
            
            if (!found) {                    
                logger.logp(Level.WARNING, "parseMapFile", IARMemoryMapParser.class.getName(), String.format("parseMapFile(File f, MemoryMapConfigMemory configuration) non existing item: %s", codeItem));
                throw new MemoryMapMemorySelectionError(String.format("Linker command element %s not found in .map file", codeItem));
            }
            
        }
        return config;

    }

    @Override
    public int getDefaultWordSize() {
       return 8;
    }

    @Extension
    public static final class DescriptorImpl extends MemoryMapParserDescriptor<IARMemoryMapParser> {

        @Override
        public String getDisplayName() {
            return "IAR";
        }

        @Override
        public IARMemoryMapParser newInstance(StaplerRequest req, JSONObject formData, AbstractMemoryMapParser instance) throws Descriptor.FormException {
            IARMemoryMapParser parser = (IARMemoryMapParser) instance;
            save();
            return parser;
        }

    }
}