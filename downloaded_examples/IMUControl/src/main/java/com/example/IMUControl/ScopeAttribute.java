package com.example.IMUControl;

/**
 * ScopeAttribute.java
 * <p/>
 * Stub for Scope's JSON attribute. Time Scope team passes its attributes in a JSON string.
 * We parse that string and store here in Java Object Notation.
 * <p/>
 * Copyright 2016-2017 The MathWorks, Inc.
 */

import java.util.ArrayList;
import java.util.List;

public class ScopeAttribute {

    private List<Double> axesColor = new ArrayList<>();
    private String axesScaling;
    private List<Double> axesTickColor = new ArrayList<>();
    private String blockType;
    private List<ScopeAttributesDisplay> displays = new ArrayList<>();
    private Boolean frameBasedProcessing;
    private List<String> inputNames = null;
    private List<Double> layoutDimensions = new ArrayList<>();
    private Double timeSpan;
    private String timeSpanOverrunMode;
    private String plotType;
    private Double sampleIncrement;
    private Double xOffset;

    /**
     * No args constructor for use in serialization
     */
    public ScopeAttribute() {
    }

    /**
     * @param displays
     * @param timeSpan
     * @param frameBasedProcessing
     * @param timeSpanOverrunMode
     * @param layoutDimensions
     * @param blockType
     * @param inputNames
     * @param axesTickColor
     * @param axesColor
     * @param axesScaling
     * @param sampleIncrement
     * @param xOffset
     * @param plotType
     */
    public ScopeAttribute(List<Double> axesColor, String axesScaling, List<Double> axesTickColor, String blockType, List<ScopeAttributesDisplay> displays, Boolean frameBasedProcessing, List<String> inputNames, List<Double> layoutDimensions, Double timeSpan, String timeSpanOverrunMode, String plotType, Double sampleIncrement, Double xOffset) {
        this.axesColor = axesColor;
        this.axesScaling = axesScaling;
        this.axesTickColor = axesTickColor;
        this.blockType = blockType;
        this.displays = displays;
        this.frameBasedProcessing = frameBasedProcessing;
        this.inputNames = inputNames;
        this.layoutDimensions = layoutDimensions;
        this.timeSpan = timeSpan;
        this.timeSpanOverrunMode = timeSpanOverrunMode;
        this.plotType = plotType;
        this.sampleIncrement = sampleIncrement;
        this.xOffset = xOffset;
    }

    /**
     * @return The axesColor
     */
    public List<Double> getAxesColor() {
        return axesColor;
    }

    /**
     * @param axesColor The axesColor
     */
    public void setAxesColor(List<Double> axesColor) {
        this.axesColor = axesColor;
    }

    /**
     * @return The axesScaling
     */
    public String getAxesScaling() {
        return axesScaling;
    }

    /**
     * @param axesScaling The axesScaling
     */
    public void setAxesScaling(String axesScaling) {
        this.axesScaling = axesScaling;
    }

    /**
     * @return The axesTickColor
     */
    public List<Double> getAxesTickColor() {
        return axesTickColor;
    }

    /**
     * @param axesTickColor The axesTickColor
     */
    public void setAxesTickColor(List<Double> axesTickColor) {
        this.axesTickColor = axesTickColor;
    }

    /**
     * @return The blockType
     */
    public String getBlockType() {
        return blockType;
    }

    /**
     * @param blockType The blockType
     */
    public void setBlockType(String blockType) {
        this.blockType = blockType;
    }
    
    /**
     * @return The displays
     */
    public List<ScopeAttributesDisplay> getDisplays() {
        return displays;
    }

    /**
     * @param displays The displays
     */
    public void setDisplays(List<ScopeAttributesDisplay> displays) {
        this.displays = displays;
    }


    /**
     * @return The frameBasedProcessing
     */
    public Boolean getFrameBasedProcessing() {
        return frameBasedProcessing;
    }

    /**
     * @param frameBasedProcessing The frameBasedProcessing
     */
    public void setFrameBasedProcessing(Boolean frameBasedProcessing) {
        this.frameBasedProcessing = frameBasedProcessing;
    }

    /**
     * @return The inputNames
     */
    public List<String> getInputNames() {
        return inputNames;
    }

    /**
     * @param inputNames The inputNames
     */
    public void setInputNames(List<String> inputNames) {
        this.inputNames = inputNames;
    }
    
    /**
     * @return The layoutDimensions
     */
    public List<Double> getLayoutDimensions() {
        return layoutDimensions;
    }

    /**
     * @param layoutDimensions The layoutDimensions
     */
    public void setLayoutDimensions(List<Double> layoutDimensions) {
        this.layoutDimensions = layoutDimensions;
    }


    /**
     * @return The timeSpan
     */
    public Double getTimeSpan() {
        return timeSpan;
    }

    /**
     * @param timeSpan The timeSpan
     */
    public void setTimeSpan(Double timeSpan) {
        this.timeSpan = timeSpan;
    }


    /**
     * @return The timeSpanOverrunMode
     */
    public String getTimeSpanOverrunMode() {
        return timeSpanOverrunMode;
    }

    /**
     * @param timeSpanOverrunMode The timeSpanOverrunMode
     */
    public void setTimeSpanOverrunMode(String timeSpanOverrunMode) {
        this.timeSpanOverrunMode = timeSpanOverrunMode;
    }

    /**
     * @return The plotType
     */
    public String getPlotType() {
        return plotType;
    }

    /**
     * @param plotType The plotType
     */
    public void setPlotType(String plotType) {
        this.plotType = plotType;
    }

    /**
     * @return The sampleIncrement
     */
    public Double getSampleIncrement() {
        return sampleIncrement;
    }

    /**
     * @param sampleIncrement The sampleIncrement
     */
    public void setSampleIncrement(Double sampleIncrement) {
        this.sampleIncrement = sampleIncrement;
    }

    /**
     * @return The setXOffset
     */
    public Double getXOffset() {
        return xOffset;
    }

    /**
     * @param xOffset The setXOffset
     */
    public void setXOffset(Double xOffset) {
        this.xOffset = xOffset;
    }
}
