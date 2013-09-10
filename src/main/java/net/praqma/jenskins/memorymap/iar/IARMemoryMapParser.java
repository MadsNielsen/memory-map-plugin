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

import hudson.Extension;
import hudson.model.Descriptor;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.praqma.jenkins.memorymap.graph.MemoryMapGraphConfiguration;
import net.praqma.jenkins.memorymap.parser.AbstractMemoryMapParser;
import net.praqma.jenkins.memorymap.parser.MemoryMapConfigFileParserDelegate;
import net.praqma.jenkins.memorymap.parser.MemoryMapMapParserDelegate;
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
     * Ram?? Flash?? Rom??
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
        super(mapFile, configurationFile, wordSize, bytesOnGraph, INTVEC, OPTBYTE, SECUID, aseg, RCODE, CODE, NEAR_ID, NEAR_CONST, SWITCH, CHECKSUM, NEAR_A, SADDR_A, FAR_HEAP_SIZE,
                NEAR_CONST_LOCATION_START, NEAR_CONST_LOCATION_END, NEAR_CONST_LOCATION, NEAR_HEAP_SIZE,
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

    private double getDividedLength(String start, String end) {
        String tenThousand = "10000";
        double divisor = (double) Integer.parseInt(tenThousand, 16);
        double startHexToInt = (double) Integer.parseInt(start, 16);
        double endHexToInt = (double) Integer.parseInt(end, 16);

        double sagmentLength = ((endHexToInt - startHexToInt)+1)/divisor;
        return sagmentLength;
    }

    private double getNotDividedLength(String start, String end) {
        double startHexToInt = (double) Integer.parseInt(start, 16);
        double endHexToInt = (double) Integer.parseInt(end, 16);

        double sagmentLength = (endHexToInt - startHexToInt) + 1;
        return sagmentLength;
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

                    Matcher m1 = getPatternForCodeMemoryDividedConfig(ms.replace(" ", "")).matcher(sequence);
                    Matcher m2 = getPatternForCodeMemoryConfig(ms.replace(" ", "")).matcher(sequence);
                    Matcher m3 = getPatternForDataMemoryConfig(ms.replace(" ", "")).matcher(sequence);
                    Matcher m4 = getPatternForDataMemoryDividedConfig(ms.replace(" ", "")).matcher(sequence);
                    Matcher m5 = getPatternForConstMemoryConfig(ms.replace(" ", "")).matcher(sequence);
                    Matcher m6 = getPatternForConstMemoryDividedConfig(ms.replace(" ", "")).matcher(sequence);
                    //                    Matcher m7 = getPatternForConstMemoryConfigSharp(ms.replace(" ", "")).matcher(sequence);

                    MemoryMapConfigMemoryItem item1 = null;
                    MemoryMapConfigMemoryItem item2 = null;
                    MemoryMapConfigMemoryItem item3 = null;
                    MemoryMapConfigMemoryItem item4 = null;
                    MemoryMapConfigMemoryItem item5 = null;
                    MemoryMapConfigMemoryItem item6 = null;
//                    MemoryMapConfigMemoryItem item7 = null;

                    while (m1.find()) {
                        item1 = new MemoryMapConfigMemoryItem(m1.group(2), m1.group(4), m1.group(5));
                        item1.setLength(getDividedLength(m1.group(4), m1.group(5)) + "");
                        config.add(item1);
                         System.out.println("/////////////////////getPatternForCodeMemoryDividedConfig/////////////////////////\n");
                         System.out.println(item1.toStringIAR());
                         System.out.println("\n----------------------------------------------------------");
                    }
                    while (m2.find()) {
                        item2 = new MemoryMapConfigMemoryItem(m2.group(2), m2.group(3), m2.group(4));
                        item2.setLength(getNotDividedLength(m2.group(3), m2.group(4)) + "");
                        config.add(item2);
                        System.out.println("///////////////////////getPatternForCodeMemoryConfig///////////////////////\n");
                        System.out.println(item2.toStringIAR());
                        System.out.println("\n******************************************");
                    }
                    while (m3.find()) {
                        item3 = new MemoryMapConfigMemoryItem(m3.group(2), m3.group(3), m3.group(4));
                        item3.setLength(getNotDividedLength(m3.group(3), m3.group(4)) + "");
                        config.add(item3);
                        System.out.println("///////////////////////getPatternForDataMemoryConfig///////////////////////\n");
                        System.out.println(item3.toStringIAR());
                        System.out.println("\n******************************************");
                    }
                    while (m4.find()) {
                        item4 = new MemoryMapConfigMemoryItem(m4.group(2), m4.group(3), m4.group(4));
                        item4.setLength(getDividedLength(m4.group(3), m4.group(4)) + "");
                        config.add(item4);
                        System.out.println("///////////////////////getPatternForDataMemoryDividedConfig///////////////////////\n");  
                        System.out.println(item4.toStringIAR());
                        System.out.println("\n******************************************");
                    }
                    while (m5.find()) {
                        item5 = new MemoryMapConfigMemoryItem(m5.group(2), m5.group(4), m5.group(5));
                        item5.setLength(getNotDividedLength(m5.group(4), m5.group(5)) + "");
                        config.add(item5);
                        System.out.println("///////////////////////getPatternForConstMemoryConfig///////////////////////\n");
                        System.out.println(item5.toStringIAR());
                        System.out.println("\n******************************************");
                    }
                    while (m6.find()) {
                        item6 = new MemoryMapConfigMemoryItem(m6.group(2), m6.group(4), m6.group(5));
                        item6.setLength(getDividedLength(m6.group(4), m6.group(5)) + "");
                        config.add(item6);
                        System.out.println("///////////////////////getPatternForConstMemoryDividedConfig///////////////////////\n");
                        System.out.println(item6.toStringIAR());
                        System.out.println("\n******************************************");
                    }
//                  while (m7.find()) {
//                      item7 = new MemoryMapConfigMemoryItem(m7.group(2), m7.group(4));
//                      config.add(item7);
//                      System.out.println(item7.toString());
//                    }


                    if (item1 == null) {
                        logger.logp(Level.WARNING, "parseConfigFile", IARMemoryMapParser.class.getName(), String.format("parseConfigFile(List<MemoryMapGraphConfiguration> graphConfig, File f) non existing item: %s", s));
                        throw new IOException(String.format("No match found for program memory named %s", s));
                    }
                    if (item2 == null) {
                        logger.logp(Level.WARNING, "parseConfigFile", IARMemoryMapParser.class.getName(), String.format("parseConfigFile(List<MemoryMapGraphConfiguration> graphConfig, File f) non existing item: %s", s));
                        throw new IOException(String.format("No match found for program memory named %s", s));
                    }
                    if (item3 == null) {
                        logger.logp(Level.WARNING, "parseConfigFile", IARMemoryMapParser.class.getName(), String.format("parseConfigFile(List<MemoryMapGraphConfiguration> graphConfig, File f) non existing item: %s", s));
                        throw new IOException(String.format("No match found for program memory named %s", s));
                    }
                    if (item4 == null) {
                        logger.logp(Level.WARNING, "parseConfigFile", IARMemoryMapParser.class.getName(), String.format("parseConfigFile(List<MemoryMapGraphConfiguration> graphConfig, File f) non existing item: %s", s));
                        throw new IOException(String.format("No match found for program memory named %s", s));
                    }
                    if (item5 == null) {
                        logger.logp(Level.WARNING, "parseConfigFile", IARMemoryMapParser.class.getName(), String.format("parseConfigFile(List<MemoryMapGraphConfiguration> graphConfig, File f) non existing item: %s", s));
                        throw new IOException(String.format("No match found for program memory named %s", s));
                    }
                    if (item6 == null) {
                        logger.logp(Level.WARNING, "parseConfigFile", IARMemoryMapParser.class.getName(), String.format("parseConfigFile(List<MemoryMapGraphConfiguration> graphConfig, File f) non existing item: %s", s));
                        throw new IOException(String.format("No match found for program memory named %s", s));
                    }
//                    if (item7 == null) {
//                        logger.logp(Level.WARNING, "parseConfigFile", IARMemoryMapParser.class.getName(), String.format("parseConfigFile(List<MemoryMapGraphConfiguration> graphConfig, File f) non existing item: %s", s));
//                        throw new IOException(String.format("No match found for program memory named %s", s));
//                    }
                }

            }
        }
        return config;
    }

    @Override
    public MemoryMapConfigMemory parseMapFile(File f, MemoryMapConfigMemory config) throws IOException {
        CharSequence sequence = createCharSequenceFromFile(f);

        for (MemoryMapConfigMemoryItem codeItem : config) {
            Matcher matcher = getPatternForCodeMemoryMap(codeItem.getName()).matcher(sequence);
            boolean found = false;
            while (matcher.find()) {
                codeItem.setUsed(matcher.group(1));
                found = true;
            }
            if (!found) {
                logger.logp(Level.WARNING, "parseMapFile", IARMemoryMapParser.class.getName(), String.format("parseMapFile(File f, MemoryMapConfigMemory configuration) non existing item: %s", codeItem));
                throw new IOException(String.format("Linker command element %s not found in .map file", codeItem));
            }
        }

        for (MemoryMapConfigMemoryItem dataItem : config) {
            Matcher matcher = getPatternForDataMemoryMap(dataItem.getName()).matcher(sequence);
            boolean found = false;
            while (matcher.find()) {
                dataItem.setUsed(matcher.group(1));
                found = true;
            }
            if (!found) {
                logger.logp(Level.WARNING, "parseMapFile", IARMemoryMapParser.class.getName(), String.format("parseMapFile(File f, MemoryMapConfigMemory configuration) non existing item: %s", dataItem));
                throw new IOException(String.format("Linker command element %s not found in .map file", dataItem));
            }
        }

        for (MemoryMapConfigMemoryItem constItem : config) {
            Matcher matcher = getPatternForConstMemoryMap(constItem.getName()).matcher(sequence);
            boolean found = false;
            while (matcher.find()) {
                constItem.setUsed(matcher.group(1));
                found = true;
            }
            if (!found) {
                logger.logp(Level.WARNING, "parseMapFile", IARMemoryMapParser.class.getName(), String.format("parseMapFile(File f, MemoryMapConfigMemory configuration) non existing item: %s", constItem));
                throw new IOException(String.format("Linker command element %s not found in .map file", constItem));
            }
        }
        return config;
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
