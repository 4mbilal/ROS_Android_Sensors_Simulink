package com.example.IMUControl;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.widget.FrameLayout;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.YAxisValueFormatter;

import java.util.ArrayList;
import java.util.List;

/**
 * Copyright 2017 The MathWorks, Inc.
 */

class StemArrayPlot extends SimScope {
    private static final String TAG = "StemArrayPlot";

    private final BarData barData = new BarData();
    private BarChart chart;
    private int scopesLineIndex;

    @Override
    void init(ScopeAttribute scopeAttribute, float[] sampleTimes) {
        this.attribute = scopeAttribute;
        this.sampleTimes = sampleTimes;
        setDataLimits();
    }

    @Override
    void cacheData(int portIdx, float[] data, int sigNumDims, int[] sigDims) {
        if (portIdx == 0) {
            scopesLineIndex = 0;
        }
        int frameLength = sigDims[0];
        int numSignals = 1;
        if (sigNumDims > 1) {
            for (int i = 1; i < sigNumDims; i++)
                numSignals *= sigDims[i];
        }
        for (int si = 0; si < numSignals; si++) {
            MAX_ENTRY = MAX_VISIBLE_X_RANGE = frameLength;

            BarDataSet dataSet = (BarDataSet) barData.getDataSetByIndex(scopesLineIndex);
            //create dataset if it is null
            if (dataSet == null) {
                dataSet = createBarDataSet(attribute, scopesLineIndex, frameLength);
                dataSet.setHighlightEnabled(true);
                barData.addDataSet(dataSet);
            }

            //add x values first
            int barDataXValCount = barData.getXValCount();

            if (barDataXValCount < MAX_ENTRY) {
                double xOffset = attribute.getXOffset();
                double sampleIncrement = attribute.getSampleIncrement();
                barData.getXVals().clear();
                if (barDataXValCount + frameLength < MAX_VISIBLE_X_RANGE) {
                    for (int i = 0; i < MAX_VISIBLE_X_RANGE; i++)
                        barData.addXValue(String.valueOf(xOffset + i * sampleIncrement));
                } else if (barDataXValCount + frameLength < MAX_ENTRY) {
                    for (int i = MAX_VISIBLE_X_RANGE - barDataXValCount - frameLength; i < MAX_VISIBLE_X_RANGE; i++) {
                        barData.addXValue(String.valueOf(xOffset + i * sampleIncrement));
                    }
                } else {
                    for (int i = MAX_VISIBLE_X_RANGE - MAX_ENTRY; i < MAX_VISIBLE_X_RANGE; i++) {
                        barData.addXValue(String.valueOf(xOffset + i * sampleIncrement));
                    }
                }
                barData.notifyDataChanged();
            }

            // If current data count + input frame size is grater than MAX_ENTRY
            // then make some space to enter current frame.
            int i;
            //finally enter data
            int offset = si * frameLength;
            for (i = 0; i < frameLength; i++) {
                dataSet.getEntryForIndex(i).setVal(data[offset + i]);
            }
            ++scopesLineIndex;
        }
    }

    @Override
    public void plotData() {
        if (null == chart) {
            Log.e(TAG, "plotData: chart is null");
            return;
        }
        chart.notifyDataSetChanged();
        chart.moveViewToX(0);
        chart.postInvalidate();
    }

