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
import net.praqma.jenkins.memorymap.parser.AbstractMemoryMapParser;
import net.praqma.jenkins.memorymap.parser.MemoryMapParserDescriptor;
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
     * Flash // Rom
     */
    private static final Pattern INTVEC = Pattern.compile("", Pattern.MULTILINE);
    private static final Pattern OPTBYTE = Pattern.compile("", Pattern.MULTILINE);
    private static final Pattern SECUID = Pattern.compile("", Pattern.MULTILINE);
    private static final Pattern aseg = Pattern.compile("", Pattern.MULTILINE);
    private static final Pattern RCODE = Pattern.compile("", Pattern.MULTILINE);
    private static final Pattern CODE = Pattern.compile("", Pattern.MULTILINE);
    private static final Pattern NEAR_ID = Pattern.compile("", Pattern.MULTILINE);
    private static final Pattern NEAR_CONST = Pattern.compile("", Pattern.MULTILINE);
    private static final Pattern SWITCH = Pattern.compile("", Pattern.MULTILINE);
    private static final Pattern CHECKSUM = Pattern.compile("", Pattern.MULTILINE);
    private static final Pattern NEAR_A = Pattern.compile("", Pattern.MULTILINE);
    private static final Pattern SADDR_A = Pattern.compile("", Pattern.MULTILINE);
    /*
     * Ram
     */
    private static final Pattern FAR_HEAP_SIZE = Pattern.compile("", Pattern.MULTILINE);
    private static final Pattern NEAR_CONST_LOCATION_START = Pattern.compile("", Pattern.MULTILINE);
    private static final Pattern NEAR_CONST_LOCATION_END = Pattern.compile("", Pattern.MULTILINE);
    private static final Pattern NEAR_CONST_LOCATION = Pattern.compile("", Pattern.MULTILINE);
    private static final Pattern NEAR_HEAP_SIZE = Pattern.compile("", Pattern.MULTILINE);
    private static final Pattern CSTACK_SIZE = Pattern.compile("", Pattern.MULTILINE);
    private static final Pattern NEAR_I = Pattern.compile("", Pattern.MULTILINE);
    private static final Pattern NEAR_Z = Pattern.compile("", Pattern.MULTILINE);
    private static final Pattern NEAR_N = Pattern.compile("", Pattern.MULTILINE);
    private static final Pattern CSTACK = Pattern.compile("", Pattern.MULTILINE);
    private static final Pattern SADDR_I = Pattern.compile("", Pattern.MULTILINE);
    private static final Pattern SADDR_Z = Pattern.compile("", Pattern.MULTILINE);

    @DataBoundConstructor
    public IARMemoryMapParser(String mapFile, String configurationFile, Integer wordSize, Boolean bytesOnGraph, Pattern... pattern) {
        super(mapFile, configurationFile, wordSize, bytesOnGraph, INTVEC, OPTBYTE, SECUID, aseg, RCODE, CODE, NEAR_ID, NEAR_CONST,
                SWITCH, CHECKSUM, NEAR_A, SADDR_A, FAR_HEAP_SIZE, NEAR_CONST_LOCATION_START, NEAR_CONST_LOCATION_END, NEAR_CONST_LOCATION, NEAR_HEAP_SIZE,
                CSTACK_SIZE, NEAR_I, NEAR_Z, NEAR_N, CSTACK, SADDR_I, SADDR_Z);
    }

    public IARMemoryMapParser() {
        super();
    }

    private static Pattern getPatternForCodeMemoryDividedConfig(String memoryTypeName) {
        String RegEx = String.format("(-[P+Z]).(%s).[\\w*,]+=(.)([A-f,0-9]{4,})\\-([A-f,0-9]{4,})(]/10000)$", memoryTypeName);
        Pattern memoryType = Pattern.compile(RegEx, Pattern.MULTILINE);
        return memoryType;
    }

    private static Pattern getPatternForCodeMemoryConfig(String memoryTypeName) {
        String RegEx = String.format("(-[P+Z]).(%s).[\\w*,]+=([A-f,0-9]{4,})\\-([A-f,0-9]{4,})$", memoryTypeName);
        Pattern memoryType = Pattern.compile(RegEx, Pattern.MULTILINE);
        return memoryType;
    }

    private static Pattern getPatternForDataMemoryConfig(String memoryTypeName) {
        String RegEx = String.format("(-[P+Z]).(%s).[\\w+,]+=([A-f,0-9]{4,})\\-([A-f,0-9]{4,})$", memoryTypeName);
        Pattern memoryType = Pattern.compile(RegEx, Pattern.MULTILINE);
        return memoryType;
    }

    private static Pattern getPatternForDataMemoryDividedConfig(String memoryTypeName) {
        String RegEx = String.format("(-[P+Z]).(%s).[\\w+,]+=(.)([A-f,0-9]{4,})-([A-f,0-9]{4,})(]/10000)$", memoryTypeName);
        Pattern memoryType = Pattern.compile(RegEx, Pattern.MULTILINE);
        return memoryType;
    }

    private static Pattern getPatternForConstMemoryConfig(String memoryTypeName) {
        String RegEx = String.format("(-[P+Z]).(%s).(\\w..)+=([A-f,0-9]{4,})\\-([A-f,0-9]{4,})$", memoryTypeName);
        Pattern memoryType = Pattern.compile(RegEx, Pattern.MULTILINE);
        return memoryType;
    }

    private static Pattern getPatternForConstMemoryDividedConfig(String memoryTypeName) {
        String RegEx = String.format("(-[P+Z]).(%s).[\\w+,]+=(.)([A-f,0-9]{4,})\\-([A-f,0-9]{4,})(]/10000)$", memoryTypeName);
        Pattern memoryType = Pattern.compile(RegEx, Pattern.MULTILINE);
        return memoryType;
    }

    private static Pattern getPatternForConstMemoryConfigSharp(String memoryTypeName) {
        String RegEx = String.format("(-[P+Z]).(%s).(\\w*)+#([A-f,0-9]{4,})$", memoryTypeName);
        Pattern memoryType = Pattern.compile(RegEx, Pattern.MULTILINE);
        return memoryType;
    }

    private static Pattern getPatternForCodeMemoryMap(String memoryTypeName) {
        String RegEx = String.format("([\\d|\\s]*)\\sbytes of (%s)", memoryTypeName);
        Pattern memoryType = Pattern.compile(RegEx, Pattern.MULTILINE);
        return memoryType;
    }

    private static Pattern getPatternForDataMemoryMap(String memoryTypeName) {
        String RegEx = String.format("([\\d|\\s]*)\\sbytes of (%s)", memoryTypeName);
        Pattern memoryType = Pattern.compile(RegEx, Pattern.MULTILINE);
        return memoryType;
    }

    private static Pattern getPatternForConstMemoryMap(String memoryTypeName) {
        String RegEx = String.format("([\\d|\\s]*)\\sbytes of (%s)", memoryTypeName);
        Pattern memoryType = Pattern.compile(RegEx, Pattern.MULTILINE);
        return memoryType;
    }

    private int getSegmentLength(String start, String end) {
        int startHexToInt = Integer.parseInt(start, 16);
        int endHexToInt = Integer.parseInt(end, 16);

        int length = ((endHexToInt - startHexToInt) + 1);
        return length;
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

                    if (ms.matches("CODE")) {

                        Matcher codeMatcher1 = getPatternForCodeMemoryDividedConfig(ms.replace(" ", "")).matcher(sequence);
                        Matcher codeMatcher2 = getPatternForCodeMemoryConfig(ms.replace(" ", "")).matcher(sequence);

                        MemoryMapConfigMemoryItem codeItem1 = null;
                        MemoryMapConfigMemoryItem codeItem2 = null;

                        while (codeMatcher1.find()) {
                            codeItem1 = new MemoryMapConfigMemoryItem(codeMatcher1.group(2), codeMatcher1.group(4));
                            codeItem1.setEndAddress(codeMatcher1.group(5));
                            codeItem1.setLength(getSegmentLength(codeMatcher1.group(4), codeMatcher1.group(5)) + "");
                            config.add(codeItem1);

                            if (codeItem1 == null) {
                                logger.logp(Level.WARNING, "parseConfigFile", IARMemoryMapParser.class.getName(),
                                        String.format("parseConfigFile(List<MemoryMapGraphConfiguration> graphConfig, File f) non existing item: %s", s));
                                throw new IOException(String.format("No match found for program memory named %s", s));
                            }
                        }
                        while (codeMatcher2.find()) {
                            codeItem2 = new MemoryMapConfigMemoryItem(codeMatcher2.group(2), codeMatcher2.group(3));
                            codeItem2.setEndAddress(codeMatcher2.group(4));
                            codeItem2.setLength(getSegmentLength(codeMatcher2.group(3), codeMatcher2.group(4)) + "");
                            config.add(codeItem2);

                            if (codeItem2 == null) {
                                logger.logp(Level.WARNING, "parseConfigFile", IARMemoryMapParser.class.getName(),
                                        String.format("parseConfigFile(List<MemoryMapGraphConfiguration> graphConfig, File f) non existing item: %s", s));
                                throw new IOException(String.format("No match found for program memory named %s", s));
                            }
                        }
                    }

                    if (ms.matches("DATA")) {

                        Matcher dataMatcher1 = getPatternForDataMemoryDividedConfig(ms.replace(" ", "")).matcher(sequence);
                        Matcher dataMatcher2 = getPatternForDataMemoryConfig(ms.replace(" ", "")).matcher(sequence);
                        MemoryMapConfigMemoryItem dataItem1 = null;
                        MemoryMapConfigMemoryItem dataItem2 = null;

                        while (dataMatcher1.find()) {
                            dataItem1 = new MemoryMapConfigMemoryItem(dataMatcher1.group(2), dataMatcher1.group(4));
                            dataItem1.setEndAddress(dataMatcher1.group(5));
                            dataItem1.setLength(getSegmentLength(dataMatcher1.group(4), dataMatcher1.group(5)) + "");
                            config.add(dataItem1);

                            if (dataItem1 == null) {
                                logger.logp(Level.WARNING, "parseConfigFile", IARMemoryMapParser.class.getName(),
                                        String.format("parseConfigFile(List<MemoryMapGraphConfiguration> graphConfig, File f) non existing item: %s", s));
                                throw new IOException(String.format("No match found for program memory named %s", s));
                            }
                        }

                        while (dataMatcher2.find()) {
                            dataItem2 = new MemoryMapConfigMemoryItem(dataMatcher2.group(2), dataMatcher2.group(3));
                            dataItem2.setEndAddress(dataMatcher2.group(4));
                            dataItem2.setLength(getSegmentLength(dataMatcher2.group(3), dataMatcher2.group(4)) + "");
                            config.add(dataItem2);

                            if (dataItem2 == null) {
                                logger.logp(Level.WARNING, "parseConfigFile", IARMemoryMapParser.class.getName(),
                                        String.format("parseConfigFile(List<MemoryMapGraphConfiguration> graphConfig, File f) non existing item: %s", s));
                                throw new IOException(String.format("No match found for program memory named %s", s));
                            }
                        }
                    }

                    if (ms.matches("CONST")) {

                        Matcher constMatcher1 = getPatternForConstMemoryDividedConfig(ms.replace(" ", "")).matcher(sequence);
                        Matcher constMatcher2 = getPatternForConstMemoryConfig(ms.replace(" ", "")).matcher(sequence);
                        Matcher constMatcher3 = getPatternForConstMemoryConfigSharp(ms.replace(" ", "")).matcher(sequence);
                        MemoryMapConfigMemoryItem constItem1 = null;
                        MemoryMapConfigMemoryItem constItem2 = null;
                        MemoryMapConfigMemoryItem constItem3 = null;

                        while (constMatcher1.find()) {
                            constItem1 = new MemoryMapConfigMemoryItem(constMatcher1.group(2), constMatcher1.group(4));
                            constItem1.setEndAddress(constMatcher1.group(5));
                            constItem1.setLength(getSegmentLength(constMatcher1.group(4), constMatcher1.group(5)) + "");
                            config.add(constItem1);

                            if (constItem1 == null) {
                                logger.logp(Level.WARNING, "parseConfigFile", IARMemoryMapParser.class.getName(),
                                        String.format("parseConfigFile(List<MemoryMapGraphConfiguration> graphConfig, File f) non existing item: %s", s));
                                throw new IOException(String.format("No match found for program memory named %s", s));
                            }
                        }

                        while (constMatcher2.find()) {
                            constItem2 = new MemoryMapConfigMemoryItem(constMatcher2.group(2), constMatcher2.group(4));
                            constItem2.setEndAddress(constMatcher2.group(5));
                            constItem2.setLength(getSegmentLength(constMatcher2.group(4), constMatcher2.group(5)) + "");
                            config.add(constItem2);

                            if (constItem2 == null) {
                                logger.logp(Level.WARNING, "parseConfigFile", IARMemoryMapParser.class.getName(),
                                        String.format("parseConfigFile(List<MemoryMapGraphConfiguration> graphConfig, File f) non existing item: %s", s));
                                throw new IOException(String.format("No match found for program memory named %s", s));
                            }
                        }

                        while (constMatcher3.find()) {
                            constItem3 = new MemoryMapConfigMemoryItem(constMatcher3.group(2), constMatcher3.group(4));
                            constItem3.setEndAddress(constMatcher3.group(4));
                            constItem3.setLength(getSegmentLength(constMatcher3.group(4), constMatcher3.group(4)) + "");
                            config.add(constItem3);

                            if (constItem3 == null) {
                                logger.logp(Level.WARNING, "parseConfigFile", IARMemoryMapParser.class.getName(),
                                        String.format("parseConfigFile(List<MemoryMapGraphConfiguration> graphConfig, File f) non existing item: %s", s));
                                throw new IOException(String.format("No match found for program memory named %s", s));
                            }
                        }
                    }
                }
            }
        }
        System.out.println("parseConfigFile: SUCCESS!");
        return config;
    }

    @Override
    public MemoryMapConfigMemory parseMapFile(File f, MemoryMapConfigMemory config) throws IOException {
        CharSequence sequence = createCharSequenceFromFile(f);
        for (MemoryMapConfigMemoryItem codeItem : config) {
            if (codeItem.getName().matches("CODE")) {
                Matcher matcher = getPatternForCodeMemoryMap(codeItem.getName()).matcher(sequence);
                boolean found = false;
                while (matcher.find()) {
                    codeItem.setUsed(matcher.group(1));
                    found = true;
                }
                if (!found) {
                    logger.logp(Level.WARNING, "parseMapFile", IARMemoryMapParser.class.getName(),
                            String.format("parseMapFile(File f, MemoryMapConfigMemory configuration) non existing item: %s", codeItem));
                    throw new IOException(String.format("Linker command element %s not found in .map file", codeItem));
                }
            }
        }

        for (MemoryMapConfigMemoryItem dataItem : config) {
            if (dataItem.getName().matches("DATA")) {
                Matcher matcher = getPatternForDataMemoryMap(dataItem.getName()).matcher(sequence);
                boolean found = false;
                while (matcher.find()) {
                    dataItem.setUsed(matcher.group(1));
                    found = true;
                }
                if (!found) {
                    logger.logp(Level.WARNING, "parseMapFile", IARMemoryMapParser.class.getName(),
                            String.format("parseMapFile(File f, MemoryMapConfigMemory configuration) non existing item: %s", dataItem));
                    throw new IOException(String.format("Linker command element %s not found in .map file", dataItem));
                }
            }
        }

        for (MemoryMapConfigMemoryItem constItem : config) {
            if (constItem.getName().matches("CONST")) {
                Matcher matcher = getPatternForConstMemoryMap(constItem.getName()).matcher(sequence);
                boolean found = false;
                while (matcher.find()) {
                    constItem.setUsed(matcher.group(1));
                    found = true;
                }
                if (!found) {
                    logger.logp(Level.WARNING, "parseMapFile", IARMemoryMapParser.class.getName(),
                            String.format("parseMapFile(File f, MemoryMapConfigMemory configuration) non existing item: %s", constItem));
                    throw new IOException(String.format("Linker command element %s not found in .map file", constItem));
                }
            }
        }
        System.out.println("parseMapFile: SUCCESS");
        return config;

    }

    @Extension
    public static final class DescriptorImpl extends MemoryMapParserDescriptor<IARMemoryMapParser> {

        @Override
        public String getDisplayName() {
            return "IAR";
        }

        @Override
        public IARMemoryMapParser newInstance(StaplerRequest req, JSONObject formData, AbstractMemoryMapParser instance)
                throws Descriptor.FormException {
            IARMemoryMapParser parser = (IARMemoryMapParser) instance;
            save();
            return parser;
        }
    }
}