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
package net.praqma.jenkins.memorymap;

import hudson.model.AbstractBuild;
import hudson.model.Action;
import hudson.util.ChartUtil;
import hudson.util.DataSetBuilder;
import hudson.util.ShiftedCategoryAxis;
import java.awt.BasicStroke;
import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.praqma.jenkins.memorymap.result.MemoryMapConfigMemory;
import net.praqma.jenkins.memorymap.result.MemoryMapConfigMemoryItem;
import net.praqma.jenkins.memorymap.util.HexUtils;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryAxis3D;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StackedAreaRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.category.CategoryDataset;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RectangleInsets;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

/**
 *
 * @author Praqma
 */
public class MemoryMapBuildAction implements Action {

    private static final double labelOffset = 1.2d;
    private HashMap<String, MemoryMapConfigMemory> memoryMapConfig;
    private AbstractBuild<?, ?> build;
    private MemoryMapRecorder recorder;

    public MemoryMapBuildAction(AbstractBuild<?, ?> build, HashMap<String, MemoryMapConfigMemory> memoryMapConfig) {
        this.build = build;
        this.memoryMapConfig = memoryMapConfig;
    }

    @Override
    public String getIconFileName() {
        return null;
    }

    @Override
    public String getDisplayName() {
        return "Memory map";
    }

    @Override
    public String getUrlName() {
        return null;
    }

    /**
     * Returns an indication wheather as to the requirements are met. You do one
     * check per set of values you wish to compare.
     *
     * @param threshold
     * @param valuenames
     * @return
     */
    public boolean validateThreshold(int threshold, String... valuenames) {
        return sumOfValues(valuenames) <= threshold;
    }

    public boolean validateThreshold(int threshold, List<String> valuenames) {
        return sumOfValues(valuenames) <= threshold;
    }

    public int sumOfValues(String... valuenames) {
        int sum = 0;
        /*
         for(MemoryMapParsingResult res : getResults()) {
         for(String s : valuenames) {
         if(res.getName().equals(s)) {
         sum+=res.getValue();
         }
         }
         }
         */
        return sum;
    }

    public int sumOfValues(List<String> values) {
        int sum = 0;
        /*
         for(MemoryMapParsingResult res : getResults()) {
         for(String s : values) {
         if(res.getName().equals(s)) {
         sum+=res.getValue();
         }
         }
         }
         */
        return sum;
    }

    /**
     * Fetches the previous MemoryMap build. Takes all succesful, but failed
     * builds.
     *
     * Goes to the end of list.
     */
    public MemoryMapBuildAction getPreviousAction(AbstractBuild<?, ?> base) {
        MemoryMapBuildAction action = null;
        AbstractBuild<?, ?> start = base;
        while (true) {
            start = start.getPreviousCompletedBuild();
            if (start == null) {
                return null;
            }
            action = start.getAction(MemoryMapBuildAction.class);
            if (action != null) {
                return action;
            }
        }
    }

    public MemoryMapBuildAction getPreviousAction() {
        MemoryMapBuildAction action = null;
        AbstractBuild<?, ?> start = build;
        while (true) {
            start = start.getPreviousCompletedBuild();
            if (start == null) {
                return null;
            }
            action = start.getAction(MemoryMapBuildAction.class);

            if (action != null && (action.isValidConfigurationWithData())) {
                return action;
            }
        }
    }
    
    /**
     * We need to filter markers. If they have the same max value. We need to remove the marker, and concat the label.
     */
    private void filterMarkers(HashMap<String, ValueMarker> markers) {
        
        HashMap<Integer, String> maxLabel = new HashMap<Integer, String>();

        for(String markerLabel : markers.keySet()) {
            int max = (int)markers.get(markerLabel).getValue();
            if(maxLabel.containsKey(max)) {
                //If the label already is contained. Store the Original value. And add the new one.
                String current = maxLabel.get(max);
                
                maxLabel.put(max, current + " "+ markerLabel);
            } else {
                maxLabel.put(max, markerLabel);
            }
        }

        markers.clear();
        for(Integer key : maxLabel.keySet()) {
            makeMarker(maxLabel.get(key), (double)key , markers);
        }
        
    }
    
