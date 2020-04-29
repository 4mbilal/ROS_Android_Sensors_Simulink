package com.example.IMUControl;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.FrameLayout;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.YAxisValueFormatter;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Copyright 2017 The MathWorks, Inc.
 */

class LineArrayPlot extends SimScope {
    private static final String TAG = "LineArrayPlot";

    private final LineData lineData = new LineData();
    private LineChart chart;
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

            LineDataSet dataSet = (LineDataSet) lineData.getDataSetByIndex(scopesLineIndex);
            //create dataset if it is null
            if (dataSet == null) {
                dataSet = createLineDataSet(attribute, scopesLineIndex, frameLength);
                dataSet.setHighlightEnabled(true);
                lineData.addDataSet(dataSet);
            }

            //add x values first
            int barDataXValCount = lineData.getXValCount();

            if (barDataXValCount < MAX_ENTRY) {
                double xOffset = attribute.getXOffset();
                double sampleIncrement = attribute.getSampleIncrement();
                lineData.getXVals().clear();
                if (barDataXValCount + frameLength < MAX_VISIBLE_X_RANGE) {
                    for (int i = 0; i < MAX_VISIBLE_X_RANGE; i++)
                        lineData.addXValue(String.valueOf(xOffset + i * sampleIncrement));
                } else if (barDataXValCount + frameLength < MAX_ENTRY) {
                    for (int i = MAX_VISIBLE_X_RANGE - barDataXValCount - frameLength; i < MAX_VISIBLE_X_RANGE; i++) {
                        lineData.addXValue(String.valueOf(xOffset + i * sampleIncrement));
                    }
                } else {
                    for (int i = MAX_VISIBLE_X_RANGE - MAX_ENTRY; i < MAX_VISIBLE_X_RANGE; i++) {
                        lineData.addXValue(String.valueOf(xOffset + i * sampleIncrement));
                    }
                }
            }

            //finally enter data
            int offset = si * frameLength;
            for (int i = 0; i < frameLength; i++) {
                dataSet.getEntryForIndex(i).setVal(data[offset + i]);
            }
            ++scopesLineIndex;
        }
        lineData.notifyDataChanged();
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
        chart = new LineChart(context);
        chart.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));
        layout.addView(chart);
        setLineChartSettings(chart);
    }

    @Override
    protected void setDataLimits() {

    }

    private void setLineChartSettings(LineChart mChart) {
        if (mChart == null) {
            Log.e(TAG, "setLineChartSettings: LineChart is null.");
            return;
        }

        if (attribute == null) {
            Log.e(TAG, "setChartSettings: ScopeAttribute is null.");
            return;
        }

        moveViewToX = 0;

        // add empty data to chart
        mChart.setData(lineData);

        /**settings for LineChart**/

        mChart.setOnChartGestureListener(new OnChartGestureListener() {
            @Override
            public void onChartGestureStart(MotionEvent motionEvent, ChartTouchListener.ChartGesture chartGesture) {
                isChartGestureOn = true;
                chartGestureStartTime = System.currentTimeMillis();
            }

            @Override
            public void onChartGestureEnd(MotionEvent motionEvent, ChartTouchListener.ChartGesture chartGesture) {

            }

            @Override
            public void onChartLongPressed(MotionEvent motionEvent) {

            }

            @Override
            public void onChartDoubleTapped(MotionEvent motionEvent) {

            }

            @Override
            public void onChartSingleTapped(MotionEvent motionEvent) {

            }

            @Override
            public void onChartFling(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {

            }

            @Override
            public void onChartScale(MotionEvent motionEvent, float v, float v1) {

            }

            @Override
            public void onChartTranslate(MotionEvent motionEvent, float v, float v1) {

            }
        });

        // set description text
        if (title == null)
            Log.e(TAG, "description is null...");
        else
            mChart.setDescription(title);
        mChart.setNoDataTextDescription("You need to provide data for the chart.");

        // enable touch gestures
        mChart.setTouchEnabled(true);

        // enable scaling and dragging
        mChart.setDragEnabled(true);
        mChart.setScaleEnabled(true);
        mChart.setHighlightPerDragEnabled(false);
        mChart.setHighlightPerTapEnabled(false);

        // if disabled, scaling can be done on x- and y-axis separately
        mChart.setPinchZoom(true);

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
        mChart.setBackgroundColor(backgroundColor);
        mChart.setDrawGridBackground(true);
        mChart.setGridBackgroundColor(backgroundColor);

        mChart.setDrawMarkerViews(false);
        if (!attribute.getAxesScaling().equalsIgnoreCase("manual")) {
            mChart.setAutoScaleMinMaxEnabled(true);
        }


        mChart.setHardwareAccelerationEnabled(true);

        // get the legend (only possible after setting data)
        Legend l = mChart.getLegend();
        l.setTextColor(axesTextColor);
        l.setForm(Legend.LegendForm.LINE);
        l.setPosition(Legend.LegendPosition.RIGHT_OF_CHART_INSIDE);
        if (attribute.getDisplays().get(0).getShowLegend())
            l.setEnabled(true);
        else
            l.setEnabled(false);

        XAxis xl = mChart.getXAxis();
        xl.setPosition(XAxis.XAxisPosition.BOTTOM);

        mChart.setDescriptionColor(axesTextColor);
        xl.setTextColor(axesTextColor);
        xl.setDrawGridLines(false);
        xl.setDrawAxisLine(true);
        xl.setDrawLabels(true);
        xl.setEnabled(true);

        YAxis leftAxis = mChart.getAxisLeft();
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

        YAxis rightAxis = mChart.getAxisRight();
        rightAxis.setEnabled(false);

        mChart.notifyDataSetChanged();
        mChart.postInvalidate();

        //uncomment below line to enable logging draw time
        //mChart.setLogEnabled(true);
    }

    private LineDataSet createLineDataSet(ScopeAttribute attribute, int x, int length) {

        ScopeAttributesDisplay display = attribute.getDisplays().get(0);
        String lineDataSetName = "LineDataSet:" + x;
        if (attribute.getInputNames() != null && attribute.getInputNames().size() > x && attribute.getInputNames().get(x) != null)
            lineDataSetName = attribute.getInputNames().get(x);
        ArrayList<Entry> values = new ArrayList<>();
        for (int i = 0; i < length; i++) {
            Entry entry = new Entry(0, i);
            values.add(entry);
        }
        LineDataSet set = new LineDataSet(values, lineDataSetName);

        double lineWidth = display.getLineWidths().get(x % 7);
        set.setLineWidth((float) lineWidth);
        List<Double> lineColors = display.getLineColors().get(x % 7);
        double red = lineColors.get(0) * 255;
        double green = lineColors.get(1) * 255;
        double blue = lineColors.get(2) * 255;

        set.setDrawValues(false);
        set.setDrawCircles(false);
        set.setDrawFilled(false);
        set.setMode(LineDataSet.Mode.LINEAR);
        if (attribute.getPlotType().equalsIgnoreCase("Stairs")) {
            set.setMode(LineDataSet.Mode.STEPPED);
        }
        set.setColor(Color.rgb((int) red, (int) green, (int) blue));
        set.setFillAlpha(1);

        String lineStyle = display.getLineStyles().get(x % 7);
        if ("--".equalsIgnoreCase(lineStyle)) {
            set.enableDashedLine(2, 3, 1);
        } else if (":".equalsIgnoreCase(lineStyle)) {
            set.enableDashedLine(1, 3, 1);
        }
        set.setAxisDependency(YAxis.AxisDependency.LEFT);

        return set;
    }
}
