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

import java.util.List;

/**
 * Copyright 2017 The MathWorks, Inc.
 */

class TimeScope extends SimScope {
    private static final String TAG = "TimeScope";
    private static final long TIME_TO_PAUSE = 3000;

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
        if (attribute.getFrameBasedProcessing()) {
            cacheFrameBasedData(portIdx, data, sigNumDims, sigDims);
        } else {
            cacheSampleBasedData(portIdx, data, sigNumDims, sigDims);
            lineData.notifyDataChanged();
        }
    }

    @Override
    public void plotData() {
        if (null == chart) {
            Log.e(TAG, "plotData: chart is null.");
            return;
        }
        // limit the number of visible entries
        chart.setVisibleXRange(0, MAX_VISIBLE_X_RANGE);

        if (isChartGestureOn) {
            if (System.currentTimeMillis() - chartGestureStartTime > TIME_TO_PAUSE) {
                isChartGestureOn = false;
            }
        } else {
            // let the chart know it's data has changed
            chart.notifyDataSetChanged();

            // move to the latest entry
            chart.moveViewToX(moveViewToX);
            chart.postInvalidate();
        }
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
        int timeSpan = (int) (attribute.getTimeSpan() != null ? attribute.getTimeSpan() : 0);

        if (attribute.getFrameBasedProcessing()) {
            if (timeSpan > 10)
                timeSpan = 10;
            MAX_FRAMES_PER_SCREEN = 5 * timeSpan;
            MAX_VISIBLE_X_RANGE = 2500;
            MAX_SUPPORTED_FRAME_SIZE = MAX_VISIBLE_X_RANGE / MAX_FRAMES_PER_SCREEN;
            MAX_ENTRY = 2 * MAX_VISIBLE_X_RANGE;
        } else {
            MAX_VISIBLE_X_RANGE = (int) (timeSpan / sampleTimes[0]);
            MAX_ENTRY = 10 * MAX_VISIBLE_X_RANGE;
        }
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
        if (!attribute.getFrameBasedProcessing()) {
            int maxLabelsOnScreen = (int) (attribute.getTimeSpan() / sampleTimes[0]);
            int labelsToSkip = maxLabelsOnScreen / 10 - 1;
            if (labelsToSkip < 0)
                labelsToSkip = 0;
            xl.setLabelsToSkip(labelsToSkip);
            xl.setAvoidFirstLastClipping(true);
            xl.setSpaceBetweenLabels(5);
        }
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

    private void cacheFrameBasedData(int portIdx, float[] data, int sigNumDims, int[] sigDims) {
        int frameLength = sigDims[0];
        int numSignals = 1;
        if (sigNumDims > 1) {
            for (int i = 1; i < sigNumDims; i++)
                numSignals *= sigDims[i];
        }
        for (int si = 0; si < numSignals; si++) {
            LineDataSet dataSet = (LineDataSet) lineData.getDataSetByIndex(scopesLineIndex);

            //create dataset if it is null
            if (dataSet == null) {
                dataSet = createSet(attribute, scopesLineIndex);
                lineData.addDataSet(dataSet);
            }

            //we have two cases here
            //1. frame size is more than data entry size
            //2. it is less.
            if (frameLength > MAX_SUPPORTED_FRAME_SIZE) {
                //add x values first
                int lineDataXValCount = dataSet.getEntryCount();

                if (lineDataXValCount < MAX_ENTRY && lineDataXValCount % MAX_VISIBLE_X_RANGE == 0) {
                    lineData.getXVals().clear();
                    for (int i = -lineDataXValCount; i < MAX_VISIBLE_X_RANGE; i++) {
                        lineData.addXValue(String.format("%.3f", i * sampleTimes[portIdx] / frameLength));
                    }
                }

                moveViewToX = lineDataXValCount - (lineDataXValCount % MAX_VISIBLE_X_RANGE);

                if (dataSet.getEntryCount() >= MAX_ENTRY) {
                    dataSet.getYVals().subList(0, MAX_SUPPORTED_FRAME_SIZE).clear();
                    int xIndex = 0;
                    for (Entry entry : dataSet.getYVals()) {
                        entry.setXIndex(xIndex);
                        ++xIndex;
                    }
                    dataSet.calcMinMax(0, dataSet.getEntryCount());
                }

                int gap = (int) Math.round((double) frameLength / MAX_SUPPORTED_FRAME_SIZE);
                int endIDX = frameLength / gap;
                int offset = si * frameLength;
                for (int i = 0; i < endIDX; i++) {
                    dataSet.addEntry(new Entry(data[offset + i * gap], dataSet.getEntryCount()));
                }
            } else {
                MAX_VISIBLE_X_RANGE = frameLength * MAX_FRAMES_PER_SCREEN;
                MAX_ENTRY = 2 * MAX_VISIBLE_X_RANGE;
                //add x values first
                int lineDataXValCount = dataSet.getEntryCount();

                if (lineDataXValCount < MAX_ENTRY) {
                    lineData.getXVals().clear();
                    if (lineDataXValCount + frameLength < MAX_VISIBLE_X_RANGE) {
                        for (int i = 0; i < MAX_VISIBLE_X_RANGE; i++)
                            lineData.addXValue(String.format("%.3f", i * sampleTimes[portIdx] / frameLength));
                    } else if (lineDataXValCount + frameLength < MAX_ENTRY) {
                        for (int i = MAX_VISIBLE_X_RANGE - lineDataXValCount - frameLength; i < MAX_VISIBLE_X_RANGE; i++) {
                            lineData.addXValue(String.format("%.3f", i * sampleTimes[portIdx] / frameLength));
                        }
                    } else {
                        for (int i = MAX_VISIBLE_X_RANGE - MAX_ENTRY; i < MAX_VISIBLE_X_RANGE; i++) {
                            lineData.addXValue(String.format("%.3f", i * sampleTimes[portIdx] / frameLength));
                        }
                    }
                }

                moveViewToX = lineData.getXValCount() - MAX_VISIBLE_X_RANGE;
                moveViewToX = moveViewToX < 0 ? 0 : moveViewToX;

                // If current data count + input frame size is grater than MAX_ENTRY
                // then make some space to enter current frame.
                int numDataEntryToRemove = 0;
                if (dataSet.getEntryCount() + frameLength > MAX_ENTRY)
                    numDataEntryToRemove = dataSet.getEntryCount() + frameLength - MAX_ENTRY;

                if (numDataEntryToRemove > 0) {
                    //remove from first
                    dataSet.getYVals().subList(0, numDataEntryToRemove).clear();

                    //reset x indexes
                    for (int i = 0; i < dataSet.getEntryCount(); i++) {
                        Entry entry = dataSet.getEntryForIndex(i);
                        entry.setXIndex(i);
                    }
                    dataSet.calcMinMax(0, dataSet.getEntryCount());
                }
                int offset = si * frameLength;
                //finally enter data
                for (int i = 0; i < frameLength; i++) {
                    dataSet.addEntry(new Entry(data[offset + i], dataSet.getEntryCount()));
                }
            }
            ++scopesLineIndex;
        }
    }

    private void cacheSampleBasedData(int portIdx, float[] data, int sigNumDims, int[] sigDims) {
        //add x values first
        int lineDataXValCount = 0;
        if (lineData.getDataSetCount() != 0)
            lineDataXValCount = lineData.getYValCount() / lineData.getDataSetCount();
        if (lineDataXValCount < MAX_ENTRY && lineDataXValCount % MAX_VISIBLE_X_RANGE == 0) {
            lineData.getXVals().clear();
            for (int i = -lineDataXValCount; i < MAX_VISIBLE_X_RANGE; i++) {
                lineData.addXValue(String.format("%.3f", (i + 1) * sampleTimes[portIdx]));
            }
        }

        //now add y values
        for (float aData : data) {
            LineDataSet dataSet = (LineDataSet) lineData.getDataSetByIndex(scopesLineIndex);

            if (dataSet == null) {
                dataSet = createSet(attribute, scopesLineIndex);
                lineData.addDataSet(dataSet);
            }
            int entryCount = dataSet.getEntryCount();
            if (entryCount == MAX_ENTRY) {
                dataSet.getYVals().subList(0, MAX_VISIBLE_X_RANGE).clear();
                entryCount = dataSet.getEntryCount();
                int xIndex = 0;
                for (Entry entry : dataSet.getYVals()) {
                    entry.setXIndex(xIndex);
                    ++xIndex;
                }
                dataSet.calcMinMax(0, entryCount);
            }
            dataSet.addEntry(new Entry(aData, entryCount));
            ++entryCount;
            moveViewToX = entryCount - (entryCount % MAX_VISIBLE_X_RANGE);
            ++scopesLineIndex;
            if (scopesLineIndex >= MAX_SUPPORTED_LINES_PER_SCOPE)
                break;
        }
    }

    private LineDataSet createSet(ScopeAttribute attribute, int x) {

        ScopeAttributesDisplay display = attribute.getDisplays().get(0);
        String lineDataSetName = "LineDataSet:" + x;
        if (attribute.getInputNames() != null && attribute.getInputNames().size() > x && attribute.getInputNames().get(x) != null)
            lineDataSetName = attribute.getInputNames().get(x);
        LineDataSet set = new LineDataSet(null, lineDataSetName);

        double lineWidth = display.getLineWidths().get(x % 7);
        set.setLineWidth((float) lineWidth);
        List<Double> lineColors = display.getLineColors().get(x % 7);
        double red = lineColors.get(0) * 255;
        double green = lineColors.get(1) * 255;
        double blue = lineColors.get(2) * 255;

        set.setDrawValues(false);
        set.setDrawCircles(false);
        set.setDrawFilled(false);
        set.setMode(LineDataSet.Mode.STEPPED);
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