    public void doDrawMemoryMapUsageGraph(StaplerRequest req, StaplerResponse rsp) throws IOException {
        DataSetBuilder<String, ChartUtil.NumberOnlyBuildLabel> dataset = new DataSetBuilder<String, ChartUtil.NumberOnlyBuildLabel>();

        String members = req.getParameter("categories");
        String graphTitle = req.getParameter("title");
        String uniqueDataSet = req.getParameter("dataset");

        int w = Integer.parseInt(req.getParameter("width"));
        int h = Integer.parseInt(req.getParameter("height"));

        List<String> memberList = Arrays.asList(members.split(","));
        
        HashMap<String,ValueMarker> markers = new HashMap<String, ValueMarker>();

        String scale = getRecorder().scale;
        
        double max = buildDataSet(memberList, uniqueDataSet, dataset, markers);
        filterMarkers(markers);

        String s = "";
        if (scale.equalsIgnoreCase("kilo")) {
            s = "k";
        } else if (scale.equalsIgnoreCase("mega")) {
            s = "M";
        } else if (scale.equalsIgnoreCase("giga")) {
            s = "G";
        }

        String byteLegend = s + "Bytes";
        String wordLegend = s + "Words";

        String legend = getRecorder().getShowBytesOnGraph() ? byteLegend : wordLegend;

        JFreeChart chart = createPairedBarCharts(graphTitle, legend, max * 1.1d, 0d, dataset.build(), markers.values());
        
        chart.setBackgroundPaint(Color.WHITE);
        chart.getLegend().setPosition(RectangleEdge.BOTTOM);
        ChartUtil.generateGraph(req, rsp, chart, w, h);
    }

    protected JFreeChart createPairedBarCharts(String title, String yaxis, double max, double min, CategoryDataset dataset, Collection<ValueMarker> markers) {
        final CategoryAxis domainAxis = new CategoryAxis3D();
        final NumberAxis rangeAxis = new NumberAxis(yaxis);
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        rangeAxis.setUpperBound(max);
        rangeAxis.setLowerBound(min);
        BarRenderer renderer = new BarRenderer();

        CategoryPlot plot = new CategoryPlot(dataset, domainAxis, rangeAxis, renderer);
        plot.setDomainAxis(domainAxis);
        domainAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_90);

        plot.setOrientation(PlotOrientation.VERTICAL);
        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlinePaint(null);
        plot.setRangeGridlinesVisible(true);
        plot.setRangeGridlinePaint(Color.black);

        for (ValueMarker mkr : markers) {
            plot.addRangeMarker(mkr);
        }

