package com.example.IMUControl;

/**
 * ScopeAttributesDisplay.java
 * <p/>
 * Scope attributes inner class
 * <p/>
 * Copyright 2016-2017 The MathWorks, Inc.
 */

import java.util.ArrayList;
import java.util.List;

class ScopeAttributesDisplay {
    private List<List<Double>> lineColors = new ArrayList<>();
    private List<String> lineStyles = new ArrayList<>();
    private List<Double> lineWidths = new ArrayList<>();
    private Boolean showGrid;
    private Boolean showLegend;
    private List<Double> yLimits = new ArrayList<>();

    /**
     * No args constructor for use in serialization
     */
    public ScopeAttributesDisplay() {
    }

    /**
     * @param yLimits
     * @param showGrid
     * @param showLegend
     * @param lineWidths
     * @param lineColors
     * @param lineStyles
     */
    public ScopeAttributesDisplay(List<List<Double>> lineColors, List<String> lineStyles, List<Double> lineWidths, Boolean showGrid, Boolean showLegend, List<Double> yLimits) {
        this.lineColors = lineColors;
        this.lineStyles = lineStyles;
        this.lineWidths = lineWidths;
        this.showGrid = showGrid;
        this.showLegend = showLegend;
        this.yLimits = yLimits;
    }

    /**
     * @return The lineColors
     */
    public List<List<Double>> getLineColors() {
        return lineColors;
    }

    /**
     * @param lineColors The lineColors
     */
    public void setLineColors(List<List<Double>> lineColors) {
        this.lineColors = lineColors;
    }

    /**
     * @return The lineStyles
     */
    public List<String> getLineStyles() {
        return lineStyles;
    }

    /**
     * @param lineStyles The lineStyles
     */
    public void setLineStyles(List<String> lineStyles) {
        this.lineStyles = lineStyles;
    }

    /**
     * @return The lineWidths
     */
    public List<Double> getLineWidths() {
        return lineWidths;
    }

    /**
     * @param lineWidths The lineWidths
     */
    public void setLineWidths(List<Double> lineWidths) {
        this.lineWidths = lineWidths;
    }

    /**
     * @return The showGrid
     */
    public Boolean getShowGrid() {
        return showGrid;
    }

    /**
     * @param showGrid The showGrid
     */
    public void setShowGrid(Boolean showGrid) {
        this.showGrid = showGrid;
    }

    /**
     * @return The showLegend
     */
    public Boolean getShowLegend() {
        return showLegend;
    }

    /**
     * @param showLegend The showLegend
     */
    public void setShowLegend(Boolean showLegend) {
        this.showLegend = showLegend;
    }

    /**
     * @return The yLimits
     */
    public List<Double> getYLimits() {
        return yLimits;
    }

    /**
     * @param yLimits The yLimits
     */
    public void setYLimits(List<Double> yLimits) {
        this.yLimits = yLimits;
    }

}