    @Override
    protected void setChartSettings(FrameLayout layout, Context context) {
        chart = new BarChart(context);
        chart.setLayoutParams(new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));
        layout.addView(chart);
        setBarChartSettings(chart);
    }

    @Override
    protected void setDataLimits() {
        barData.setGroupSpace(0);
    }

    private void setBarChartSettings(BarChart barChart) {
        if (barChart == null) {
            Log.e(TAG, "setBarChartSettings: BarChart is null.");
            return;
        }

        if (attribute == null) {
            Log.e(TAG, "setChartSettings: ScopeAttribute is null.");
            return;
        }

        // add empty data to chart
        barChart.setData(barData);

        // set description text
        if (getTitle() == null)
            Log.e(TAG, "description is null for Scope.");
        else
            barChart.setDescription(getTitle());
        barChart.setNoDataTextDescription("You need to provide data for the chart.");

        // enable touch gestures
        barChart.setTouchEnabled(true);


        // enable scaling and dragging
        barChart.setDragEnabled(true);
        barChart.setScaleEnabled(true);
        barChart.setHighlightPerDragEnabled(false);
        barChart.setHighlightPerTapEnabled(false);

        // if disabled, scaling can be done on x- and y-axis separately
        barChart.setPinchZoom(true);

        int red = (int) (255 * attribute.getAxesColor().get(0));
        int green = (int) (255 * attribute.getAxesColor().get(1));
        int blue = (int) (255 * attribute.getAxesColor().get(2));
        int backgroundColor = Color.rgb(red, green, blue);
        red = (int) (255 * attribute.getAxesTickColor().get(0));
        green = (int) (255 * attribute.getAxesTickColor().get(1));
        blue = (int) (255 * attribute.getAxesTickColor().get(2));
        int axesTickColor = Color.argb(168, red, green, blue);
        int axesTextColor = Color.argb(255, red, green, blue);
        // set an alternative background color
        barChart.setBackgroundColor(backgroundColor);
        barChart.setDrawGridBackground(true);
        barChart.setGridBackgroundColor(backgroundColor);
        barChart.setDescriptionColor(axesTextColor);

        if (!attribute.getAxesScaling().equalsIgnoreCase("manual")) {
            barChart.setAutoScaleMinMaxEnabled(true);
        }

        barChart.setHardwareAccelerationEnabled(true);

        // get the legend (only possible after setting data)
        Legend l = barChart.getLegend();
        l.setTextColor(axesTextColor);
        l.setForm(Legend.LegendForm.LINE);
        l.setPosition(Legend.LegendPosition.RIGHT_OF_CHART_INSIDE);
        if (attribute.getDisplays().get(0).getShowLegend())
            l.setEnabled(true);
        else
            l.setEnabled(false);

        XAxis xl = barChart.getXAxis();
        xl.setPosition(XAxis.XAxisPosition.BOTTOM);


        xl.setTextColor(axesTextColor);
        xl.setDrawGridLines(false);
        xl.setDrawAxisLine(true);
        xl.setDrawLabels(true);

        xl.setAvoidFirstLastClipping(true);
        xl.setSpaceBetweenLabels(5);
        xl.setEnabled(true);

        YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        leftAxis.setTextColor(axesTextColor);

        leftAxis.setGranularityEnabled(true);
        if (attribute.getDisplays().get(0).getShowGrid()) {
            leftAxis.setDrawGridLines(true);
            xl.setDrawGridLines(true);
            leftAxis.setGridColor(axesTickColor);
            xl.setGridColor(axesTickColor);
        } else {
            leftAxis.setDrawGridLines(false);
            xl.setDrawGridLines(false);
        }
        if (attribute.getAxesScaling().equalsIgnoreCase("manual")) {
            double axisMinValue = attribute.getDisplays().get(0).getYLimits().get(0);
            double axisMaxValue = attribute.getDisplays().get(0).getYLimits().get(1);
            leftAxis.setAxisMinValue((float) axisMinValue);
            leftAxis.setAxisMaxValue((float) axisMaxValue);

            YAxisValueFormatter yAxisValueFormatter = new YAxisValueFormatter() {
                @Override
                public String getFormattedValue(float v, YAxis yAxis) {
                    return String.format("%.3f", v);
                }
            };
            leftAxis.setValueFormatter(yAxisValueFormatter);
            leftAxis.setGranularity(0.001f);
        }

        YAxis rightAxis = barChart.getAxisRight();
        rightAxis.setEnabled(false);

        barChart.notifyDataSetChanged();
        barChart.postInvalidate();
    }

    private BarDataSet createBarDataSet(ScopeAttribute attribute, int x, int length) {
        ScopeAttributesDisplay display = attribute.getDisplays().get(0);
        String barDataSetName = "BarDataSet:" + x;
        if (attribute.getInputNames() != null && attribute.getInputNames().size() > x && attribute.getInputNames().get(x) != null)
            barDataSetName = attribute.getInputNames().get(x);
        ArrayList<BarEntry> values = new ArrayList<>();
        for (int i = 0; i < length; i++) {
            BarEntry barEntry = new BarEntry(0, i);
            values.add(barEntry);
        }
        BarDataSet set = new BarDataSet(values, barDataSetName);

        double lineWidth = display.getLineWidths().get(x % 7);
        set.setLineWidth((float) lineWidth);
        List<Double> lineColors = display.getLineColors().get(x % 7);
        double red = lineColors.get(0) * 255;
        double green = lineColors.get(1) * 255;
        double blue = lineColors.get(2) * 255;

        set.setDrawValues(false);
        set.setColor(Color.rgb((int) red, (int) green, (int) blue));
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setMWStem(true);

        return set;
    }
}