        JFreeChart chart = new JFreeChart(plot);        
        chart.setTitle(title);
        return chart;
    }

    /**
     * @return the recorder
     */
    public MemoryMapRecorder getRecorder() {
        return recorder;
    }

    /**
     * @param recorder the recorder to set
     */
    public void setRecorder(MemoryMapRecorder recorder) {
        this.recorder = recorder;
    }

    /**
     * @return the memoryMapConfig
     */
    public HashMap<String, MemoryMapConfigMemory> getMemoryMapConfig() {
        return memoryMapConfig;
    }

    /**
     * @param memoryMapConfig the memoryMapConfig to set
     */
    public void setMemoryMapConfig(HashMap<String, MemoryMapConfigMemory> memoryMapConfig) {
        this.memoryMapConfig = memoryMapConfig;
    }

    public boolean isValidConfigurationWithData() {
        return memoryMapConfig != null && memoryMapConfig.size() >= 1;
    }
    
    private String constructMaxLabel(String... parts) {
        StringBuilder builder = new StringBuilder();
        builder.append("(MAX)");
        for(String part : parts) {
            builder.append(" ").append(part);
        }
        
        return builder.toString();
    }
    
    private String constructCategoryLabel(String... parts) {
        StringBuilder builder = new StringBuilder();
        for(String part : parts) {
            if(parts.length > 1) {
                builder.append(part).append("+");
            } else {
                builder.append(part);
            }            
        }
        
        if(parts.length >  1) {
            int plusIndex = builder.lastIndexOf("+");
            return builder.substring(0, plusIndex).toString();
        }
        
        return builder.toString();        
    }
    /**
     * Extracts the value from a given memory map item. If multiple objects are passed in, values get added.
     * @param item
     * @return 
     */
    private double extractValue(MemoryMapConfigMemoryItem... item) {
        double value = 0d;
        String scale = getRecorder().scale;
        for(MemoryMapConfigMemoryItem it : item) {

            if (getRecorder().getShowBytesOnGraph()) {
                value = value + HexUtils.byteCount(it.getUsed(), getRecorder().getWordSize(), scale);
            } else {
                value = value + HexUtils.wordCount(it.getUsed(), getRecorder().getWordSize(), scale);
            }
        }
        return value;
    }
    
    private double extractMaxValue(MemoryMapConfigMemoryItem... item) {
        double value = 0d;
        String scale = getRecorder().scale;
        for(MemoryMapConfigMemoryItem it : item) {
            if (getRecorder().getShowBytesOnGraph()) {
                value = HexUtils.byteCount(it.getTopLevelMemoryMax(), getRecorder().getWordSize(), scale);
            } else {
                value = value + HexUtils.wordCount(it.getTopLevelMemoryMax(), getRecorder().getWordSize(), scale);
            }
        }
        return value;
    }
    
    public void makeMarker(String labelName, double value, HashMap<String,ValueMarker> markers) {
        if(!markers.containsKey(labelName)) {
            ValueMarker vm = new ValueMarker((double) value, Color.BLACK, new BasicStroke(
                                1.2f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER,
                                1.0f, new float[]{6.0f, 6.0f}, 0.0f));
            vm.setLabel(labelName);

            double i = vm.getLabel().length() * labelOffset + 40;
            vm.setLabelOffset(new RectangleInsets(5, i, -20, 5));
            vm.setLabelAnchor(RectangleAnchor.TOP_LEFT);
            vm.setPaint(Color.BLACK);
            vm.setOutlinePaint(Color.BLACK);
            vm.setAlpha(1.0f);
            markers.put(labelName, vm);
        }
    }
    
    /**
     * Builds the dataset. Returns the maximum value.
     * @param graphData
     * @param dataset
     * @param graphDataset
     * @param markers
     * @return 
     */
    public double buildDataSet(List<String> graphData, String dataset, DataSetBuilder<String, ChartUtil.NumberOnlyBuildLabel> graphDataset, HashMap<String,ValueMarker> markers) {
        double max = 0d;
        String scale = getRecorder().scale;
        
        for(String s : graphData) {
            if(s.contains(" ")) {
                String[] parts = s.split(" ");
                
                String maxLabel = constructMaxLabel(parts);
                String categoryLabel = constructCategoryLabel(parts);
                
                for (MemoryMapBuildAction membuild = this; membuild != null; membuild = membuild.getPreviousAction()) {
                    MemoryMapConfigMemory result = membuild.getMemoryMapConfig().get(dataset);
                    ChartUtil.NumberOnlyBuildLabel label = new ChartUtil.NumberOnlyBuildLabel(membuild.build);
                    if(result != null) {
                        List<MemoryMapConfigMemoryItem> ourItems = result.getItemByNames(parts);
                        MemoryMapConfigMemoryItem[] ourItemsArray = ourItems.toArray(new MemoryMapConfigMemoryItem[ourItems.size()]);
                        double value = extractValue(ourItemsArray);

                        boolean allBelongSameParent = MemoryMapConfigMemoryItem.allBelongSameParent(ourItemsArray);
                        
                        
                        if(allBelongSameParent) {
                            max = extractMaxValue(ourItems.get(0));
                        } else {
                            max = extractMaxValue(ourItemsArray);
                        }

                        graphDataset.add(value, categoryLabel, label);
                        makeMarker(maxLabel, max, markers);
                    }                    
                }
                
            } else {
                HashMap<String, String> maximumValues = new HashMap<String, String>();
                for (MemoryMapBuildAction membuild = this; membuild != null; membuild = membuild.getPreviousAction()) {
                    MemoryMapConfigMemory result = membuild.getMemoryMapConfig().get(dataset);
                    ChartUtil.NumberOnlyBuildLabel label = new ChartUtil.NumberOnlyBuildLabel(membuild.build);
                    
                    if(result != null) {
                        //Do something we have a result                        
                        
                        for(MemoryMapConfigMemoryItem item : result) {
                            //The name of the item matches the configured grap item
                           
                            if(item.getName().equals(s)) {
                                String maxLabel = constructMaxLabel(item.getName());
                                max = extractMaxValue(item);
                                double value = extractValue(item);
                                String categoryLabel = constructCategoryLabel(item.getName());                                
                                graphDataset.add(value, categoryLabel, label);
                                makeMarker(maxLabel, max, markers);
                                
                            }
                            
                        }

                    }
                }
            }
        }
        return max;
    }
}
