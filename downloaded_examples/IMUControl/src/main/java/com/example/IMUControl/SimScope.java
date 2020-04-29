package com.example.IMUControl;

import android.content.Context;
import android.widget.FrameLayout;

/**
 * Copyright 2017 The MathWorks, Inc.
 */

abstract class SimScope {

    final int MAX_SUPPORTED_LINES_PER_SCOPE = 7;
    /**
     * Data Limits
     */
    int MAX_VISIBLE_X_RANGE;
    int MAX_ENTRY;
    int MAX_SUPPORTED_FRAME_SIZE;
    int MAX_FRAMES_PER_SCREEN;
    /**
     * Attribute
     */
    ScopeAttribute attribute;

    float[] sampleTimes;
    String title;

    int moveViewToX = 0;
    boolean isChartGestureOn = false;
    long chartGestureStartTime;


    String getTitle() {
        return title;
    }

    void setTitle(String title) {
        this.title = title;
    }

    abstract void init(ScopeAttribute scopeAttribute, float[] sampleTimes);

    abstract void cacheData(int portIdx, float[] data, int sigNumDims, int[] sigDims);

    abstract void plotData();

    abstract void setChartSettings(FrameLayout layout, Context context);

    abstract void setDataLimits();
}
